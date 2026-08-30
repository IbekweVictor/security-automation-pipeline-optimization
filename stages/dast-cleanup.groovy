dir('authenticated-dast') {

    bat '''
    docker compose down -v
    docker rm -f web-dvwa 2>NUL
    docker rm -f scanner 2>NUL
    docker rm -f zapy 2>NUL
    exit /b 0
    '''
}