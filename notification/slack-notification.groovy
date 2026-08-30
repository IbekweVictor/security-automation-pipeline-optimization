def send() {

    /*
     * ============================================================
     * BUILD INFORMATION
     * ============================================================
     */

    def result =
        currentBuild.currentResult ?: 'SUCCESS'

    def buildNumber =
        env.BUILD_NUMBER ?: 'N/A'

    def jobName =
        env.JOB_NAME ?: 'Security Automation Pipeline'

    def duration =
        currentBuild.durationString ?: 'N/A'

    def buildUrl =
        env.BUILD_URL ?: ''


    /*
     * ============================================================
     * SLACK CONFIGURATION
     * ============================================================
     */

    def slackChannel =
        'all-security-team'

    def slackCredential =
        'slack-webhook'


    /*
     * ============================================================
     * SLACK COLOR
     * ============================================================
     */

    def slackColor =
        result == 'SUCCESS'
            ? '#166534'
            : result == 'UNSTABLE'
                ? '#ca8a04'
                : '#dc2626'


    /*
     * ============================================================
     * STATUS TITLE
     * ============================================================
     */

    def statusTitle =
        result == 'SUCCESS'
            ? '🟢 SECURITY PIPELINE PASSED'
            : result == 'UNSTABLE'
                ? '🟡 SECURITY PIPELINE UNSTABLE'
                : '🔴 SECURITY PIPELINE FAILED'


    /*
     * ============================================================
     * SCANNER STATUS HELPER
     *
     * null = scanner did not execute / did not produce a result
     * 0   = scanner executed and found zero findings
     * >0  = scanner executed and produced findings
     * ============================================================
     */

    def scannerStatus = { value ->

        if (value != null) {
            return '✓ Executed'
        }

        return '- Not executed'
    }


    /*
     * ============================================================
     * SEMGREP SAST
     * ============================================================
     */

    def semgrepStatus =
        scannerStatus(
            env.SEMGREP_CRITICAL
        )


    /*
     * ============================================================
     * GITLEAKS SECRET DETECTION
     * ============================================================
     */

    def gitleaksStatus =
        scannerStatus(
            env.GITLEAKS_COUNT
        )


    /*
     * ============================================================
     * SNYK SOFTWARE COMPOSITION ANALYSIS
     * ============================================================
     */

    def snykStatus =
        scannerStatus(
            env.SNYK_CRITICAL
        )


    /*
     * ============================================================
     * TRIVY CONTAINER SECURITY
     * ============================================================
     */

    def trivyStatus =
        scannerStatus(
            env.TRIVY_CRITICAL
        )


    /*
     * ============================================================
     * OWASP ZAP AUTHENTICATED DAST
     * ============================================================
     */

    def zapStatus =
        scannerStatus(
            env.DAST_HIGH
        )


    /*
     * ============================================================
     * OPA SECURITY POLICY
     * ============================================================
     */

    def opaDecision =
        env.OPA_DECISION ?: ''

    def opaStatus

    if (opaDecision == 'ALLOW') {

        opaStatus =
            '✓ Allowed'

    } else if (opaDecision == 'BLOCK') {

        opaStatus =
            '✗ Blocked'

    } else if (opaDecision == 'WARNING') {

        opaStatus =
            '⚠ Warning'

    } else {

        opaStatus =
            '- Not evaluated'
    }


    /*
     * ============================================================
     * WAF
     *
     * Supports the environment values exposed by the WAF stage.
     * ============================================================
     */

    def wafValue =
        env.WAF_STATUS ?:
        env.WAF_DECISION ?:
        env.WAF_RESULT ?:
        ''

    def wafStatus

    if (wafValue) {

        def wafUpper =
            wafValue
                .toString()
                .toUpperCase()

        if (
            wafUpper == 'PASS' ||
            wafUpper == 'ALLOW' ||
            wafUpper == 'ACTIVE' ||
            wafUpper == 'SUCCESS'
        ) {

            wafStatus =
                '✓ Active'

        } else if (
            wafUpper == 'FAIL' ||
            wafUpper == 'FAILED' ||
            wafUpper == 'BLOCK'
        ) {

            wafStatus =
                '✗ Failed'

        } else {

            wafStatus =
                "✓ ${wafValue}"
        }

    } else {

        wafStatus =
            '- Not reported'
    }


    /*
     * ============================================================
     * MONITORING
     *
     * Monitoring is part of the project architecture.
     *
     * The notification does not assume that Prometheus/Grafana
     * executed successfully unless the monitoring stage exposes
     * an environment result.
     *
     * ============================================================
     */

    def monitoringValue =
        env.MONITORING_STATUS ?:
        env.MONITORING_RESULT ?:
        ''

    def monitoringStatus

    if (monitoringValue) {

        def monitoringUpper =
            monitoringValue
                .toString()
                .toUpperCase()

        if (
            monitoringUpper == 'PASS' ||
            monitoringUpper == 'ACTIVE' ||
            monitoringUpper == 'SUCCESS' ||
            monitoringUpper == 'AVAILABLE'
        ) {

            monitoringStatus =
                '✓ Active'

        } else if (
            monitoringUpper == 'FAIL' ||
            monitoringUpper == 'FAILED'
        ) {

            monitoringStatus =
                '✗ Failed'

        } else {

            monitoringStatus =
                "✓ ${monitoringValue}"
        }

    } else {

        monitoringStatus =
            '- Not reported'
    }


    /*
     * ============================================================
     * PIPELINE EXECUTION MESSAGE
     * ============================================================
     */

    def pipelineStatusMessage

    if (result == 'SUCCESS') {

        pipelineStatusMessage =
            'Security automation pipeline completed successfully.'

    } else if (result == 'UNSTABLE') {

        pipelineStatusMessage =
            'Pipeline completed with security findings or policy warnings.'

    } else {

        pipelineStatusMessage =
            'Pipeline stopped during execution.'
    }


    /*
     * ============================================================
     * BUILD SUMMARY
     * ============================================================
     */

    def buildSummary = """
*${statusTitle}*

`${jobName}`  •  Build `#${buildNumber}`  •  ${duration}

${pipelineStatusMessage}
"""


    /*
     * ============================================================
     * SECURITY SCANS
     * ============================================================
     */

    def scannerSection = """
*SECURITY SCANS*

Semgrep SAST       ${semgrepStatus}
Gitleaks Secrets   ${gitleaksStatus}
Snyk SCA           ${snykStatus}
Trivy Container    ${trivyStatus}
OWASP ZAP DAST     ${zapStatus}
"""


    /*
     * ============================================================
     * SECURITY CONTROLS
     * ============================================================
     */

    def controlsSection = """
*SECURITY CONTROLS*

OPA Policy         ${opaStatus}
WAF Protection     ${wafStatus}
"""


    /*
     * ============================================================
     * MONITORING
     * ============================================================
     */

    def monitoringSection = """
*MONITORING*

Prometheus / Grafana    ${monitoringStatus}
"""


    /*
     * ============================================================
     * SECURITY FINDINGS
     *
     * Only display findings for scanners that actually produced
     * result values.
     * ============================================================
     */

    def securitySignal = ''

    def signalLines = []


    if (env.SEMGREP_CRITICAL != null) {

        signalLines <<
            "Semgrep Critical: ${env.SEMGREP_CRITICAL ?: '0'}"
    }


    if (env.GITLEAKS_COUNT != null) {

        signalLines <<
            "Secrets: ${env.GITLEAKS_COUNT ?: '0'}"
    }


    if (env.SNYK_CRITICAL != null) {

        signalLines <<
            "Snyk Critical: ${env.SNYK_CRITICAL ?: '0'}"
    }


    if (env.TRIVY_CRITICAL != null) {

        signalLines <<
            "Trivy Critical: ${env.TRIVY_CRITICAL ?: '0'}"
    }


    if (env.DAST_HIGH != null) {

        signalLines <<
            "ZAP High: ${env.DAST_HIGH ?: '0'}"
    }


    if (!signalLines.isEmpty()) {

        securitySignal = """
*SECURITY SIGNAL*

${signalLines.join('  •  ')}

"""
    }


    /*
     * ============================================================
     * OPA POLICY MESSAGE
     * ============================================================
     */

    def opaMessage =
        env.OPA_MESSAGE ?: ''

    def opaMessageSection = ''

    if (opaMessage) {

        opaMessageSection = """
*OPA POLICY RESULT*

${opaMessage}

"""
    }


    /*
     * ============================================================
     * ACTION
     * ============================================================
     */

    def actionSection

    if (result == 'FAILURE') {

        actionSection = """
*ACTION*

❗ Pipeline execution failed.

Review the Jenkins build, console output and preserved security
evidence to identify and resolve the execution failure.

✉ Detailed failure information was sent by email.

"""

    } else if (result == 'UNSTABLE') {

        actionSection = """
*ACTION*

⚠ Security review required.

Review the scanner findings, OPA policy result and WAF response
before proceeding with the release.

✉ Detailed security assessment sent by email.

"""

    } else {

        actionSection = """
*ACTION*

✓ Security automation completed successfully.

Review the generated security evidence and assessment results
as part of the release validation process.

✉ Detailed security assessment sent by email.

"""
    }


    /*
     * ============================================================
     * JENKINS BUILD LINK
     * ============================================================
     */

    def buildLinkSection = ''

    if (buildUrl) {

        buildLinkSection = """
→ <${buildUrl}|View Jenkins Build>
"""
    }


    /*
     * ============================================================
     * FINAL SLACK MESSAGE
     * ============================================================
     */

    def message =
        buildSummary +
        "\n" +
        scannerSection +
        "\n" +
        controlsSection +
        "\n" +
        monitoringSection +
        securitySignal +
        opaMessageSection +
        actionSection +
        buildLinkSection


    /*
     * ============================================================
     * SEND SLACK
     *
     * Notification failure must NEVER fail the Jenkins pipeline.
     * ============================================================
     */

    echo ""

    echo "Sending Slack security notification..."

    try {

        slackSend(
            channel: slackChannel,
            color: slackColor,
            message: message,
            tokenCredentialId: slackCredential
        )

        echo "Slack notification sent: ${result}"

    } catch (Exception e) {

        echo "WARNING: Slack notification failed."

        echo "Slack error: ${e.getMessage()}"

    }
}


/*
 * ================================================================
 * RETURN SCRIPT
 * ================================================================
 */

return this