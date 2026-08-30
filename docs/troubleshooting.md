# Troubleshooting

## Stage Cannot Be Loaded

Confirm the referenced file exists under:

```text
stages/<stage-name>.groovy
```

The Jenkinsfile loads these scripts with `load(...).run()`.

## Docker Problems

Check Docker availability on the Jenkins agent:

```text
docker version
docker ps
```

For WAF/DAST, verify the expected containers and Docker networks.

## DefectDojo Problems

Check:

- `DD_URL`
- `DEFECTDOJO_API`
- product and engagement IDs
- configured test IDs
- report-import results
- active findings returned by the API

## OPA Problems

Verify:

```text
unified-findings.json
opa/policy.rego
```

Validate the policy with:

```text
opa check opa/policy.rego
```

The input must contain numeric:

```text
critical
high
medium
low
info
```

## WAF Problems

Check:

```text
docker compose config
docker compose ps
docker logs modsecurity-waf
```

Confirm the external Docker network exists and that the configured backend is reachable.

## Email Problems

Verify the Email Extension plugin, SMTP configuration, notification files, and report paths:

```text
notification/security-email.groovy
notification/security-email.html
reports/
```

## Slack Problems

Verify the Slack plugin, `slack-webhook` credential, channel, and connectivity.

## Monitoring Problems

For Prometheus, check the configuration and target health.

For Grafana, check the datasource, dashboard provisioning, and panel queries.

Monitoring failures should not prevent retention of the core security evidence.

## Recovery

1. Identify the first failed stage.
2. Inspect its Jenkins console output.
3. Check the expected report.
4. Verify dependent services and credentials.
5. Correct the underlying issue.
6. Re-run the assessment.
7. Preserve failed-build evidence when investigation is required.
