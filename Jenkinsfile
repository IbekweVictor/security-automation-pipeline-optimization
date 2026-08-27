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
        //
        // Reserved for future Prometheus/Grafana implementation.
        // No monitoring stage currently executes.
        // ============================================================

        PROMETHEUS_URL =
            'http://localhost:9090'

        GRAFANA_URL =
            'http://localhost:3000'
    }


    stages {

        // ============================================================
        // SOURCE + WORKSPACE
        // ============================================================

        stage('Checkout Repositories') {
            steps {
                script {
                    load('stages/checkout.groovy').run()
                }
            }
        }

        stage('Verify Docker') {
            steps {
                script {
                    load('stages/docker-verification.groovy').run()
                }
            }
        }

        stage('Prepare Reports') {
            steps {
                script {
                    load('stages/report-preparation.groovy').run()
                }
            }
        }


        // ============================================================
        // SAST
        // ============================================================

        stage('Semgrep SAST') {
            steps {
                script {
                    load('stages/semgrep-sast.groovy').run()
                }
            }
        }

        stage('Analyze Semgrep') {
            steps {
                script {
                    load('stages/semgrep-analysis.groovy').run()
                }
            }
        }


        // ============================================================
        // PARALLEL SECURITY SCANS
        // ============================================================

        stage('Security Scans') {

            parallel {

                stage('Gitleaks Secret Scan') {
                    steps {
                        script {
                            load('stages/gitleaks-scan.groovy').run()
                        }
                    }
                }

                stage('Snyk Dependency Scan') {
                    steps {
                        script {
                            load('stages/snyk-scan.groovy').run()
                        }
                    }
                }

                stage('Trivy Container Scan') {
                    steps {
                        script {
                            load('stages/trivy-scan.groovy').run()
                        }
                    }
                }
            }
        }

        stage('Analyze Static Scans') {
            steps {
                script {
                    load('stages/static-analysis.groovy').run()
                }
            }
        }


        // ============================================================
        // AUTHENTICATED DAST
        // ============================================================

        stage('Cleanup Old DAST') {
            steps {
                script {
                    load('stages/dast-cleanup.groovy').run()
                }
            }
        }

        stage('Start DAST Environment') {
            steps {
                script {
                    load('stages/dast-start.groovy').run()
                }
            }
        }

        stage('Wait For DAST Scan') {
            steps {
                script {
                    load('stages/dast-wait.groovy').run()
                }
            }
        }

        stage('Collect DAST Reports') {
            steps {
                script {
                    load('stages/dast-reports.groovy').run()
                }
            }
        }

        stage('Stop DAST Environment') {
            steps {
                script {
                    load('stages/dast-stop.groovy').run()
                }
            }
        }

        stage('Analyze DAST Results') {
            steps {
                script {
                    load('stages/dast-analysis.groovy').run()
                }
            }
        }


        // ============================================================
        // SUMMARY
        // ============================================================

        stage('Generate Summary Report') {
            steps {
                script {
                    load('stages/summary-report.groovy').run()
                }
            }
        }


        // ============================================================
        // DEFECTDOJO
        // ============================================================

        stage('Start DefectDojo') {
            steps {
                script {
                    load('stages/defectdojo-start.groovy').run()
                }
            }
        }

        stage('Upload Reports to DefectDojo') {
            steps {
                script {
                    load('stages/defectdojo-upload.groovy').run()
                }
            }
        }

        stage('Collect Unified Findings') {
            steps {
                script {
                    load('stages/defectdojo-findings.groovy').run()
                }
            }
        }


        // ============================================================
        // POLICY
        // ============================================================

        stage('OPA Policy Evaluation') {

            steps {
                script {
                    load('stages/opa-evaluation.groovy').run()
                }
            }

            post {
                always {
                    archiveArtifacts(
                        artifacts:
                            'opa-result.json,opa-evaluation.json',
                        allowEmptyArchive: true,
                        fingerprint: true
                    )
                }
            }
        }


        // ============================================================
        // WAF
        // ============================================================

        stage('Dynamic WAF Protection') {
            steps {
                script {
                    load('stages/waf-protection.groovy').run()
                }
            }
        }


        // ============================================================
        // PROMETHEUS + GRAFANA
        //
        // INTENTIONALLY NOT EXECUTED YET.
        //
        // monitoring/ remains reserved for the future implementation.
        // ============================================================


        // ============================================================
        // ARCHIVE
        // ============================================================

        stage('Archive Reports') {
            steps {
                script {
                    load('stages/archive-reports.groovy').run()
                }
            }
        }
    }


    // ================================================================
    // POST ACTIONS
    // ================================================================

    post {

        always {

            script {

                // ----------------------------------------------------
                // VERIFY INTERNAL NOTIFICATION COMPONENTS
                // ----------------------------------------------------

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


                // ----------------------------------------------------
                // SAFETY ARCHIVE
                // ----------------------------------------------------

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

                        archiveArtifacts(
                            artifacts: 'reports/**/*',
                            fingerprint: true,
                            allowEmptyArchive: true
                        )

                        echo '✓ Existing security evidence preserved.'

                    } else {

                        echo 'No security evidence available to preserve.'
                    }

                } catch (Exception archiveError) {

                    echo 'WARNING: Security evidence preservation failed.'
                    echo "Archive error: ${archiveError}"
                }


                // ----------------------------------------------------
                // EMAIL
                // ----------------------------------------------------

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


                // ----------------------------------------------------
                // SLACK
                // ----------------------------------------------------

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


                // ----------------------------------------------------
                // CLEANUP
                // ----------------------------------------------------

                echo ''
                echo 'Cleaning Docker environment...'

                cleanWs()
            }
        }


        // ============================================================
        // SUCCESS
        // ============================================================

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
✓ WAF Rules Generated
✓ Security Evidence Archived
✓ Security Email Notification Sent
✓ Slack Notification Sent

Prometheus/Grafana monitoring is reserved
for a future implementation.

=========================================
'''
        }


        // ============================================================
        // UNSTABLE
        // ============================================================

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

Prometheus/Grafana monitoring is not yet
part of the active pipeline.

The pipeline completed, but remediation
may be required before release.

=========================================
'''
        }


        // ============================================================
        // FAILURE
        // ============================================================

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