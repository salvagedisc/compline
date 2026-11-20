// Armour that gets dirty, by the compline mod's patina module: from 0 to 100, full after 40 minutes of walking,
// half as fast again running, 1.6 times on dirt or sand, two walks' worth more in a fight; water washes 12 points
// a second. Three looks, from 12, 35 and 65, rust at the end on bare metal.
import { useEffect, useMemo, useRef, useState } from 'react'
import { bruit, hasard, nombre, useBoucle } from './outils'

const N = 32 // texels across the drawing
const ACCELERE = 40 // getting dirty is shown 40 times faster; washing is real time
const PAR_MARCHE = 100 / (40 * 60) // points a second while walking

const PLASTRON = [
  '................',
  '..###......###..',
  '.#####....#####.',
  '.######..######.',
  '.##############.',
  '.##############.',
  '..############..',
  '...##########...',
  '...##########...',
  '...##########...',
  '...##########...',
  '...##########...',
  '...##########...',
  '....########....',
  '................',
  '................',
]

type Allure = 'immobile' | 'marche' | 'course'
const ALLURES: { id: Allure; nom: string; k: number }[] = [
  { id: 'immobile', nom: 'Immobile', k: 0 }, { id: 'marche', nom: 'Marcher', k: 1 }, { id: 'course', nom: 'Courir', k: 1.5 },
]
const PALIERS: [number, string][] = [[0, 'Propre'], [12, 'Un peu sale'], [35, 'Sale'], [65, 'Couverte de boue']]

