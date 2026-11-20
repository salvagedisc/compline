// Footprints, as Brumaire draws them: one print every 0.7 block, left then right, 4 texels wide and wider at the
// toe, turned the way of the walk, darker in the hollow (blue on snow, brown on sand), gone after four minutes.
// The visitor walks with the pointer; a fox wanders by on its own, as the four nearest creatures do in the game.
import { useEffect, useRef, useState } from 'react'
import { bruit, hasard, lisse, nombre, pointeur, useBoucle, useCalme, useToile } from './outils'

type Sol = 'neige' | 'sable'
interface Empreinte { x: number; y: number; dx: number; dy: number; nee: number; patte: boolean }

const TEXELS = 16 // texels per block, the game's grid
const PAS = 0.7 * TEXELS // one print every 0.7 block
const MINUTES = 4 // TRACK_MINUTES, the default
const ACCELERE = 20 // shown 20 times faster: 12 s here for 4 min in the game

const SOLS: Record<Sol, { fond: [number, number, number]; creux: [number, number, number]; grain: number }> = {
  neige: { fond: [244, 248, 252], creux: [124, 146, 182], grain: 0.045 },
  sable: { fond: [219, 203, 158], creux: [139, 108, 70], grain: 0.09 },
}

/** The ground, drawn once per size: the game's block grid of texels, with soft drifts. */
function peindreSol(w: number, h: number, sol: Sol) {
  const { fond, grain } = SOLS[sol]
  const d = new Uint8ClampedArray(w * h * 4)
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const bloc = hasard(Math.floor(x / TEXELS), Math.floor(y / TEXELS), 3) * 0.02
      const t = hasard(x % TEXELS, y % TEXELS, sol === 'neige' ? 11 : 12) // the same texture repeats in every block
      const amas = bruit(x / 46, y / 30, 5) * 0.06 + bruit(x / 14, y / 11, 6) * 0.025
      let k = 1 - grain + t * grain * 1.6 - amas - bloc
      if (sol === 'sable' && t > 0.93) k -= 0.12 // darker grains
      const i = (y * w + x) * 4
      d[i] = fond[0] * k; d[i + 1] = fond[1] * k; d[i + 2] = fond[2] * (sol === 'neige' ? k * 0.98 + 0.02 : k); d[i + 3] = 255
    }
  }
  return d
}

/** Depth of a print at local coordinates (u along the walk, v across), 0 outside. */
function creuxBotte(u: number, v: number) {
  if (u < -3.6 || u > 3.6) return 0
  const demi = u > 0.4 ? 2.05 : u > -1 ? 1.3 : 1.7 // toe, arch, heel
  const a = Math.abs(v)
  if (a > demi) return 0
  return 0.65 + 0.35 * (1 - a / demi)
}
function creuxPatte(u: number, v: number) {
  if (u * u + v * v < 1.3) return 1 // the pad
  for (const [pu, pv] of [[1.9, -0.9], [2.3, 0.1], [1.9, 1.1]]) if ((u - pu) ** 2 + (v - pv) ** 2 < 0.36) return 0.85
  return 0
}

/** The fox seen from above, as a union of shapes in its own frame (u forward). */
function renard(u: number, v: number): [number, number, number] | null {
  const e = (cu: number, cv: number, ru: number, rv: number) => ((u - cu) / ru) ** 2 + ((v - cv) / rv) ** 2 < 1
  if (e(6.1, 0, 0.8, 0.6)) return [40, 26, 20]
  if (e(3.9, -1.3, 0.7, 0.6) || e(3.9, 1.3, 0.7, 0.6)) return [64, 38, 24]
  if (e(4.6, 0, 1.7, 1.4)) return [204, 112, 52]
  if (e(-7.3, 0, 1.1, 0.95)) return [238, 234, 226]
  if (e(-4.9, 0, 2.8, 1.2)) return [186, 96, 44]
  if (e(0.4, 0, 3.6, 1.7)) return u > 0.2 && Math.abs(v) < 0.6 ? [214, 124, 62] : [196, 104, 48]
  return null
}

