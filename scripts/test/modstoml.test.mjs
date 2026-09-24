import { test } from 'node:test';
import assert from 'node:assert/strict';
import { parseModsToml, findMissing } from '../deps/modstoml.mjs';

const TOML = `modLoader="javafml"
[[mods]]
modId="icys_better_horses"
version="2.0.1"

[[dependencies.icys_better_horses]]
    modId="neoforge"
    type="required"
    versionRange="[21.1,)"
[[dependencies.icys_better_horses]]
    modId="modonomicon"
    type="required"
    versionRange="[1.120.4,)"
[[dependencies.icys_better_horses]]
    modId="cloth_config"
    type="optional"
`;

test('parseModsToml reads provided mod ids and dependencies with their type', () => {
  const r = parseModsToml(TOML);
  assert.deepEqual(r.mods, ['icys_better_horses']);
  assert.deepEqual(r.deps.map((d) => [d.dep, d.type, d.range]), [
    ['neoforge', 'required', '[21.1,)'],
    ['modonomicon', 'required', '[1.120.4,)'],
    ['cloth_config', 'optional', undefined],
  ]);
});

test('parseModsToml maps legacy mandatory=true to required', () => {
  const r = parseModsToml('[[mods]]\nmodId="a"\n[[dependencies.a]]\nmodId="b"\nmandatory=true\n');
  assert.equal(r.deps[0].type, 'required');
});

test('parseModsToml ignores dependency modIds when listing provided mods', () => {
  const r = parseModsToml(TOML);
  assert.equal(r.mods.includes('modonomicon'), false);
});

test('findMissing reports only required deps that are neither built in nor provided', () => {
  const missing = findMissing(new Set(['icys_better_horses']), [
    { jar: 'h.jar', dep: 'neoforge', type: 'required' },
    { jar: 'h.jar', dep: 'minecraft', type: 'required' },
    { jar: 'h.jar', dep: 'modonomicon', type: 'required', range: '[1,)' },
    { jar: 'h.jar', dep: 'cloth_config', type: 'optional' },
  ]);
  assert.deepEqual(missing.map((m) => m.dep), ['modonomicon']);
});
