// Starcatcher's reel, played by its own rules: a handle sweeps the wheel at the fish's speed (degrees per tick, 20
// ticks a second); a press inside a sweet spot adds its reward and moves the spot, a press outside costs the fish's
// penalty; the line slackens by its decay every five ticks. Start at 20, land the fish at its hp, lose it below 0.
// The four fish are the pack's own files (bass, bluefish, catfish) and one legendary from the mod.
import { useEffect, useRef, useState } from 'react'
import { Capture } from '../Capture'
import { hasard, nombre, useBoucle } from './outils'

interface Zone { taille: number; gain: number; derive: number; efface?: number }
interface Poisson {
  id: string; nom: string; rarete: string; vitesse: number; pv: number; penalite: number; usure: number
  zones: Zone[]; gel?: number; taille: [number, number]; poids: [number, number]
}

const POISSONS: Poisson[] = [
  { id: 'bar', nom: 'Bar', rarete: 'Commun', vitesse: 7, pv: 65, penalite: 10, usure: 1,
    zones: [{ taille: 22, gain: 15, derive: 0.5 }, { taille: 15, gain: 20, derive: 0.5 }], taille: [40, 10], poids: [1500, 500] },
  { id: 'tassergal', nom: 'Tassergal', rarete: 'Commun', vitesse: 11, pv: 55, penalite: 6, usure: 1,
    zones: [{ taille: 22, gain: 15, derive: 0.65 }, { taille: 15, gain: 20, derive: 0.65 }], taille: [50, 15], poids: [2500, 900] },
  { id: 'poisson-chat', nom: 'Poisson-chat vaseux', rarete: 'Peu commun', vitesse: 10, pv: 100, penalite: 20, usure: 1,
    zones: [{ taille: 22, gain: 15, derive: 1 }, { taille: 15, gain: 20, derive: 1 }], taille: [70, 20], poids: [4000, 1500] },
  { id: 'vairon', nom: 'Vairon à dos cristallin azur', rarete: 'Légendaire', vitesse: 29, pv: 300, penalite: 75, usure: 3,
    zones: [{ taille: 15, gain: 20, derive: 0, efface: 0.1 }, { taille: 15, gain: 20, derive: 0, efface: 0.1 }], gel: 40, taille: [6, 4], poids: [5, 3] },
]

type Phase = 'pret' | 'touche' | 'combat' | 'pris' | 'perdu'
interface Tache { pos: number; zone: Zone; alpha: number; eclat: number }

const TICK = 1 / 20
const ecart = (a: number, b: number) => { const d = Math.abs(a - b) % 360; return Math.min(d, 360 - d) }

/** A free place on the wheel for a spot, away from the others. */
function placeLibre(taches: Tache[], taille: number, graine: number) {
  for (let essai = 0; essai < 40; essai++) {
    const p = Math.floor(hasard(graine, essai, 77) * 360)
    if (taches.every((t) => ecart(t.pos, p) > (t.zone.taille + taille) / 2 + 20)) return p
  }
  return Math.floor(hasard(graine, 99, 77) * 360)
}

