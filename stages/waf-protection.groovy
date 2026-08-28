echo '======================================'
echo 'DYNAMIC WAF PROTECTION'
echo '======================================'

/*
 * ---------------------------------------------------------------
 * WAF DIRECTORY VALIDATION
 * ---------------------------------------------------------------
 */

if (!fileExists('waf')) {
    error(
        'WAF ERROR: waf directory was not found.'
    )
}

echo 'WAF directory found.'

/*
 * ---------------------------------------------------------------
 * REQUIRED WAF FILES
 * ---------------------------------------------------------------
 */

if (!fileExists(
    'waf/docker-compose.yml'
)) {
    error(
        'WAF ERROR: waf/docker-compose.yml was not found.'
    )
}

if (!fileExists(
    'waf/dynamic_rules.conf'
)) {
    error(
        'WAF ERROR: waf/dynamic_rules.conf was not found.'
    )
}

echo 'WAF Docker Compose configuration found.'
echo 'Dynamic WAF rules configuration found.'

/*
 * ---------------------------------------------------------------
 * DOCKER VERIFICATION
 * ---------------------------------------------------------------
 */

echo ''
echo 'Checking Docker availability...'

def dockerStatus = bat(
    script: 'docker --version',
    returnStatus: true
)

if (dockerStatus != 0) {
    error(
        'WAF ERROR: Docker is not available.'
    )
}

echo 'Docker is available.'

/*
 * ---------------------------------------------------------------
 * VALIDATE WAF COMPOSE CONFIGURATION
 * ---------------------------------------------------------------
 */

echo ''
echo 'Validating WAF Docker Compose configuration...'

def composeConfigStatus = bat(
    script: '''
    docker compose -f "waf/docker-compose.yml" config
    ''',
    returnStatus: true
)

if (composeConfigStatus != 0) {
    error(
        'WAF ERROR: docker-compose.yml failed validation.'
    )
}

echo 'WAF Docker Compose configuration: PASSED'

/*
 * ---------------------------------------------------------------
 * DISPLAY ACTIVE DYNAMIC RULES
 * ---------------------------------------------------------------
 */

echo ''
echo '======================================'
echo 'DYNAMIC WAF RULES'
echo '======================================'

bat '''
type "waf\\dynamic_rules.conf"
'''

echo '======================================'

/*
 * ---------------------------------------------------------------
 * STOP ANY PREVIOUS WAF INSTANCE
 *
 * Failure is intentionally ignored because the WAF may not
 * already be running.
 * ---------------------------------------------------------------
 */

echo ''
echo 'Cleaning previous WAF deployment...'

bat(
    script: '''
    docker compose -f "waf/docker-compose.yml" down --remove-orphans
    ''',
    returnStatus: true
)

/*
 * ---------------------------------------------------------------
 * START WAF
 * ---------------------------------------------------------------
 */

echo ''
echo 'Starting Dynamic WAF Protection...'

def wafStartStatus = bat(
    script: '''
    docker compose -f "waf/docker-compose.yml" up -d --build
    ''',
    returnStatus: true
)

if (wafStartStatus != 0) {
    error(
        'WAF ERROR: Unable to start the WAF environment.'
    )
}

echo 'WAF environment started successfully.'

/*
 * ---------------------------------------------------------------
 * VERIFY WAF CONTAINERS
 * ---------------------------------------------------------------
 */

echo ''
echo 'Verifying WAF containers...'

def containerStatus = bat(
    script: '''
    docker compose -f "waf/docker-compose.yml" ps
    ''',
    returnStatus: true
)

if (containerStatus != 0) {
    error(
        'WAF ERROR: Unable to inspect WAF container status.'
    )
}

/*
 * ---------------------------------------------------------------
 * WAF STATUS
 * ---------------------------------------------------------------
 */

echo ''
echo '======================================'
echo 'WAF PROTECTION STATUS'
echo '======================================'

bat '''
docker compose -f "waf/docker-compose.yml" ps
'''

echo '======================================'

/*
 * ---------------------------------------------------------------
 * PRESERVE WAF EVIDENCE
 * ---------------------------------------------------------------
 */

echo ''
echo 'Preserving WAF configuration evidence...'

bat '''
if not exist "reports\\waf" mkdir "reports\\waf"

copy /Y "waf\\dynamic_rules.conf" ^
    "reports\\waf\\dynamic_rules.conf" >nul

copy /Y "waf\\docker-compose.yml" ^
    "reports\\waf\\docker-compose.yml" >nul
'''

echo 'WAF configuration evidence preserved.'

/*
 * ---------------------------------------------------------------
 * PIPELINE ENVIRONMENT VARIABLES
 * ---------------------------------------------------------------
 */

env.WAF_STATUS = 'ACTIVE'
env.WAF_CONFIG = 'waf/docker-compose.yml'
env.WAF_RULES  = 'waf/dynamic_rules.conf'

/*
 * ---------------------------------------------------------------
 * COMPLETION
 * ---------------------------------------------------------------
 */

echo ''
echo '======================================'
echo 'DYNAMIC WAF PROTECTION COMPLETED'
echo '======================================'

echo 'WAF Status : ACTIVE'
echo 'Compose    : waf/docker-compose.yml'
echo 'Rules      : waf/dynamic_rules.conf'
echo 'Evidence   : reports/waf/'

echo '======================================'