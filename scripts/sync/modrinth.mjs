const API = 'https://api.modrinth.com/v2';
const HEADERS = { 'User-Agent': 'salvagedisc/tilth sync-from-instance', 'Content-Type': 'application/json' };

async function call(fetch, url, init) {
  const res = await fetch(url, { ...init, headers: HEADERS });
  if (!res.ok) throw new Error(`Modrinth API ${res.status} on ${url}`);
  return res.json();
}

export async function lookupVersions(sha512s, { fetch = globalThis.fetch } = {}) {
  const body = await call(fetch, `${API}/version_files`, {
    method: 'POST', body: JSON.stringify({ hashes: sha512s, algorithm: 'sha512' }),
  });
  return new Map(Object.entries(body).map(([hash, v]) => [hash, { projectId: v.project_id, versionId: v.id, versionNumber: v.version_number }]));
}

export async function lookupProjects(ids, { fetch = globalThis.fetch } = {}) {
  if (ids.length === 0) return new Map();
  const body = await call(fetch, `${API}/projects?ids=${encodeURIComponent(JSON.stringify(ids))}`, { method: 'GET' });
  return new Map(body.map((p) => [p.id, { slug: p.slug, title: p.title }]));
}
