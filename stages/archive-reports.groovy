echo ''
echo '=============================================='
echo ' ARCHIVING SECURITY EVIDENCE'
echo '=============================================='

/*
 * ============================================================
 * INDIVIDUAL SECURITY REPORTS
 * ============================================================
 *
 * Individual reports are the primary security evidence.
 * They are archived even when ZIP creation is unavailable.
 */

def reportFiles =
    findFiles(
        glob: 'reports/**/*'
    ).findAll { file ->
        !file.directory &&
        !file.name.startsWith('security-reports-')
    }

if (reportFiles.size() > 0) {

    echo "Security evidence files found: ${reportFiles.size()}"

    reportFiles.each { file ->
        echo "  ✓ ${file.path}"
    }

    archiveArtifacts(
        artifacts: 'reports/**/*',
        fingerprint: true,
        allowEmptyArchive: true
    )

    echo ''
    echo '✓ Individual security evidence archived.'

} else {

    echo '⚠ No security evidence files found.'
}

/*
 * ============================================================
 * OPTIONAL SECURITY EVIDENCE ZIP
 * ============================================================
 *
 * ZIP creation is supplementary.
 * Failure to create the ZIP must NOT remove or invalidate
 * the individual security evidence already archived.
 */

echo ''
echo 'Attempting optional security evidence ZIP...'

try {

    def zipFile =
        "reports/security-reports-${env.BUILD_NUMBER}.zip"

    powershell(
        script: """
\$zipPath = "${zipFile}"

\$files = Get-ChildItem `
    -Path "reports" `
    -Recurse `
    -File |
    Where-Object {
        \$_.Name -notlike "security-reports-*.zip"
    }

if (\$files.Count -gt 0) {

    if (Test-Path \$zipPath) {
        Remove-Item \$zipPath -Force
    }

    Compress-Archive `
        -Path \$files.FullName `
        -DestinationPath \$zipPath `
        -Force

    Write-Host "Security report ZIP created:"
    Write-Host \$zipPath

} else {

    Write-Host "No files available for ZIP creation."
}
"""
    )

    if (fileExists(zipFile)) {

        echo ''
        echo '✓ Optional security ZIP created.'
        echo "  ${zipFile}"

        archiveArtifacts(
            artifacts: zipFile,
            fingerprint: true,
            allowEmptyArchive: true
        )

    } else {

        echo ''
        echo '⚠ ZIP was not created.'
        echo 'Individual security reports remain archived.'
    }

} catch (Exception zipError) {

    echo ''
    echo '⚠ Optional ZIP creation failed.'
    echo "ZIP error: ${zipError}"
    echo ''
    echo 'Individual security reports remain available.'
}

echo ''
echo '=============================================='
echo ' SECURITY EVIDENCE ARCHIVE COMPLETE'
echo '=============================================='