// The edge of the disc, as the compline mod guards it: from 64 blocks before the edge (5 936) a line warns every
// five seconds; past 6 000 the boat is set back to 5 998 on the line to the centre, stopped, its rower still aboard.
// The visitor steers the boat with the pointer (or the arrow keys); until then it heads for the edge by itself.
import { useEffect, useRef, useState } from 'react'
import { bruit, hasard, lisse, nombre, pointeur, useBoucle, useCalme, useToile } from './outils'

const BORD = 6000, ALERTE = BORD - 64, RETOUR = BORD - 2
const R0 = 5880, LARGEUR = 190 // blocks from the left edge of the view, and across it
const VITESSE = 8 // blocks a second, a rowing boat on still water
const PROCHE = 'La brume s’épaissit. Le monde s’arrête un peu plus loin.'
const LIMITE = 'La brume ne laisse passer personne.'

/** The boat from above, in its own frame (u forward). */
function barque(u: number, v: number): [number, number, number] | null {
  const coque = (u / 5.4) ** 2 + (v / 2.3) ** 2
  if (coque > 1) {
    if (Math.abs(u + 0.6) < 0.6 && Math.abs(v) < 4.2 && Math.abs(v) > 2.2) return [118, 86, 52] // oars
    return null
  }
  if (coque > 0.62) return [96, 66, 38]
  if (Math.abs(u - 0.4) < 0.55) return [128, 92, 54] // the thwart
  if (Math.abs(u + 2.4) < 1.1 && Math.abs(v) < 1) return [58, 70, 96] // the rower
  return [150, 110, 66]
}

