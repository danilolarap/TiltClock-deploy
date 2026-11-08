$ErrorActionPreference='Stop'
$adb='C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe'
$serial='emulator-5580'
function Tap($x,$y) { & $adb -s $serial shell input tap $x $y; Start-Sleep -Seconds 2 }
function Swipe($end) { & $adb -s $serial shell input swipe 400 650 400 $end 600; Start-Sleep -Seconds 2 }
function Back { & $adb -s $serial shell input keyevent 4; Start-Sleep -Seconds 2 }
& $adb -s $serial shell am force-stop com.tiltclock.wear
& $adb -s $serial shell am start -n com.tiltclock.wear/.verification.RendererTestActivity
Start-Sleep -Seconds 15
Tap 60 400
Swipe 320
Tap 400 480
Swipe 320
Tap 400 280
Back
Swipe 180
Tap 400 520
Swipe 320
Tap 400 480
Back
Back
node tools/capture-android.mjs $serial docs/captures/phase3-preferences.png
& $adb -s $serial shell am force-stop com.tiltclock.wear
& $adb -s $serial shell am start -n com.tiltclock.wear/.verification.RendererTestActivity
Start-Sleep -Seconds 15
node tools/capture-android.mjs $serial docs/captures/phase3-restored.png
