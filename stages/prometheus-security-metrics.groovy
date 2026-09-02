/*
 * ============================================================
 * PROMETHEUS SECURITY METRICS
 * ============================================================
 *
 * Starts the independent Pushgateway / Prometheus / Grafana
 * stack, verifies that Pushgateway is available, collects
 * unified security metrics produced by DefectDojo, and pushes
 * those metrics to Pushgateway.
 *
 * Monitoring failure must NOT fail the security pipeline.
 *
 * Architecture:
 *
 * Jenkins Pipeline
 *       |
 *       | POST metrics
 *       v
 * Pushgateway :9091
 *       |
 *       | scrape
 *       v
 * Prometheus :9090
 *       |
 *       v
 * Grafana :3000
 *
 * ============================================================
 */

echo ""

echo "=============================================="
echo " PROMETHEUS SECURITY METRICS"
echo "=============================================="

/*
 * ============================================================
 * CONFIGURATION
 * ============================================================
 */

def exporterDirectory =
    'C:\\prometheus-exporter'

def pushgatewayUrl =
    env.PUSHGATEWAY_URL ?: 'http://localhost:9091'

def jobName =
    env.JOB_NAME ?: 'security-automation-pipeline'

def buildNumber =
    env.BUILD_NUMBER ?: '0'

def encodedJobName =
    java.net.URLEncoder
        .encode(jobName, 'UTF-8')
        .replace('+', '%20')


