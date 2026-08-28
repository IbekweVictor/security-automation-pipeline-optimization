echo ''

echo '===================================================='
echo '        SECURITY PIPELINE SUMMARY REPORT'
echo '===================================================='

// Helper to avoid displaying null values in the report.
def valueOrZero = { value ->
    return value != null && value.toString().trim()
        ? value.toString()
        : '0'
}

def opaDecision =
    env.OPA_DECISION ?: 'NOT EVALUATED'

def opaMessage =
    env.OPA_MESSAGE ?: 'OPA policy evaluation was not completed.'

def wafStatus =
    env.WAF_STATUS ?:
    env.WAF_DECISION ?:
    env.WAF_RESULT ?:
    'NOT EVALUATED'

def summary = """

====================================================
        SECURITY PIPELINE SUMMARY REPORT
====================================================

Project           : DVWA
Build Number      : ${env.BUILD_NUMBER ?: 'N/A'}
Build URL         : ${env.BUILD_URL ?: 'N/A'}
Generated         : ${new Date()}

----------------------------------------------------
SEMGREP SAST
----------------------------------------------------

Critical : ${valueOrZero(env.SEMGREP_CRITICAL)}
Warning  : ${valueOrZero(env.SEMGREP_WARNING)}
Info     : ${valueOrZero(env.SEMGREP_INFO)}

----------------------------------------------------
GITLEAKS SECRET DETECTION
----------------------------------------------------

Secrets Found : ${valueOrZero(env.GITLEAKS_COUNT)}

----------------------------------------------------
SNYK DEPENDENCY / SCA
----------------------------------------------------

Critical : ${valueOrZero(env.SNYK_CRITICAL)}
High     : ${valueOrZero(env.SNYK_HIGH)}
Medium   : ${valueOrZero(env.SNYK_MEDIUM)}
Low      : ${valueOrZero(env.SNYK_LOW)}

----------------------------------------------------
TRIVY CONTAINER SECURITY
----------------------------------------------------

Critical : ${valueOrZero(env.TRIVY_CRITICAL)}
High     : ${valueOrZero(env.TRIVY_HIGH)}

----------------------------------------------------
OWASP ZAP AUTHENTICATED DAST
----------------------------------------------------

High     : ${valueOrZero(env.DAST_HIGH)}
Medium   : ${valueOrZero(env.DAST_MEDIUM)}
Low      : ${valueOrZero(env.DAST_LOW)}
Info     : ${valueOrZero(env.DAST_INFO)}

----------------------------------------------------
OPA SECURITY POLICY
----------------------------------------------------

Decision : ${opaDecision}
Message  : ${opaMessage}

----------------------------------------------------
WAF PROTECTION
----------------------------------------------------

Status   : ${wafStatus}

----------------------------------------------------
GENERATED SECURITY EVIDENCE
----------------------------------------------------

✓ semgrep-report.json
✓ gitleaks-report.json
✓ snyk-report.json
✓ trivy-report.json
✓ zap-report.json
✓ zap-report.html
✓ zap-report.xml
✓ scanner.log
✓ summary-report.txt

Additional OPA/WAF evidence is included when generated
by the corresponding pipeline stages.

====================================================
End of Report
====================================================

"""

writeFile(
    file: 'reports/summary-report.txt',
    text: summary.trim() + System.lineSeparator()
)

echo summary