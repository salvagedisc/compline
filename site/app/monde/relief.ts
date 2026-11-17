// Builds the voxel relief of the disc from the bench sample (see scripts/relief.py).
// Scene units: one unit is 100 blocks across; heights are exaggerated so the relief reads from far away.

import { BufferAttribute, BufferGeometry } from 'three'

export const N = 259 // cells per side
export const PAS = 0.48 // cell size in scene units (3 samples of 16 blocks)
export const DEMI = 62 // the grid spans -62..62 units (-6200..6200 blocks)
export const BORD = 60 // the edge of the world, 6000 blocks
export const MER = 63 // sea level in blocks
export const ETAGE = 0.04 // scene units per block of height
const MARCHE = 5 // heights are rounded to steps of this many blocks, for the terraced look
const FOND = -3 // where walls stop when a cell has no neighbour

export interface Relief {
  hauteurs: Float32Array // top of each cell in scene units, NaN where there is nothing
  couleurs: Uint8Array // RGB per cell
}

export async function chargerRelief(url: string): Promise<Relief> {
  const brut = new Uint8Array(await (await fetch(url)).arrayBuffer())
  const hauteurs = new Float32Array(N * N)
  const couleurs = new Uint8Array(N * N * 3)
  for (let k = 0; k < N * N; k++) {
    const code = brut[k * 4]
    if (code === 0) { hauteurs[k] = NaN; continue }
    const bloc = (code - 1) * 1.5 - 64
    const marche = Math.round(bloc / MARCHE) * MARCHE
    hauteurs[k] = (marche - MER) * ETAGE
    couleurs.set(brut.subarray(k * 4 + 1, k * 4 + 4), k * 3)
  }
  return { hauteurs, couleurs }
}

/** World position of a map fraction (0..1 across the map image, as the places are stored). */
export const versMonde = (fx: number, fy: number) => ({ x: (fx - 0.5) * DEMI * 2, z: (fy - 0.5) * DEMI * 2 })

/** Top of the relief at a world position, or sea level over water. */
export function hauteurEn(r: Relief, x: number, z: number) {
  const i = Math.round((x + DEMI) / PAS), j = Math.round((z + DEMI) / PAS)
  if (i < 0 || j < 0 || i >= N || j >= N) return 0
  const h = r.hauteurs[j * N + i]
  return Number.isNaN(h) ? 0 : Math.max(0, h)
}

// Small stable noise per cell, so neighbouring blocks of one biome are not one flat colour.
const bruit = (i: number, j: number) => {
  const s = Math.sin(i * 127.1 + j * 311.7) * 43758.5453
  return s - Math.floor(s)
}

// The four side walls of a cell: neighbour offset, outward normal, and the two corners of the wall's
// top edge as (x, z) picks from the cell's [x0, x1] and [z0, z1], wound so the wall faces outwards.
const MURS: { di: number; dj: number; n: [number, number, number]; p: [number, number]; q: [number, number] }[] = [
  { di: 1, dj: 0, n: [1, 0, 0], p: [1, 1], q: [1, 0] },
  { di: -1, dj: 0, n: [-1, 0, 0], p: [0, 0], q: [0, 1] },
  { di: 0, dj: 1, n: [0, 0, 1], p: [0, 1], q: [1, 1] },
  { di: 0, dj: -1, n: [0, 0, -1], p: [1, 0], q: [0, 0] },
]

/**
 * Builds one indexed mesh for the whole relief: a top face per cell and a darker wall towards each lower
 * neighbour. Typed arrays are sized in a first pass, normals and colours are stored as normalised bytes,
 * so the geometry costs about a third of the memory of plain float triangles, which matters on phones.
 */
export function construireRelief(r: Relief): BufferGeometry {
  const h = (i: number, j: number) => (i < 0 || j < 0 || i >= N || j >= N ? NaN : r.hauteurs[j * N + i])
  const basVoisin = (i: number, j: number) => { const y = h(i, j); return Number.isNaN(y) ? FOND : y }

  let quads = 0
  for (let j = 0; j < N; j++) {
    for (let i = 0; i < N; i++) {
      const y = h(i, j)
      if (Number.isNaN(y)) continue
      quads++
      for (const m of MURS) if (basVoisin(i + m.di, j + m.dj) < y) quads++
    }
  }

  const pos = new Float32Array(quads * 12)
  const nor = new Int8Array(quads * 12)
  const col = new Uint8Array(quads * 12)
  const idx = new Uint32Array(quads * 6)
  let q = 0
  const quad = (coins: number[], n: readonly number[], rgb: readonly number[]) => {
    const v = q * 4
    for (let c = 0; c < 4; c++) {
      pos.set(coins.slice(c * 3, c * 3 + 3), (v + c) * 3)
      nor.set([n[0] * 127, n[1] * 127, n[2] * 127], (v + c) * 3)
      col.set(rgb, (v + c) * 3)
    }
    idx.set([v, v + 1, v + 2, v, v + 2, v + 3], q * 6)
    q++
  }

  for (let j = 0; j < N; j++) {
    for (let i = 0; i < N; i++) {
      const y = h(i, j)
      if (Number.isNaN(y)) continue
      const k = (j * N + i) * 3
      const v = 0.94 + bruit(i, j) * 0.12
      const rgb = [0, 1, 2].map((c) => Math.min(255, Math.round(r.couleurs[k + c] * v)))
      const xs = [-DEMI + i * PAS - PAS / 2, -DEMI + i * PAS + PAS / 2]
      const zs = [-DEMI + j * PAS - PAS / 2, -DEMI + j * PAS + PAS / 2]
      quad([xs[0], y, zs[0], xs[0], y, zs[1], xs[1], y, zs[1], xs[1], y, zs[0]], [0, 1, 0], rgb)
      // Walls down to each lower neighbour, a little darker like the side of a block.
      const cote = rgb.map((c) => Math.round(c * 0.78))
      for (const m of MURS) {
        const bas = basVoisin(i + m.di, j + m.dj)
        if (bas >= y) continue
        const px = xs[m.p[0]], pz = zs[m.p[1]], qx = xs[m.q[0]], qz = zs[m.q[1]]
        quad([qx, bas, qz, qx, y, qz, px, y, pz, px, bas, pz], m.n, cote)
      }
    }
  }
  const g = new BufferGeometry()
  g.setAttribute('position', new BufferAttribute(pos, 3))
  g.setAttribute('normal', new BufferAttribute(nor, 3, true))
  g.setAttribute('color', new BufferAttribute(col, 3, true))
  g.setIndex(new BufferAttribute(idx, 1))
  g.computeBoundingSphere()
  return g
}
