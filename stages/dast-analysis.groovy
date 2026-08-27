def run() {

    int high     = 0
    int medium   = 0
    int low      = 0
    int info     = 0


    if (fileExists('reports/zap-report.json')) {

        def report =
            readJSON(
                file: 'reports/zap-report.json'
            )


        report.each {

            switch (it.risk?.toLowerCase()) {

                case 'high':
                    high++
                    break

                case 'medium':
                    medium++
                    break

                case 'low':
                    low++
                    break

                case 'informational':
                    info++
                    break
            }
        }
    }


    env.DAST_HIGH =
        "${high}"

    env.DAST_MEDIUM =
        "${medium}"

    env.DAST_LOW =
        "${low}"

    env.DAST_INFO =
        "${info}"


    echo '======================================'
    echo 'OWASP ZAP SUMMARY'
    echo '======================================'

    echo "High   : ${high}"
    echo "Medium : ${medium}"
    echo "Low    : ${low}"
    echo "Info   : ${info}"

    echo '======================================'
}


return this