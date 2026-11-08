$ErrorActionPreference='Stop'
$tests=0; $failures=0
Get-ChildItem app/build/test-results/testDebugUnitTest/TEST-*.xml | ForEach-Object {
    [xml]$xml=Get-Content -Raw $_.FullName
    $tests+=[int]$xml.testsuite.tests; $failures+=[int]$xml.testsuite.failures+[int]$xml.testsuite.errors
}
if($tests -lt 35 -or $failures) { throw 'Unit test validation failed' }
[xml]$lint=Get-Content -Raw app/build/reports/lint-results-debug.xml
$errors=@($lint.issues.issue | Where-Object severity -eq 'Error').Count
if($errors) { throw 'Lint errors' }
$gpu=Get-Content -Raw docs/phase5-gpu/report.json | ConvertFrom-Json
$device=Get-Content -Raw docs/phase5-device.json | ConvertFrom-Json
if(!$gpu.success -or @($gpu.results).Count -ne 6 -or !$device.importedGpuAccepted -or !$device.importedRestoredAfterRestart) { throw 'Device checks incomplete' }
& node tools/audit-reference.mjs --verify
if($LASTEXITCODE) { throw 'Original changed' }
$sdk='C:/Users/Danilo/AppData/Local/Android/Sdk/build-tools/36.0.0'
$manifest=& "$sdk/aapt.exe" dump xmltree app/build/outputs/apk/release/app-release-unsigned.apk AndroidManifest.xml
if($LASTEXITCODE -or ($manifest -match 'RendererTestActivity|Phase4Instrumentation|ImportTestProvider|RendererInstrumentation')) { throw 'Debug component in release' }
$manifest | Set-Content -Encoding utf8 docs/phase5-release-manifest.txt
$env:JAVA_HOME='C:/Program Files/Android/Android Studio/jbr'
$ErrorActionPreference='Continue'
& "$sdk/apksigner.bat" verify --verbose app/build/outputs/apk/debug/app-debug.apk *> docs/phase5-signature.log
$signingExit=$LASTEXITCODE
$ErrorActionPreference='Stop'
if($signingExit) { throw 'Debug APK signature invalid' }
$result=@{tests=$tests;failures=$failures;lintErrors=$errors;lintWarnings=@($lint.issues.issue | Where-Object severity -eq 'Warning').Count;
    apkSha256=(Get-FileHash app/build/outputs/apk/debug/app-debug.apk -Algorithm SHA256).Hash.ToLower();
    releaseSha256=(Get-FileHash app/build/outputs/apk/release/app-release-unsigned.apk -Algorithm SHA256).Hash.ToLower();
    releaseExcludesDebugComponents=$true;gpuPresets=6;physicalSamsung='not executed';original='20 source files unchanged'}
$result | ConvertTo-Json | Set-Content -Encoding utf8 docs/phase5-verification.json
$result | ConvertTo-Json
