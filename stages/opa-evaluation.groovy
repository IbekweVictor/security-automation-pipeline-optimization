def run() {

    echo '======================================'
    echo 'OPA POLICY EVALUATION'
    echo '======================================'

    /*
     * ------------------------------------------------------------
     * VERIFY REQUIRED INPUTS
     * ------------------------------------------------------------
     */

    if (!fileExists('unified-findings.json')) {

        error(
            'OPA ERROR: unified-findings.json was not found.'
        )
    }

    echo 'Unified findings found.'

    if (!fileExists('opa/policy.rego')) {

        error(
            'OPA ERROR: opa/policy.rego was not found.'
        )
    }

    echo 'Local project OPA policy found.'


    /*
     * ------------------------------------------------------------
     * VERIFY OPA
     * ------------------------------------------------------------
     */

    def versionStatus = bat(
        script: 'opa version',
        returnStatus: true
    )

    if (versionStatus != 0) {

        error(
            'OPA ERROR: Open Policy Agent is not installed or not available in PATH.'
        )
    }


    /*
     * ------------------------------------------------------------
     * VALIDATE POLICY
     * ------------------------------------------------------------
     */

    echo 'Validating opa/policy.rego...'

    def checkStatus = bat(
        script:
            'opa check opa/policy.rego',
        returnStatus: true
    )

    if (checkStatus != 0) {

        error(
            'OPA ERROR: opa/policy.rego failed validation.'
        )
    }

    echo 'OPA policy validation: PASSED'


    /*
     * ------------------------------------------------------------
     * DISPLAY UNIFIED FINDINGS
     * ------------------------------------------------------------
     */

    echo 'Checking unified findings input...'

    bat '''
    echo.
    echo ===== UNIFIED FINDINGS =====
    type unified-findings.json
    echo.
    '''


    /*
     * ------------------------------------------------------------
     * EXECUTE OPA POLICY
     * ------------------------------------------------------------
     */

    echo 'Running OPA policy evaluation...'

    def evalStatus = bat(
        script: '''
        if exist "opa-evaluation.json" del /Q "opa-evaluation.json"

        opa eval ^
          --format=json ^
          --input "unified-findings.json" ^
          --data "opa/policy.rego" ^
          "data.cicd.security.result" ^
          > "opa-evaluation.json"
        ''',
        returnStatus: true
    )

    if (evalStatus != 0) {

        error(
            'OPA ERROR: Policy evaluation failed.'
        )
    }

    if (!fileExists('opa-evaluation.json')) {

        error(
            'OPA ERROR: opa-evaluation.json was not generated.'
        )
    }

    echo 'OPA evaluation completed successfully.'


    /*
     * ------------------------------------------------------------
     * EXTRACT DECISION
     * ------------------------------------------------------------
     */

    def decisionStatus = bat(
        script: '''
        if exist "opa-decision.txt" del /Q "opa-decision.txt"

        opa eval ^
          --format=raw ^
          --input "unified-findings.json" ^
          --data "opa/policy.rego" ^
          "data.cicd.security.result.decision" ^
          > "opa-decision.txt"
        ''',
        returnStatus: true
    )

    if (decisionStatus != 0) {

        error(
            'OPA ERROR: Unable to extract policy decision.'
        )
    }


    /*
     * ------------------------------------------------------------
     * EXTRACT MESSAGE
     * ------------------------------------------------------------
     */

    def messageStatus = bat(
        script: '''
        if exist "opa-message.txt" del /Q "opa-message.txt"

        opa eval ^
          --format=raw ^
          --input "unified-findings.json" ^
          --data "opa/policy.rego" ^
          "data.cicd.security.result.message" ^
          > "opa-message.txt"
        ''',
        returnStatus: true
    )

    if (messageStatus != 0) {

        error(
            'OPA ERROR: Unable to extract policy message.'
        )
    }


    /*
     * ------------------------------------------------------------
     * READ OPA RESULT
     * ------------------------------------------------------------
     */

    def opaDecision =
        readFile(
            'opa-decision.txt'
        )
        .trim()
        .toUpperCase()

    def opaMessage =
        readFile(
            'opa-message.txt'
        )
        .trim()


    /*
     * ------------------------------------------------------------
     * VALIDATE OPA OUTPUT
     * ------------------------------------------------------------
     */

    if (!opaDecision) {

        error(
            'OPA ERROR: OPA returned an empty decision.'
        )
    }

    if (!opaMessage) {

        error(
            'OPA ERROR: OPA returned an empty message.'
        )
    }


    def validDecisions = [
        'ALLOW',
        'WARNING',
        'BLOCK'
    ]

    if (!validDecisions.contains(opaDecision)) {

        error(
            "OPA ERROR: Invalid decision returned: ${opaDecision}"
        )
    }


    /*
     * ------------------------------------------------------------
     * CREATE PIPELINE-FRIENDLY RESULT
     * ------------------------------------------------------------
     */

    def escapedMessage =
        opaMessage
            .replace('\\', '\\\\')
            .replace('"', '\\"')
            .replace('\r', '')
            .replace('\n', '\\n')

    def cleanOpaResult = """{
    "decision": "${opaDecision}",
    "message": "${escapedMessage}"
}"""

    writeFile(
        file: 'opa-result.json',
        text: cleanOpaResult
    )


    /*
     * ------------------------------------------------------------
     * EXPORT PIPELINE ENVIRONMENT VARIABLES
     * ------------------------------------------------------------
     */

    env.OPA_DECISION =
        opaDecision

    env.OPA_MESSAGE =
        opaMessage


    /*
     * ------------------------------------------------------------
     * DISPLAY RESULT
     * ------------------------------------------------------------
 */

    echo ''
    echo '======================================'
    echo 'OPA POLICY RESULT'
    echo '======================================'

    echo "Decision : ${opaDecision}"
    echo "Message  : ${opaMessage}"

    echo '======================================'


    /*
     * ------------------------------------------------------------
     * PIPELINE STATUS
     *
     * BLOCK and WARNING are deliberately marked UNSTABLE
     * rather than FAILED so that the pipeline can continue to
     * archive evidence and execute notification stages.
     * ------------------------------------------------------------
     */

    if (opaDecision == 'BLOCK') {

        unstable(
            "Security policy BLOCK: ${opaMessage}"
        )

    } else if (opaDecision == 'WARNING') {

        unstable(
            "Security policy WARNING: ${opaMessage}"
        )

    } else {

        echo 'OPA DECISION: ALLOW'
        echo 'Policy requirements satisfied.'
    }

    echo ''
    echo 'OPA POLICY EVALUATION COMPLETED.'
    echo ''
}


return this