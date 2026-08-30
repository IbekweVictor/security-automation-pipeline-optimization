pipeline {

    agent any

    options {

        timestamps()

        disableConcurrentBuilds()

        buildDiscarder(
            logRotator(
                numToKeepStr: '20'
            )
        )

        timeout(
            time: 90,
            unit: 'MINUTES'
        )
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

        DEFECTDOJO_API =
            credentials('defectdojo_api_key')


        // ============================================================
        // DEFECTDOJO
        // ============================================================

        DD_URL =
            'http://localhost:8080'

        DD_PRODUCT =
            '1'

        DD_ENGAGEMENT =
            '1'


        // ============================================================
        // JIRA
        // ============================================================

        JIRA_URL =
            'https://yourcompany.atlassian.net'

        JIRA_PROJECT =
            'SEC'

        JIRA_ISSUE_TYPE =
            'Bug'


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
        // Reserved for future Prometheus/Grafana implementation.
        // ============================================================

        PROMETHEUS_URL =
            'http://localhost:9090'

        GRAFANA_URL =
            'http://localhost:3000'
    }


    stages {


        // ============================================================
        // 1. CHECKOUT
        // ============================================================

        stage('Checkout Repositories') {

            steps {

                script {

                    load(
                        'stages/checkout.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 2. DOCKER VERIFICATION
        // ============================================================

        stage('Verify Docker') {

            steps {

                script {

                    load(
                        'stages/docker-verification.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 3. PREPARE REPORTS
        // ============================================================

        stage('Prepare Reports') {

            steps {

                script {

                    load(
                        'stages/report-preparation.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 4. SEMGREP SAST
        // ============================================================

        stage('Semgrep SAST') {

            steps {

                script {

                    load(
                        'stages/semgrep-sast.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 5. ANALYZE SEMGREP
        // ============================================================

        stage('Analyze Semgrep') {

            steps {

                script {

                    load(
                        'stages/semgrep-analysis.groovy'
                    )
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

                            load(
                                'stages/gitleaks-scan.groovy'
                            )
                        }
                    }
                }


                stage('Snyk Dependency Scan') {

                    steps {

                        script {

                            load(
                                'stages/snyk-scan.groovy'
                            )
                        }
                    }
                }


                stage('Trivy Container Scan') {

                    steps {

                        script {

                            load(
                                'stages/trivy-scan.groovy'
                            )
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

                    load(
                        'stages/static-analysis.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 10. CLEANUP OLD DAST
        // ============================================================

        stage('Cleanup Old DAST') {

            steps {

                script {

                    load(
                        'stages/dast-cleanup.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 11. START DAST ENVIRONMENT
        // ============================================================

        stage('Start DAST Environment') {

            steps {

                script {

                    load(
                        'stages/dast-start.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 12. WAIT FOR DAST SCAN
        // ============================================================

        stage('Wait For DAST Scan') {

            steps {

                script {

                    load(
                        'stages/dast-wait.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 13. COLLECT DAST REPORTS
        // ============================================================

        stage('Collect DAST Reports') {

            steps {

                script {

                    load(
                        'stages/dast-reports.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 14. STOP DAST ENVIRONMENT
        // ============================================================

        stage('Stop DAST Environment') {

            steps {

                script {

                    load(
                        'stages/dast-stop.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 15. ANALYZE DAST RESULTS
        // ============================================================

        stage('Analyze DAST Results') {

            steps {

                script {

                    load(
                        'stages/dast-analysis.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 16. GENERATE SUMMARY REPORT
        // ============================================================

        stage('Generate Summary Report') {

            steps {

                script {

                    load(
                        'stages/summary-report.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 17. START DEFECTDOJO
        // ============================================================

        stage('Start DefectDojo') {

            steps {

                script {

                    load(
                        'stages/defectdojo-start.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 18. UPLOAD REPORTS TO DEFECTDOJO
        // ============================================================

        stage('Upload Reports to DefectDojo') {

            steps {

                script {

                    load(
                        'stages/defectdojo-upload.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 19. COLLECT UNIFIED FINDINGS
        // ============================================================

        stage('Collect Unified Findings') {

            steps {

                script {

                    load(
                        'stages/defectdojo-findings.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 20. OPA POLICY EVALUATION
        // ============================================================

        stage('OPA Policy Evaluation') {

            steps {

                script {

                    load(
                        'stages/opa-evaluation.groovy'
                    )
                }
            }


            post {

                always {

                    archiveArtifacts(

                        artifacts:
                            'opa-result.json,opa-evaluation.json',

                        allowEmptyArchive:
                            true,

                        fingerprint:
                            true
                    )
                }
            }
        }


        // ============================================================
        // 21. DYNAMIC WAF PROTECTION
        // ============================================================

        stage('Dynamic WAF Protection') {

            steps {

                script {

                    load(
                        'stages/waf-protection.groovy'
                    )
                }
            }
        }


        // ============================================================
        // 22. ARCHIVE REPORTS
        // ============================================================

        stage('Archive Reports') {

            steps {

                script {

                    load(
                        'stages/archive-reports.groovy'
                    )
                }
            }
        }
    }
}