# Architecture

## Overview

Jenkins orchestrates a layered security workflow. Each control has a defined responsibility and produces evidence consumed by later stages.

```text
                         Jenkins
                           |
        +------------------+------------------+
        |                  |                  |
     Source/SAST      Parallel Scans        DAST
      Semgrep        Gitleaks/Snyk/Trivy   OWASP ZAP
        |                  |                  |
        +------------------+------------------+
                           |
                    Result Analysis
                           |
                       DefectDojo
                           |
                 unified-findings.json
                           |
                          OPA
                    ALLOW/WARNING/BLOCK
                           |
                          WAF
                           |
          +----------------+----------------+
          |                |                |
       Archive          Email/Slack    Prometheus/Grafana
```

## Component Responsibilities

**Jenkins** — orchestration, sequencing, parallel execution, environment management, artifact retention, and notifications.

**Semgrep** — source-code security analysis.

**Gitleaks** — exposed-secret detection.

**Snyk** — dependency/SCA vulnerability analysis.

**Trivy** — container vulnerability analysis.

**OWASP ZAP** — authenticated runtime/web-application testing.

**DefectDojo** — centralized vulnerability findings management.

**OPA** — policy-based security decision making.

**ModSecurity + OWASP CRS** — runtime web-application protection.

**Prometheus + Grafana** — security-pipeline and operational monitoring.

## Security Data Flow

```text
Scanner Reports
      ↓
DefectDojo
      ↓
Severity Aggregation
      ↓
unified-findings.json
      ↓
OPA Policy
      ↓
ALLOW / WARNING / BLOCK
      ↓
WAF + Evidence + Notifications + Monitoring
```

The separation keeps security policy independent from Jenkins orchestration and allows the policy to be validated and tested independently.
