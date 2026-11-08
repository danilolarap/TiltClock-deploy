import fs from 'node:fs';
import {spawnSync} from 'node:child_process';
import {randomUUID,createHash} from 'node:crypto';
const [serial,file]=process.argv.slice(2);
if(!serial||!file) throw new Error('Usage: node tools/transfer-avatar.mjs SERIAL MODEL.glb');
const bytes=fs.readFileSync(file);
if(bytes.length<28||bytes.length>32*1024*1024||bytes.readUInt32LE(0)!==0x46546c67) throw new Error('A GLB up to 32 MiB is required');
const adb=process.env.ADB_PATH??'C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe';
function run(args,input) { const r=spawnSync(adb,['-s',serial,...args],{input,windowsHide:true,maxBuffer:1024*1024}); if(r.status!==0) throw new Error(r.stderr.toString()); }
run(['shell','run-as','com.tiltclock.wear','mkdir','-p','files/inbox']);
const staging='/data/local/tmp/tiltclock-'+randomUUID()+'.glb';
try {
  run(['push',file,staging]);
  run(['shell','run-as','com.tiltclock.wear','cp',staging,'files/inbox/avatar.part']);
  const hash=spawnSync(adb,['-s',serial,'shell','run-as','com.tiltclock.wear','sha256sum','files/inbox/avatar.part'],{windowsHide:true});
  if(hash.status!==0||hash.stdout.toString().trim().split(/\s+/)[0]!==createHash('sha256').update(bytes).digest('hex')) throw new Error('Transferred hash differs; inbox not committed');
  run(['shell','run-as','com.tiltclock.wear','mv','files/inbox/avatar.part','files/inbox/avatar.glb']);
} finally { run(['shell','rm','-f',staging]); }
console.log('Transferred to private debug inbox. Tap Upload Avatar on the watch. Requires a debuggable APK and authorized ADB.');
