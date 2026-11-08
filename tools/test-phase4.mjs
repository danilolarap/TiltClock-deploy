import fs from 'node:fs';
import {spawnSync} from 'node:child_process';
const serial=process.argv[2]??'emulator-5580';
const adb=process.env.ADB_PATH??'C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe';
function run(args) { const r=spawnSync(adb,['-s',serial,...args],{windowsHide:true,maxBuffer:4*1024*1024,timeout:90000}); if(r.status!==0) throw new Error(r.stderr.toString()||String(r.error));return r.stdout; }
const wait=ms=>new Promise(resolve=>setTimeout(resolve,ms));
function loadedAvatar() {
  const pid=run(['shell','pidof','com.tiltclock.wear']).toString().trim();
  const log=run(['logcat','-d','--pid',pid,'-s','TiltClockMetrics:I']).toString();
  fs.appendFileSync('docs/phase4-native.log',log);
  return [...log.matchAll(/"event":"loaded","avatar":"([^"]+)"/g)].at(-1)?.[1];
}
fs.writeFileSync('docs/phase4-native.log','');
run(['install','-r','app/build/outputs/apk/debug/app-debug.apk']);
const result=run(['shell','am','instrument','-w','com.tiltclock.wear/.verification.Phase4Instrumentation']);
fs.writeFileSync('docs/phase4-instrumentation.log',result);
const report=JSON.parse(run(['exec-out','run-as','com.tiltclock.wear','cat','files/phase4-report.json']).toString());
fs.writeFileSync('docs/phase4-device.json',JSON.stringify(report,null,2));
if(!report.success) throw new Error(JSON.stringify(report));
const transfer=spawnSync(process.execPath,['tools/transfer-avatar.mjs',serial,'app/src/main/assets/models/fox.glb'],{windowsHide:true});
if(transfer.status!==0) throw new Error(transfer.stderr.toString());
run(['shell','am','force-stop','com.tiltclock.wear']);
run(['shell','am','start','-n','com.tiltclock.wear/.verification.RendererTestActivity']);
await wait(15000);
run(['shell','input','tap','640','550']); // dismiss the API 37 immersive explanation at density 430
run(['shell','input','tap','400','690']);
await wait(10000);
const imported=loadedAvatar();
if(!imported?.startsWith('import_')) throw new Error('GPU did not accept imported GLB');
fs.writeFileSync('docs/captures/phase4-import.png',run(['exec-out','screencap','-p']));
run(['shell','am','force-stop','com.tiltclock.wear']);
run(['shell','am','start','-n','com.tiltclock.wear/.verification.RendererTestActivity']);
await wait(15000);
if(loadedAvatar()!==imported) throw new Error('Imported avatar was not restored');
report.importedGpuAccepted=true;report.importedRestoredAfterRestart=true;
fs.writeFileSync('docs/phase4-device.json',JSON.stringify(report,null,2));
fs.writeFileSync('docs/captures/phase4-restored.png',run(['exec-out','screencap','-p']));
run(['shell','input','tap','400','690']);
await wait(3000);
report.documentPickerOpened=run(['shell','dumpsys','activity','activities']).toString().includes('documentsui');
fs.writeFileSync('docs/phase4-device.json',JSON.stringify(report,null,2));
run(['shell','input','keyevent','4']);
console.log(JSON.stringify(report));
