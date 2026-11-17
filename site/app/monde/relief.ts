// Builds the voxel relief of the disc from the bench sample (see scripts/relief.py).
// Scene units: one unit is 100 blocks across; heights are exaggerated so the relief reads from far away.

import { BufferGeometry, Float32BufferAttribute } from 'three'

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

export function construireRelief(r: Relief): BufferGeometry {
  const pos: number[] = [], nor: number[] = [], col: number[] = []
  const quad = (
    a: number[], b: number[], c: number[], d: number[],
    n: number[], rgb: number[],
  ) => {
    for (const p of [a, b, c, a, c, d]) { pos.push(p[0], p[1], p[2]); nor.push(n[0], n[1], n[2]); col.push(rgb[0], rgb[1], rgb[2]) }
  }
  const h = (i: number, j: number) => (i < 0 || j < 0 || i >= N || j >= N ? NaN : r.hauteurs[j * N + i])

  for (let j = 0; j < N; j++) {
    for (let i = 0; i < N; i++) {
      const y = h(i, j)
      if (Number.isNaN(y)) continue
      const k = (j * N + i) * 3
      const v = 0.94 + bruit(i, j) * 0.12
      const rgb = [r.couleurs[k] / 255 * v, r.couleurs[k + 1] / 255 * v, r.couleurs[k + 2] / 255 * v]
      const x0 = -DEMI + i * PAS - PAS / 2, x1 = x0 + PAS
      const z0 = -DEMI + j * PAS - PAS / 2, z1 = z0 + PAS
      quad([x0, y, z0], [x0, y, z1], [x1, y, z1], [x1, y, z0], [0, 1, 0], rgb)
      // Walls down to each lower neighbour, a little darker like the side of a block.
      const cote = rgb.map((c) => c * 0.78)
      const murs: [number, number, number[], number[][]][] = [
        [i + 1, j, [1, 0, 0], [[x1, 0, z1], [x1, 0, z0]]],
        [i - 1, j, [-1, 0, 0], [[x0, 0, z0], [x0, 0, z1]]],
        [i, j + 1, [0, 0, 1], [[x0, 0, z1], [x1, 0, z1]]],
        [i, j - 1, [0, 0, -1], [[x1, 0, z0], [x0, 0, z0]]],
      ]
      for (const [ni, nj, n, [p, q]] of murs) {
        const yn = h(ni, nj)
        const bas = Number.isNaN(yn) ? FOND : yn
        if (bas >= y) continue
        quad([q[0], bas, q[2]], [q[0], y, q[2]], [p[0], y, p[2]], [p[0], bas, p[2]], n, cote)
      }
    }
  }
  const g = new BufferGeometry()
  g.setAttribute('position', new Float32BufferAttribute(pos, 3))
  g.setAttribute('normal', new Float32BufferAttribute(nor, 3))
  g.setAttribute('color', new Float32BufferAttribute(col, 3))
  g.computeBoundingSphere()
  return g
}
