pipeline {

    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
        timeout(time: 90, unit: 'MINUTES')
    }

    environment {

        // ============================================================
        // REPOSITORIES
        // ============================================================

        DVWA_REPO =
            'https://github.com/IbekweVictor/DVWA.git'

        DAST_REPO =
            'https://github.com/IbekweVictor/Authenticated-Dast-Scan.git'


        // ============================================================
        // CREDENTIALS
        // ============================================================

        SNYK_TOKEN =
            credentials('snyk_token')


        // ============================================================
        // PROJECT PATHS
        // ============================================================

        REPORT_DIR =
            "${env.WORKSPACE}\\reports"

        OPA_DIR =
            "${env.WORKSPACE}\\opa"

        WAF_DIR =
            "${env.WORKSPACE}\\waf"

        NOTIFICATION_DIR =
            "${env.WORKSPACE}\\notification"

        MONITORING_DIR =
            "${env.WORKSPACE}\\monitoring"


        // ============================================================
        // MONITORING
        // Reserved for later Prometheus/Grafana implementation.
        // ============================================================

        PROMETHEUS_URL =
            'http://localhost:9090'

        GRAFANA_URL =
            'http://localhost:3000'
    }


    stages {

        // ============================================================
        // 1. SOURCE + WORKSPACE
        // ============================================================

        stage('Checkout Repositories') {

            steps {

                script {

                    load('stages/checkout.groovy')
                }
            }
        }


        // ============================================================
        // 2. DOCKER VERIFICATION
        // ============================================================

        stage('Verify Docker') {

            steps {

                script {

                    load('stages/docker-verification.groovy')
                }
            }
        }


        // ============================================================
        // 3. PREPARE REPORTS
        // ============================================================

        stage('Prepare Reports') {

            steps {

                script {

                    load('stages/report-preparation.groovy')
                }
            }
        }


        // ============================================================
        // 4. SEMGREP SAST
        // ============================================================

        stage('Semgrep SAST') {

            steps {

                script {

                    load('stages/semgrep-sast.groovy')
                }
            }
        }


        // ============================================================
        // 5. ANALYZE SEMGREP
        // ============================================================

        stage('Analyze Semgrep') {

            steps {

                script {

                    load('stages/semgrep-analysis.groovy')
                }
            }
        }


        // ============================================================
        // 6-8. PARALLEL SECURITY SCANS
        // ============================================================

        stage('Security Scans') {

            parallel {

                stage('Gitleaks Secret Scan') {

                    steps {

                        script {

                            load('stages/gitleaks-scan.groovy')
                        }
                    }
                }


                stage('Snyk Dependency Scan') {

                    steps {

                        script {

                            load('stages/snyk-scan.groovy')
                        }
                    }
                }


                stage('Trivy Container Scan') {

                    steps {

                        script {

                            load('stages/trivy-scan.groovy')
                        }
                    }
                }
            }
        }


        // ============================================================
        // 9. ANALYZE STATIC SCANS
        // ============================================================

        stage('Analyze Static Scans') {

            steps {

                script {

                    load('stages/static-analysis.groovy')
                }
            }
        }


        // ============================================================
        // 10. CLEANUP OLD DAST
        // ============================================================

        stage('Cleanup Old DAST') {

            steps {

                script {

                    load('stages/dast-cleanup.groovy')
                }
            }
        }
    }
}