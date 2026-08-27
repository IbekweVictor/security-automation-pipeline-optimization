# Pipeline Workflow

The Jenkinsfile is organized into sequential security phases with independent scans executed in parallel where appropriate.

## Stages

1. **Checkout Repositories** — retrieves application and authenticated-DAST sources.
2. **Verify Docker** — verifies Docker availability.
3. **Prepare Reports** — prepares the security evidence workspace.
4. **Semgrep SAST** — performs static analysis.
5. **Analyze Semgrep** — processes Semgrep results.
6. **Security Scans** — runs Gitleaks, Snyk, and Trivy in parallel.
7. **Analyze Static Scans** — consolidates static/supply-chain scan information.
8. **Cleanup Old DAST** — removes stale DAST resources.
9. **Start DAST Environment** — starts the authenticated ZAP environment.
10. **Wait For DAST Scan** — waits for the scan to complete.
11. **Collect DAST Reports** — retrieves ZAP evidence.
12. **Stop DAST Environment** — releases DAST resources.
13. **Analyze DAST Results** — extracts DAST severity information.
14. **Generate Summary Report** — produces `reports/summary-report.txt`.
15. **Start DefectDojo** — ensures findings management is available.
16. **Upload Reports to DefectDojo** — imports scanner reports.
17. **Collect Unified Findings** — creates `unified-findings.json`.
18. **OPA Policy Evaluation** — validates and evaluates `opa/policy.rego`.
19. **Dynamic WAF Protection** — applies/validates the WAF protection layer.
20. **Archive Reports** — retains security evidence and creates the optional ZIP.
21. **Post Actions** — preserves evidence, sends notifications, and cleans the workspace.

## OPA Outputs

The OPA stage produces:

```text
opa-evaluation.json
opa-result.json
```

and exposes:

```text
OPA_DECISION
OPA_MESSAGE
```

## Failure Handling

Notification failures are isolated from the primary pipeline result. Evidence generated before a failure is preserved where possible.

OPA distinguishes between `ALLOW`, `WARNING`, and `BLOCK`; the resulting security state is included in email and Slack reporting.
