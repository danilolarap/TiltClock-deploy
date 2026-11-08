import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import assert from 'node:assert/strict';

const source = path.resolve(process.argv.slice(2).find(arg => !arg.startsWith('--')) ?? 'C:/Users/Danilo/Documents/TiltClock');
const output = path.resolve('docs');
assert.notEqual(source, process.cwd());
assert(!output.startsWith(source + path.sep), 'Outputs must never be inside the reference');
fs.mkdirSync(output, { recursive: true });
const sha = bytes => crypto.createHash('sha256').update(bytes).digest('hex');
function walk(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(e => {
    if (e.name === '.git') return [];
    const p = path.join(dir, e.name);
    return e.isDirectory() ? walk(p) : [p];
  });
}
const files = walk(source).map(p => {
  const data = fs.readFileSync(p);
  return { file: path.relative(source, p).replaceAll('\\', '/'), bytes: data.length, sha256: sha(data) };
}).sort((a,b) => a.file.localeCompare(b.file));
const manifestPath = path.join(output, 'reference-sha256.json');
if (process.argv.includes('--verify')) {
  assert.deepEqual(files, JSON.parse(fs.readFileSync(manifestPath)).files, 'Original reference changed');
  console.log(`PASS: ${files.length} source files unchanged (excluding .git).`);
} else {
  assert(!fs.existsSync(manifestPath), 'Baseline already exists; use --verify to protect it');
  fs.writeFileSync(manifestPath, JSON.stringify({ source, capturedAt: new Date().toISOString(), files }, null, 2));
}

const inventory = files.filter(f => f.file.endsWith('.glb')).map(f => {
  const bytes = fs.readFileSync(path.join(source, f.file));
  assert.equal(bytes.toString('ascii', 0, 4), 'glTF');
  assert.equal(bytes.readUInt32LE(4), 2);
  assert.equal(bytes.readUInt32LE(8), bytes.length);
  assert.equal(bytes.readUInt32LE(16), 0x4e4f534a);
  const gltf = JSON.parse(bytes.toString('utf8', 20, 20 + bytes.readUInt32LE(12)).trim());
  let triangles = 0, vertices = 0;
  for (const mesh of gltf.meshes ?? []) for (const primitive of mesh.primitives) {
    const count = gltf.accessors?.[primitive.indices]?.count ?? gltf.accessors?.[primitive.attributes?.POSITION]?.count ?? 0;
    vertices += gltf.accessors?.[primitive.attributes?.POSITION]?.count ?? 0;
    if ((primitive.mode ?? 4) === 4) triangles += count / 3;
  }
  const dest = path.join('app/src/main/assets/models', path.basename(f.file));
  assert.equal(sha(fs.readFileSync(dest)), f.sha256, 'Copied model differs');
  const report = {
    ...f, gltfVersion: gltf.asset.version, generator: gltf.asset.generator,
    extensionsUsed: gltf.extensionsUsed ?? [], extensionsRequired: gltf.extensionsRequired ?? [],
    draco: (gltf.meshes ?? []).some(m => m.primitives.some(p => p.extensions?.KHR_draco_mesh_compression)),
    meshes: gltf.meshes?.length ?? 0, nodes: gltf.nodes?.length ?? 0,
    vertices, triangles, materials: gltf.materials?.length ?? 0,
    images: (gltf.images ?? []).map(i => ({ mimeType: i.mimeType, embedded: i.bufferView !== undefined || i.uri?.startsWith('data:') })),
    animations: (gltf.animations ?? []).map(a => ({ name: a.name ?? '(unnamed)', channels: a.channels.length })),
    skins: gltf.skins?.length ?? 0,
    externalUris: [...(gltf.buffers ?? []), ...(gltf.images ?? [])].map(i=>i.uri).filter(u=>u && !u.startsWith('data:')),
    validation: 'GLB structure + copy hash only. Not a Khronos validator or GPU load test.',
  };
  return report;
});
fs.writeFileSync(path.join(output, 'models-inventory.json'), JSON.stringify(inventory, null, 2));
console.log(JSON.stringify(inventory.map(m=>({name:m.file,bytes:m.bytes,draco:m.draco,extensions:m.extensionsUsed,triangles:m.triangles,animations:m.animations.length,external:m.externalUris})),null,2));
