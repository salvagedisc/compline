import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const packDir = join(dirname(fileURLToPath(import.meta.url)), '..', '..', 'pack');
const offeringDir = join(packDir, 'kubejs', 'data', 'nomansland', 'nomansland', 'dialogue_pools', 'offering');

async function readOffering(relativePath) {
  return JSON.parse(await readFile(join(offeringDir, relativePath), 'utf8'));
}

// No Man's Land 1.6.1 gates this dialogue on nomansland:tortoise_scute, an item that does
// not exist (its scute is sturdy_scute). Dialogue pools are datapack registries, which
// skip an entry whose neoforge:conditions fail, so the dialogue never loaded.
test('scute offering loads and accepts only scutes that exist', async () => {
  const offering = await readOffering('scute.json');
  assert.equal(offering['neoforge:conditions'], undefined);
  assert.deepEqual(offering.condition.items, ['minecraft:turtle_scute', 'minecraft:armadillo_scute']);
});
