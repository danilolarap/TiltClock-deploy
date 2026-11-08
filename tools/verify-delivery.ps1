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
    $foxSource = Get-Content reference/fox-source.json -Raw | ConvertFrom-Json
    $fox = $apk.GetEntry('assets/models/fox.glb')
    if ($null -eq $fox) { throw 'Fox missing' }
    $foxStream = $fox.Open(); $foxHasher = [System.Security.Cryptography.SHA256]::Create()
    try { $foxHash = [BitConverter]::ToString($foxHasher.ComputeHash($foxStream)).Replace('-', '').ToLowerInvariant() }
    finally { $foxStream.Dispose(); $foxHasher.Dispose() }
    if ($foxHash -ne $foxSource.sha256) { throw 'Fox source hash mismatch' }
    $models += @{ name = 'assets/models/fox.glb'; sha256 = $foxHash; bytes = $fox.Length; verified = $true }
    foreach ($license in @('Filament-Apache-2.0.txt','Fox-attribution.md','Fox-CC-BY-4.0.txt','Fox-CC0-1.0.txt')) {
        if ($null -eq $apk.GetEntry('assets/licenses/' + $license)) { throw "Missing license: $license" }
    }
    if ($null -eq $apk.GetEntry('assets/materials/shadow.filamat')) { throw 'Missing compiled shadow material' }
} finally { $apk.Dispose() }
$tests = 0; $failures = 0; $errors = 0; $skipped = 0
Get-ChildItem app\build\test-results\testDebugUnitTest -Filter 'TEST-*.xml' | ForEach-Object {
    [xml]$testXml = Get-Content $_.FullName
    $tests += [int]$testXml.testsuite.tests
    $failures += [int]$testXml.testsuite.failures
    $errors += [int]$testXml.testsuite.errors
    $skipped += [int]$testXml.testsuite.skipped
}
if ($tests -lt 20 -or $failures -ne 0 -or $errors -ne 0 -or $skipped -ne 0) { throw 'Unit tests missing or not successful' }
[xml]$lint = Get-Content app/build/reports/lint-results-debug.xml
$lintErrors = @($lint.issues.issue | Where-Object severity -In 'Error','Fatal').Count
$lintWarnings = @($lint.issues.issue | Where-Object severity -EQ 'Warning').Count
if ($lintErrors -ne 0) { throw 'Lint errors present' }
$probe = Get-Content docs/render-probe/report.json -Raw | ConvertFrom-Json
if (!$probe.success -or @($probe.results).Count -ne 6) { throw 'Six-model GPU probe has not passed' }
if (@($probe.results | Where-Object { $_.coloredPixels -le 100 }).Count -ne 0) { throw 'Blank or dark GPU output' }
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
    visualComparison = 'Web captures and native images present; final 2D layout/overlay belongs to phase 3'
    lint = @{ errors = $lintErrors; warnings = $lintWarnings }
    renderer = @{ engine = 'Filament/gltfio 1.77.3'; report = 'render-probe/report.json'; gpu = 'Android Emulator OpenGL ES Translator, AMD Radeon(TM) Graphics'; samsungValidated = $false }
}
$report | ConvertTo-Json -Depth 8 | Set-Content docs\delivery-phase2.json -Encoding UTF8
Write-Output "PASS: $tests unit tests, 5 original GLB hashes, licenses, shadow material, 6 real GPU loads, Lint $lintErrors errors/$lintWarnings warnings, no forbidden product options."
