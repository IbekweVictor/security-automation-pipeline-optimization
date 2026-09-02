/*
 * ============================================================
 * PROMETHEUS SECURITY METRICS
 * ============================================================
 *
 * Starts the independent Pushgateway / Prometheus / Grafana
 * stack, verifies that Pushgateway is available, collects
 * metrics produced by previous security stages, and pushes
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
         * 3. COLLECT SECURITY FINDINGS
         * ====================================================
         *
         * These values come from previous pipeline stages.
         */

        def critical =
            env.CRITICAL_FINDINGS ?: '0'

        def high =
            env.HIGH_FINDINGS ?: '0'

        def medium =
            env.MEDIUM_FINDINGS ?: '0'

        def low =
            env.LOW_FINDINGS ?: '0'


        /*
         * ====================================================
         * 4. OPA RESULT
         * ====================================================
         *
         * Track BOTH PASS and BLOCK.
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
         */

        def durationSeconds = 0


        if (currentBuild.duration) {

            durationSeconds =
                Math.round(
                    currentBuild.duration / 1000
                )
        }


        /*
         * ====================================================
         * 8. JENKINS CURRENTLY UP
         * ====================================================
         *
         * The pipeline successfully reached this stage,
         * therefore Jenkins is currently operational.
         *
         * This is a point-in-time metric.
         *
         * A true Jenkins availability / uptime metric will
         * eventually come from Prometheus scraping Jenkins.
         */

        def jenkinsUp = 1


        /*
         * ====================================================
         * 9. GENERATE PROMETHEUS METRICS
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
        echo "Critical          : ${critical}"
        echo "High              : ${high}"
        echo "Medium            : ${medium}"
        echo "Low               : ${low}"
        echo "OPA Decision      : ${opaDecision}"
        echo "OPA PASS          : ${opaPass}"
        echo "OPA BLOCK         : ${opaBlock}"
        echo "WAF Status        : ${wafStatus}"
        echo "WAF Active        : ${wafActive}"
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