$ErrorActionPreference = 'Stop'
Set-Location (Split-Path -Parent $PSScriptRoot)
Add-Type -AssemblyName System.IO.Compression.FileSystem
$apkPath = Join-Path $PWD 'app\build\outputs\apk\debug\app-debug.apk'
$apk = [System.IO.Compression.ZipFile]::OpenRead($apkPath)
$inventory = Get-Content docs\models-inventory.json -Raw | ConvertFrom-Json
$models = @()
try {
    foreach ($model in $inventory) {
        $entryName = 'assets/models/' + [System.IO.Path]::GetFileName($model.file)
        $entry = $apk.GetEntry($entryName)
        if ($null -eq $entry) { throw "Missing model: $entryName" }
        $entryStream = $entry.Open()
        $hashProvider = [System.Security.Cryptography.SHA256]::Create()
        try {
            $hash = [BitConverter]::ToString($hashProvider.ComputeHash($entryStream)).Replace('-', '').ToLowerInvariant()
        } finally { $entryStream.Dispose(); $hashProvider.Dispose() }
        if ($hash -ne $model.sha256) { throw "Packaged model changed: $entryName" }
        $models += @{ name = $entryName; sha256 = $hash; bytes = $entry.Length; verified = $true }
    }
    if ($null -eq $apk.GetEntry('assets/licenses/TiltClock-MIT.txt')) { throw 'Missing original MIT license in APK' }
    if ($null -ne $apk.GetEntry('assets/models/fox.glb')) { throw 'Fox unexpectedly packaged' }
} finally { $apk.Dispose() }
$tests = 0; $failures = 0; $errors = 0; $skipped = 0
Get-ChildItem app\build\test-results\testDebugUnitTest -Filter 'TEST-*.xml' | ForEach-Object {
    [xml]$testXml = Get-Content $_.FullName
    $tests += [int]$testXml.testsuite.tests
    $failures += [int]$testXml.testsuite.failures
    $errors += [int]$testXml.testsuite.errors
    $skipped += [int]$testXml.testsuite.skipped
}
if ($tests -lt 5 -or $failures -ne 0 -or $errors -ne 0 -or $skipped -ne 0) { throw 'Unit tests missing or not successful' }
$productCode = Get-ChildItem app\src\main -Recurse -File | Where-Object Extension -In '.kt', '.xml'
$forbidden = $productCode | Select-String -Pattern 'Apple Watch|Xiaomi|DeviceSelector|DemoStepsScreen|WebView|1259|1,259|1\.259|85%'
if ($forbidden) { throw 'Forbidden product implementation or placeholder found' }
$report = @{
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
    apk = @{ path = $apkPath; bytes = (Get-Item $apkPath).Length; sha256 = (Get-FileHash $apkPath -Algorithm SHA256).Hash.ToLowerInvariant() }
    models = $models
    unitTests = @{ total = $tests; failures = $failures; errors = $errors; skipped = $skipped }
    productScan = 'PASS: no other device formats, WebView or fixed measurement placeholders in main Kotlin/XML'
    physicalWatch = 'not tested'
    visualComparison = 'blocked: see captures/reference-report.json'
    lint = 'blocked: offline cache missing lint-gradle:32.4.1'
}
$report | ConvertTo-Json -Depth 8 | Set-Content docs\delivery-verification.json -Encoding UTF8
Write-Output "PASS: $tests unit tests, 4 packaged models with original hashes, original license packaged, no forbidden product options."
