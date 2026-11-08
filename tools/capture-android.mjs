import {spawnSync} from 'node:child_process';
import fs from 'node:fs';
const adb=process.env.ADB_PATH ?? 'C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe';
const output=process.argv[3] ?? 'docs/captures/android-phase2.png';
const result=spawnSync(adb,['-s',process.argv[2] ?? 'emulator-5580','exec-out','screencap','-p'],
  {windowsHide:true,maxBuffer:16*1024*1024});
if (result.status!==0) throw new Error(result.stderr.toString());
fs.writeFileSync(output,result.stdout);
console.log(output);
