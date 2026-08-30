def send() {

    // ============================================================
    // BUILD INFORMATION
    // ============================================================

    def result =
        currentBuild.currentResult ?: 'SUCCESS'

    def duration =
        currentBuild.durationString ?: 'N/A'

    def buildNumber =
        env.BUILD_NUMBER ?: 'N/A'


    // ============================================================
    // RECIPIENTS
    // ============================================================

    def recipients = [
        'victoribekwe006@gmail.com',
        'paulozieme@gmail.com',
        'ibekwevictor042@gmail.com'
    ].join(',')


    // ============================================================
    // PIPELINE STATUS
    // ============================================================

    def statusColor =
        result == 'SUCCESS' ? '#166534' :
        result == 'FAILURE' ? '#dc2626' :
        '#c45a00'


    // ============================================================
    // OPA SECURITY GATE
    // ============================================================

    def opa =
        env.OPA_DECISION ?: 'NOT EVALUATED'

    def opaColor =
        opa == 'ALLOW' ? '#166534' :
        opa == 'BLOCK' ? '#dc2626' :
        '#ca8a04'

    def opaMessage =
        env.OPA_MESSAGE ?:
        'Security policy evaluation was not completed.'


    // ============================================================
    // WAF STATUS
    // ============================================================

    def wafValue =
        env.WAF_STATUS ?:
        env.WAF_DECISION ?:
        env.WAF_RESULT ?:
        ''

    def wafStatus

    if (wafValue) {

        def wafUpper =
            wafValue.toString().toUpperCase()

        if (
            wafUpper == 'PASS' ||
            wafUpper == 'ALLOW' ||
            wafUpper == 'ACTIVE' ||
            wafUpper == 'SUCCESS'
        ) {

            wafStatus = 'Active'

        } else if (
            wafUpper == 'FAIL' ||
            wafUpper == 'FAILED' ||
            wafUpper == 'BLOCK'
        ) {

            wafStatus = 'Failed'

        } else {

            wafStatus = wafValue.toString()
        }

    } else {

        wafStatus = 'Not executed'
    }


    // ============================================================
    // FAILURE SECTION
    // ============================================================

    def failureSection = ''

    if (result == 'FAILURE') {

        failureSection = """

<div style="margin-top:24px;background:#fff7ed;border:1px solid #fed7aa;border-left:5px solid #dc2626;border-radius:8px;padding:18px">

    <div style="font-size:16px;font-weight:bold;color:#991b1b">
        ⚠ Pipeline Execution Failure
    </div>

    <div style="margin-top:8px;font-size:12px;color:#64748b;line-height:1.6">
        The security automation pipeline failed during execution.
        Review the Jenkins build and available security evidence to
        identify and resolve the underlying execution issue.
    </div>

    <div style="margin-top:14px;background:#ffffff;border:1px solid #e2e8f0;border-radius:6px;padding:14px;font-size:12px;color:#475569">

        <div style="font-weight:bold;color:#334155">
            📄 Jenkins Console Output
        </div>

        <div style="margin-top:5px;color:#64748b">
            The Jenkins console output is attached to this notification
            when the pipeline execution fails.
        </div>

    </div>

</div>

"""
    }


    // ============================================================
    // RECOMMENDATION
    // ============================================================

    def recommendation =

        result == 'FAILURE' ?

        'Investigate the Jenkins pipeline failure using the build console output and preserved security evidence. Resolve the underlying execution issue and rerun the assessment.' :

        opa == 'BLOCK' ?

        'Critical security findings or policy violations require remediation before the release can proceed.' :

        opa == 'WARNING' ?

        'Review the reported security findings and policy warnings before proceeding with deployment.' :

        'The automated security assessment completed successfully and the security policy requirements were satisfied.'


    // ============================================================
    // SECURITY SCAN RESULTS
    // ============================================================

    def scanRows = """

<tr>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">
        SAST
    </td>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;color:#64748b">
        Semgrep
    </td>

    <td align="right" style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">

        ${
            env.SEMGREP_CRITICAL != null ?

            "${env.SEMGREP_CRITICAL ?: 0}C · ${env.SEMGREP_WARNING ?: 0}W · ${env.SEMGREP_INFO ?: 0}I" :

            "NOT EXECUTED"
        }

    </td>

</tr>


<tr>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">
        Secrets Detection
    </td>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;color:#64748b">
        Gitleaks
    </td>

    <td align="right" style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">

        ${
            env.GITLEAKS_COUNT != null ?

            "${env.GITLEAKS_COUNT ?: 0} Secret(s)" :

            "NOT EXECUTED"
        }

    </td>

</tr>


<tr>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">
        Dependency / SCA
    </td>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;color:#64748b">
        Snyk
    </td>

    <td align="right" style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">

        ${
            env.SNYK_CRITICAL != null ?

            "${env.SNYK_CRITICAL ?: 0}C · ${env.SNYK_HIGH ?: 0}H · ${env.SNYK_MEDIUM ?: 0}M · ${env.SNYK_LOW ?: 0}L" :

            "NOT EXECUTED"
        }

    </td>

</tr>


<tr>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">
        Container Security
    </td>

    <td style="padding:13px;border-bottom:1px solid #e2e8f0;color:#64748b">
        Trivy
    </td>

    <td align="right" style="padding:13px;border-bottom:1px solid #e2e8f0;font-weight:bold">

        ${
            env.TRIVY_CRITICAL != null ?

            "${env.TRIVY_CRITICAL ?: 0}C · ${env.TRIVY_HIGH ?: 0}H" :

            "NOT EXECUTED"
        }

    </td>

</tr>


<tr>

    <td style="padding:13px;font-weight:bold">
        DAST
    </td>

    <td style="padding:13px;color:#64748b">
        OWASP ZAP
    </td>

    <td align="right" style="padding:13px;font-weight:bold">

        ${
            env.DAST_HIGH != null ?

            "${env.DAST_HIGH ?: 0}H · ${env.DAST_MEDIUM ?: 0}M · ${env.DAST_LOW ?: 0}L · ${env.DAST_INFO ?: 0}I" :

            "NOT EXECUTED"
        }

    </td>

</tr>

"""


    // ============================================================
    // LOAD EMAIL TEMPLATE
    //
    // The template is located in:
    // notification/security-email.html
    // ============================================================

    def html =
        readFile('notification/security-email.html')


    // ============================================================
    // TEMPLATE VALUES
    // ============================================================

    def values = [

        '{{STATUS}}':
            result,

        '{{STATUS_COLOR}}':
            statusColor,

        '{{BUILD_NUMBER}}':
            buildNumber,

        '{{DURATION}}':
            duration,

        '{{SCAN_ROWS}}':
            scanRows,

        '{{FAILURE_SECTION}}':
            failureSection,

        '{{OPA}}':
            opa,

        '{{OPA_COLOR}}':
            opaColor,

        '{{OPA_MESSAGE}}':
            opaMessage,

        '{{WAF_STATUS}}':
            wafStatus,

        '{{ACTION_COLOR}}':
            result == 'FAILURE'
                ? '#dc2626'
                : opaColor,

        '{{RECOMMENDATION}}':
            recommendation
    ]


    // ============================================================
    // REPLACE TEMPLATE PLACEHOLDERS
    // ============================================================

    values.each { key, value ->

        html =
            html.replace(
                key,
                value.toString()
            )
    }


    // ============================================================
    // EMAIL SUBJECT
    // ============================================================

    def subject =
        "Security Pipeline #${buildNumber} — ${result}"


    // ============================================================
    // SECURITY REPORT ATTACHMENTS
    //
    // Primary attachment:
    // reports/security-reports-<BUILD_NUMBER>.zip
    //
    // Fallback:
    // Individual security reports
    // ============================================================

    def zipFile =
        "reports/security-reports-${buildNumber}.zip"

    def zipExists =
        fileExists(zipFile)

    def attachmentPattern = ''


    // ============================================================
    // ZIP AVAILABLE
    // ============================================================

    if (zipExists) {

        attachmentPattern =
            zipFile

        echo ''
        echo '=============================================='
        echo ' SECURITY EMAIL ATTACHMENTS'
        echo '=============================================='
        echo "ZIP report found:"
        echo "  ${zipFile}"
        echo ''
        echo 'Email attachment mode: ZIP'
        echo '=============================================='
        echo ''

    }


    // ============================================================
    // ZIP NOT AVAILABLE
    //
    // Fall back to individual reports.
    // ============================================================

    else {

        echo ''
        echo '=============================================='
        echo ' SECURITY EMAIL ATTACHMENTS'
        echo '=============================================='
        echo 'ZIP report was not found.'
        echo 'Falling back to individual security reports.'
        echo '=============================================='
        echo ''

        def individualReports =
            findFiles(
                glob: 'reports/**'
            ).findAll { file ->

                !file.directory &&
                !file.name.endsWith('.zip')
            }


        if (individualReports.size() > 0) {

            attachmentPattern =
                individualReports
                    .collect { it.path }
                    .join(',')

            echo 'Individual security reports attached:'

            individualReports.each { file ->

                echo "  ✓ ${file.path}"
            }

        } else {

            echo 'No individual security reports were found.'

            attachmentPattern = ''
        }
    }


    // ============================================================
    // SEND EMAIL
    // ============================================================

    emailext(

        to:
            recipients,

        subject:
            subject,

        mimeType:
            'text/html',

        body:
            html,

        attachmentsPattern:
            attachmentPattern,

        // Attach Jenkins console log only when
        // the pipeline actually failed.
        attachLog:
            result == 'FAILURE'
    )


    // ============================================================
    // NOTIFICATION SUMMARY
    // ============================================================

    echo ''

    echo '=============================================='
    echo ' SECURITY EMAIL NOTIFICATION'
    echo '=============================================='

    echo "Result        : ${result}"
    echo "Build         : #${buildNumber}"
    echo "Recipients    : ${recipients}"

    if (zipExists) {

        echo 'Attachments   : ZIP'
        echo "ZIP File      : ${zipFile}"

    } else {

        echo 'Attachments   : Individual reports'
    }

    echo "Console Log   : ${result == 'FAILURE' ? 'ATTACHED' : 'NOT ATTACHED'}"

    echo '=============================================='
    echo ''

    echo 'Security assessment email completed.'
}


// ================================================================
// RETURN SCRIPT
// ================================================================

return this