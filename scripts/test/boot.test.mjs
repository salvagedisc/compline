import { test } from 'node:test';
import assert from 'node:assert/strict';
import { listedItems, unknownItems } from '../boot/ids.mjs';

test('boot: the removal lists, the EMI arrays and both sides of FUSIONS are read', () => {
  const source = [
    "const TRI_OBJETS = ['a:one', 'a:two']",
    "RecipeViewerEvents.removeEntriesCompletely('item', event => {\n  ['b:three'].forEach(id => event.remove(id))\n})",
    "const FUSIONS = {\n  'c:old': 'c:new',\n}",
  ].join('\n');
  assert.deepEqual(listedItems('x.js', source).map((e) => e.id), ['a:one', 'a:two', 'b:three', 'c:old', 'c:new']);
});

test('boot: ids that are not items are reported with the reason', () => {
  const registries = { mods: ['minecraft', 'a'], item: ['a:one'], block: ['a:one', 'a:ore_sample'] };
  const listed = ['a:one', 'a:ore_sample', 'a:nothing', 'z:thing'].map((id) => ({ id, file: 'x.js' }));
  assert.deepEqual(unknownItems(listed, registries).map((e) => `${e.id} ${e.why}`),
    ['a:ore_sample bloc sans objet', 'a:nothing objet inconnu', 'z:thing mod z absent']);
});
