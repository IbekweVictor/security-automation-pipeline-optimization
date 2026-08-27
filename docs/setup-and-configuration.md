# Setup and Configuration

## Prerequisites

The current implementation requires a Jenkins environment with access to:

- Git
- Docker/Docker Compose
- Semgrep
- Gitleaks
- Snyk CLI
- Trivy
- OWASP ZAP authenticated-DAST environment
- DefectDojo
- Open Policy Agent (`opa`)
- Jenkins Email Extension plugin
- Jenkins Slack plugin
- Prometheus
- Grafana

The current stage scripts are designed for the Windows Jenkins agent environment used by the project.

## Jenkins Credentials

Credentials must be stored in Jenkins Credentials Manager.

The pipeline references:

```text
snyk_token
defectdojo_api_key
slack-webhook
```

Never commit tokens, passwords, API keys, or webhook secrets to the repository.

## Main Configuration

The Jenkinsfile defines project and service settings including:

```text
DVWA_REPO
DAST_REPO
DD_URL
DD_PRODUCT
DD_ENGAGEMENT
JIRA_URL
JIRA_PROJECT
JIRA_ISSUE_TYPE
REPORT_DIR
OPA_DIR
WAF_DIR
NOTIFICATION_DIR
MONITORING_DIR
PROMETHEUS_URL
GRAFANA_URL
```

Update environment-specific values before deployment.

## DefectDojo

Confirm that the configured DefectDojo URL is reachable, the API credential is valid, the product and engagement exist, and the configured test IDs correspond to the current assessment.

The findings stage converts DefectDojo severity information into `unified-findings.json`.

## WAF

The WAF project contains:

```text
waf/
├── docker-compose.yml
├── dynamic_rules.conf
└── README.md
```

The Compose configuration uses the OWASP ModSecurity CRS NGINX image and mounts the dynamic rules into the CRS rule directory.

## Monitoring

Monitoring is maintained under:

```text
monitoring/
├── prometheus/
├── grafana/
├── provisioning/
└── docker-compose.yml
```

Prometheus and Grafana should be validated independently before their telemetry is connected to the Jenkins workflow.
