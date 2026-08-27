def run() {

    echo '======================================'
    echo 'PROMETHEUS + GRAFANA MONITORING'
    echo '======================================'


    if (!fileExists(
        'monitoring/docker-compose.yml'
    )) {

        echo 'WARNING: monitoring/docker-compose.yml not found.'

        echo 'Monitoring stage skipped.'

        return
    }


    echo 'Starting Prometheus and Grafana...'


    dir('monitoring') {

        bat 'docker compose up -d'
    }


    echo 'Waiting for monitoring services...'


    sleep(
        time: 10,
        unit: 'SECONDS'
    )


    // ============================================================
    // PROMETHEUS
    // ============================================================

    def prometheusStatus = bat(
        script: """
        @curl -s -o NUL -w %%{http_code} ^
        "%PROMETHEUS_URL%/-/ready"
        """,
        returnStdout: true
    ).trim()


    echo "Prometheus HTTP Status: ${prometheusStatus}"


    // ============================================================
    // GRAFANA
    // ============================================================

    def grafanaStatus = bat(
        script: """
        @curl -s -o NUL -w %%{http_code} ^
        "%GRAFANA_URL%/api/health"
        """,
        returnStdout: true
    ).trim()


    echo "Grafana HTTP Status: ${grafanaStatus}"


    if (
        prometheusStatus == '200' &&
        grafanaStatus == '200'
    ) {

        env.MONITORING_STATUS =
            'ACTIVE'


        echo '======================================'
        echo 'MONITORING STACK ACTIVE'
        echo '======================================'

        echo "Prometheus : ${env.PROMETHEUS_URL}"
        echo "Grafana    : ${env.GRAFANA_URL}"

    } else {

        env.MONITORING_STATUS =
            'WARNING'


        echo 'WARNING: Monitoring stack did not return expected health status.'

        currentBuild.result =
            'UNSTABLE'
    }
}


return this