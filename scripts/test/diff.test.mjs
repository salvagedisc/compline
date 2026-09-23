import { test } from 'node:test';
import assert from 'node:assert/strict';
import { diffMods } from '../sync/diff.mjs';

const pack = (key, projectId, versionId) => ({ key, name: key, projectId, versionId });
const inst = (filename, projectId, versionId) => ({ filename, sha512: filename, projectId, versionId, versionNumber: versionId });

test('classifies added, removed, changed, unchanged and unknown', () => {
  const d = diffMods(
    [inst('create.jar', 'P1', 'v2'), inst('sable.jar', 'P2', 'v1'), inst('new.jar', 'P4', 'v1'), inst('manual.jar', null, null)],
    [pack('create', 'P1', 'v1'), pack('sable', 'P2', 'v1'), pack('old', 'P3', 'v1')],
  );
  assert.deepEqual(d.added.map((m) => m.filename), ['new.jar']);
  assert.deepEqual(d.removed.map((m) => m.key), ['old']);
  assert.equal(d.changed.length, 1);
  assert.equal(d.changed[0].from.key, 'create');
  assert.equal(d.changed[0].to.versionId, 'v2');
  assert.deepEqual(d.unchanged.map((m) => m.key), ['sable']);
  assert.deepEqual(d.unknown.map((m) => m.filename), ['manual.jar']);
});

test('pack mods without modrinth id are never marked removed', () => {
  const d = diffMods([], [pack('local', null, null)]);
  assert.deepEqual(d.removed, []);
});

test('server-only pack mods are never marked removed (absent from client instances by design)', () => {
  const d = diffMods([], [{ key: 'srv', name: 'srv', projectId: 'PS', versionId: 'v1', side: 'server' }]);
  assert.deepEqual(d.removed, []);
  assert.deepEqual(d.serverOnly.map((m) => m.key), ['srv']);
});
