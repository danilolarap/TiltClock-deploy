import sharp from 'sharp';
const models = ['mapache', 'moneda', 'pajaro', 'riot'];
const layers = [];
for (const [row, folder] of ['phase5-gpu', 'performance-gpu'].entries()) {
  for (const [column, id] of models.entries()) {
    layers.push({ input: `docs/${folder}/${id}.png`, left: column * 400, top: row * 440 + 40 });
    const label = `${id} · ${row === 0 ? 'original' : 'reloj'}`;
    layers.push({ input: Buffer.from(`<svg width="400" height="40"><text x="12" y="28" font-family="sans-serif" font-size="22">${label}</text></svg>`),
      left: column * 400, top: row * 440 });
  }
}
await sharp({ create: { width: 1600, height: 880, channels: 4, background: '#e2e8f0' } })
  .composite(layers).png().toFile('docs/performance-comparison.png');
