def run() {

    timeout(time: 15, unit: 'MINUTES') {

        def missing = bat(
            script:
                'docker image inspect semgrep/semgrep:canary >NUL 2>&1',
            returnStatus: true
        ) != 0


        if (missing) {

            echo '[Semgrep] Image not found locally — pulling...'

            bat 'docker pull semgrep/semgrep:canary'

        } else {

            echo '[Semgrep] Using locally cached image'
        }


        dir('dvwa') {

            def ws =
                pwd().replace('\\', '/')


            bat """
            docker run --rm ^
                -v "${ws}:/src" ^
                -v "%WORKSPACE%\\reports:/reports" ^
                semgrep/semgrep:canary ^
                semgrep scan ^
                --config=auto ^
                --json ^
                --output=/reports/semgrep-report.json ^
                /src || exit 0
            """
        }
    }
}


return this