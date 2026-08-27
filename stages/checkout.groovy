def run() {

    stage('Checkout Source Repositories') {

        dir('dvwa') {

            checkout scmGit(
                branches: [[name: '*/master']],
                userRemoteConfigs: [[
                    url: env.DVWA_REPO
                ]]
            )
        }


        dir('authenticated-dast') {

            checkout scmGit(
                branches: [[name: '*/main']],
                userRemoteConfigs: [[
                    url: env.DAST_REPO
                ]]
            )
        }
    }
}


return this