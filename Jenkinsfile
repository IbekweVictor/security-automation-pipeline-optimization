pipeline {

    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 20, unit: 'MINUTES')
    }

    environment {

        DVWA_REPO =
            'https://github.com/IbekweVictor/DVWA.git'

        DAST_REPO =
            'https://github.com/IbekweVictor/Authenticated-Dast-Scan.git'
    }

    stages {

        stage('Checkout Repositories') {

            steps {

                script {

                    echo 'Loading checkout stage...'

                    load('stages/checkout.groovy')

                    echo 'Checkout stage completed.'
                }
            }
        }
    }

    post {

        success {

            echo '''
=========================================
 CHECKOUT TEST PASSED
=========================================

✓ DVWA repository checked out
✓ Authenticated DAST repository checked out

=========================================
'''
        }

        failure {

            echo '''
=========================================
 CHECKOUT TEST FAILED
=========================================

Review the Jenkins console output.

=========================================
'''
        }
    }
}