export default function Patine() {
  const toile = useRef<HTMLCanvasElement>(null)
  const [patine, setPatine] = useState(48)
  const [allure, setAllure] = useState<Allure>('marche')
  const [terre, setTerre] = useState(false)
  const [combat, setCombat] = useState(false)
  const [eau, setEau] = useState(false)
  const valeur = useRef(48)
  const niveauEau = useRef(0)

  // Per texel: inside the plate or not, its metal shade, and the dirt level at which it turns muddy.
  const carte = useMemo(() => {
    const dedans = (x: number, y: number) => x >= 0 && y >= 0 && x < N && y < N && PLASTRON[y >> 1][x >> 1] === '#'
    const t = Array.from({ length: N * N }, (_, i) => {
      const x = i % N, y = (i - x) / N
      if (!dedans(x, y)) return null
      const bord = !dedans(x - 1, y) || !dedans(x + 1, y) || !dedans(x, y - 1) || !dedans(x, y + 1)
      const lisiere = !bord && [[-1, 0], [1, 0], [0, -1], [0, 1], [-1, -1], [1, 1], [-1, 1], [1, -1]].some(([dx, dy]) => {
        const nx = x + dx, ny = y + dy
        return !dedans(nx, ny) || !dedans(nx - 1, ny) || !dedans(nx + 1, ny) || !dedans(nx, ny - 1) || !dedans(nx, ny + 1)
      })
      // Mud gathers low and on the sides first, in clumps.
      const seuil = Math.max(1, (bruit(x / 5, y / 5, 31) * 0.85 + hasard(x >> 1, y >> 1, 32) * 0.15) * 110 - (y / N) * 50 + 8)
      // Metal lit from the upper left: a bright rim inside the outline on that side, shade on the other.
      const rebord = lisiere ? (x < N / 2 ? 1.18 : 0.8) : 1
      const lumiere = (1.04 - (x / N) * 0.22 - (y / N) * 0.1 + (hasard(x >> 1, y >> 1, 33) - 0.5) * 0.05) * rebord
      return { bord, lisiere, seuil, lumiere, couture: x === N / 2 - 1 || x === N / 2 }
    })
    return t
  }, [])

  useEffect(() => { valeur.current = patine }, [patine])

  useBoucle(toile, (dt) => {
    const c = toile.current
    if (!c) return
    // The rules, each second of the game.
    let v = valeur.current
    if (eau) v -= 12 * dt
    else {
      const k = ALLURES.find((a) => a.id === allure)!.k
      const gain = PAR_MARCHE * (k > 0 ? k * (terre ? 1.6 : 1) + (combat ? 2 : 0) : combat ? 2 : 0)
      v += gain * ACCELERE * dt
    }
    v = Math.max(0, Math.min(100, v))
    if (Math.round(v) !== Math.round(valeur.current)) setPatine(v)
    valeur.current = v
    niveauEau.current += ((eau ? 1 : 0) - niveauEau.current) * Math.min(1, dt * 4)

    const ctx = c.getContext('2d')!
    const img = ctx.createImageData(N, N)
    const d = img.data
    for (let i = 0; i < N * N; i++) {
      const t = carte[i]
      if (!t) continue
      const x = i % N, y = (i - x) / N
      let r: number, g: number, b: number
      if (t.bord) { r = 40; g = 42; b = 48 } else {
        const m = 196 * t.lumiere * (t.couture ? 0.82 : 1)
        r = m; g = m * 1.01; b = m * 1.05
      }
      if (v > t.seuil) {
        // Mud, darker where it is thicker.
        const epais = Math.min(1, (v - t.seuil) / 30)
        const mr = 108 - epais * 34 + hasard(x, y, 35) * 14, mg = 82 - epais * 28 + hasard(x, y, 35) * 10, mb = 52 - epais * 18
        const k = 0.55 + epais * 0.45
        r += (mr - r) * k; g += (mg - g) * k; b += (mb - b) * k
      }
      if (v >= 65 && t.lisiere && hasard(x, y, 36) > 0.86 - (v - 65) / 140) { r = 138; g = 76; b = 38 } // rust, on the bare edges
      const eauY = N * (1 - niveauEau.current * 1.1)
      if (y > eauY) { r = r * 0.7 + 30; g = g * 0.78 + 50; b = b * 0.8 + 70 }
      const p = i * 4
      d[p] = r; d[p + 1] = g; d[p + 2] = b; d[p + 3] = 255
    }
    ctx.putImageData(img, 0, 0)
  })

  const palier = [...PALIERS].reverse().find(([s]) => patine >= s)!
  return (
    <div className="scene demo demo-patine">
      <div className="patine-table">
        <div className={`patine-bassin${eau ? ' plein' : ''}`}>
          <canvas ref={toile} width={N} height={N} className="patine-plastron pixels" role="img" aria-label={`Un plastron de fer, ${palier[1].toLowerCase()}`} />
        </div>
        <div className="patine-bulle" aria-live="polite">
          <b>Plastron en fer</b>
          {patine >= 12 ? <span className="sale">{palier[1]}</span> : <span>Propre</span>}
          {patine >= 12 && <span className="lave">Se lave dans l'eau</span>}
        </div>
      </div>
      <div className="demo-panneau">
        <p className="petites-caps">Salissez-la, lavez-la</p>
        <div className="demo-choix" role="group" aria-label="Allure">
          {ALLURES.map((a) => <button key={a.id} type="button" aria-pressed={allure === a.id && !eau} onClick={() => { setAllure(a.id); setEau(false) }}>{a.nom}</button>)}
        </div>
        <div className="demo-choix" role="group" aria-label="Circonstances">
          <button type="button" aria-pressed={terre} onClick={() => setTerre(!terre)}>Sur la terre</button>
          <button type="button" aria-pressed={combat} onClick={() => setCombat(!combat)}>En combat</button>
        </div>
        <button type="button" className={`demo-bouton${eau ? ' fort' : ''}`} aria-pressed={eau} onClick={() => setEau(!eau)}>
          {eau ? 'Sortir de l’eau' : 'Plonger dans l’eau'}
        </button>
        <div className="patine-jauge" style={{ '--p': patine / 100 } as React.CSSProperties} role="meter" aria-valuemin={0} aria-valuemax={100} aria-valuenow={Math.round(patine)} aria-label="Salissure">
          <span className="patine-rempli" />
          {[12, 35, 65].map((s) => <span key={s} className="patine-cran" style={{ left: `${s}%` }}><small>{s}</small></span>)}
        </div>
        <dl className="demo-mesures">
          <div><dt>Salissure</dt><dd>{nombre(patine)} / 100</dd></div>
        </dl>
        <p className="demo-note">Se salir est montré 40 fois plus vite (40 min de marche en 1 min). Le lavage est en temps réel : 12 points par seconde.</p>
      </div>
    </div>
  )
}
