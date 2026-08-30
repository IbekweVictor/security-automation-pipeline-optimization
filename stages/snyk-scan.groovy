timeout(time: 15, unit: 'MINUTES') {

    def missing = bat(
        script:
            'docker image inspect snyk/snyk:alpine >NUL 2>&1',
        returnStatus: true
    ) != 0

    if (missing) {
        echo '[Snyk] Image not found locally — pulling...'
        bat 'docker pull snyk/snyk:alpine'
    } else {
        echo '[Snyk] Using locally cached image'
    }

    dir('dvwa') {

        bat """
        docker run --rm ^
            -e SNYK_TOKEN=%SNYK_TOKEN% ^
            -v "%WORKSPACE%\\dvwa:/project" ^
            -w /project ^
            snyk/snyk:alpine ^
            snyk test ^
            --all-projects ^
            --json ^
            > "%WORKSPACE%\\reports\\snyk-report.json" ^
            || exit 0
        """
    }
}