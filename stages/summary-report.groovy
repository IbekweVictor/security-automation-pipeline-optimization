echo '======================================'
echo 'GENERATING SUMMARY REPORT'
echo '======================================'

def summary = """\
====================================================
        SECURITY PIPELINE SUMMARY REPORT
====================================================

Project           : DVWA
Build Number      : ${env.BUILD_NUMBER}
Build URL         : ${env.BUILD_URL}
Generated         : ${new Date()}

----------------------------------------------------
SEMGREP
----------------------------------------------------
Critical : ${env.SEMGREP_CRITICAL ?: '0'}
Warning  : ${env.SEMGREP_WARNING ?: '0'}
Info     : ${env.SEMGREP_INFO ?: '0'}

----------------------------------------------------
GITLEAKS
----------------------------------------------------
Secrets Found : ${env.GITLEAKS_COUNT ?: '0'}

----------------------------------------------------
SNYK
----------------------------------------------------
Critical : ${env.SNYK_CRITICAL ?: '0'}
High     : ${env.SNYK_HIGH ?: '0'}
Medium   : ${env.SNYK_MEDIUM ?: '0'}
Low      : ${env.SNYK_LOW ?: '0'}

----------------------------------------------------
TRIVY
----------------------------------------------------
Critical : ${env.TRIVY_CRITICAL ?: '0'}
High     : ${env.TRIVY_HIGH ?: '0'}

----------------------------------------------------
OWASP ZAP DAST
----------------------------------------------------
High     : ${env.DAST_HIGH ?: '0'}
Medium   : ${env.DAST_MEDIUM ?: '0'}
Low      : ${env.DAST_LOW ?: '0'}
Info     : ${env.DAST_INFO ?: '0'}

----------------------------------------------------
Generated Reports
----------------------------------------------------
✓ semgrep-report.json
✓ gitleaks-report.json
✓ snyk-report.json
✓ trivy-report.json
✓ zap-report.json
✓ zap-report.html
✓ scanner.log

====================================================
End of Report
====================================================
"""

writeFile(
    file: 'reports/summary-report.txt',
    text: summary
)

echo summary

echo '======================================'
echo 'SUMMARY REPORT GENERATED'
echo '======================================'