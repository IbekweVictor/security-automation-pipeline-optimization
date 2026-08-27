def run() {

    echo '======================================'
    echo 'CHECKING OUT SOURCE REPOSITORIES'
    echo '======================================'

    echo "DVWA repository : ${env.DVWA_REPO}"
    echo "DAST repository : ${env.DAST_REPO}"

    // ============================================================
    // DVWA
    // ============================================================

    dir('dvwa') {

        checkout scmGit(
            branches: [[name: '*/master']],
            userRemoteConfigs: [[
                url: env.DVWA_REPO
            ]]
        )
    }

    echo '✓ DVWA repository checked out.'

    // ============================================================
    // AUTHENTICATED DAST
    // ============================================================

    dir('authenticated-dast') {

        checkout scmGit(
            branches: [[name: '*/main']],
            userRemoteConfigs: [[
                url: env.DAST_REPO
            ]]
        )
    }

    echo '✓ Authenticated DAST repository checked out.'

    echo '======================================'
    echo 'SOURCE REPOSITORIES READY'
    echo '======================================'
}

return this
