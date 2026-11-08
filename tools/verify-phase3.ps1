$ErrorActionPreference='Stop'
$tests=Get-ChildItem app/build/test-results/testDebugUnitTest/TEST-*.xml
$passed=0; $failed=0
foreach($test in $tests) { [xml]$xml=Get-Content -Raw $test.FullName; $passed+=[int]$xml.testsuite.tests; $failed+=[int]$xml.testsuite.failures+[int]$xml.testsuite.errors }
if($failed -gt 0 -or $passed -lt 22) { throw 'Unit test evidence incomplete' }
[xml]$lint=Get-Content -Raw app/build/reports/lint-results-debug.xml
$errors=@($lint.issues.issue | Where-Object severity -eq 'Error').Count
if($errors -gt 0) { throw 'Lint errors' }
& node tools/audit-reference.mjs --verify
if($LASTEXITCODE -ne 0) { throw 'Original changed' }
$fontFiles=Get-ChildItem app/src/main/res/font/*.ttf
$fonts=@($fontFiles | ForEach-Object { @{file=$_.Name;sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLower()} })
$report=@{tests=$passed;failures=$failed;lintErrors=$errors;lintWarnings=@($lint.issues.issue | Where-Object severity -eq 'Warning').Count;
    apkSha256=(Get-FileHash app/build/outputs/apk/debug/app-debug.apk -Algorithm SHA256).Hash.ToLower();fonts=$fonts;
    original='20 files unchanged';device='Android API 37 phone emulator with debug-only circular configuration; not Samsung physical validation'}
$report | ConvertTo-Json -Depth 5 | Set-Content -Encoding utf8 docs/phase3-verification.json
$report | ConvertTo-Json -Depth 5
