import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import assert from 'node:assert/strict';

// Deliberately limited to an emulator: never changes a connected physical watch.
const serial = process.argv[2] ?? 'emulator-5580';
assert(serial.startsWith('emulator-'), 'Run this navigation test on a dedicated emulator.');
const adb = process.env.ADB_PATH ?? 'C:/Users/Danilo/AppData/Local/Android/Sdk/platform-tools/adb.exe';
const out = 'docs/captures';
const matrixOnly = process.argv.includes('--matrix-only');
const report = matrixOnly ? JSON.parse(fs.readFileSync('docs/settings-verification.json', 'utf8'))
  : { serial, checks: [], captures: [] };
delete report.error; delete report.success;
report.captures = report.captures.filter(file => !file.endsWith('settings-failure.png'));
const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));
function command(...args) {
  const result = spawnSync(adb, ['-s', serial, ...args], { windowsHide: true, maxBuffer: 16 * 1024 * 1024 });
  assert.equal(result.status, 0, result.stderr?.toString());
  return result.stdout;
}
const shell = (...args) => command('shell', ...args).toString().trim();
const decode = value => value.replaceAll('&amp;', '&').replaceAll('&quot;', '"')
  .replaceAll('&#10;', '\n').replaceAll('&apos;', "'");
function nodes() {
  shell('uiautomator', 'dump', '/sdcard/tiltclock-settings.xml');
  const xml = shell('cat', '/sdcard/tiltclock-settings.xml');
  fs.writeFileSync('build/settings-last-ui.xml', xml);
  const list = [], stack = [];
  for (const match of xml.matchAll(/<\/?node\b[^>]*>/g)) {
    if (match[0].startsWith('</')) { stack.pop(); continue; }
    const node = Object.fromEntries([...match[0].matchAll(/([\w-]+)="([^"]*)"/g)].map(m => [m[1], decode(m[2])]));
    node.parent = stack.at(-1); list.push(node);
    if (!match[0].endsWith('/>')) stack.push(node);
  }
  return list;
}
const label = node => `${node.text ?? ''}\n${node['content-desc'] ?? ''}`;
function find(list, text) {
  const result = list.find(n => label(n).split('\n').includes(text));
  assert(result, `Missing ${text}: ${list.map(label).filter(s => s.trim()).join(' | ')}`);
  return result;
}
function isSelected(node) {
  while (node) {
    if (node.checked === 'true' || node.selected === 'true') return true;
    node = node.parent;
  }
  return false;
}
async function tap(text) {
  const node = find(nodes(), text);
  const [x1, y1, x2, y2] = node.bounds.match(/\d+/g).map(Number);
  shell('input', 'tap', Math.round((x1 + x2) / 2), Math.round((y1 + y2) / 2));
  await sleep(650);
}
function capture(name) {
  const file = `${out}/settings-${name}.png`;
  fs.writeFileSync(file, command('exec-out', 'screencap', '-p'));
  if (!report.captures.includes(file)) report.captures.push(file);
}
function check(name, assertion) {
  assertion(); if (!report.checks.includes(name)) report.checks.push(name); console.log(`PASS ${name}`);
}
async function back() { shell('input', 'keyevent', '4'); await sleep(600); }
async function launch() {
  shell('am', 'force-stop', 'com.tiltclock.wear');
  shell('am', 'start', '-n', 'com.tiltclock.wear/.verification.RendererTestActivity');
  await sleep(9000);
}
async function openSettings() {
  // Language can be either value after persistence checks.
  const list = nodes();
  const edit = list.find(n => /Edit model|Editar modelo/.test(label(n)));
  assert(edit, 'The clock must expose its edit action.');
  const [x1,y1,x2,y2] = edit.bounds.match(/\d+/g).map(Number);
  shell('input', 'tap', (x1+x2)/2, (y1+y2)/2);
  await sleep(600);
}

const oldSize = shell('wm', 'size').match(/Override size: (\S+)/)?.[1] ?? 'reset';
const oldDensity = shell('wm', 'density').match(/Override density: (\S+)/)?.[1] ?? 'reset';
const oldFont = shell('settings', 'get', 'system', 'font_scale');
const oldImmersive = shell('settings', 'get', 'secure', 'immersive_mode_confirmations');
const launcherWasDisabled = shell('pm', 'list', 'packages', '-d', 'com.google.android.apps.nexuslauncher')
  .includes('package:com.google.android.apps.nexuslauncher');
try {
  // The phone launcher crashes on a 192 dp display. It is outside the tested app.
  if (!launcherWasDisabled) shell('pm', 'disable-user', '--user', '0', 'com.google.android.apps.nexuslauncher');
  shell('settings', 'put', 'secure', 'immersive_mode_confirmations', 'confirmed');
  shell('input', 'keyevent', '4');
  command('install', '-r', 'app/build/outputs/apk/debug/app-debug.apk');
  shell('wm', 'size', '800x800'); shell('wm', 'density', '560');
  shell('settings', 'put', 'system', 'font_scale', '1.0');
  if (!matrixOnly) {
  await launch(); await openSettings();
  // Normalize language through the UI if this emulator was used for English tests.
  if (nodes().some(n => label(n).split('\n').includes('Language'))) {
    await tap('Language'); await tap('Español'); await back();
  }
  const first = nodes();
  check('All three settings are visible immediately without scrolling', () => {
    for (const text of ['Modelos', 'Tema', 'Idioma']) find(first, text);
    assert(!first.some(n => n.scrollable === 'true'));
    assert(!first.some(n => n.text === 'Ajustes' && n.clickable === 'true'));
  });
  const initialModel = ['Átomo holográfico', 'Zorro', 'Pájaro', 'Mapache', 'Moneda', 'Riot', 'Avatar importado']
    .find(name => first.some(node => label(node).split('\n').includes(name)));
  await tap('Tema'); await tap('Claro'); capture('theme-light'); await back(); capture('hub-light');
  await tap('Tema');
  await tap('Oscuro');
  check('Dark theme has an explicit selection state', () =>
    assert(isSelected(find(nodes(), 'Oscuro'))));
  capture('theme-dark'); await back(); capture('hub-dark');
  await tap('Idioma'); await tap('English');
  check('Language changes immediately and marks the chosen option', () => {
    const list = nodes(); find(list, 'Language'); assert(isSelected(find(list, 'English')));
  });
  capture('language-en'); await back();
  const preferencesBeforeBrowse = command('exec-out', 'run-as', 'com.tiltclock.wear', 'cat', 'files/datastore/tiltclock.preferences_pb');
  await tap('Models');
  // Return to the first preset regardless of the emulator's existing selection.
  while (nodes().some(n => n['content-desc'] === 'Previous model' && n.enabled === 'true')) await tap('Previous model');
  capture('model-holo');
  const modelNames = ['Fox', 'Bird', 'Raccoon', 'Coin', 'Riot'];
  for (const name of modelNames) {
    await tap('Next model');
    check(`Preview ${name} is reachable with the visible arrow`, () => find(nodes(), name));
    capture(`model-${name.toLowerCase()}`);
  }
  await back();
  check('Browsing previews does not change the selected model', () => {
    assert.deepEqual(command('exec-out', 'run-as', 'com.tiltclock.wear', 'cat', 'files/datastore/tiltclock.preferences_pb'), preferencesBeforeBrowse);
    const list = nodes();
    if (initialModel) {
      const translations = { 'Átomo holográfico': 'Holographic atom', Zorro: 'Fox', 'Pájaro': 'Bird', Mapache: 'Raccoon', Moneda: 'Coin', Riot: 'Riot', 'Avatar importado': 'Imported avatar' };
      find(list, translations[initialModel]);
    }
  });
  await tap('Models');
  while (nodes().some(n => n['content-desc'] === 'Previous model' && n.enabled === 'true')) await tap('Previous model');
  shell('input', 'swipe', '500', '360', '200', '360', '400'); await sleep(700);
  check('Swiping previews works alongside the arrow buttons', () => find(nodes(), 'Fox'));
  const appliedModel = initialModel === 'Zorro' ? 'Bird' : 'Fox';
  if (appliedModel === 'Bird') await tap('Next model');
  await tap('Use model'); await sleep(8000); await openSettings();
  check('Applying a model returns to the clock and updates the selection', () => find(nodes(), appliedModel));
  await tap('Close'); await launch(); await openSettings();
  check('Theme, language and model survive an app restart', () => {
    const list = nodes(); for (const text of [appliedModel, 'Dark', 'Language', 'English']) find(list, text);
  });
  }
  // Check small round screens and enlarged text, with both actual themes/languages.
  const sizes = [[667, '1.0', '192dp'], [667, '1.3', '192dp-large-font'], [430, '1.0', '298dp']]
    .filter(([, font]) => !process.argv.includes('--large-font-only') || font === '1.3');
  for (const [density, font, tag] of sizes) {
    shell('wm', 'density', `${density}`); shell('settings', 'put', 'system', 'font_scale', font);
    await launch(); await openSettings();
    check(`Settings remain discoverable at ${tag}`, () => {
      const list = nodes(); for (const text of ['Models', 'Theme', 'Language']) find(list, text);
    });
    capture(`hub-${tag}`);
    await tap('Theme'); capture(`theme-${tag}`); await back();
    await tap('Models'); capture(`models-${tag}`); await back();
    await tap('Language'); capture(`language-${tag}`);
  }
  report.success = true;
} catch (error) {
  report.success = false; report.error = error.stack; capture('failure'); throw error;
} finally {
  fs.writeFileSync('docs/settings-verification.json', JSON.stringify(report, null, 2) + '\n');
  shell('settings', 'put', 'system', 'font_scale', oldFont === 'null' ? '1.0' : oldFont);
  shell('wm', 'density', oldDensity); shell('wm', 'size', oldSize);
  if (!oldImmersive || oldImmersive === 'null') shell('settings', 'delete', 'secure', 'immersive_mode_confirmations');
  else shell('settings', 'put', 'secure', 'immersive_mode_confirmations', oldImmersive);
  if (!launcherWasDisabled) shell('pm', 'enable', '--user', '0', 'com.google.android.apps.nexuslauncher');
}
