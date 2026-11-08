param([switch]$Offline,[string]$LogPath='docs/build-phase2.log')
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
$env:ANDROID_USER_HOME = Join-Path $PWD '.android'
$env:GRADLE_RO_DEP_CACHE = 'C:\Users\Danilo\.gradle\caches'
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
$gradle = 'C:\Users\Danilo\.gradle\wrapper\dists\gradle-9.6.0-bin\42k10rwplmzkhuboz9kdazi7s\gradle-9.6.0\bin\gradle.bat'
if (!(Test-Path $gradle)) { $gradle = Join-Path $PWD 'gradlew.bat' }
New-Item -ItemType Directory -Force $env:ANDROID_USER_HOME | Out-Null
$debugKey = Join-Path $env:ANDROID_USER_HOME 'debug.keystore'
if (!(Test-Path $debugKey)) {
    & (Join-Path $env:JAVA_HOME 'bin/keytool.exe') -genkeypair -keystore $debugKey -storepass android -alias androiddebugkey -keypass android -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
$arguments = @('--gradle-user-home','.gradle-user-home','--no-daemon',':app:assembleDebug',':app:testDebugUnitTest',':app:lintDebug')
if ($Offline) { $arguments += '--offline' }
$ErrorActionPreference = 'Continue'
& $gradle @arguments *> $LogPath
$result = $LASTEXITCODE
Get-Content $LogPath -Tail 65
exit $result
