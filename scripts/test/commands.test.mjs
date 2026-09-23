import { test } from 'node:test';
import assert from 'node:assert/strict';
import { toPackwizCommands } from '../sync/commands.mjs';

test('builds remove then add commands, pinning exact versions', () => {
  const cmds = toPackwizCommands({
    added: [{ projectId: 'P4', versionId: 'v9' }],
    removed: [{ key: 'old' }],
    changed: [{ from: { key: 'create' }, to: { projectId: 'P1', versionId: 'v2' } }],
    unknown: [{ filename: 'manual.jar' }], unchanged: [],
  });
  assert.deepEqual(cmds, [
    ['remove', 'old'],
    ['remove', 'create'],
    ['-y', 'modrinth', 'add', 'https://modrinth.com/mod/P1/version/v2'],
    ['-y', 'modrinth', 'add', 'https://modrinth.com/mod/P4/version/v9'],
  ]);
});
