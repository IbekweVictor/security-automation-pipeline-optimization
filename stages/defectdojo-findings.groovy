def run() {

    echo '======================================'
    echo 'COLLECTING FINDINGS FROM DEFECTDOJO'
    echo '======================================'


    def tests = [
        5,
        6,
        7,
        8,
        9
    ]


    int critical = 0
    int high     = 0
    int medium   = 0
    int low      = 0
    int info     = 0


    tests.each { testId ->

        echo "Reading findings from Test ID: ${testId}"


        bat """
        @curl -s ^
          -H "Authorization: Token %DEFECTDOJO_API%" ^
          "%DD_URL%/api/v2/findings/?test=${testId}&active=true&limit=5000" ^
          -o findings-${testId}.json
        """


        if (!fileExists(
            "findings-${testId}.json"
        )) {

            echo "Unable to retrieve findings for Test ${testId}"

            return
        }


        def findings =
            readJSON(
                file: "findings-${testId}.json"
            )


        if (findings.results != null) {

            findings.results.each { finding ->

                switch (finding.severity) {

                    case 'Critical':
                        critical++
                        break

                    case 'High':
                        high++
                        break

                    case 'Medium':
                        medium++
                        break

                    case 'Low':
                        low++
                        break

                    default:
                        info++
                }
            }
        }
    }


    env.UNIFIED_CRITICAL =
        "${critical}"

    env.UNIFIED_HIGH =
        "${high}"

    env.UNIFIED_MEDIUM =
        "${medium}"

    env.UNIFIED_LOW =
        "${low}"

    env.UNIFIED_INFO =
        "${info}"


    echo ''
    echo '======================================'
    echo 'UNIFIED DEFECTDOJO RESULTS'
    echo '======================================'

    echo "Critical : ${critical}"
    echo "High     : ${high}"
    echo "Medium   : ${medium}"
    echo "Low      : ${low}"
    echo "Info     : ${info}"

    echo '======================================'


    writeJSON(
        file: 'unified-findings.json',
        pretty: 4,
        json: [
            critical: critical,
            high:     high,
            medium:   medium,
            low:      low,
            info:     info
        ]
    )


    archiveArtifacts(
        artifacts: 'unified-findings.json',
        fingerprint: true
    )
}


return this