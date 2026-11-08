param([Parameter(Mandatory=$true)][string]$Matc)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
New-Item -ItemType Directory -Force app/src/main/assets/materials | Out-Null
& $Matc -p mobile -a opengl -o app/src/main/assets/materials/shadow.filamat app/src/main/materials/shadow.mat
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Get-FileHash app/src/main/assets/materials/shadow.filamat -Algorithm SHA256
