// Coloured block light, as Brumaire works it out: light spreads from each source losing one level per block,
// stopped by solid blocks; each cell keeps the colour of its strongest light, and two lights of the same strength
// mix. Brightness stays the game's own. The visitor drags the lights about a ruined cellar.
import { useEffect, useMemo, useRef, useState } from 'react'
import { bruit, hasard, pointeur, useBoucle, useCalme, useToile } from './outils'

const T = 16 // texels per block

interface Source { id: string; nom: string; famille: string; rvb: [number, number, number]; niveau: number; x: number; y: number }

// Colours from Brumaire's voxel table; levels are the game's own (torch 14, soul lantern 10, lichen 7, cluster 5).
const SOURCES: Omit<Source, 'x' | 'y'>[] = [
  { id: 'torche', nom: 'Torche', famille: 'flamme', rvb: [1, 0.6, 0.3], niveau: 14 },
  { id: 'ames', nom: 'Lanterne des âmes', famille: 'âmes', rvb: [0.3, 0.78, 1], niveau: 10 },
  { id: 'lichen', nom: 'Lichen lumineux', famille: 'vert', rvb: [0.62, 1, 0.52], niveau: 7 },
  { id: 'amethyste', nom: "Grappe d'améthyste", famille: 'violet', rvb: [0.78, 0.45, 1], niveau: 5 },
]
const BASE: [number, number, number] = [1, 0.86, 0.66] // the game's own block light, warm and uncoloured

type Bloc = 0 | 1 | 2 | 3 | 4 // air before the back wall, stone, bricks, dirt, moss on dirt
const plein = (b: Bloc) => b !== 0

/** A cellar cut open: ceiling, floor with a step, two pillars with doorways, a back wall of bricks. */
function batir(cols: number, rangs: number): Bloc[][] {
  const g: Bloc[][] = []
  const sol = rangs - 3
  for (let y = 0; y < rangs; y++) {
    const r: Bloc[] = []
    for (let x = 0; x < cols; x++) {
      let b: Bloc = 0
      const plafond = 2 + (hasard(x, 0, 41) > 0.7 ? 1 : 0)
      const marche = x > cols * 0.62 ? 1 : 0
      if (y < plafond) b = 1
      else if (y >= sol - marche) b = y === sol - marche ? 4 : 3
      const p1 = Math.round(cols * 0.36), p2 = Math.round(cols * 0.7)
      if ((x === p1 || x === p2) && y >= plafond && y < sol - marche) {
        // Pillars of bricks with an opening: the low one a doorway, the high one a window.
        const ouvert = x === p1 ? y >= sol - 3 : y >= plafond + 1 && y <= plafond + 2
        if (!ouvert) b = 2
      }
      r.push(b)
    }
    g.push(r)
  }
  return g
}

/** Albedo of one texel, by block. Air shows the back wall, darker. */
function texel(b: Bloc, x: number, y: number): [number, number, number] {
  const n = hasard(x, y, 7), m = bruit(x / 5, y / 5, 8)
  switch (b) {
    case 1: { const v = 96 + n * 22 + m * 26; return [v, v * 0.98, v * 0.95] }
    case 3: { const v = 0.75 + n * 0.25; return [112 * v, 80 * v, 56 * v] }
    case 4: {
      const herbe = (y % T) < 3 + Math.floor(hasard(x, 1, 9) * 2)
      const v = 0.75 + n * 0.25
      return herbe ? [70 * v, 104 * v, 52 * v] : [112 * v, 80 * v, 56 * v]
    }
    default: {
      // Bricks: 8 texels high, 16 long, every other row offset by half; the back wall is the same, in shade.
      const rang = Math.floor(y / 8), dec = rang % 2 ? 8 : 0
      const joint = y % 8 === 7 || (x + dec) % 16 === 15
      const v = joint ? 62 + n * 10 : 104 + n * 18 + m * 16
      const k = b === 2 ? 1 : 0.66
      return [v * k, v * 0.97 * k, v * 0.93 * k]
    }
  }
}

/** Brightness of a light level, as the game's lightmap curves it. */
const eclat = (l: number) => { const v = Math.max(0, l) / 15; return v / (4 - 3 * v) }