export default function Traces() {
  const toile = useRef<HTMLCanvasElement>(null)
  const { w, h } = useToile(toile, (l) => Math.max(3, Math.round(l / 340)))
  const calme = useCalme()
  const [sol, setSol] = useState<Sol>('neige')
  const [compte, setCompte] = useState(0)
  const etat = useRef({
    base: null as Uint8ClampedArray | null,
    image: null as ImageData | null,
    empreintes: [] as Empreinte[],
    marcheur: null as null | { x: number; y: number; dx: number; dy: number; reste: number; cote: number },
    bete: { x: 0, y: 0, cap: 0.3, reste: 0, cote: 1, t: 0 },
    temps: 0,
  })

  useEffect(() => {
    const c = toile.current
    if (!c || !w) return
    const s = etat.current
    s.base = peindreSol(w, h, sol)
    s.image = c.getContext('2d')!.createImageData(w, h)
    s.empreintes = []
    s.bete = { x: w * 0.72, y: h * 0.3, cap: 2.6, reste: 0, cote: 1, t: 0 }
    setCompte(0)
  }, [w, h, sol])

  const poser = (x: number, y: number, dx: number, dy: number, cote: number, patte: boolean) => {
    const s = etat.current
    const ecart = patte ? 0.55 : 1.6 // half the stride width, in texels
    s.empreintes.push({ x: x - dy * ecart * cote, y: y + dx * ecart * cote, dx, dy, nee: s.temps, patte })
    if (!patte) setCompte((n) => n + 1)
  }

  // The visitor walks: every 0.7 block of pointer travel leaves the next print.
  const marcher = (e: React.PointerEvent) => {
    const c = toile.current
    if (!c) return
    const p = pointeur(c, e)
    const s = etat.current
    const m = s.marcheur
    if (!m) { s.marcheur = { x: p.x, y: p.y, dx: 1, dy: 0, reste: PAS * 0.5, cote: 1 }; return }
    let ddx = p.x - m.x, ddy = p.y - m.y
    const d = Math.hypot(ddx, ddy)
    if (d < 0.5) return
    if (d > 60) { Object.assign(m, { x: p.x, y: p.y }); return } // a jump, not a walk
    ddx /= d; ddy /= d
    // The heading turns smoothly, as feet do.
    const nx = m.dx * 0.6 + ddx * 0.4, ny = m.dy * 0.6 + ddy * 0.4, n = Math.hypot(nx, ny) || 1
    m.dx = nx / n; m.dy = ny / n
    let parcouru = d
    while (m.reste <= parcouru) {
      parcouru -= m.reste
      const t = 1 - parcouru / d
      poser(m.x + (p.x - m.x) * t, m.y + (p.y - m.y) * t, m.dx, m.dy, m.cote, false)
      m.cote = -m.cote
      m.reste = PAS
    }
    m.reste -= parcouru
    m.x = p.x; m.y = p.y
  }

  useBoucle(toile, (dt) => {
    const c = toile.current
    const s = etat.current
    if (!c || !s.base || !s.image) return
    s.temps += dt
    const duree = (MINUTES * 60) / ACCELERE
    s.empreintes = s.empreintes.filter((e) => s.temps - e.nee < duree)

    // The fox strolls about at 1.6 blocks a second, turning slowly, and comes back in from the other side.
    const b = s.bete
    if (!calme) {
      b.t += dt
      b.cap += (bruit(b.t * 0.35, 0, 9) - 0.5) * dt * 2.2
      const vx = Math.cos(b.cap), vy = Math.sin(b.cap) * 0.7
      const v = 1.6 * TEXELS * dt
      b.x += vx * v; b.y += vy * v
      b.reste -= v
      const marge = 14
      if (b.x > w + marge) b.x = -marge; if (b.x < -marge) b.x = w + marge
      if (b.y > h + marge) b.y = -marge; if (b.y < -marge) b.y = h + marge
      if (b.reste <= 0) { const n = Math.hypot(vx, vy); poser(b.x, b.y, vx / n, vy / n, b.cote, true); b.cote = -b.cote; b.reste = 0.45 * TEXELS }
    }

    const px = s.image.data
    px.set(s.base)
    const creux = SOLS[sol].creux
    for (const e of s.empreintes) {
      const age = (s.temps - e.nee) / duree
      const force = (1 - lisse(0, 1, age)) * 0.62
      const r = e.patte ? 3 : 5
      const x0 = Math.max(0, Math.floor(e.x - r)), x1 = Math.min(w - 1, Math.ceil(e.x + r))
      const y0 = Math.max(0, Math.floor(e.y - r)), y1 = Math.min(h - 1, Math.ceil(e.y + r))
      const forme = (x: number, y: number) => {
        const ox = x + 0.5 - e.x, oy = y + 0.5 - e.y
        const u = ox * e.dx + oy * e.dy, v = -ox * e.dy + oy * e.dx
        return e.patte ? creuxPatte(u, v) : creuxBotte(u, v)
      }
      for (let y = y0; y <= y1; y++) {
        for (let x = x0; x <= x1; x++) {
          const i = (y * w + x) * 4
          const f = forme(x, y)
          // Light comes from the upper left: the wall of the hollow on that side is in shade, the rim facing it
          // catches the light.
          if (!f) {
            if (forme(x - 1, y - 1)) { const g = 1 + 0.07 * force / 0.62; px[i] *= g; px[i + 1] *= g; px[i + 2] *= g }
            continue
          }
          const k = Math.min(1, f * force * (forme(x - 1, y - 1) ? 1 : 1.3))
          px[i] += (creux[0] - px[i]) * k; px[i + 1] += (creux[1] - px[i + 1]) * k; px[i + 2] += (creux[2] - px[i + 2]) * k
        }
      }
    }

    // Glints where the low sun catches the snow, rarer on sand.
    if (!calme) {
      const n = sol === 'neige' ? 60 : 18
      const tick = Math.floor(s.temps * 6)
      for (let k = 0; k < n; k++) {
        const x = Math.floor(hasard(k, tick, 21) * w), y = Math.floor(hasard(k, tick, 22) * h)
        const i = (y * w + x) * 4
        const f = sol === 'neige' ? [255, 255, 255] : [255, 236, 190]
        px[i] = f[0]; px[i + 1] = f[1]; px[i + 2] = f[2]
      }
    }

    // The fox itself, drawn texel by texel in its own frame so it turns without blurring.
    {
      const n = Math.hypot(Math.cos(b.cap), Math.sin(b.cap) * 0.7) || 1
      const fx = Math.cos(b.cap) / n, fy = (Math.sin(b.cap) * 0.7) / n
      for (let y = Math.floor(b.y - 9); y <= b.y + 9; y++) {
        if (y < 0 || y >= h) continue
        for (let x = Math.floor(b.x - 9); x <= b.x + 9; x++) {
          if (x < 0 || x >= w) continue
          const ox = x + 0.5 - b.x, oy = y + 0.5 - b.y
          const col = renard(ox * fx + oy * fy, -ox * fy + oy * fx)
          if (!col) continue
          const i = (y * w + x) * 4
          px[i] = col[0]; px[i + 1] = col[1]; px[i + 2] = col[2]
        }
      }
      // Its shadow, a little to the side.
      for (let y = Math.floor(b.y - 3); y <= b.y + 5; y++) {
        if (y < 0 || y >= h) continue
        for (let x = Math.floor(b.x - 5); x <= b.x + 7; x++) {
          if (x < 0 || x >= w) continue
          const ox = x + 0.5 - b.x - 1.5, oy = y + 0.5 - b.y - 2.2
          if ((ox / 5) ** 2 + (oy / 2) ** 2 > 1) continue
          const i = (y * w + x) * 4
          if (renard((x + 0.5 - b.x) * fx + (y + 0.5 - b.y) * fy, -(x + 0.5 - b.x) * fy + (y + 0.5 - b.y) * fx)) continue
          px[i] *= 0.86; px[i + 1] *= 0.88; px[i + 2] *= 0.93
        }
      }
    }
    c.getContext('2d')!.putImageData(s.image, 0, 0)
  })

  return (
    <div className="scene demo demo-traces">
      <canvas
        ref={toile}
        className="demo-toile pixels"
        role="img"
        aria-label={`Un sol de ${sol} vu d'en haut : le pointeur y laisse des empreintes, un renard passe et laisse les siennes`}
        onPointerMove={marcher}
        onPointerDown={(e) => { etat.current.marcheur = null; marcher(e) }}
        onPointerLeave={() => { etat.current.marcheur = null }}
      />
      <div className="demo-panneau">
        <p className="petites-caps">Marchez avec le pointeur</p>
        <div className="demo-choix" role="group" aria-label="Sol">
          {(['neige', 'sable'] as Sol[]).map((s) => (
            <button key={s} type="button" aria-pressed={sol === s} onClick={() => setSol(s)}>{s === 'neige' ? 'Neige' : 'Sable'}</button>
          ))}
        </div>
        <dl className="demo-mesures">
          <div><dt>Un pas</dt><dd>0,7 bloc</dd></div>
          <div><dt>Effacé en</dt><dd>4 min <small>(ici {nombre((MINUTES * 60) / ACCELERE)} s)</small></dd></div>
          <div><dt>Vos pas</dt><dd>{nombre(compte)}</dd></div>
        </dl>
      </div>
    </div>
  )
}
