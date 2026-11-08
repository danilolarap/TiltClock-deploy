param([switch]$IncludeLint)
$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
$env:ANDROID_USER_HOME = Join-Path $PWD '.android'
New-Item -ItemType Directory -Force $env:ANDROID_USER_HOME | Out-Null
# Use an existing dependency cache read-only; Gradle writes only under this workspace.
$sharedCache = 'C:\Users\Danilo\.gradle\caches'
if (Test-Path $sharedCache) { $env:GRADLE_RO_DEP_CACHE = $sharedCache }
$localGradle = 'C:\Users\Danilo\.gradle\wrapper\dists\gradle-9.6.0-bin\42k10rwplmzkhuboz9kdazi7s\gradle-9.6.0\bin\gradle.bat'
if (!(Test-Path $localGradle)) { $localGradle = Join-Path $PWD 'gradlew.bat' }
$debugKey = Join-Path $env:ANDROID_USER_HOME 'debug.keystore'
$ErrorActionPreference = 'Continue' # Native tools write informational diagnostics to stderr.
if (!(Test-Path $debugKey)) {
    & (Join-Path $env:JAVA_HOME 'bin\keytool.exe') -genkeypair -keystore $debugKey -storepass android -alias androiddebugkey -keypass android -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
$tasks = @(':app:assembleDebug', ':app:testDebugUnitTest')
if ($IncludeLint) { $tasks += ':app:lintDebug' }
& $localGradle --gradle-user-home .gradle-user-home --offline --no-daemon @tasks *> docs\build-phase1.log
$buildResult = $LASTEXITCODE
Get-Content docs\build-phase1.log -Tail 60
exit $buildResult
