/*
 * ARCHIVE SECURITY REPORTS
 * ------------------------
 * Archives all generated security evidence and optionally creates
 * a consolidated ZIP archive.
 */

echo ''
echo '=============================================='
echo ' ARCHIVING SECURITY EVIDENCE'
echo '=============================================='

// Find all security evidence files
def reportFiles = findFiles(glob: 'reports/**').findAll { file ->
    !file.directory && !file.name.startsWith('security-reports-')
}

echo "Security evidence files found: ${reportFiles.size()}"

// Display files that will be archived
reportFiles.each { file ->
    echo "  ✓ ${file.path}"
}

// Archive individual security evidence
if (reportFiles.size() > 0) {
    echo 'Archiving artifacts'

    archiveArtifacts(
        artifacts: 'reports/**',
        fingerprint: true,
        allowEmptyArchive: true
    )

    echo ''
    echo '✓ Individual security evidence archived.'
} else {
    echo ''
    echo '⚠ No security evidence files found to archive.'
}

echo ''
echo 'Attempting optional security evidence ZIP...'

try {

    def zipFile = "reports/security-reports-${env.BUILD_NUMBER}.zip"

    /*
     * FIX:
     * Jenkins cannot resolve "powershell" from its PATH.
     * Use the full Windows PowerShell executable path instead.
     */
    bat(
        script: """
            "C:\\Windows\\System32\\WindowsPowerShell\\v1.0\\powershell.exe" -NoProfile -ExecutionPolicy Bypass -Command ^
            "\\$zipPath = '${zipFile}'; ^
            \\$files = @(Get-ChildItem -Path 'reports' -Recurse -File | Where-Object { \\$_.Name -notlike 'security-reports-*.zip' }); ^
            if (\\$files.Count -gt 0) { ^
                if (Test-Path \\$zipPath) { Remove-Item \\$zipPath -Force }; ^
                Compress-Archive -Path \\$files.FullName -DestinationPath \\$zipPath -Force; ^
                Write-Host 'Security report ZIP created:'; ^
                Write-Host \\$zipPath ^
            } else { ^
                Write-Host 'No files available for ZIP creation.' ^
            }"
        """
    )

    if (fileExists(zipFile)) {
        echo ''
        echo '✓ Optional security ZIP created.'
        echo "  ZIP: ${zipFile}"

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