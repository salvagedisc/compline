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

// The NML goose and tortoise no longer spawn (docs/plan/faune-nml.md): their offering
// dialogues must react to a creature that is still in the world.
test('goose and tortoise offerings target creatures that still spawn', async () => {
  const wildex = await readFile(join(packDir, 'config', 'wildex', 'wildex-common.toml'), 'utf8');
  const excluded = JSON.parse(wildex.match(/excludedModIds = (\[[^\]]*\])/)[1]);
  for (const name of ['goose', 'tortoise']) {
    const offering = await readOffering(join('entity', `${name}.json`));
    const entity = offering.condition.entity;
    assert.ok(!entity.startsWith('nomansland:'), `${name}: ${entity}`);
    assert.ok(!excluded.includes(entity), `${name}: ${entity} is excluded from the Wildex`);
  }
});
