param([string]$Serial='emulator-5580')
$ErrorActionPreference='Stop'
$adb='C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe'
& $adb -s $Serial install -r app/build/outputs/apk/debug/app-debug.apk
& $adb -s $Serial shell wm size 800x800
& $adb -s $Serial shell wm density 430
& $adb -s $Serial shell am instrument -w com.tiltclock.wear/.verification.RendererInstrumentation | Set-Content docs/phase5-gpu.log
node tools/export-render-probe.mjs $Serial docs/phase5-gpu
if($LASTEXITCODE) { throw 'GPU export failed' }
$env:TILT_REPORT_PREFIX='docs/phase5'
& $adb -s $Serial shell svc wifi disable
& $adb -s $Serial shell svc data disable
try {
    node tools/test-phase4.mjs $Serial
    if($LASTEXITCODE) { throw 'Import workflow failed' }
} finally {
    & $adb -s $Serial shell svc wifi enable
    & $adb -s $Serial shell svc data enable
    Remove-Item Env:TILT_REPORT_PREFIX
}
& powershell -ExecutionPolicy Bypass -File tools/capture-phase3.ps1 -Serial $Serial -Prefix phase5
if($LASTEXITCODE) { throw 'Capture matrix failed' }
