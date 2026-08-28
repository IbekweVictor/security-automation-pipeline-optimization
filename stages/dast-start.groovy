echo '======================================'
echo 'STARTING AUTHENTICATED DAST ENVIRONMENT'
echo '======================================'

dir('authenticated-dast') {

    bat 'docker compose up -d'
}

echo '✓ Authenticated DAST environment started.'

echo '======================================'
echo 'DAST ENVIRONMENT READY'
echo '======================================'