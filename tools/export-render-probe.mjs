import {spawnSync} from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
const adb=process.env.ADB_PATH ?? 'C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe';
const serial=process.argv[2] ?? 'emulator-5580';
const out=path.resolve(process.argv[3] ?? 'docs/render-probe'); fs.mkdirSync(out,{recursive:true});
function pull(privatePath,filename) {
  const result=spawnSync(adb,['-s',serial,'exec-out','run-as','com.tiltclock.wear','cat',privatePath],
    {windowsHide:true,maxBuffer:16*1024*1024});
  if (result.status!==0) throw new Error(result.stderr.toString());
  fs.writeFileSync(path.join(out,filename),result.stdout);
}
pull('files/phase2-report.json','report.json');
const report=JSON.parse(fs.readFileSync(path.join(out,'report.json'),'utf8'));
for (const {id} of report.results) pull(`files/phase2/${id}.png`,`${id}.png`);
console.log(JSON.stringify({success:report.success,exported:report.results.map(r=>r.id)},null,2));
