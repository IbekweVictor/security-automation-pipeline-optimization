# Security Controls

## SAST — Semgrep

Semgrep performs source-code analysis. The analyzed severity values are exposed as:

```text
SEMGREP_CRITICAL
SEMGREP_WARNING
SEMGREP_INFO
```

## Secrets — Gitleaks

Gitleaks identifies exposed credentials and secret material. The normalized count is:

```text
GITLEAKS_COUNT
```

## SCA — Snyk

Snyk evaluates project dependencies:

```text
SNYK_CRITICAL
SNYK_HIGH
SNYK_MEDIUM
SNYK_LOW
```

## Container Security — Trivy

Trivy assesses container vulnerabilities, with summary values including:

```text
TRIVY_CRITICAL
TRIVY_HIGH
```

## DAST — OWASP ZAP

Authenticated ZAP tests the running web application:

```text
Cleanup → Start → Wait → Collect → Stop → Analyze
```

Severity values include:

```text
DAST_HIGH
DAST_MEDIUM
DAST_LOW
DAST_INFO
```

## DefectDojo

DefectDojo provides centralized findings management. Active findings from the configured tests are aggregated into:

```json
{
  "critical": 0,
  "high": 0,
  "medium": 0,
  "low": 0,
  "info": 0
}
```

This file is the OPA policy input.

## OPA

`opa/policy.rego` implements the project's security gate:

| Condition | Result |
|---|---|
| Critical > 0 | BLOCK |
| High > 10 | BLOCK |
| High > 0 | WARNING |
| Medium > 20 | WARNING |
| Low > 50 | WARNING |
| Info > 100 | WARNING |
| No threshold exceeded | ALLOW |

## WAF

The WAF uses ModSecurity with the OWASP Core Rule Set and project-specific dynamic rules. It adds runtime protection and is complementary to vulnerability scanning.

## Monitoring

Prometheus and Grafana provide operational visibility into pipeline execution, scanner status, finding trends, policy decisions, WAF status, and service health.
