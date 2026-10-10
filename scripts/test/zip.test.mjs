import { test } from 'node:test';
import assert from 'node:assert/strict';
import { deflateRawSync } from 'node:zlib';
import { listEntries, readEntry } from '../deps/zip.mjs';

// Builds a minimal zip archive (one local header + central directory entry per file).
function makeZip(files) {
  const locals = [];
  const centrals = [];
  let offset = 0;
  for (const { name, data, deflate } of files) {
    const raw = Buffer.from(data);
    const body = deflate ? deflateRawSync(raw) : raw;
    const nameBuf = Buffer.from(name);
    const local = Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50, 0);
    local.writeUInt16LE(deflate ? 8 : 0, 8);
    local.writeUInt32LE(body.length, 18);
    local.writeUInt32LE(raw.length, 22);
    local.writeUInt16LE(nameBuf.length, 26);
    const central = Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50, 0);
    central.writeUInt16LE(deflate ? 8 : 0, 10);
    central.writeUInt32LE(body.length, 20);
    central.writeUInt32LE(raw.length, 24);
    central.writeUInt16LE(nameBuf.length, 28);
    central.writeUInt32LE(offset, 42);
    locals.push(local, nameBuf, body);
    centrals.push(central, nameBuf);
    offset += local.length + nameBuf.length + body.length;
  }
  const cd = Buffer.concat(centrals);
  const end = Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50, 0);
  end.writeUInt16LE(files.length, 8);
  end.writeUInt16LE(files.length, 10);
  end.writeUInt32LE(cd.length, 12);
  end.writeUInt32LE(offset, 16);
  return Buffer.concat([...locals, cd, end]);
}

test('listEntries returns every file name in the archive', () => {
  const zip = makeZip([{ name: 'a.txt', data: 'A' }, { name: 'META-INF/jarjar/x.jar', data: 'X' }]);
  assert.deepEqual(listEntries(zip), ['a.txt', 'META-INF/jarjar/x.jar']);
});

test('readEntry returns stored and deflated contents, null when absent', () => {
  const zip = makeZip([
    { name: 'stored.txt', data: 'hello' },
    { name: 'META-INF/neoforge.mods.toml', data: 'modId="x"\n'.repeat(20), deflate: true },
  ]);
  assert.equal(readEntry(zip, 'stored.txt').toString(), 'hello');
  assert.equal(readEntry(zip, 'META-INF/neoforge.mods.toml').toString(), 'modId="x"\n'.repeat(20));
  assert.equal(readEntry(zip, 'missing'), null);
});
