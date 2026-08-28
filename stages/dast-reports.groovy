/************************************************
 * Collect DAST Reports
 ************************************************/

if (!fileExists(env.REPORT_DIR)) {
    bat """
    if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
    """
}

bat """
docker cp scanner:/app/reports/zap-report.json "%REPORT_DIR%\\zap-report.json"
docker cp scanner:/app/reports/zap-report.html "%REPORT_DIR%\\zap-report.html"
docker cp scanner:/app/reports/zap-report.xml "%REPORT_DIR%\\zap-report.xml"
docker logs scanner > "%REPORT_DIR%\\scanner.log" 2>&1
"""

echo '✓ DAST reports collected.'