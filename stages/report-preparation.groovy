echo '======================================'
echo 'PREPARING SECURITY REPORT DIRECTORY'
echo '======================================'

bat """
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
"""

echo '✓ Security report directory ready.'

echo '======================================'
echo 'REPORT PREPARATION COMPLETE'
echo '======================================'