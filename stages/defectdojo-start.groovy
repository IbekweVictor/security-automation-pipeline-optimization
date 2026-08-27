def run() {

    echo '======================================'
    echo 'STARTING DEFECTDOJO'
    echo '======================================'


    /*
     * Existing DefectDojo installation remains external
     * because the supplied Jenkinsfile references:
     *
     * C:/django-DefectDojo
     *
     * This can be migrated into the project later.
     */

    dir('C:/django-DefectDojo') {

        bat 'docker compose up -d'
    }


    echo 'Waiting for DefectDojo backend...'


    timeout(time: 10, unit: 'MINUTES') {

        waitUntil {

            sleep(
                time: 15,
                unit: 'SECONDS'
            )


            bat '''
            @curl -s -o NUL -w %%{http_code} http://localhost:8080 > dojo_status.txt
            '''


            def status =
                readFile(
                    'dojo_status.txt'
                ).trim()


            echo "DefectDojo Status: ${status}"


            if (status == '502') {

                return false
            }


            echo '======================================'
            echo 'DefectDojo backend is ready.'
            echo 'Proceeding with report upload...'
            echo '======================================'


            return true
        }
    }
}


return this