export default function Bord() {
  const toile = useRef<HTMLCanvasElement>(null)
  const { w, h } = useToile(toile, (l) => Math.max(3, Math.round(l / 400)))
  const calme = useCalme()
  const [r, setR] = useState(5900)
  const [barre, setBarre] = useState<{ texte: string; n: number } | null>(null)
  const [renvois, setRenvois] = useState(0)
  const etat = useRef({
    x: 5900, y: -18, cap: 0, v: 0,
    cible: null as null | { x: number; y: number },
    auto: true, touches: new Set<string>(),
    alerte: -99, temps: 0, sillage: [] as { x: number; y: number; t: number; cap: number }[],
    bouffee: -99,
    image: null as ImageData | null,
  })

  useEffect(() => {
    if (w) etat.current.image = toile.current!.getContext('2d')!.createImageData(w, h)
  }, [w, h])

  const k = w / LARGEUR // canvas pixels per block
  const versEcran = (x: number, y: number) => ({ x: (x - R0) * k, y: h / 2 + y * k })
  const versMonde = (px: number, py: number) => ({ x: R0 + px / k, y: (py - h / 2) / k })

  const dire = (texte: string) => setBarre((b) => ({ texte, n: (b?.n ?? 0) + 1 }))
  useEffect(() => {
    if (!barre) return
    const t = window.setTimeout(() => setBarre(null), 2600)
    return () => window.clearTimeout(t)
  }, [barre])

  useBoucle(toile, (dt) => {
    const c = toile.current, s = etat.current, img = s.image
    if (!c || !img || !w) return
    s.temps += dt

    // Steering: towards the pointer, the arrow keys, or on its own towards the edge.
    let vise: { x: number; y: number } | null = s.cible
    if (s.touches.size) {
      const dx = (s.touches.has('ArrowRight') ? 1 : 0) - (s.touches.has('ArrowLeft') ? 1 : 0)
      const dy = (s.touches.has('ArrowDown') ? 1 : 0) - (s.touches.has('ArrowUp') ? 1 : 0)
      vise = dx || dy ? { x: s.x + dx * 10, y: s.y + dy * 10 } : null
    } else if (s.auto) vise = { x: BORD + 30, y: -18 + Math.sin(s.temps * 0.3) * 6 }
    if (vise) {
      const dx = vise.x - s.x, dy = vise.y - s.y, d = Math.hypot(dx, dy)
      if (d > 1.2) {
        let dc = Math.atan2(dy, dx) - s.cap
        dc = Math.atan2(Math.sin(dc), Math.cos(dc))
        s.cap += Math.max(-2.4 * dt, Math.min(2.4 * dt, dc))
        s.v = Math.min(VITESSE, s.v + VITESSE * 1.2 * dt) * (Math.abs(dc) > 1.4 ? 0.96 : 1)
      } else s.v = Math.max(0, s.v - VITESSE * 2 * dt)
    } else s.v = Math.max(0, s.v - VITESSE * 0.8 * dt)
    s.x += Math.cos(s.cap) * s.v * dt
    s.y += Math.sin(s.cap) * s.v * dt
    const marge = (h / 2) / k - 2
    s.y = Math.max(-marge, Math.min(marge, s.y))

    // The mod's rule, on the distance to the centre (the view is a thin slice east of it, so x stands for r).
    if (s.x > BORD) {
      s.x = RETOUR; s.v = 0
      s.bouffee = s.temps
      dire(LIMITE)
      setRenvois((n) => n + 1)
    } else if (s.x >= ALERTE && s.temps - s.alerte >= 5) {
      s.alerte = s.temps
      dire(PROCHE)
    }
    if (s.v > 0.5 && hasard(Math.floor(s.temps * 30), 0, 5) > 0.3) s.sillage.push({ x: s.x - Math.cos(s.cap) * 3.2, y: s.y - Math.sin(s.cap) * 3.2, t: s.temps, cap: s.cap })
    s.sillage = s.sillage.filter((p) => s.temps - p.t < 3.5)
    setR((v) => (Math.round(v) === Math.round(s.x) ? v : s.x))

    // The sea, the mist thickening towards the edge, and beyond it.
    const px = img.data
    const t = calme ? 0 : s.temps
    for (let y = 0; y < h; y++) {
      for (let x = 0; x < w; x++) {
        const m = versMonde(x, y)
        // Long low swells, and here and there a glint of light on a crest.
        const houle = bruit(x / 16 + t * 0.35, y / 2.6, 2) * 0.65 + bruit(x / 6 - t * 0.5, y / 1.6 + t * 0.15, 3) * 0.35
        const reflet = houle > 0.78 && hasard(x, y, Math.floor(t * 2)) > 0.55 ? 1 : 0
        let cr = 24 + houle * 22 + reflet * 40, cg = 54 + houle * 30 + reflet * 44, cb = 70 + houle * 30 + reflet * 40
        const brume = Math.min(0.94, lisse(5880, BORD, m.x) * 0.72 + lisse(BORD - 6, BORD + 30, m.x) * 0.24
          + (bruit(x / 22 - t * 0.25, y / 14, 7) - 0.5) * 0.22 * lisse(5860, BORD, m.x))
        const b = Math.max(0, brume)
        cr += (196 - cr) * b; cg += (202 - cg) * b; cb += (206 - cb) * b
        const i = (y * w + x) * 4
        px[i] = cr; px[i + 1] = cg; px[i + 2] = cb; px[i + 3] = 255
      }
    }
    // Lines on the water: the warning band and the edge itself.
    const xa = Math.round(versEcran(ALERTE, 0).x), xb = Math.round(versEcran(BORD, 0).x)
    for (let y = 0; y < h; y++) {
      if (Math.floor(y / 3) % 2 === 0 && xa >= 0 && xa < w) { const i = (y * w + xa) * 4; px[i] = 236; px[i + 1] = 214; px[i + 2] = 170 }
      if (xb >= 0 && xb < w) { const i = (y * w + xb) * 4; px[i] = 250; px[i + 1] = 250; px[i + 2] = 250 }
    }
    // The wake: two lines of foam that open out behind the boat and fade.
    for (const p of s.sillage) {
      const age = s.temps - p.t, a = 1 - age / 3.5
      const nx = -Math.sin(p.cap), ny = Math.cos(p.cap)
      for (const cote of [-1, 1]) {
        const e = versEcran(p.x + nx * cote * (1 + age * 1.6), p.y + ny * cote * (1 + age * 1.6))
        const x = Math.round(e.x), y = Math.round(e.y)
        if (x < 0 || y < 0 || x >= w || y >= h) continue
        const i = (y * w + x) * 4
        px[i] += (226 - px[i]) * a * 0.75; px[i + 1] += (236 - px[i + 1]) * a * 0.75; px[i + 2] += (240 - px[i + 2]) * a * 0.75
      }
    }
    // The boat, drawn texel by texel in its own frame. It is drawn larger than life so it reads at this scale.
    const e = versEcran(s.x, s.y)
    const fx = Math.cos(s.cap), fy = Math.sin(s.cap)
    for (let y = Math.floor(e.y - 11); y <= e.y + 11; y++) {
      if (y < 0 || y >= h) continue
      for (let x = Math.floor(e.x - 11); x <= e.x + 11; x++) {
        if (x < 0 || x >= w) continue
        const ox = x + 0.5 - e.x, oy = y + 0.5 - e.y
        const col = barque((ox * fx + oy * fy) * 0.62, (-ox * fy + oy * fx) * 0.62)
        if (!col) continue
        const i = (y * w + x) * 4
        px[i] = col[0]; px[i + 1] = col[1]; px[i + 2] = col[2]
      }
    }
    // Mist rolling over the boat for a moment after it is set back.
    const pb = s.temps - s.bouffee
    if (pb < 1.2) {
      const a = (1 - pb / 1.2) * 0.85, R = 10 + pb * 14
      for (let y = Math.floor(e.y - R); y <= e.y + R; y++) for (let x = Math.floor(e.x - R); x <= e.x + R; x++) {
        if (x < 0 || y < 0 || x >= w || y >= h) continue
        const d = Math.hypot(x - e.x, y - e.y) / R
        if (d > 1) continue
        const f = a * (1 - d) * (0.6 + bruit(x / 3, y / 3, 12) * 0.6)
        const i = (y * w + x) * 4
        px[i] += (214 - px[i]) * f; px[i + 1] += (220 - px[i + 1]) * f; px[i + 2] += (224 - px[i + 2]) * f
      }
    }
    c.getContext('2d')!.putImageData(img, 0, 0)
  })

  const viser = (e: React.PointerEvent) => {
    const s = etat.current
    if (e.pointerType !== 'mouse' && e.buttons === 0) return
    const p = pointeur(toile.current!, e)
    s.cible = versMonde(p.x, p.y)
    s.auto = false
  }
  const touche = (e: React.KeyboardEvent, bas: boolean) => {
    if (!e.key.startsWith('Arrow')) return
    e.preventDefault()
    const s = etat.current
    s.auto = false; s.cible = null
    if (bas) s.touches.add(e.key); else s.touches.delete(e.key)
  }

  const reste = Math.max(0, BORD - r)
  const pc = (x: number) => `${((x - R0) / LARGEUR) * 100}%`
  return (
    <div className="scene demo demo-bord">
      <canvas
        ref={toile}
        className="demo-toile pixels"
        tabIndex={0}
        data-clavier
        role="application"
        aria-label="La mer au bord du disque, vue d'en haut. Dirigez la barque au pointeur ou aux flèches du clavier."
        onPointerMove={viser}
        onPointerDown={viser}
        onPointerLeave={() => { etat.current.cible = null }}
        onKeyDown={(e) => touche(e, true)}
        onKeyUp={(e) => touche(e, false)}
        onBlur={() => etat.current.touches.clear()}
      />
      <div className="bord-reperes" aria-hidden="true">
        <span style={{ left: pc(ALERTE) }}>5 936<small>premier avertissement</small></span>
        <span className="bord-limite" style={{ left: pc(BORD) }}>6 000<small>le bord</small></span>
      </div>
      <div className="demo-panneau">
        <p className="petites-caps">Menez la barque vers le bord</p>
        <dl className="demo-mesures">
          <div><dt>Du centre</dt><dd>{nombre(Math.min(r, BORD))} blocs</dd></div>
          <div><dt>Jusqu'au bord</dt><dd>{nombre(reste)} blocs</dd></div>
          <div><dt>Refoulée</dt><dd>{nombre(renvois)} fois</dd></div>
        </dl>
        <svg className="bord-carte" viewBox="-60 -60 120 120" aria-hidden="true">
          <circle r="57" className="bord-carte-mer" />
          <path d="M0-52C20-50 38-38 46-22 54-6 50 14 44 28 34 44 16 52-2 51-22 52-38 42-46 26-54 8-52-12-44-28-34-44-18-53 0-52Z" className="bord-carte-terre" />
          <circle r="57" className="bord-carte-bord" />
          <circle cx={Math.min(57, (r / BORD) * 57)} cy="0" r="3.2" className="bord-carte-ici" />
        </svg>
        <p className="demo-note">Le disque fait 6 000 blocs de rayon ; la côte est vers 5 700. Ici, une tranche de mer à l'est.</p>
      </div>
      <p key={barre?.n} className={`demo-barre${barre ? ' vue' : ''}`} aria-live="polite">{barre?.texte}</p>
    </div>
  )
}
