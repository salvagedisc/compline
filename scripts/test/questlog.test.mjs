import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile, readdir } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..');
const questlog = join(root, 'pack', 'config', 'questlog');

async function load(sub) {
  const out = new Map();
  for (const name of await readdir(join(questlog, sub))) {
    if (name.endsWith('.json')) out.set(`questlog:${name.slice(0, -5)}`, JSON.parse(await readFile(join(questlog, sub, name), 'utf8')));
  }
  return out;
}

function refs(objectives = [], out = []) {
  for (const o of objectives) {
    if (o.type === 'questlog:quest_complete') out.push(o.quest);
    refs(o.objectives, out);
    if (o.objective) refs([o.objective], out);
  }
  return out;
}

test('every Tilth quest points to an existing chapter and existing quests', async () => {
  const chapters = await load('chapters');
  const quests = await load('quests');
  assert.ok(quests.size > 0);
  for (const [id, q] of quests) {
    assert.ok(chapters.has(q.chapter), `${id}: chapter ${q.chapter}`);
    assert.ok(q.title && q.description, `${id}: title and description`);
    for (const r of [...refs(q.requirements), ...refs(q.objectives)]) assert.ok(quests.has(r), `${id}: quest ${r}`);
  }
  assert.equal([...chapters.values()].filter((c) => c.default_chapter).length, 1, 'one default chapter');
});