export default function Peche() {
  const toile = useRef<HTMLCanvasElement>(null)
  const [choix, setChoix] = useState(0)
  const [phase, setPhase] = useState<Phase>('pret')
  const [prise, setPrise] = useState<{ cm: number; g: number; parfaite: boolean } | null>(null)
  const [record, setRecord] = useState<Record<string, number>>({})
  const p = POISSONS[choix]
  const jeu = useRef({
    poignee: 0, sens: 1, progres: 20, gel: 0, taches: [] as Tache[], cumul: 0, graine: 1, parfaite: true,
    touche: 0, tick: 0, eclats: [] as { pos: number; texte: string; bon: boolean; t: number }[], temps: 0,
  })

  const lancer = () => {
    const g = jeu.current
    g.graine++
    g.poignee = 0; g.sens = 1; g.progres = 20; g.tick = 0; g.gel = 0; g.parfaite = true; g.eclats = []
    g.taches = []
    for (const z of p.zones) g.taches.push({ pos: placeLibre(g.taches, z.taille, g.graine * 7 + g.taches.length), zone: z, alpha: 1, eclat: 0 })
    g.touche = g.temps + 0.9 + hasard(g.graine, 1, 3) * 0.8 // 5 to 15 s in the game; shortened here
    setPrise(null)
    setPhase('touche')
  }

  const ferrer = () => {
    const g = jeu.current
    if (phase !== 'combat') return
    const t = g.taches.find((x) => ecart(x.pos, g.poignee) < x.zone.taille / 2)
    if (t) {
      g.progres += t.zone.gain
      g.eclats.push({ pos: t.pos, texte: `+${t.zone.gain}`, bon: true, t: g.temps })
      t.pos = placeLibre(g.taches.filter((x) => x !== t), t.zone.taille, g.graine * 13 + Math.floor(g.temps * 10))
      t.alpha = 1
    } else {
      g.progres -= p.penalite
      g.parfaite = false
      g.eclats.push({ pos: g.poignee, texte: `−${p.penalite}`, bon: false, t: g.temps })
      for (const x of g.taches) x.alpha = 1 // a miss shows the vanishing spots again
      if (p.gel) g.gel = p.gel
    }
  }

  // Space or Enter play, as in the game, while this chapter is the one on screen.
  useEffect(() => {
    const f = (e: KeyboardEvent) => {
      if (e.key !== ' ' && e.key !== 'Enter') return
      if (!toile.current?.closest('.couche.la, .couche.entre')) return
      if ((e.target as HTMLElement).closest('button, a, input')) return
      e.preventDefault()
      if (phase === 'combat') ferrer()
      else if (phase !== 'touche') lancer()
    }
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  })

  useBoucle(toile, (dt) => {
    const c = toile.current, g = jeu.current
    if (!c) return
    g.temps += dt
    if (phase === 'touche' && g.temps >= g.touche) setPhase('combat')
    if (phase === 'combat') {
      g.cumul += dt
      while (g.cumul >= TICK) {
        g.cumul -= TICK
        // The handle: frozen after a miss on some fish, then ramping back over 10 ticks.
        const rampe = g.gel > 10 ? 0 : g.gel > 0 ? 1 - g.gel / 10 : 1
        if (g.gel > 0) g.gel--
        g.poignee = (g.poignee + p.vitesse * g.sens * rampe + 360) % 360
        for (const t of g.taches) {
          t.pos = (t.pos - t.zone.derive + 360) % 360
          if (t.zone.efface) t.alpha = Math.max(0, t.alpha - t.zone.efface)
        }
        g.tick++
        if (g.tick % 5 === 0) g.progres -= p.usure
      }
      if (g.progres >= p.pv) {
        const cm = Math.max(1, p.taille[0] + (hasard(g.graine, 5, 1) - 0.5) * 2 * p.taille[1])
        const gr = Math.max(1, p.poids[0] + (hasard(g.graine, 6, 1) - 0.5) * 2 * p.poids[1])
        setPrise({ cm, g: gr, parfaite: g.parfaite })
        setRecord((r) => ({ ...r, [p.id]: Math.max(r[p.id] ?? 0, cm) }))
        setPhase('pris')
      } else if (g.progres < 0) setPhase('perdu')
    }
    dessiner(c, g, p, phase)
  })

  return (
    <div className="scene demo demo-peche" data-molette>
      <div className="scene-fond-sombre" aria-hidden="true"><Capture nom="bru-couchant.jpg" alt="" prioritaire /></div>
      <div className="peche-table">
        <canvas
          ref={toile}
          width={120}
          height={120}
          className="peche-roue pixels"
          role="button"
          tabIndex={0}
          aria-label={phase === 'combat' ? 'Ferrer (Espace)' : 'Lancer la ligne (Espace)'}
          onPointerDown={(e) => { e.preventDefault(); if (phase === 'combat') ferrer(); else if (phase !== 'touche') lancer() }}
        />
        <div className="peche-etat" aria-live="polite">
          {phase === 'pret' && <p><b>Cliquez sur la roue</b> ou appuyez sur Espace pour lancer la ligne.</p>}
          {phase === 'touche' && <p><b>Le bouchon frémit…</b></p>}
          {phase === 'combat' && <p><b>Ferrez</b> quand l'aiguille passe dans le vert : clic ou Espace.</p>}
          {phase === 'pris' && prise && (
            <p className="peche-prise">
              <b>{p.nom}</b>
              <span>{nombre(prise.cm, prise.cm < 10 ? 1 : 0)} cm · {prise.g >= 1000 ? `${nombre(prise.g / 1000, 1)} kg` : `${nombre(prise.g)} g`}{prise.parfaite ? ' · prise parfaite' : ''}</span>
              <small>Cliquez pour relancer.</small>
            </p>
          )}
          {phase === 'perdu' && <p><b>Il s'est décroché.</b> Cliquez pour relancer.</p>}
        </div>
      </div>
      <div className="demo-panneau">
        <p className="petites-caps">Choisissez un poisson</p>
        <ul className="demo-sources">
          {POISSONS.map((x, i) => (
            <li key={x.id}>
              <button type="button" aria-pressed={choix === i} onClick={() => { setChoix(i); setPhase('pret') }}>
                <span className={`peche-rarete r-${x.id === 'vairon' ? 'legende' : x.rarete === 'Commun' ? 'commun' : 'peu'}`} />
                <span>{x.nom}</span>
                <span className="peche-record">{record[x.id] ? `${nombre(record[x.id], record[x.id] < 10 ? 1 : 0)} cm` : ''}</span>
              </button>
            </li>
          ))}
        </ul>
        <dl className="demo-mesures">
          <div><dt>Rareté</dt><dd>{p.rarete}</dd></div>
          <div><dt>Aiguille</dt><dd>{nombre(p.vitesse * 20)}°/s</dd></div>
          <div><dt>Pour le sortir</dt><dd>{p.pv} points</dd></div>
          <div><dt>Un raté</dt><dd>−{p.penalite}{p.gel ? ', aiguille gelée' : ''}</dd></div>
        </dl>
        <p className="demo-note">Les chiffres sont ceux des fichiers du jeu. En vrai, le poisson mord après 5 à 15 secondes.</p>
      </div>
    </div>
  )
}

/** The reel in pixels: a ring, its sweet spots, the handle, and the line's tension as a gauge around it. */
function dessiner(c: HTMLCanvasElement, g: { poignee: number; progres: number; taches: Tache[]; eclats: { pos: number; texte: string; bon: boolean; t: number }[]; temps: number; gel: number }, p: Poisson, phase: Phase) {
  const ctx = c.getContext('2d')!
  const W = c.width, cx = W / 2, cy = W / 2
  const img = ctx.createImageData(W, W)
  const d = img.data
  const R1 = 44, R0 = 33 // the ring
  const rad = (a: number) => ((a - 90) * Math.PI) / 180
  const niveau = Math.max(0, Math.min(1, g.progres / p.pv))
  for (let y = 0; y < W; y++) for (let x = 0; x < W; x++) {
    const dx = x + 0.5 - cx, dy = y + 0.5 - cy, r = Math.hypot(dx, dy)
    let a = (Math.atan2(dy, dx) * 180) / Math.PI + 90
    if (a < 0) a += 360
    const i = (y * W + x) * 4
    let col: [number, number, number, number] | null = null
    if (r >= R0 && r < R1) {
      const bord = r < R0 + 1.2 || r > R1 - 1.2
      const v = 46 + hasard(Math.floor(a / 6), Math.floor(r), 3) * 10
      col = bord ? [24, 22, 20, 255] : [v + 18, v + 8, v - 4, 255]
      for (const t of g.taches) {
        if (t.alpha <= 0.02 || ecart(t.pos, a) >= t.zone.taille / 2) continue
        const k = t.alpha
        const fin = t.zone.taille < 18
        const vert: [number, number, number] = fin ? [120, 255, 90] : [15, 240, 0]
        col = [col[0] + (vert[0] - col[0]) * k, col[1] + (vert[1] - col[1]) * k, col[2] + (vert[2] - col[2]) * k, 255]
        if (bord) col = [col[0] * 0.5, col[1] * 0.5, col[2] * 0.5, 255]
      }
    } else if (r >= R1 + 3 && r < R1 + 7) {
      // The line's tension around the ring: fills clockwise towards landing the fish.
      const plein = a / 360 <= niveau
      const edge = r < R1 + 3.8 || r > R1 + 6.2
      col = edge ? [16, 18, 22, 230] : plein ? (phase === 'pris' ? [244, 207, 148, 255] : [120, 186, 222, 255]) : [36, 42, 52, 220]
    } else if (r < R0 - 2) {
      // The water seen through the reel.
      const v = hasard(x >> 1, y >> 1, 9) * 8
      col = [18 + v, 40 + v, 56 + v, r < R0 - 3 ? 235 : 255]
      if (r > R0 - 3) col = [24, 22, 20, 255]
    }
    if (col) { d[i] = col[0]; d[i + 1] = col[1]; d[i + 2] = col[2]; d[i + 3] = col[3] }
  }
  ctx.putImageData(img, 0, 0)
  // The handle: a needle from the hub to the ring.
  const a = rad(g.poignee)
  ctx.save()
  ctx.lineCap = 'square'
  ctx.strokeStyle = g.gel > 0 ? '#bfe8ff' : '#f4ead2'
  ctx.lineWidth = 3
  ctx.beginPath(); ctx.moveTo(cx, cy); ctx.lineTo(cx + Math.cos(a) * (R1 - 1), cy + Math.sin(a) * (R1 - 1)); ctx.stroke()
  ctx.fillStyle = '#2a2420'
  ctx.fillRect(cx - 4, cy - 4, 8, 8)
  ctx.fillStyle = '#c99a5a'
  ctx.fillRect(cx - 2, cy - 2, 4, 4)
  // Hits and misses rise and fade where they happened.
  ctx.font = '10px serif'
  ctx.textAlign = 'center'
  for (const e of g.eclats) {
    const age = g.temps - e.t
    if (age > 0.9) continue
    const b = rad(e.pos)
    ctx.globalAlpha = 1 - age / 0.9
    ctx.fillStyle = e.bon ? '#b6ff9a' : '#ff9a8a'
    ctx.fillText(e.texte, cx + Math.cos(b) * (R1 + 10), cy + Math.sin(b) * (R1 + 10) - age * 8)
  }
  ctx.globalAlpha = 1
  ctx.restore()
}
