pipeline {

    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    environment {

        // ============================================================
        // REPOSITORIES
        // ============================================================

        DVWA_REPO =
            'https://github.com/IbekweVictor/DVWA.git'

        DAST_REPO =
            'https://github.com/IbekweVictor/Authenticated-Dast-Scan.git'
    }

    stages {

        // ============================================================
        // CHECKOUT
        // ============================================================

        stage('Checkout Repositories') {

            steps {

                script {

                    echo 'Loading checkout stage...'

                    load('stages/checkout.groovy')

                    echo 'Checkout stage completed.'
                }
            }
        }


        // ============================================================
        // DOCKER VERIFICATION
        // ============================================================

        stage('Verify Docker') {

            steps {

                script {

                    echo 'Loading Docker verification stage...'

                    load('stages/docker-verification.groovy')

                    echo 'Docker verification stage completed.'
                }
            }
        }
    }


    // ================================================================
    // POST ACTIONS
    // ================================================================

    post {

        success {

            echo '''
=========================================
 CHECKOUT + DOCKER TEST PASSED
=========================================

✓ DVWA repository checked out
✓ Authenticated DAST repository checked out
✓ Docker verified
✓ Docker Compose verified

The pipeline is ready for the next stage.

=========================================
'''
        }


        failure {

            echo '''
=========================================
 CHECKOUT + DOCKER TEST FAILED
=========================================

Review the Jenkins console output to identify
whether Checkout or Docker verification failed.

=========================================
'''
        }
    }
}