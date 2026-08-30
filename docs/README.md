# Security Automation Pipeline Documentation

## Optimization of Security Automation Pipeline for Vulnerability Detection and Mitigation in Web Applications

This directory documents the Jenkins-based DevSecOps pipeline used to automate vulnerability detection, findings consolidation, security-policy evaluation, runtime protection, evidence retention, notification, and monitoring.

## Documentation

- [Architecture](architecture.md) — components and security data flow
- [Pipeline Workflow](pipeline-workflow.md) — Jenkins execution sequence
- [Setup & Configuration](setup-and-configuration.md) — prerequisites and configuration
- [Security Controls](security-controls.md) — scanners, DefectDojo, OPA, WAF, and monitoring
- [Monitoring](monitoring.md) — Prometheus/Grafana implementation and validation
- [Reporting & Notifications](reporting-and-notifications.md) — evidence, email, and Slack
- [Troubleshooting](troubleshooting.md) — common operational problems

## Security Lifecycle

**Detect → Analyze → Consolidate → Govern → Protect → Report → Monitor**

The pipeline combines Semgrep SAST, Gitleaks, Snyk, Trivy, authenticated OWASP ZAP DAST, DefectDojo, Open Policy Agent (OPA), ModSecurity/OWASP CRS, Prometheus, Grafana, Jenkins, email, and Slack.

## Repository Structure

```text
security_automation_pipeline/
├── Jenkinsfile
├── stages/
├── opa/
├── waf/
├── monitoring/
├── notification/
├── reports/
├── docs/
└── README.md
```

## Security Gate

OPA evaluates the consolidated severity counts:

| Condition | Decision |
|---|---|
| Critical > 0 | BLOCK |
| High > 10 | BLOCK |
| High > 0 | WARNING |
| Medium > 20 | WARNING |
| Low > 50 | WARNING |
| Info > 100 | WARNING |
| Otherwise | ALLOW |

The policy input is `unified-findings.json`. The decision and message are exposed to downstream reporting.

## Evidence

Scanner and pipeline evidence is stored beneath `reports/` and archived as Jenkins build artifacts. A build-specific security evidence ZIP can also be generated.

Notifications provide a concise operational summary; the detailed reports remain the primary evidence for investigation and review.
