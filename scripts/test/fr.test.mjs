import { test } from 'node:test';
import assert from 'node:assert/strict';
import {
  compareDialogues, compareNamespace, dialogueDataEntry, dialogueLangEntry, langEntry, parseLang, placeholders,
} from '../fr/lang.mjs';

test('parseLang ignore les commentaires et renvoie null sur un JSON invalide', () => {
  assert.deepEqual(parseLang('{\n  // note\n  "a": "b"\n}'), { a: 'b' });
  assert.equal(parseLang('{"a": '), null);
});

test('langEntry reconnaît les fichiers en_us et fr_fr', () => {
  assert.deepEqual(langEntry('assets/quark/lang/fr_fr.json'), { namespace: 'quark', locale: 'fr_fr' });
  assert.equal(langEntry('assets/quark/lang/de_de.json'), null);
});

test('placeholders trie les variables', () => {
  assert.deepEqual(placeholders('§a%2$s puis %1$s {0}'), ['%1$s', '%2$s', '{0}', '§a']);
});

test('compareNamespace signale variables, clés inconnues et textes manquants', () => {
  const en = { a: 'Hello %s', b: 'Bye', c: 'Tree' };
  const result = compareNamespace(en, { c: 'Arbre' }, { a: 'Bonjour', z: 'x' });
  assert.deepEqual(result, { unknown: ['z'], badPlaceholders: ['a'], missing: ['b'] });
});

test('dialogueLangEntry et dialogueDataEntry reconnaissent les dialogues de la Lune Amie', () => {
  assert.deepEqual(dialogueLangEntry('assets/nomansland/lang/nomansland/dialogue_pools/offering/fr_fr.json'),
    { pool: 'offering', locale: 'fr_fr' });
  assert.equal(dialogueLangEntry('assets/nomansland/lang/nomansland/dialogue_pools/offering/it_it.json'), null);
  assert.equal(langEntry('assets/nomansland/lang/nomansland/dialogue_pools/offering/fr_fr.json'), null);
  assert.deepEqual(dialogueDataEntry('data/nomansland/nomansland/dialogue_pools/offering/entity/goose.json'),
    { pool: 'offering', key: 'entity.goose' });
  // Condition types live in a code registry, not a dialogue pool.
  assert.equal(dialogueDataEntry('data/nomansland/nomansland/dialogue_pools/conditional/x.json'), null);
});

test('compareDialogues signale les dialogues sans français et le nom du joueur perdu', () => {
  const en = { a: 'Hello, &PlayerName', b: 'Goodbye', c: '...', d: '', e: 'Moon' };
  const result = compareDialogues(en, { a: 'Bonjour', e: 'Lune' }, { b: 'Au revoir' });
  assert.deepEqual(result, { missing: [], badPlaceholders: ['a'] });
  assert.deepEqual(compareDialogues(en, {}, {}).missing, ['a', 'b', 'e']);
});
