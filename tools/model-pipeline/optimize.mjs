import { NodeIO } from '@gltf-transform/core';
import { ALL_EXTENSIONS } from '@gltf-transform/extensions';
import { weld, simplifyPrimitive, prune, dedup, textureCompress } from '@gltf-transform/functions';
import { MeshoptSimplifier } from 'meshoptimizer';
import draco from 'draco3dgltf';
import sharp from 'sharp';
import fs from 'node:fs/promises';
import path from 'node:path';
import assert from 'node:assert/strict';

const source = process.argv[2];
assert(source, 'Pass the original desktop models directory as the first argument.');
const destination = path.resolve('build/watch-models');
assert.notEqual(path.resolve(source), destination);
await fs.mkdir(destination, { recursive: true });
await MeshoptSimplifier.ready;
const io = new NodeIO().registerExtensions(ALL_EXTENSIONS)
  .registerDependencies({ 'draco3d.decoder': await draco.createDecoderModule() });
function stats(doc) {
  const root = doc.getRoot();
  return {
    triangles: root.listMeshes().flatMap(m => m.listPrimitives()).reduce((n, p) => n + (p.getIndices()?.getCount() ?? p.getAttribute('POSITION').getCount()) / 3, 0),
    textures: root.listTextures().map(t => t.getSize()),
    animations: root.listAnimations().length,
  };
}
const report = [];
for (const id of ['mapache', 'moneda', 'pajaro', 'riot']) {
  const doc = await io.read(path.join(source, `${id}.glb`));
  const before = stats(doc);
  // Decode once at build time. The watch receives ordinary glTF buffers and PNG textures.
  for (const extension of doc.getRoot().listExtensionsUsed()) {
    assert(['KHR_draco_mesh_compression', 'EXT_texture_webp'].includes(extension.extensionName));
    extension.dispose();
  }
  await doc.transform(weld());
  for (const primitive of doc.getRoot().listMeshes().flatMap(mesh => mesh.listPrimitives())) {
    const normal = primitive.getAttribute('NORMAL');
    const uv = primitive.getAttribute('TEXCOORD_0');
    assert(normal && uv);
    const attributes = new Float32Array(normal.getCount() * 5);
    const n = [], t = [];
    for (let i = 0; i < normal.getCount(); i++) {
      normal.getElement(i, n); uv.getElement(i, t);
      attributes.set([...n, ...t], i * 5);
    }
    const attributeAware = { simplify: (indices, positions, stride, target, error, flags) =>
      MeshoptSimplifier.simplifyWithAttributes(indices, positions, stride, attributes, 5,
        [1, 1, 1, .1, .1], null, target, error, flags) };
    simplifyPrimitive(primitive, { simplifier: attributeAware,
      ratio: Math.min(1, (id === 'moneda' ? 200000 : 45000) / before.triangles), error: id === 'moneda' ? .001 : .005 });
  }
  await doc.transform(
    textureCompress({ encoder: (image, options) => sharp(image, options).toColourspace('srgb'),
      targetFormat: 'png', resize: [512, 512] }), dedup(), prune());
  const output = path.join(destination, `${id}.glb`);
  await io.write(output, doc);
  const reloaded = await io.read(output);
  const after = stats(reloaded);
  assert(after.triangles > 0 && after.triangles < before.triangles * .25, `${id}: insufficient simplification`);
  assert.equal(after.animations, before.animations);
  assert(after.textures.every(([w, h]) => w <= 512 && h <= 512));
  assert.equal(reloaded.getRoot().listExtensionsRequired().length, 0);
  report.push({ id, before, after, bytes: (await fs.stat(output)).size });
  console.log(JSON.stringify(report.at(-1)));
}
await fs.writeFile('docs/watch-model-optimization.json', JSON.stringify(report, null, 2) + '\n');

