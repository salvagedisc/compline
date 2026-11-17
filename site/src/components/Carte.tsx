import { useEffect, useRef, useState } from 'react'
import { animate, motion, useMotionValue } from 'motion/react'
import { LIEUX, type Lieu } from '../data/lieux'
import { Brume } from './Brume'
import { Pixel } from './Pixel'

const BORD = 6000 // blocks from the centre to the edge
// The map image spans -6200..6200 blocks at one pixel per 16 blocks (775 px).
export const R_IMG = 6000 / 12400 // edge radius as a fraction of the map image
export const R_RIVAGE = 5700 / 12400 // open sea starts here

export const CARTE_SRC = `${import.meta.env.BASE_URL}captures/carte-disque.png`

interface Props {
  intro: boolean
  actif: Lieu | null
  onChoisir: (l: Lieu) => void
  reduit: boolean
}

/** Reads a map position as distance from the centre and climate. */
export function lireCarte(fx: number, fy: number) {
  const dx = fx - 0.5, dy = fy - 0.5
  const d = Math.hypot(dx, dy) / R_IMG
  const blocs = Math.round((d * BORD) / 10) * 10
  if (blocs > BORD) return { titre: 'Au-delà du bord', texte: 'La brume ne laisse passer personne.' }
  const lat = dy / R_IMG
  const climat = lat < -0.35 ? 'froid' : lat > 0.35 ? 'chaud' : 'tempéré'
  const minutes = Math.max(1, Math.round(blocs / 4.3 / 60))
  return {
    titre: `${blocs.toLocaleString('fr-FR')} blocs du centre`,
    texte: blocs > 5700 ? 'Pleine mer, avant le bord.' : `Climat ${climat} · ${minutes} min de marche`,
  }
}

export function Carte({ intro, actif, onChoisir, reduit }: Props) {
  const scene = useRef<HTMLDivElement>(null)
  const canvas = useRef<HTMLCanvasElement>(null)
  const brume = useRef<Brume | null>(null)
  const [survol, setSurvol] = useState<Lieu | null>(null)
  const [lecture, setLecture] = useState<{ titre: string; texte: string } | null>(null)
  const scale = useMotionValue(1)
  const x = useMotionValue(0)
  const y = useMotionValue(0)

  // Mist over the whole scene, with a small clearing around every place.
  useEffect(() => {
    if (!canvas.current) return
    const b = new Brume(canvas.current, { base: 0.46, colour: [196, 203, 194], nuit: [30, 37, 34] }, 220)
    brume.current = b
    const placer = () => {
      const m = scene.current?.querySelector('.carte-disque') as HTMLElement | null
      const s = scene.current?.getBoundingClientRect()
      if (!m || !s) return
      const r = m.getBoundingClientRect()
      b.disc = {
        x: ((r.left - s.left + r.width / 2) / s.width) * b.cols,
        y: ((r.top - s.top + r.height / 2) / s.height) * b.rows,
        r: (r.width / s.width) * b.cols * R_IMG,
      }
      b.holes = LIEUX.map((l) => ({
        x: ((r.left - s.left + l.x * r.width) / s.width) * b.cols,
        y: ((r.top - s.top + l.y * r.height) / s.height) * b.rows,
        r: 11, a: 0.45,
      }))
    }
    placer()
    window.addEventListener('resize', placer)
    if (intro && !reduit) {
      // Arrival: the whole screen starts in mist, which then lifts off the disc.
      b.voile = 1
      animate(1, 0, { duration: 2.6, delay: 0.9, ease: [0.4, 0, 0.2, 1], onUpdate: (v) => { b.voile = v } })
    }
    const stop = b.start(!reduit)
    return () => { stop(); window.removeEventListener('resize', placer) }
    // The opening veil only plays once, on the first mount.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [reduit])

  // Camera: zoom on the chosen place while the mist lifts, back out when it closes.
  useEffect(() => {
    const s = scene.current?.getBoundingClientRect()
    const m = scene.current?.querySelector('.carte-disque') as HTMLElement | null
    if (!s || !m) return
    const d = reduit ? 0 : 1.1
    const ease = [0.65, 0, 0.35, 1] as const
    if (actif) {
      // The map is centred in the scene and scaled around its centre, so the place
      // lands in the middle once shifted back by its offset times the zoom.
      const w = m.offsetWidth, h = m.offsetHeight
      const k = 3.2
      animate(scale, k, { duration: d, ease })
      animate(x, -(actif.x - 0.5) * w * k, { duration: d, ease })
      animate(y, -(actif.y - 0.5) * h * k, { duration: d, ease })
      if (brume.current) {
        const b = brume.current
        animate(0, 1, { duration: d * 0.9, ease, onUpdate: (v) => { b.lift = v; if (reduit) b.draw() } })
      }
    } else {
      animate(scale, 1, { duration: d, ease })
      animate(x, 0, { duration: d, ease })
      animate(y, 0, { duration: d, ease })
      if (brume.current) {
        const b = brume.current
        animate(b.lift, 0, { duration: d * 1.2, ease, onUpdate: (v) => { b.lift = v; if (reduit) b.draw() } })
      }
    }
  }, [actif, reduit, scale, x, y])

  const bouger = (e: React.PointerEvent) => {
    brume.current?.stir(e.clientX, e.clientY)
    const m = scene.current?.querySelector('.carte-disque') as HTMLElement | null
    if (!m || actif) return
    const r = m.getBoundingClientRect()
    const fx = (e.clientX - r.left) / r.width, fy = (e.clientY - r.top) / r.height
    setLecture(fx >= 0 && fx <= 1 && fy >= 0 && fy <= 1 ? lireCarte(fx, fy) : null)
  }

  return (
    <div className="carte-scene" ref={scene} onPointerMove={bouger} onPointerDown={bouger}>
      <motion.div className="monde" style={{ scale, x, y }}>
        <div className="carte-disque">
          <img src={CARTE_SRC} alt="Le monde-disque vu du dessus, avec ses biomes" draggable={false} />
          <span className="cardinal nord">Nord · froid</span>
          <span className="cardinal sud">Sud · chaud</span>
        </div>
        <canvas ref={canvas} className="brume" aria-hidden="true" />
        <div className="lieux-calque">
          <div className="carte-disque fantome">
            {LIEUX.map((l) => (
              <button
                key={l.id}
                type="button"
                className={`lieu-repere${survol?.id === l.id ? ' survol' : ''}`}
                style={{ left: `${l.x * 100}%`, top: `${l.y * 100}%` }}
                onClick={() => onChoisir(l)}
                onPointerEnter={() => setSurvol(l)}
                onPointerLeave={() => setSurvol(null)}
                onFocus={() => setSurvol(l)}
                onBlur={() => setSurvol(null)}
                tabIndex={actif ? -1 : 0}
                aria-label={`${l.nom} : ${l.theme}`}
              >
                <span className="repere-icone"><Pixel nom={l.id} taille={28} /></span>
                <span className="repere-nom">{l.nom}</span>
                {survol?.id === l.id && (
                  <span className="infobulle" role="tooltip">
                    <b>{l.theme}</b>
                    <span>{l.fonctions.length} fonctions à voir</span>
                    <span className="gris">Touche {l.touche}</span>
                  </span>
                )}
              </button>
            ))}
          </div>
        </div>
      </motion.div>
      {!actif && (
        <div className="lecture-carte" aria-live="polite">
          {lecture ? (<><span className="pix">{lecture.titre}</span><span>{lecture.texte}</span></>) : (
            <span className="pix">Passez sur la carte pour lever la brume</span>
          )}
        </div>
      )}
    </div>
  )
}