try {

    /*
     * ========================================================
     * 1. START PROMETHEUS MONITORING STACK
     * ========================================================
     */

    echo ""
    echo "Starting independent Prometheus monitoring stack..."

    bat """
        @echo off
        cd /d "${exporterDirectory}"

        echo.
        echo ==============================================
        echo PROMETHEUS EXPORTER DIRECTORY
        echo ==============================================
        echo %CD%

        echo.
        echo Starting Docker Compose services...

        docker compose up -d

        if errorlevel 1 (
            echo ERROR: Docker Compose failed.
            exit /b 1
        )

        echo.
        echo Docker Compose services:
        docker compose ps
    """


    /*
     * ========================================================
     * Give Docker a moment to initialise Pushgateway.
     * ========================================================
     */

    echo ""
    echo "Waiting for Pushgateway..."

    sleep(
        time: 5,
        unit: 'SECONDS'
    )


    /*
     * ========================================================
     * 2. CHECK PUSHGATEWAY
     * ========================================================
     *
     * IMPORTANT:
     *
     * Windows cmd treats % specially.
     *
     * Therefore curl's:
     *
     *     %{http_code}
     *
     * must be written as:
     *
     *     %%{http_code}
     *
     * inside the Jenkins bat command.
     * ========================================================
     */

    echo ""
    echo "Checking Pushgateway availability..."

    def pushgatewayStatus =
        bat(
            script: """
                @curl -s -o nul -w "%%{http_code}" "${pushgatewayUrl}/-/ready"
            """,
            returnStdout: true
        ).trim()

    echo "Pushgateway HTTP status: ${pushgatewayStatus}"


    if (!(pushgatewayStatus in ['200', '202'])) {

        env.PROMETHEUS_METRICS_STATUS =
            'PUSHGATEWAY_UNAVAILABLE'

        echo ""
        echo "WARNING: Pushgateway is not ready."
        echo "Metrics will not be pushed."
        echo "HTTP status: ${pushgatewayStatus}"

    } else {

        echo "✓ Pushgateway is UP."


        /*
         * ====================================================
         * 3. COLLECT UNIFIED DEFECTDOJO FINDINGS
         * ====================================================
         *
         * These values are produced by:
         *
         *     defectdojo-findings.groovy
         *
         * That stage sets:
         *
         *     env.UNIFIED_CRITICAL
         *     env.UNIFIED_HIGH
         *     env.UNIFIED_MEDIUM
         *     env.UNIFIED_LOW
         *     env.UNIFIED_INFO
         *
         * We deliberately use the unified DefectDojo values
         * rather than trying to re-count individual scanners.
         * ====================================================
         */

        echo ""
        echo "Collecting unified DefectDojo security findings..."

        /*
         * Keep the raw values for logging/debugging.
         */
        def rawCritical = env.UNIFIED_CRITICAL
        def rawHigh     = env.UNIFIED_HIGH
        def rawMedium   = env.UNIFIED_MEDIUM
        def rawLow      = env.UNIFIED_LOW
        def rawInfo     = env.UNIFIED_INFO

        echo ""
        echo "DefectDojo unified finding variables:"
        echo "UNIFIED_CRITICAL = ${rawCritical ?: '[NOT SET]'}"
        echo "UNIFIED_HIGH     = ${rawHigh ?: '[NOT SET]'}"
        echo "UNIFIED_MEDIUM   = ${rawMedium ?: '[NOT SET]'}"
        echo "UNIFIED_LOW      = ${rawLow ?: '[NOT SET]'}"
        echo "UNIFIED_INFO     = ${rawInfo ?: '[NOT SET]'}"


        /*
         * Convert the values safely to integers.
         *
         * Missing values default to zero, but we explicitly
         * report them above so they cannot silently hide the
         * source of a problem.
         */

        def critical = 0
        def high     = 0
        def medium   = 0
        def low      = 0
        def info     = 0


        try {
            critical = rawCritical?.toString()?.trim()
                ? rawCritical.toString().trim().toInteger()
                : 0
        } catch (Exception ignored) {
            echo "WARNING: Invalid UNIFIED_CRITICAL value: ${rawCritical}"
        }


        try {
            high = rawHigh?.toString()?.trim()
                ? rawHigh.toString().trim().toInteger()
                : 0
        } catch (Exception ignored) {
            echo "WARNING: Invalid UNIFIED_HIGH value: ${rawHigh}"
        }


        try {
            medium = rawMedium?.toString()?.trim()
                ? rawMedium.toString().trim().toInteger()
                : 0
        } catch (Exception ignored) {
            echo "WARNING: Invalid UNIFIED_MEDIUM value: ${rawMedium}"
        }


        try {
            low = rawLow?.toString()?.trim()
                ? rawLow.toString().trim().toInteger()
                : 0
        } catch (Exception ignored) {
            echo "WARNING: Invalid UNIFIED_LOW value: ${rawLow}"
        }


        try {
            info = rawInfo?.toString()?.trim()
                ? rawInfo.toString().trim().toInteger()
                : 0
        } catch (Exception ignored) {
            echo "WARNING: Invalid UNIFIED_INFO value: ${rawInfo}"
        }


        echo ""
        echo "=============================================="
        echo " UNIFIED DEFECTDOJO FINDINGS"
        echo "=============================================="
        echo "Critical : ${critical}"
        echo "High     : ${high}"
        echo "Medium   : ${medium}"
        echo "Low      : ${low}"
        echo "Info     : ${info}"
        echo "=============================================="



        /*
         * ====================================================
         * 4. OPA RESULT
         * ====================================================
         *
         * Track BOTH PASS and BLOCK.
         * ====================================================
         */

        def opaDecision =
            env.OPA_DECISION ?: 'NOT_EVALUATED'

        def opaPass = 0
        def opaBlock = 0


        if (
            opaDecision.toUpperCase() == 'PASS'
        ) {

            opaPass = 1

        } else if (
            opaDecision.toUpperCase() == 'BLOCK' ||
            opaDecision.toUpperCase() == 'FAIL'
        ) {

            opaBlock = 1
        }



        /*
         * ====================================================
         * 5. WAF RESULT
         * ====================================================
         */

        def wafStatus =
            env.WAF_STATUS ?: 'NOT_EVALUATED'

        def wafActive = 0


        if (
            wafStatus.toUpperCase() == 'ACTIVE' ||
            wafStatus.toUpperCase() == 'RUNNING' ||
            wafStatus.toUpperCase() == 'ENABLED'
        ) {

            wafActive = 1
        }



        /*
         * ====================================================
         * 6. PIPELINE RESULT
         * ====================================================
         */

        def pipelineSuccess = 0
        def pipelineFailure = 0
        def pipelineUnstable = 0


        if (
            currentBuild.currentResult ==
            'SUCCESS'
        ) {

            pipelineSuccess = 1

        } else if (
            currentBuild.currentResult ==
            'UNSTABLE'
        ) {

            pipelineUnstable = 1

        } else if (
            currentBuild.currentResult ==
            'FAILURE'
        ) {

            pipelineFailure = 1
        }



        /*
         * ====================================================
         * 7. PIPELINE DURATION
         * ====================================================
         *
         * Do NOT use:
         *
         *     Math.round(currentBuild.duration / 1000)
         *
         * Jenkins/Groovy converts the division result to
         * BigDecimal and the Jenkins Script Security sandbox
         * can reject Math.round(BigDecimal).
         *
         * intdiv() avoids that problem.
         * ====================================================
         */

        def durationSeconds = 0L

        if (currentBuild.duration != null) {

            durationSeconds =
                (currentBuild.duration as long).intdiv(1000L)
        }



        /*
         * ====================================================
         * 8. JENKINS CURRENTLY UP
         * ====================================================
         */

        def jenkinsUp = 1



        /*
         * ====================================================
         * 9. GENERATE PROMETHEUS METRICS
         * ====================================================
         *
         * Security findings now come from the unified
         * DefectDojo values.
         * ====================================================
         */

        def metrics = """
# TYPE security_pipeline_build_info gauge
security_pipeline_build_info{job="${jobName}",build="${buildNumber}"} 1

# TYPE security_pipeline_build_success gauge
security_pipeline_build_success{job="${jobName}"} ${pipelineSuccess}

# TYPE security_pipeline_build_failure gauge
security_pipeline_build_failure{job="${jobName}"} ${pipelineFailure}

# TYPE security_pipeline_build_unstable gauge
security_pipeline_build_unstable{job="${jobName}"} ${pipelineUnstable}

# TYPE security_pipeline_duration_seconds gauge
security_pipeline_duration_seconds{job="${jobName}"} ${durationSeconds}

# TYPE security_findings_critical gauge
security_findings_critical{job="${jobName}"} ${critical}

# TYPE security_findings_high gauge
security_findings_high{job="${jobName}"} ${high}

# TYPE security_findings_medium gauge
security_findings_medium{job="${jobName}"} ${medium}

# TYPE security_findings_low gauge
security_findings_low{job="${jobName}"} ${low}

# TYPE security_findings_info gauge
security_findings_info{job="${jobName}"} ${info}

# TYPE security_opa_pass gauge
security_opa_pass{job="${jobName}"} ${opaPass}

# TYPE security_opa_block gauge
security_opa_block{job="${jobName}"} ${opaBlock}

# TYPE security_waf_active gauge
security_waf_active{job="${jobName}"} ${wafActive}

# TYPE jenkins_up gauge
jenkins_up{job="${jobName}"} ${jenkinsUp}
"""



        /*
         * ====================================================
         * 10. CREATE METRICS DIRECTORY
         * ====================================================
         */

        bat '''
            @if not exist "reports\\prometheus" mkdir "reports\\prometheus"
        '''



        /*
         * ====================================================
         * 11. WRITE METRICS FILE
         * ====================================================
         */

        writeFile(
            file: 'security-metrics.prom',
            text: metrics.trim() + '\n'
        )


        echo ""
        echo "Generated Prometheus metrics:"
        echo ""

        bat '''
            @type security-metrics.prom
        '''



        /*
         * ====================================================
         * 12. PRESERVE METRICS AS SECURITY EVIDENCE
         * ====================================================
         */

        bat '''
            @copy /Y ^
            "security-metrics.prom" ^
            "reports\\prometheus\\security-metrics.prom" >nul
        '''


        echo ""
        echo "✓ Prometheus metrics evidence saved:"
        echo "  reports/prometheus/security-metrics.prom"



        /*
         * ====================================================
         * 13. PUSH TO PUSHGATEWAY
         * ====================================================
         */

        echo ""
        echo "Pushing security metrics to Pushgateway..."


        def pushStatus =
            bat(
                script: """
                    @curl --fail ^
                    --data-binary "@security-metrics.prom" ^
                    "${pushgatewayUrl}/metrics/job/${encodedJobName}"
                """,
                returnStatus: true
            )


        if (pushStatus != 0) {

            env.PROMETHEUS_METRICS_STATUS =
                'PUSH_FAILED'

            echo ""
            echo "WARNING: Unable to push metrics to Pushgateway."
            echo "Curl exit code: ${pushStatus}"

        } else {

            env.PROMETHEUS_METRICS_STATUS =
                'PUSHED'

            echo ""
            echo "✓ Security metrics successfully pushed."
        }



        /*
         * ====================================================
         * 14. SUMMARY
         * ====================================================
         */

        echo ""
        echo "=============================================="
        echo " PROMETHEUS METRICS COMPLETED"
        echo "=============================================="
        echo "Pushgateway       : ${pushgatewayUrl}"
        echo "Pipeline          : ${jobName}"
        echo "Build             : ${buildNumber}"
        echo "Pipeline Result   : ${currentBuild.currentResult}"
        echo "Jenkins UP        : ${jenkinsUp}"
        echo ""
        echo "UNIFIED DEFECTDOJO FINDINGS"
        echo "Critical          : ${critical}"
        echo "High              : ${high}"
        echo "Medium            : ${medium}"
        echo "Low               : ${low}"
        echo "Info              : ${info}"
        echo ""
        echo "OPA Decision      : ${opaDecision}"
        echo "OPA PASS          : ${opaPass}"
        echo "OPA BLOCK         : ${opaBlock}"
        echo ""
        echo "WAF Status        : ${wafStatus}"
        echo "WAF Active        : ${wafActive}"
        echo ""
        echo "Duration          : ${durationSeconds}s"
        echo "Metrics Status    : ${env.PROMETHEUS_METRICS_STATUS}"
        echo "=============================================="
        echo ""

    }


} catch (Exception metricsError) {


    /*
     * ========================================================
     * MONITORING FAILURE MUST NOT BREAK SECURITY PIPELINE
     * ========================================================
     */

    env.PROMETHEUS_METRICS_STATUS =
        'ERROR'


    echo ""
    echo "=============================================="
    echo " WARNING: PROMETHEUS METRICS FAILED"
    echo "=============================================="
    echo "Metrics error: ${metricsError}"
    echo ""
    echo "Security pipeline execution will continue."
    echo "=============================================="
    echo ""
}