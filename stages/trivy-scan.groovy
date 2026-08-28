timeout(time: 20, unit: 'MINUTES') {

    def trivyMissing = bat(
        script:
            'docker image inspect aquasec/trivy:latest >NUL 2>&1',
        returnStatus: true
    ) != 0

    if (trivyMissing) {
        echo '[Trivy] Image not found locally — pulling...'
        bat 'docker pull aquasec/trivy:latest'
    } else {
        echo '[Trivy] Using locally cached image'
    }

    def dvwaMissing = bat(
        script:
            'docker image inspect vulnerables/web-dvwa >NUL 2>&1',
        returnStatus: true
    ) != 0

    if (dvwaMissing) {
        echo '[DVWA] Image not found locally — pulling...'
        bat 'docker pull vulnerables/web-dvwa'
    } else {
        echo '[DVWA] Using locally cached image'
    }

    bat """
    docker run --rm ^
        -v "C:/Users/Ibekw/.cache/trivy:/root/.cache/trivy" ^
        -v "%WORKSPACE%\\reports:/workspace" ^
        -e TRIVY_DOCKER_HOST=npipe:////./pipe/docker_engine ^
        aquasec/trivy:latest ^
        image ^
        --skip-db-update ^
        --skip-java-db-update ^
        --scanners vuln ^
        --severity HIGH,CRITICAL ^
        --format json ^
        --output /workspace/trivy-report.json ^
        --timeout 10m ^
        --exit-code 0 ^
        vulnerables/web-dvwa
    """
}