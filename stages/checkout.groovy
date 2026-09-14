echo '======================================'
echo 'CHECKING OUT SOURCE REPOSITORIES'
echo '======================================'

echo "DVWA repository : ${env.DVWA_REPO}"
echo "DAST repository : ${env.DAST_REPO}"

dir('dvwa') {

    checkout scmGit(
        branches: [[name: '*/master']],
        userRemoteConfigs: [[
            url: env.DVWA_REPO
        ]],
        extensions: [
            cloneOption(
                shallow: true,
                depth: 1,
                noTags: true,
                timeout: 20
            )
        ]
    )
}

echo '✓ DVWA repository checked out.'

dir('authenticated-dast') {

    checkout scmGit(
        branches: [[name: '*/main']],
        userRemoteConfigs: [[
            url: env.DAST_REPO
        ]],
        extensions: [
            cloneOption(
                shallow: true,
                depth: 1,
                noTags: true,
                timeout: 20
            )
        ]
    )
}

echo '✓ Authenticated DAST repository checked out.'

echo '======================================'
echo 'SOURCE REPOSITORIES READY'
echo '======================================'