param([string]$Serial='emulator-5580')
$ErrorActionPreference='Stop'
$adb='C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe'
function Launch($theme,$language) {
    & $adb -s $Serial shell am force-stop com.tiltclock.wear
    & $adb -s $Serial shell am start -n com.tiltclock.wear/.verification.RendererTestActivity --ez fixture true --es theme $theme --es language $language
    Start-Sleep -Seconds 15
}
& $adb -s $Serial install -r app/build/outputs/apk/debug/app-debug.apk
& $adb -s $Serial shell wm size 800x800
& $adb -s $Serial shell wm density 430
Launch light es
node tools/capture-android.mjs $Serial docs/captures/phase3-light.png
Launch dark es
node tools/capture-android.mjs $Serial docs/captures/phase3-dark.png
& $adb -s $Serial shell input tap 60 400
Start-Sleep -Seconds 1
& $adb -s $Serial shell input swipe 400 650 400 320 600
Start-Sleep -Seconds 1
node tools/capture-android.mjs $Serial docs/captures/phase3-menu.png
& $adb -s $Serial shell wm density 560
Launch light es
node tools/capture-android.mjs $Serial docs/captures/phase3-small.png
& $adb -s $Serial shell settings put system font_scale 1.3
Launch dark en
node tools/capture-android.mjs $Serial docs/captures/phase3-large-font.png
& $adb -s $Serial shell settings put system font_scale 1.0
& $adb -s $Serial shell wm density 430
