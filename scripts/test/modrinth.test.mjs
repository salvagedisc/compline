import { test } from 'node:test';
import assert from 'node:assert/strict';
import { lookupVersions, lookupProjects } from '../sync/modrinth.mjs';

const fakeFetch = (status, body, calls = []) => async (url, init) => {
  calls.push({ url, init });
  return { ok: status < 300, status, json: async () => body };
};

test('lookupVersions posts sha512 hashes and maps the response', async () => {
  const calls = [];
  const fetch = fakeFetch(200, { h1: { id: 'v1', project_id: 'p1', version_number: '1.0' } }, calls);
  const map = await lookupVersions(['h1', 'h2'], { fetch });
  assert.equal(calls[0].url, 'https://api.modrinth.com/v2/version_files');
  assert.deepEqual(JSON.parse(calls[0].init.body), { hashes: ['h1', 'h2'], algorithm: 'sha512' });
  assert.match(calls[0].init.headers['User-Agent'], /tilth/);
  assert.deepEqual(map.get('h1'), { projectId: 'p1', versionId: 'v1', versionNumber: '1.0' });
  assert.equal(map.has('h2'), false);
});

test('lookupVersions throws on HTTP error', async () => {
  await assert.rejects(lookupVersions(['h1'], { fetch: fakeFetch(503, {}) }), /Modrinth API 503/);
});

test('lookupProjects maps ids to slug/title', async () => {
  const map = await lookupProjects(['p1'], { fetch: fakeFetch(200, [{ id: 'p1', slug: 'create', title: 'Create' }]) });
  assert.deepEqual(map.get('p1'), { slug: 'create', title: 'Create' });
});
