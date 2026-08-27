# Reporting and Notifications

## Evidence

Security reports are stored under:

```text
reports/
```

The archive stage recursively discovers report files and archives them as Jenkins artifacts with fingerprinting.

A build-specific ZIP may be generated as:

```text
reports/security-reports-<BUILD_NUMBER>.zip
```

## Summary

The summary stage creates:

```text
reports/summary-report.txt
```

It records build information and severity summaries for Semgrep, Gitleaks, Snyk, Trivy, and OWASP ZAP.

## Email

Email notification is implemented in:

```text
notification/security-email.groovy
notification/security-email.html
```

The Groovy script prepares build status, scanner status, OPA decision, recommendation, and report attachments. The HTML file is the presentation template.

For failed builds, the Jenkins console log can be attached for investigation.

## Slack

Slack notification is implemented in:

```text
notification/slack-notification.groovy
```

It reports build status, scanner execution, OPA/WAF status, security signals, recommended action, and the Jenkins build link.

Slack delivery failures are caught and logged so that notification problems do not replace the underlying security result.

## Evidence Principle

**Notifications summarize. Reports provide detail. Jenkins artifacts provide retained evidence.**
