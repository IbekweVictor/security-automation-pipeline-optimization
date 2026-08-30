echo '======================================'
echo 'DEFECTDOJO REPORT REIMPORT'
echo '======================================'

def reports = [

    [
        name: 'Semgrep',
        type: 'Semgrep JSON Report',
        file: 'reports\\semgrep-report.json',
        test: 5
    ],

    [
        name: 'Gitleaks',
        type: 'Gitleaks Scan',
        file: 'reports\\gitleaks-report.json',
        test: 6
    ],

    [
        name: 'Snyk',
        type: 'Snyk Scan',
        file: 'reports\\snyk-report.json',
        test: 7
    ],

    [
        name: 'Trivy',
        type: 'Trivy Scan',
        file: 'reports\\trivy-report.json',
        test: 8
    ],

    [
        name: 'OWASP ZAP',
        type: 'ZAP Scan',
        file: 'reports\\zap-report.xml',
        test: 9
    ]
]

int uploaded = 0

reports.each { report ->

    if (fileExists(report.file)) {

        echo "Reimporting ${report.name}..."

        def fullPath =
            "${env.WORKSPACE}\\${report.file}"

        def status = bat(
            script: """
            @curl -s -w "%%{http_code}" ^
                -o "%WORKSPACE%\\curl_response.txt" ^
                -X POST ^
                %DD_URL%/api/v2/reimport-scan/ ^
                -H "Authorization: Token %DEFECTDOJO_API%" ^
                -F "test=${report.test}" ^
                -F "scan_type=${report.type}" ^
                -F "active=true" ^
                -F "verified=true" ^
                -F "close_old_findings=false" ^
                -F "do_not_reactivate=false" ^
                -F "minimum_severity=Info" ^
                -F "file=@${fullPath}"
            """,
            returnStdout: true
        ).trim()

        if (
            status.endsWith('201') ||
            status.endsWith('200')
        ) {

            echo "✓ ${report.name} reimported successfully."

            uploaded++

        } else {

            echo "✗ ${report.name} failed (HTTP ${status})"

            if (fileExists('curl_response.txt')) {
                echo readFile('curl_response.txt')
            }
        }

    } else {

        echo "Skipping ${report.name} (report not found)."
    }
}

echo ''
echo '======================================'
echo 'DEFECTDOJO REIMPORT COMPLETE'
echo '======================================'

echo "Reports Updated : ${uploaded}/${reports.size()}"

echo 'Dashboard       : http://localhost:8080/engagement/1'

echo '======================================'