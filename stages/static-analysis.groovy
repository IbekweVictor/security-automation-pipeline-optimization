// ============================================================
// GITLEAKS
// ============================================================

int leaks = 0

if (fileExists('reports/gitleaks-report.json')) {

    def txt = readFile(
        'reports/gitleaks-report.json'
    ).trim()

    if (
        txt &&
        txt != '[]' &&
        txt != 'null'
    ) {

        def r = readJSON(
            text: txt
        )

        leaks = r.size()

        if (leaks > 0) {

            echo 'Top Secret Findings:'

            r.take(5).each {
                echo "  [${it.RuleID}] ${it.Description}"
            }
        }
    }
}

env.GITLEAKS_COUNT = "${leaks}"


// ============================================================
// SNYK
// ============================================================

int snykCritical = 0
int snykHigh     = 0
int snykMedium   = 0
int snykLow      = 0

if (fileExists('reports/snyk-report.json')) {

    def txt = readFile(
        'reports/snyk-report.json'
    ).trim()

    if (
        txt &&
        txt != '{}' &&
        txt.startsWith('{')
    ) {

        try {

            def r = readJSON(
                text: txt
            )

            def vulns = r.vulnerabilities ?: []

            vulns.each {

                switch (it.severity) {

                    case 'critical':
                        snykCritical++
                        break

                    case 'high':
                        snykHigh++
                        break

                    case 'medium':
                        snykMedium++
                        break

                    case 'low':
                        snykLow++
                        break
                }
            }

            if (vulns.size() > 0) {

                echo 'Top Snyk Findings:'

                vulns.take(5).each {
                    echo "  [${it.severity.toUpperCase()}] ${it.title}"
                }
            }

        } catch (Exception e) {

            echo '[Snyk] Could not parse report — no supported manifest found.'
        }

    } else {

        echo '[Snyk] No supported package manifest found in DVWA.'
    }
}

env.SNYK_CRITICAL = "${snykCritical}"
env.SNYK_HIGH     = "${snykHigh}"
env.SNYK_MEDIUM   = "${snykMedium}"
env.SNYK_LOW      = "${snykLow}"


// ============================================================
// TRIVY
// ============================================================

int trivyCritical = 0
int trivyHigh     = 0

if (fileExists('reports/trivy-report.json')) {

    def r = readJSON(
        file: 'reports/trivy-report.json'
    )

    (r.Results ?: []).each { result ->

        (result.Vulnerabilities ?: []).each { vuln ->

            if (vuln.Severity == 'CRITICAL') {
                trivyCritical++
            }

            if (vuln.Severity == 'HIGH') {
                trivyHigh++
            }
        }
    }
}

env.TRIVY_CRITICAL = "${trivyCritical}"
env.TRIVY_HIGH     = "${trivyHigh}"


// ============================================================
// SUMMARY
// ============================================================

echo '======================================'
echo 'STATIC SCAN SUMMARY'
echo '======================================'

echo "Semgrep  → Critical: ${env.SEMGREP_CRITICAL} | Warning: ${env.SEMGREP_WARNING} | Info: ${env.SEMGREP_INFO}"
echo "Gitleaks → Secrets: ${leaks}"
echo "Snyk     → Critical: ${snykCritical} | High: ${snykHigh} | Medium: ${snykMedium} | Low: ${snykLow}"
echo "Trivy    → Critical: ${trivyCritical} | High: ${trivyHigh}"

echo '======================================'