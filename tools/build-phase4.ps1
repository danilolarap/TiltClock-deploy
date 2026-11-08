param([switch]$Offline)
$arguments=@('-ExecutionPolicy','Bypass','-File',(Join-Path $PSScriptRoot 'build-phase2.ps1'),'-LogPath','docs/build-phase4.log')
if($Offline) { $arguments+='-Offline' }
& powershell @arguments
$result=$LASTEXITCODE
exit $result
