$ErrorActionPreference='Stop'
$total=0; $failures=0
Get-ChildItem app/build/test-results/testDebugUnitTest/TEST-*.xml | ForEach-Object {
    [xml]$test=Get-Content -Raw $_.FullName
    $total+=[int]$test.testsuite.tests
    $failures+=[int]$test.testsuite.failures+[int]$test.testsuite.errors
}
if($total -lt 31 -or $failures -ne 0) { throw 'Tests incomplete or failed' }
[xml]$lint=Get-Content -Raw app/build/reports/lint-results-debug.xml
$errors=@($lint.issues.issue | Where-Object severity -eq 'Error').Count
if($errors) { throw 'Lint errors' }
& node tools/audit-reference.mjs --verify
if($LASTEXITCODE) { throw 'Original changed' }
$device=Get-Content -Raw docs/phase4-device.json | ConvertFrom-Json
if(!$device.success) { throw 'Device probe incomplete' }
$result=@{tests=$total;failures=$failures;lintErrors=$errors;lintWarnings=@($lint.issues.issue | Where-Object severity -eq 'Warning').Count;
    apkSha256=(Get-FileHash app/build/outputs/apk/debug/app-debug.apk -Algorithm SHA256).Hash.ToLower();original='20 source files unchanged';
    environment='API 37 Android phone emulator, debug circular host; Samsung physical tests pending'}
$result | ConvertTo-Json | Set-Content -Encoding utf8 docs/phase4-verification.json
$result | ConvertTo-Json
