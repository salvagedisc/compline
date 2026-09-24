import { inflateRawSync } from 'node:zlib';

// Minimal zip reader (central directory, stored + deflate). Enough for reading metadata out of mod jars.
function centralEntries(buf) {
  let eocd = -1;
  for (let i = buf.length - 22; i >= Math.max(0, buf.length - 65557); i--) {
    if (buf.readUInt32LE(i) === 0x06054b50) { eocd = i; break; }
  }
  if (eocd < 0) throw new Error('not a zip archive');
  const count = buf.readUInt16LE(eocd + 10);
  let p = buf.readUInt32LE(eocd + 16);
  const entries = [];
  for (let n = 0; n < count; n++) {
    const nameLen = buf.readUInt16LE(p + 28);
    const extraLen = buf.readUInt16LE(p + 30);
    const commentLen = buf.readUInt16LE(p + 32);
    entries.push({
      name: buf.toString('utf8', p + 46, p + 46 + nameLen),
      method: buf.readUInt16LE(p + 10),
      compressedSize: buf.readUInt32LE(p + 20),
      localOffset: buf.readUInt32LE(p + 42),
    });
    p += 46 + nameLen + extraLen + commentLen;
  }
  return entries;
}

export function listEntries(buf) {
  return centralEntries(buf).map((e) => e.name);
}

export function readEntry(buf, name) {
  const e = centralEntries(buf).find((x) => x.name === name);
  if (!e) return null;
  const start = e.localOffset + 30 + buf.readUInt16LE(e.localOffset + 26) + buf.readUInt16LE(e.localOffset + 28);
  const data = buf.subarray(start, start + e.compressedSize);
  if (e.method === 0) return Buffer.from(data);
  if (e.method === 8) return inflateRawSync(data);
  throw new Error(`unsupported compression method ${e.method} for ${name}`);
}
