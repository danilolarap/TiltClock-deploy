param([switch]$Offline)
$arguments=@('-ExecutionPolicy','Bypass','-File',(Join-Path $PSScriptRoot 'build-phase2.ps1'))
if($Offline) { $arguments+='-Offline' }
& powershell @arguments
$result=$LASTEXITCODE
Copy-Item -LiteralPath (Join-Path $PSScriptRoot '../docs/build-phase2.log') -Destination (Join-Path $PSScriptRoot '../docs/build-phase3.log')
exit $result
