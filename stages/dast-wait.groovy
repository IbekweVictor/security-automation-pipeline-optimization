def run() {

    timeout(time: 30, unit: 'MINUTES') {

        boolean finished = false


        while (!finished) {

            def logs = bat(
                script:
                    'docker logs scanner 2>&1',
                returnStdout: true
            ).trim()


            echo logs


            if (logs.contains('[+] Finished')) {

                finished = true

                echo 'Authenticated DAST Completed.'

            } else {

                sleep(
                    time: 30,
                    unit: 'SECONDS'
                )
            }
        }
    }
}


return this