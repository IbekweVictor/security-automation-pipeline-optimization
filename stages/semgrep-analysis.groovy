int semgrepCritical = 0
int semgrepWarning  = 0
int semgrepInfo     = 0

if (fileExists('reports/semgrep-report.json')) {

    def r = readJSON(
        file: 'reports/semgrep-report.json'
    )

    def results = r.results ?: []

    semgrepCritical = results.count {
        it.extra?.severity == 'ERROR'
    }

    semgrepWarning = results.count {
        it.extra?.severity == 'WARNING'
    }

    semgrepInfo = results.count {
        it.extra?.severity == 'INFO'
    }

    if (results.size() > 0) {

        echo 'Top Semgrep Findings:'

        results.take(5).each {
            echo "  [${it.extra?.severity}] ${it.check_id}"
        }
    }
}

env.SEMGREP_CRITICAL = "${semgrepCritical}"
env.SEMGREP_WARNING  = "${semgrepWarning}"
env.SEMGREP_INFO     = "${semgrepInfo}"

echo '''
======================================
SEMGREP RESULTS
======================================
'''

echo "Critical : ${semgrepCritical}"
echo "Warning  : ${semgrepWarning}"
echo "Info     : ${semgrepInfo}"

echo '======================================'