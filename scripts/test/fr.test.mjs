import { test } from 'node:test';
import assert from 'node:assert/strict';
import { compareNamespace, langEntry, parseLang, placeholders } from '../fr/lang.mjs';

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
