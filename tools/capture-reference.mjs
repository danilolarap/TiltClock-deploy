// Development-only capture harness. Never imported or packaged by the Android app.
import { spawn } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
const out = path.resolve('docs/captures');
fs.mkdirSync(out, { recursive: true });
const chrome = process.env.CHROME_PATH ?? 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const source = path.resolve(process.argv[2] ?? 'C:/Users/Danilo/Documents/TiltClock/index.html');
const child = spawn(chrome, ['--headless=new', '--no-first-run', '--no-default-browser-check',
  '--disable-gpu',
  `--user-data-dir=${path.resolve('.browser-profile-pipe')}`, '--remote-debugging-pipe', 'about:blank'],
  { windowsHide: true, stdio: ['ignore','ignore','pipe','pipe','pipe'] });
let stderr = '', buffer = '', next = 1;
const pending = new Map();
child.stderr.on('data', b => stderr += b);
child.stdio[4].on('data', b => {
  buffer += b.toString();
  let end;
  while ((end = buffer.indexOf('\0')) >= 0) {
    const raw = buffer.slice(0,end); buffer = buffer.slice(end+1);
    if (!raw) continue;
    const msg = JSON.parse(raw), callback = pending.get(msg.id);
    if (callback) { pending.delete(msg.id); callback(msg); }
  }
});
function call(method, params = {}, sessionId) {
  const id = next++;
  return new Promise((resolve,reject) => {
    const timer = setTimeout(() => { pending.delete(id); reject(new Error(`CDP timeout: ${method}`)); }, 15000);
    pending.set(id, msg => { clearTimeout(timer); msg.error ? reject(new Error(JSON.stringify(msg.error))) : resolve(msg.result); });
    child.stdio[3].write(JSON.stringify({id,method,params, ...(sessionId ? {sessionId} : {})})+'\0');
  });
}
const report = { viewport: { width:390, height:844, deviceScaleFactor:1 }, source, captures:[], status:'pending' };
try {
  const { targetId } = await call('Target.createTarget', {url:'about:blank'});
  const { sessionId } = await call('Target.attachToTarget', {targetId,flatten:true});
  const c = (method,params={})=>call(method,params,sessionId);
  await c('Page.enable');
  await c('Emulation.setDeviceMetricsOverride', {...report.viewport,mobile:true});
  await c('Page.navigate', {url: new URL(`file:///${source.replaceAll('\\','/')}`).href});
  await new Promise(r=>setTimeout(r,12000));
  const evaluate = async expression => (await c('Runtime.evaluate',{expression,returnByValue:true,awaitPromise:true})).result.value;
  const readiness = await evaluate(`(async()=>{
    await document.fonts.ready;
    const screen=document.querySelector('.watch-screen');
    const rect=screen.getBoundingClientRect();
    return {three:typeof THREE!=='undefined',canvas:!!document.querySelector('canvas'),
      fonts:[...document.fonts].map(f=>({family:f.family,status:f.status})),
      screen:{x:rect.x,y:rect.y,width:rect.width,height:rect.height},
      timeFont:getComputedStyle(document.querySelector('#watch-time')).fontSize,
      frameWidth:getComputedStyle(document.querySelector('#watch-frame')).width};
  })()`);
  report.readiness = readiness;
  const ready = readiness.three && readiness.canvas && readiness.screen.width === 300 &&
    readiness.timeFont === '30px' && readiness.fonts.some(f=>f.family.includes('Inter')&&f.status==='loaded') &&
    readiness.fonts.some(f=>f.family.includes('Space Grotesk')&&f.status==='loaded');
  if (!ready) {
    report.status='blocked: reference dependencies or fonts not loaded; no valid comparison';
    const snap=await c('Page.captureScreenshot',{format:'png'});
    fs.writeFileSync(path.join(out,'diagnostic-incomplete-reference.png'),Buffer.from(snap.data,'base64'));
  } else {
    for (const theme of ['light','dark']) {
      await evaluate(`(()=>{
        document.body.classList.toggle('dark-theme',${theme==='dark'});
        document.querySelector('main').style.animation='none';
        document.querySelector('#watch-time').textContent='08:27';
        document.querySelector('#watch-ampm').textContent='AM';
        document.querySelector('#watch-date').textContent='LUN, 24 OCT';
        document.querySelector('#watch-steps').textContent='1.259';
        document.querySelector('#watch-battery').textContent='85%';
        document.querySelectorAll('.scale-controls-container,.edit-control-container').forEach(x=>x.style.opacity='1');
        userScaleFactor=1; createHoloOrbitals(); mouseTargetX=0;mouseTargetY=0;window.gyroData={beta:0,gamma:0};
        orbitalRingsGroup.rotation.set(0,0,0);
      })()`);
      await new Promise(r=>setTimeout(r,50));
      // Snapshot immediately after fixture injection so the real clock interval cannot change it.
      await evaluate(`document.querySelector('#watch-time').textContent='08:27';document.querySelector('#watch-date').textContent='LUN, 24 OCT'`);
      const rect=await evaluate(`(()=>{const r=document.querySelector('.watch-screen').getBoundingClientRect();return {x:r.x,y:r.y,width:r.width,height:r.height,scale:1}})()`);
      const snap=await c('Page.captureScreenshot',{format:'png',clip:rect});
      fs.writeFileSync(path.join(out,`reference-${theme}.png`),Buffer.from(snap.data,'base64'));
      report.captures.push({theme,clip:rect,avatar:'holo',scale:1,orientation:[0,0,0]});
    }
    report.status='captured; inspect screenshots before visual comparison';
  }
  await call('Browser.close');
} catch(error) { report.status='blocked';report.error=String(error);child.kill(); }
finally {
  fs.writeFileSync(path.join(out,'reference-report.json'),JSON.stringify(report,null,2));
  fs.writeFileSync(path.join(out,'browser.log'),stderr);
  console.log(JSON.stringify(report,null,2));
}
