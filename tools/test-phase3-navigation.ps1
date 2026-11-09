param([string]$Serial='emulator-5580')
$ErrorActionPreference='Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
node tools/test-settings.mjs $Serial
exit $LASTEXITCODE