export default function Lumiere() {
  const toile = useRef<HTMLCanvasElement>(null)
  const { w, h } = useToile(toile, (l) => Math.max(3, Math.round(l / 380)))
  const calme = useCalme()
  const cols = Math.ceil(w / T), rangs = Math.ceil(h / T)
  const grille = useMemo(() => (cols && rangs ? batir(cols, rangs) : []), [cols, rangs])
  const [sources, setSources] = useState<Source[]>([])
  const [couleur, setCouleur] = useState(true)
  const [prise, setPrise] = useState<string | null>(null)
  const [choisie, setChoisie] = useState('torche')
  const albedo = useRef<Float32Array | null>(null)
  const lumiere = useRef<Float32Array | null>(null) // r, g, b per cell, already curved
  const image = useRef<ImageData | null>(null)

  // Place the lights in the room once its size is known.
  useEffect(() => {
    if (!cols) return
    const sol = rangs - 3
    const places: [number, number][] = [
      [Math.round(cols * 0.5), sol - 3], [Math.round(cols * 0.82), sol - 4],
      [Math.round(cols * 0.56), sol - 1], [Math.round(cols * 0.3), sol - 1],
    ]
    setSources(SOURCES.map((s, i) => ({ ...s, x: places[i][0], y: places[i][1] })))
  }, [cols, rangs])

  // Texture of the whole cellar, once per size.
  useEffect(() => {
    if (!w || !grille.length) return
    const a = new Float32Array(w * h * 3)
    for (let y = 0; y < h; y++) for (let x = 0; x < w; x++) {
      const b = grille[Math.floor(y / T)]?.[Math.floor(x / T)] ?? 1
      const c = texel(b, x, y)
      a.set(c, (y * w + x) * 3)
    }
    albedo.current = a
    image.current = toile.current!.getContext('2d')!.createImageData(w, h)
  }, [w, h, grille])

  // Spread the light: one level lost per block, stopped by solid blocks.
  useEffect(() => {
    if (!grille.length || !sources.length) return
    const n = cols * rangs
    const niveaux = sources.map((s) => {
      const l = new Int8Array(n)
      const file: number[] = []
      const i0 = s.y * cols + s.x
      l[i0] = s.niveau; file.push(i0)
      while (file.length) {
        const i = file.shift()!
        const x = i % cols, y = (i - x) / cols, v = l[i] - 1
        if (v <= 0) continue
        for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
          const nx = x + dx, ny = y + dy
          if (nx < 0 || ny < 0 || nx >= cols || ny >= rangs || plein(grille[ny][nx])) continue
          const j = ny * cols + nx
          if (l[j] < v) { l[j] = v; file.push(j) }
        }
      }
      return l
    })
    const out = new Float32Array(n * 3)
    for (let i = 0; i < n; i++) {
      const x = i % cols, y = (i - x) / cols
      let max = 0
      // A solid block shows the light of the air in front of it, one level down.
      const niveauIci = (k: number) => {
        if (!plein(grille[y][x])) return niveaux[k][i]
        let m = 0
        for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
          const nx = x + dx, ny = y + dy
          if (nx < 0 || ny < 0 || nx >= cols || ny >= rangs || plein(grille[ny][nx])) continue
          m = Math.max(m, niveaux[k][ny * cols + nx] - 1)
        }
        return m
      }
      const ls = sources.map((_, k) => niveauIci(k))
      for (const l of ls) max = Math.max(max, l)
      if (max <= 0) continue
      // The colour of the strongest light; equal ones mix.
      let r = 0, g = 0, b = 0, c = 0
      ls.forEach((l, k) => {
        if (l !== max) return
        const col = couleur ? sources[k].rvb : BASE
        r += col[0]; g += col[1]; b += col[2]; c++
      })
      const e = eclat(max) * 2.3
      out[i * 3] = (r / c) * e; out[i * 3 + 1] = (g / c) * e; out[i * 3 + 2] = (b / c) * e
    }
    lumiere.current = out
  }, [grille, sources, cols, rangs, couleur])

  useBoucle(toile, (_dt, t) => {
    const c = toile.current, a = albedo.current, L = lumiere.current, img = image.current
    if (!c || !a || !L || !img) return
    const px = img.data
    const vacille = calme ? 1 : 0.93 + 0.07 * bruit(t * 7, 0, 3)
    const ambiant = [0.035, 0.04, 0.06]
    for (let y = 0; y < h; y++) {
      const fy = y / T - 0.5, y0 = Math.max(0, Math.floor(fy)), y1 = Math.min(rangs - 1, y0 + 1), ty = Math.min(1, Math.max(0, fy - y0))
      for (let x = 0; x < w; x++) {
        const fx = x / T - 0.5, x0 = Math.max(0, Math.floor(fx)), x1 = Math.min(cols - 1, x0 + 1), tx = Math.min(1, Math.max(0, fx - x0))
        const i00 = (y0 * cols + x0) * 3, i10 = (y0 * cols + x1) * 3, i01 = (y1 * cols + x0) * 3, i11 = (y1 * cols + x1) * 3
        const o = (y * w + x) * 3, p = (y * w + x) * 4
        for (let k = 0; k < 3; k++) {
          const l = (L[i00 + k] * (1 - tx) + L[i10 + k] * tx) * (1 - ty) + (L[i01 + k] * (1 - tx) + L[i11 + k] * tx) * ty
          px[p + k] = a[o + k] * (ambiant[k] + l * vacille)
        }
        px[p + 3] = 255
      }
    }
    // The sources themselves, a few texels each.
    for (const s of sources) {
      const cx = s.x * T + T / 2, cy = s.y * T + T / 2
      const col = couleur ? s.rvb : BASE
      const vif = (k: number) => Math.min(255, 140 + col[k] * 125)
      const poser = (dx: number, dy: number, r: number, g: number, b: number) => {
        const x = Math.round(cx + dx), y = Math.round(cy + dy)
        if (x < 0 || y < 0 || x >= w || y >= h) return
        const p = (y * w + x) * 4
        px[p] = r; px[p + 1] = g; px[p + 2] = b
      }
      if (s.id === 'torche') {
        for (let d = 0; d < 8; d++) poser(0, d - 1, 92, 66, 38)
        poser(0, -2, 255, 236, 160); poser(0, -3, vif(0), vif(1), vif(2)); poser(-1, -2, vif(0), vif(1) * 0.8, vif(2) * 0.6); poser(1, -2, vif(0), vif(1) * 0.8, vif(2) * 0.6)
      } else if (s.id === 'ames') {
        for (let dy = -3; dy <= 3; dy++) for (let dx = -3; dx <= 3; dx++) {
          const bord = Math.abs(dx) === 3 || Math.abs(dy) === 3
          if (bord) poser(dx, dy, 44, 46, 54); else poser(dx, dy, vif(0) * 0.9, vif(1) * 0.95, vif(2))
        }
        poser(0, -4, 44, 46, 54); poser(0, -5, 44, 46, 54)
      } else if (s.id === 'lichen') {
        for (const [dx, dy] of [[-3, -1], [-1, -3], [1, 0], [3, -2], [0, 2], [-2, 2], [2, 3], [-4, 1]]) poser(dx, dy, vif(0) * 0.8, vif(1), vif(2) * 0.8)
      } else {
        for (const [dx, dy, k] of [[0, -4, 1], [0, -3, 1], [0, -2, 0.9], [-1, -2, 0.8], [-2, -1, 0.75], [1, -1, 0.8], [2, -2, 0.9], [2, -3, 1], [0, -1, 0.7], [-1, 0, 0.6], [1, 0, 0.6], [0, 0, 0.6]] as const) {
          poser(dx, dy + 3, vif(0) * k, vif(1) * k, vif(2) * k)
        }
      }
    }
    const ctx = c.getContext('2d')!
    ctx.putImageData(img, 0, 0)
    // A soft glow around each source, as the eye sees a flame in the dark.
    ctx.globalCompositeOperation = 'lighter'
    for (const s of sources) {
      const col = (couleur ? s.rvb : BASE).map((v) => Math.round(v * 255)).join(',')
      const cx = s.x * T + T / 2, cy = s.y * T + T / 2, r = T * (0.6 + s.niveau / 10)
      const g = ctx.createRadialGradient(cx, cy, 0, cx, cy, r)
      g.addColorStop(0, `rgba(${col},${0.22 * vacille})`)
      g.addColorStop(1, `rgba(${col},0)`)
      ctx.fillStyle = g
      ctx.fillRect(cx - r, cy - r, r * 2, r * 2)
    }
    ctx.globalCompositeOperation = 'source-over'
  })

  // Dragging: a light is picked within a block of the pointer and snaps to free cells.
  const cellule = (e: React.PointerEvent) => {
    const p = pointeur(toile.current!, e)
    return { x: Math.floor(p.x / T), y: Math.floor(p.y / T) }
  }
  const deplacer = (id: string, x: number, y: number) => {
    if (x < 0 || y < 0 || x >= cols || y >= rangs || plein(grille[y][x])) return
    setSources((ss) => ss.map((s) => (s.id === id ? { ...s, x, y } : s)))
  }
  const surBas = (e: React.PointerEvent) => {
    const c = cellule(e)
    let mieux: Source | null = null, d = 2.5
    for (const s of sources) { const dd = Math.hypot(s.x - c.x, s.y - c.y); if (dd < d) { d = dd; mieux = s } }
    if (!mieux) return
    ;(e.target as Element).setPointerCapture(e.pointerId)
    setPrise(mieux.id); setChoisie(mieux.id)
  }
  const surBouge = (e: React.PointerEvent) => {
    const c = cellule(e)
    if (prise) { deplacer(prise, c.x, c.y); return }
    const proche = sources.some((s) => Math.hypot(s.x - c.x, s.y - c.y) < 1.6)
    toile.current!.style.cursor = proche ? 'grab' : ''
  }
  const clavier = (e: React.KeyboardEvent) => {
    const s = sources.find((x) => x.id === choisie)
    if (!s) return
    const d = { ArrowLeft: [-1, 0], ArrowRight: [1, 0], ArrowUp: [0, -1], ArrowDown: [0, 1] }[e.key]
    if (!d) return
    e.preventDefault()
    deplacer(s.id, s.x + d[0], s.y + d[1])
  }

  return (
    <div className="scene demo demo-lumiere">
      <canvas
        ref={toile}
        className={`demo-toile pixels${prise ? ' tenue' : ''}`}
        role="img"
        aria-label="Une cave en ruine, de nuit, éclairée par une torche, une lanterne des âmes, du lichen et une grappe d'améthyste"
        onPointerDown={surBas}
        onPointerMove={surBouge}
        onPointerUp={() => setPrise(null)}
        onPointerCancel={() => setPrise(null)}
      />
      <div className="demo-panneau" data-clavier onKeyDown={clavier}>
        <p className="petites-caps">Déplacez les lumières</p>
        <div className="demo-choix" role="group" aria-label="Rendu">
          <button type="button" aria-pressed={couleur} onClick={() => setCouleur(true)}>Brumaire</button>
          <button type="button" aria-pressed={!couleur} onClick={() => setCouleur(false)}>Jeu de base</button>
        </div>
        <ul className="demo-sources" aria-label="Lumières, déplaçables aux flèches du clavier">
          {SOURCES.map((s) => (
            <li key={s.id}>
              <button type="button" aria-pressed={choisie === s.id} onClick={() => setChoisie(s.id)}>
                <span className="demo-pastille" style={{ background: `rgb(${(couleur ? s.rvb : BASE).map((v) => Math.round(v * 255)).join(',')})` }} />
                <span>{s.nom}</span>
                <span className="demo-niveau">{s.niveau}</span>
              </button>
            </li>
          ))}
        </ul>
        <p className="demo-note">
          {couleur
            ? 'Chaque bloc prend la couleur de la lumière la plus forte ; à force égale, elles se mélangent.'
            : 'Sans Brumaire, toute lumière de bloc a la même teinte chaude.'}
        </p>
      </div>
    </div>
  )
}
