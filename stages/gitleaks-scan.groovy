def run() {

    timeout(time: 10, unit: 'MINUTES') {

        def missing = bat(
            script:
                'docker image inspect zricethezav/gitleaks:latest >NUL 2>&1',
            returnStatus: true
        ) != 0


        if (missing) {

            echo '[Gitleaks] Image not found locally — pulling...'

            bat 'docker pull zricethezav/gitleaks:latest'

        } else {

            echo '[Gitleaks] Using locally cached image'
        }


        bat """
        docker run --rm ^
            -v "%WORKSPACE%\\dvwa:/repo" ^
            -v "%WORKSPACE%\\reports:/reports" ^
            zricethezav/gitleaks:latest detect ^
            --source=/repo ^
            --no-git ^
            --report-format=json ^
            --report-path=/reports/gitleaks-report.json ^
            --exit-code=0
        """
    }
}


return this