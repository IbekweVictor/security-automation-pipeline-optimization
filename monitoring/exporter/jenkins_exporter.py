import os
import time
import requests
from flask import Flask, Response


# ============================================================
# CONFIGURATION
# ============================================================

JENKINS_URL = os.getenv(
    "JENKINS_URL",
    "http://localhost:8081"
).rstrip("/")

JENKINS_USER = os.getenv(
    "JENKINS_USER",
    "admin"
)

JENKINS_API_TOKEN = os.getenv(
    "JENKINS_API_TOKEN",
    ""
)

JENKINS_JOB = os.getenv(
    "JENKINS_JOB",
    "security-automation-pipeline"
)

EXPORTER_PORT = int(
    os.getenv("EXPORTER_PORT", "8000")
)


# ============================================================
# FLASK APPLICATION
# ============================================================

app = Flask(__name__)


# ============================================================
# JENKINS API
# ============================================================

def get_jenkins_data():

    url = (
        f"{JENKINS_URL}/job/"
        f"{JENKINS_JOB}/api/json"
    )

    params = {
        "tree": (
            "builds[number,result,duration],"
            "lastBuild[number,result,duration],"
            "lastCompletedBuild[number,result,duration]"
        )
    }

    try:

        response = requests.get(
            url,
            params=params,
            auth=(
                JENKINS_USER,
                JENKINS_API_TOKEN
            ),
            timeout=10
        )

        response.raise_for_status()

        return response.json()

    except Exception as error:

        print(
            f"Jenkins API error: {error}"
        )

        return None


# ============================================================
# METRICS
# ============================================================

def generate_metrics():

    timestamp = int(time.time())

    lines = []


    # --------------------------------------------------------
    # Jenkins availability
    # --------------------------------------------------------

    jenkins_up = 0
    jenkins_info = 0

    try:

        response = requests.get(
            f"{JENKINS_URL}/api/json",
            auth=(
                JENKINS_USER,
                JENKINS_API_TOKEN
            ),
            timeout=10
        )

        if response.status_code == 200:

            jenkins_up = 1
            jenkins_info = 1

    except Exception as error:

        print(
            f"Jenkins availability error: {error}"
        )


    lines.append(
        "# HELP jenkins_up "
        "Jenkins controller availability"
    )

    lines.append(
        "# TYPE jenkins_up gauge"
    )

    lines.append(
        f"jenkins_up {jenkins_up}"
    )


    lines.append(
        "# HELP jenkins_info "
        "Jenkins API information availability"
    )

    lines.append(
        "# TYPE jenkins_info gauge"
    )

    lines.append(
        f'jenkins_info{{url="{JENKINS_URL}"}} '
        f"{jenkins_info}"
    )


    # --------------------------------------------------------
    # Job metrics
    # --------------------------------------------------------

    data = get_jenkins_data()


    total_builds = 0
    successful_builds = 0
    failed_builds = 0
    unstable_builds = 0

    last_build_number = 0
    last_build_duration = 0


    if data:

        builds = data.get(
            "builds",
            []
        )

        total_builds = len(builds)


        for build in builds:

            result = build.get(
                "result"
            )

            if result == "SUCCESS":

                successful_builds += 1

            elif result == "FAILURE":

                failed_builds += 1

            elif result == "UNSTABLE":

                unstable_builds += 1


        last_build = data.get(
            "lastBuild"
        )

        if last_build:

            last_build_number = (
                last_build.get(
                    "number",
                    0
                )
            )

            last_build_duration = (
                last_build.get(
                    "duration",
                    0
                ) / 1000
            )


    # --------------------------------------------------------
    # Total builds
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_builds_total "
        "Number of Jenkins pipeline builds"
    )

    lines.append(
        "# TYPE jenkins_pipeline_builds_total gauge"
    )

    lines.append(
        f'jenkins_pipeline_builds_total'
        f'{{job="{JENKINS_JOB}"}} '
        f"{total_builds}"
    )


    # --------------------------------------------------------
    # Successful builds
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_builds_successful "
        "Number of successful Jenkins builds"
    )

    lines.append(
        "# TYPE jenkins_pipeline_builds_successful gauge"
    )

    lines.append(
        f'jenkins_pipeline_builds_successful'
        f'{{job="{JENKINS_JOB}"}} '
        f"{successful_builds}"
    )


    # --------------------------------------------------------
    # Failed builds
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_builds_failed "
        "Number of failed Jenkins builds"
    )

    lines.append(
        "# TYPE jenkins_pipeline_builds_failed gauge"
    )

    lines.append(
        f'jenkins_pipeline_builds_failed'
        f'{{job="{JENKINS_JOB}"}} '
        f"{failed_builds}"
    )


    # --------------------------------------------------------
    # Unstable builds
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_builds_unstable "
        "Number of unstable Jenkins builds"
    )

    lines.append(
        "# TYPE jenkins_pipeline_builds_unstable gauge"
    )

    lines.append(
        f'jenkins_pipeline_builds_unstable'
        f'{{job="{JENKINS_JOB}"}} '
        f"{unstable_builds}"
    )


    # --------------------------------------------------------
    # Last build number
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_last_build_number "
        "Number of the latest Jenkins build"
    )

    lines.append(
        "# TYPE jenkins_pipeline_last_build_number gauge"
    )

    lines.append(
        f'jenkins_pipeline_last_build_number'
        f'{{job="{JENKINS_JOB}"}} '
        f"{last_build_number}"
    )


    # --------------------------------------------------------
    # Last build duration
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_pipeline_last_build_duration_seconds "
        "Duration of the latest Jenkins build"
    )

    lines.append(
        "# TYPE jenkins_pipeline_last_build_duration_seconds gauge"
    )

    lines.append(
        f'jenkins_pipeline_last_build_duration_seconds'
        f'{{job="{JENKINS_JOB}"}} '
        f"{last_build_duration}"
    )


    # --------------------------------------------------------
    # Exporter scrape timestamp
    # --------------------------------------------------------

    lines.append(
        "# HELP jenkins_exporter_last_scrape_timestamp_seconds "
        "Unix timestamp of the latest exporter scrape"
    )

    lines.append(
        "# TYPE jenkins_exporter_last_scrape_timestamp_seconds gauge"
    )

    lines.append(
        f"jenkins_exporter_last_scrape_timestamp_seconds "
        f"{timestamp}"
    )


    return "\n".join(lines) + "\n"


# ============================================================
# /metrics
# ============================================================

@app.route("/metrics")
def metrics():

    output = generate_metrics()

    return Response(
        output,
        mimetype="text/plain"
    )


# ============================================================
# HEALTH CHECK
# ============================================================

@app.route("/")
def home():

    return (
        "Jenkins Prometheus Exporter is running\n"
        f"Jenkins: {JENKINS_URL}\n"
        f"Job: {JENKINS_JOB}\n"
        "Metrics: /metrics\n"
    )


# ============================================================
# START EXPORTER
# ============================================================

if __name__ == "__main__":

    print("")
    print("==========================================")
    print(" JENKINS PROMETHEUS EXPORTER")
    print("==========================================")
    print(f"Jenkins URL : {JENKINS_URL}")
    print(f"Jenkins Job : {JENKINS_JOB}")
    print(f"Exporter    : http://0.0.0.0:{EXPORTER_PORT}")
    print("Metrics     : /metrics")
    print("==========================================")
    print("")

    app.run(
        host="0.0.0.0",
        port=EXPORTER_PORT
    )