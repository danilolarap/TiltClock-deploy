param([switch]$Offline)
$args5=@('-ExecutionPolicy','Bypass','-File',(Join-Path $PSScriptRoot 'build-phase2.ps1'),'-LogPath','docs/build-phase5.log')
if($Offline) { $args5+='-Offline' }
& powershell @args5
if($LASTEXITCODE) { exit $LASTEXITCODE }
$env:JAVA_HOME='C:/Program Files/Android/Android Studio/jbr'
$env:ANDROID_USER_HOME=Join-Path (Get-Location) '.android'
$env:GRADLE_RO_DEP_CACHE='C:/Users/Danilo/.gradle/caches'
$gradle5='C:/Users/Danilo/.gradle/wrapper/dists/gradle-9.6.0-bin/42k10rwplmzkhuboz9kdazi7s/gradle-9.6.0/bin/gradle.bat'
$releaseArgs=@('--gradle-user-home','.gradle-user-home','--no-daemon',':app:assembleRelease')
if($Offline) { $releaseArgs+='--offline' }
& $gradle5 @releaseArgs *> docs/build-phase5-release.log
exit $LASTEXITCODE
