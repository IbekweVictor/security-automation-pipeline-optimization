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

        DVWA_REPO = 'https://github.com/IbekweVictor/DVWA.git'
        DAST_REPO = 'https://github.com/IbekweVictor/Authenticated-Dast-Scan.git'

        SNYK_TOKEN = credentials('snyk_token')
        DEFECTDOJO_API = credentials('defectdojo_api_key')

        DD_URL = 'http://localhost:8080'
        DD_PRODUCT = '1'
        DD_ENGAGEMENT = '1'

        REPORT_DIR = "${env.WORKSPACE}\\reports"
        OPA_DIR = "${env.WORKSPACE}\\opa"
        WAF_DIR = "${env.WORKSPACE}\\waf"
        NOTIFICATION_DIR = "${env.WORKSPACE}\\notification"
        MONITORING_DIR = "${env.WORKSPACE}\\monitoring"

        PROMETHEUS_URL = 'http://localhost:9090'
        GRAFANA_URL = 'http://localhost:3000'
    }

    stages {

        stage('Checkout Repositories') {
            steps {
                script {
                    load('stages/checkout.groovy')
                }
            }
        }

        stage('Verify Docker') {
            steps {
                script {
                    load('stages/docker-verification.groovy')
                }
            }
        }

        stage('Prepare Reports') {
            steps {
                script {
                    load('stages/report-preparation.groovy')
                }
            }
        }

        stage('Semgrep SAST') {
            steps {
                script {
                    load('stages/semgrep-sast.groovy')
                }
            }
        }

        stage('Analyze Semgrep') {
            steps {
                script {
                    load('stages/semgrep-analysis.groovy')
                }
            }
        }

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

        stage('Analyze Static Scans') {
            steps {
                script {
                    load('stages/static-analysis.groovy')
                }
            }
        }

        stage('Cleanup Old DAST') {
            steps {
                script {
                    load('stages/dast-cleanup.groovy')
                }
            }
        }

        stage('Start DAST Environment') {
            steps {
                script {
                    load('stages/dast-start.groovy')
                }
            }
        }

        stage('Wait For DAST Scan') {
            steps {
                script {
                    load('stages/dast-wait.groovy')
                }
            }
        }

        stage('Collect DAST Reports') {
            steps {
                script {
                    load('stages/dast-reports.groovy')
                }
            }
        }

        stage('Stop DAST Environment') {
            steps {
                script {
                    load('stages/dast-stop.groovy')
                }
            }
        }

        stage('Analyze DAST Results') {
            steps {
                script {
                    load('stages/dast-analysis.groovy')
                }
            }
        }

        stage('Generate Summary Report') {
            steps {
                script {
                    load('stages/summary-report.groovy')
                }
            }
        }

        stage('Start DefectDojo') {
            steps {
                script {
                    load('stages/defectdojo-start.groovy')
                }
            }
        }

        stage('Upload Reports to DefectDojo') {
            steps {
                script {
                    load('stages/defectdojo-upload.groovy')
                }
            }
        }

        stage('Collect Unified Findings') {
            steps {
                script {
                    load('stages/defectdojo-findings.groovy')
                }
            }
        }

        stage('OPA Policy Evaluation') {
            steps {
                script {
                    load('stages/opa-evaluation.groovy')
                }
            }
        }

        stage('Dynamic WAF Protection') {
            steps {
                script {
                    load('stages/waf-protection.groovy')
                }
            }
        }

        stage('Archive Reports') {
            steps {
                script {
                    load('stages/archive-reports.groovy')
                }
            }
        }

        stage('Monitoring Metrics') {
            steps {
                script {
                    load('stages/prometheus-security-metrics.groovy')
                }
            }
        }
    }

    post {

        always {

            script {

                /*
                 * ==========================================================
                 * VERIFY NOTIFICATION COMPONENTS
                 * ==========================================================
                 */

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


                /*
                 * ==========================================================
                 * PRESERVE SECURITY EVIDENCE
                 * ==========================================================
                 */

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
                            artifacts: 'reports/**/*',
                            fingerprint: true,
                            allowEmptyArchive: true
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


                /*
                 * ==========================================================
                 * EMAIL NOTIFICATION
                 * ==========================================================
                 */

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


                /*
                 * ==========================================================
                 * SLACK NOTIFICATION
                 * ==========================================================
                 */

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


                /*
                 * ==========================================================
                 * WORKSPACE CLEANUP
                 *
                 * Preserve:
                 *   - dvwa/
                 *   - authenticated-dast/
                 *
                 * Everything else is cleaned.
                 *
                 * This allows the next build to reuse the existing
                 * Git repositories instead of cloning them from scratch.
                 * ==========================================================
                 */

                echo ''
                echo 'Cleaning Jenkins workspace...'
                echo 'Preserving DVWA repository...'
                echo 'Preserving Authenticated DAST repository...'

                cleanWs(
                    deleteDirs: true,
                    disableDeferredWipeout: true,
                    notFailBuild: true,
                    patterns: [
                        [
                            pattern: 'dvwa/**',
                            type: 'EXCLUDE'
                        ],
                        [
                            pattern: 'authenticated-dast/**',
                            type: 'EXCLUDE'
                        ]
                    ]
                )

                echo '✓ Workspace cleanup completed.'
                echo '✓ DVWA repository preserved.'
                echo '✓ Authenticated DAST repository preserved.'
            }
        }


        /*
         * ==============================================================
         * BUILD RESULT MESSAGES
         * ==============================================================
         */

        success {

            echo ''
            echo '======================================'
            echo 'SECURITY PIPELINE COMPLETED SUCCESSFULLY'
            echo '======================================'
        }

        unstable {

            echo ''
            echo '======================================'
            echo 'SECURITY PIPELINE COMPLETED WITH WARNINGS'
            echo '======================================'
        }

        failure {

            echo ''
            echo '======================================'
            echo 'SECURITY PIPELINE FAILED'
            echo '======================================'
        }
    }
}