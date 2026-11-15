import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';

const read = (path) => JSON.parse(readFileSync(new URL(`../../${path}`, import.meta.url), 'utf8'));
const ids = (tag) => tag.values.map((value) => (typeof value === 'string' ? value : value.id));
const TAGS = 'pack/kubejs/data/compline/tags/entity_type/zoo';

test('zoo: every Overworld mob that In Control refuses is absent from the zoo list', () => {
  const rules = read('pack/config/incontrol/spawn.json');
  const denied = rules
    .filter((rule) => rule.result === 'deny' && rule.dimension === 'minecraft:overworld' && Array.isArray(rule.mob))
    .flatMap((rule) => rule.mob);
  assert.ok(denied.length > 0);
  const absent = new Set(ids(read(`${TAGS}/absents.json`)));
  assert.deepEqual(denied.filter((mob) => !absent.has(mob)), []);
});

test('zoo: the dinosaurs keep their own stamp page, out of the zoo list', () => {
  const excluded = new Set(ids(read(`${TAGS}/exclus.json`)));
  for (const dino of ['lushsaur', 'therimoss', 'coralosaur', 'obsidiceratops', 'sculkraptor', 'chorusoatl', 'sculksaur']) {
    assert.ok(excluded.has(`ec_ad:${dino}`), dino);
  }
});

test('zoo: the guidebook text patches have a French and an English file with the same targets', () => {
  const dir = 'mods-custom/compline/src/main/resources/assets/compline/archie/guide';
  const targets = (file) => Object.entries(read(`${dir}/${file}`))
    .filter(([key]) => key !== '_')
    .flatMap(([category, patches]) => patches.map((p) => `${category}/${p.page}/${p.label}`));
  const fr = targets('fr_fr.json');
  const en = new Set(targets('en_us.json'));
  assert.deepEqual(fr.filter((t) => !en.has(t)), []);
});
