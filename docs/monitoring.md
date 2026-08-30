# Monitoring

## Purpose

Monitoring provides operational visibility over the security automation environment. It complements the detailed scanner evidence and does not replace the security controls.

## Components

- **Prometheus** — metrics collection and time-series storage.
- **Grafana** — visualization and dashboards.

## Project Layout

```text
monitoring/
├── grafana/
│   └── dashboards/
│       └── dashboard.json
├── provisioning/
│   └── datasources.yml
├── prometheus/
│   └── prometheus.yml
└── docker-compose.yml
```

## Independent Validation

Before connecting monitoring to the Jenkins workflow:

1. Start the monitoring Compose stack.
2. Confirm Prometheus loads its configuration.
3. Confirm Prometheus targets are healthy.
4. Confirm metrics are available.
5. Confirm Grafana starts.
6. Confirm the Prometheus datasource works.
7. Confirm the security dashboard loads and returns data.

## Pipeline Telemetry

Useful metrics include:

```text
pipeline build status
pipeline duration
scanner execution status
findings by severity
OPA decisions
WAF status
report/archive status
```

Monitoring should remain observational and should not become a dependency that prevents security evidence generation.
