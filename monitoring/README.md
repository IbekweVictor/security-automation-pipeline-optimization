# Independent Jenkins Prometheus Monitoring Stack

Runs independently of the Jenkins project. The exporter is a long-running service and polls Jenkins; it is not invoked once per build.

Start with `docker compose up -d --build`.

Exporter: http://localhost:8000/metrics
Prometheus: http://localhost:9090
Grafana: http://localhost:3000

Set JENKINS_URL, JENKINS_USER, JENKINS_TOKEN and POLL_INTERVAL in `.env`.
