def run() {

    dir('authenticated-dast') {

        bat 'docker compose up -d'
    }
}


return this