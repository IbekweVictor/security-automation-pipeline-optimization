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

        // *============================================================*
        // *REPOSITORIES*
        // *============================================================*

        DVWA_REPO =
            'https://github.com/IbekweVictor/DVWA.git'

        DAST_REPO =
            'https://github.com/IbekweVictor/Authenticated-Dast-Scan.git'


        // *============================================================*
        // *CREDENTIALS*
        // *============================================================*

        SNYK_TOKEN =
            credentials('snyk_token')

        DEFECTDOJO_API =
            credentials('defectdojo_api_key')


        // *============================================================*
        // *DEFECTDOJO*
        // *============================================================*

        DD_URL =
            'http://localhost:8080'

        DD_PRODUCT =
            '1'

        DD_ENGAGEMENT =
            '1'


        // *============================================================*
        // *JIRA*
        // *============================================================*

        JIRA_URL =
            'https://yourcompany.atlassian.net'

        JIRA_PROJECT =
            'SEC'

        JIRA_ISSUE_TYPE =
            'Bug'


        // *============================================================*
        // *PROJECT PATHS*
        // *============================================================*

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


        // *============================================================*
        // *MONITORING*
        // *============================================================*

        PROMETHEUS_URL =
            'http://localhost:9090'

        GRAFANA_URL =
            'http://localhost:3000'
    }


    stages {


        // *============================================================*
        // *1. CHECKOUT*
        // *============================================================*

        stage('Checkout Repositories') {

            steps {

                script {

                    load(
                        'stages/checkout.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *2. DOCKER VERIFICATION*
        // *============================================================*

        stage('Verify Docker') {

            steps {

                script {

                    load(
                        'stages/docker-verification.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *3. PREPARE REPORTS*
        // *============================================================*

        stage('Prepare Reports') {

            steps {

                script {

                    load(
                        'stages/report-preparation.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *4. SEMGREP SAST*
        // *============================================================*

        stage('Semgrep SAST') {

            steps {

                script {

                    load(
                        'stages/semgrep-sast.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *5. ANALYZE SEMGREP*
        // *============================================================*

        stage('Analyze Semgrep') {

            steps {

                script {

                    load(
                        'stages/semgrep-analysis.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *6-8. PARALLEL SECURITY SCANS*
        // *============================================================*

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


        // *============================================================*
        // *9. ANALYZE STATIC SCANS*
        // *============================================================*

        stage('Analyze Static Scans') {

            steps {

                script {

                    load(
                        'stages/static-analysis.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *10. CLEANUP OLD DAST*
        // *============================================================*

        stage('Cleanup Old DAST') {

            steps {

                script {

                    load(
                        'stages/dast-cleanup.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *11. START DAST ENVIRONMENT*
        // *============================================================*

        stage('Start DAST Environment') {

            steps {

                script {

                    load(
                        'stages/dast-start.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *12. WAIT FOR DAST SCAN*
        // *============================================================*

        stage('Wait For DAST Scan') {

            steps {

                script {

                    load(
                        'stages/dast-wait.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *13. COLLECT DAST REPORTS*
        // *============================================================*

        stage('Collect DAST Reports') {

            steps {

                script {

                    load(
                        'stages/dast-reports.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *14. STOP DAST ENVIRONMENT*
        // *============================================================*

        stage('Stop DAST Environment') {

            steps {

                script {

                    load(
                        'stages/dast-stop.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *15. ANALYZE DAST RESULTS*
        // *============================================================*

        stage('Analyze DAST Results') {

            steps {

                script {

                    load(
                        'stages/dast-analysis.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *16. GENERATE SUMMARY REPORT*
        // *============================================================*

        stage('Generate Summary Report') {

            steps {

                script {

                    load(
                        'stages/summary-report.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *17. START DEFECTDOJO*
        // *============================================================*

        stage('Start DefectDojo') {

            steps {

                script {

                    load(
                        'stages/defectdojo-start.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *18. UPLOAD REPORTS TO DEFECTDOJO*
        // *============================================================*

        stage('Upload Reports to DefectDojo') {

            steps {

                script {

                    load(
                        'stages/defectdojo-upload.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *19. COLLECT UNIFIED FINDINGS*
        // *============================================================*

        stage('Collect Unified Findings') {

            steps {

                script {

                    load(
                        'stages/defectdojo-findings.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *20. OPA POLICY EVALUATION*
        // *============================================================*

        stage('OPA Policy Evaluation') {

            steps {

                script {

                    load(
                        'stages/opa-evaluation.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *21. DYNAMIC WAF PROTECTION*
        // *============================================================*

        stage('Dynamic WAF Protection') {

            steps {

                script {

                    load(
                        'stages/waf-protection.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *22. ARCHIVE*
        // *============================================================*

        stage('Archive Reports') {

            steps {

                script {

                    load(
                        'stages/archive-reports.groovy'
                    )

                }

            }

        }


        // *============================================================*
        // *23. PROMETHEUS SECURITY METRICS*
        // *============================================================*
        //
        // *Starts the independent monitoring stack from*
        // *C:\prometheus-exporter, generates the security*
        // *metrics file, and pushes the metrics to Pushgateway.*
        //
        // *Monitoring failure must NOT fail the security pipeline.*
        //
        // *============================================================*

        stage('Prometheus Security Metrics') {

            steps {

                script {

                    load(
                        'stages/prometheus-security-metrics.groovy'
                    )

                }

            }

        }

    }


    // *=================================================================*
    // *POST ACTIONS*
    // *=================================================================*

    post {

        always {

            script {


                // *-----------------------------------------------------*
                // *VERIFY NOTIFICATION COMPONENTS*
                // *-----------------------------------------------------*

                try {

                    echo ''

                    echo 'Preparing notification components...'

                    bat '''

                    if not exist notification mkdir notification

                    if not exist "notification\\security-email.groovy" (
                        echo ERROR: security-email.groovy not found.
                        exit /b 1
                    )

                    if not exist "notification\\security-email.html" (
                        echo ERROR: security-email.html not found.
                        exit /b 1
                    )

                    if not exist "notification\\slack-notification.groovy" (
                        echo ERROR: slack-notification.groovy not found.
                        exit /b 1
                    )

                    '''

                    echo 'Notification components verified.'

                } catch (Exception setupError) {

                    echo 'WARNING: Notification preparation failed.'

                    echo "Setup error: ${setupError}"

                }


                // *-----------------------------------------------------*
                // *SAFETY ARCHIVE*
                // *-----------------------------------------------------*

                try {

                    echo ''

                    echo 'Preserving security evidence from completed stages...'

                    def remainingReports =
                        findFiles(
                            glob: 'reports/**/*'
                        ).findAll { file ->
                            !file.directory
                        }


                    if (remainingReports.size() > 0) {

                        echo "Security evidence files found: ${remainingReports.size()}"

                        remainingReports.each { file ->

                            echo "  ✓ ${file.path}"

                        }


                        archiveArtifacts(

                            artifacts:
                                'reports/**/*',

                            fingerprint:
                                true,

                            allowEmptyArchive:
                                true

                        )


                        echo ''

                        echo '✓ Existing security evidence preserved.'

                    } else {

                        echo 'No security evidence available to preserve.'

                    }

                } catch (Exception archiveError) {

                    echo 'WARNING: Security evidence preservation failed.'

                    echo "Archive error: ${archiveError}"

                }


                // *-----------------------------------------------------*
                // *EMAIL*
                // *-----------------------------------------------------*

                try {

                    def emailNotification =
                        load 'notification/security-email.groovy'


                    echo ''

                    echo 'Sending security assessment email...'


                    emailNotification.send()


                    echo 'Security assessment email completed.'

                } catch (Exception emailError) {

                    echo ''

                    echo 'WARNING: Email notification failed.'

                    echo "Email error: ${emailError}"

                }


                // *-----------------------------------------------------*
                // *SLACK*
                // *-----------------------------------------------------*

                try {

                    def slackNotification =
                        load 'notification/slack-notification.groovy'


                    echo ''

                    echo 'Sending Slack security notification...'


                    slackNotification.send()


                    echo 'Slack notification completed.'

                } catch (Exception slackError) {

                    echo ''

                    echo 'WARNING: Slack notification failed.'

                    echo "Slack error: ${slackError}"

                }


                // *-----------------------------------------------------*
                // *CLEANUP*
                // *-----------------------------------------------------*

                echo ''

                echo 'Cleaning Docker environment...'

                cleanWs()

            }

        }


        // *=============================================================*
        // *SUCCESS*
        // *=============================================================*

        success {

            echo '''

=========================================
 SECURITY AUTOMATION PIPELINE COMPLETED
=========================================

✓ Semgrep SAST Completed
✓ Gitleaks Secret Scan Completed
✓ Snyk Dependency Scan Completed
✓ Trivy Container Scan Completed
✓ Authenticated ZAP DAST Completed
✓ Summary Report Generated
✓ DefectDojo Reports Reimported
✓ Unified Findings Collected
✓ OPA Policy Evaluated
✓ WAF Protection Completed
✓ Security Evidence Archived
✓ Prometheus Security Metrics Published
✓ Security Email Notification Sent
✓ Slack Notification Sent

Prometheus/Grafana monitoring is active
through the independent monitoring stack.

=========================================

'''

        }


        // *=============================================================*
        // *UNSTABLE*
        // *=============================================================*

        unstable {

            echo '''

=========================================
 PIPELINE UNSTABLE
=========================================

Security scans completed, but one or more
security thresholds were exceeded.

Review:

- Individual archived scanner reports
- Security assessment email
- Slack security summary
- DefectDojo findings
- OPA/WAF results
- Prometheus/Grafana security metrics

The pipeline completed, but remediation
may be required before release.

=========================================

'''

        }


        // *=============================================================*
        // *FAILURE*
        // *=============================================================*

        failure {

            echo '''

=========================================
 PIPELINE FAILED
=========================================

The pipeline stopped during execution.

Security evidence generated before the
failure was preserved individually where
available.

The security assessment email contains
the downloadable Jenkins console output.

Review the Jenkins build and security
evidence before rerunning.

=========================================

'''

        }

    }

}