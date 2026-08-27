def run() {

    bat """
    if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"
    """
}


return this