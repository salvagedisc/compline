import { useRef, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import type { Media, Zoom } from '../data/lieux'
import { CARTE_SRC, lireCarte } from './Carte'

const LOUPE = 2.6 // magnification of the lens
const LENTILLE = 200 // lens size in CSS pixels, kept in step with .lentille

// Background position that frames a window of width 2r centred on c (fractions of the image).
const pct = (c: number, r: number) => Math.max(0, Math.min(100, ((c - r) / (1 - 2 * r)) * 100))

/** A screenshot with a magnifying lens and named zoom spots. */
function ImageLoupe({ src, alt, zooms }: { src: string; alt: string; zooms?: Zoom[] }) {
  const boite = useRef<HTMLDivElement>(null)
  const [lens, setLens] = useState<{ x: number; y: number; w: number; h: number } | null>(null)
  const [zoom, setZoom] = useState<Zoom | null>(null)
  const bouger = (e: React.PointerEvent) => {
    if (e.pointerType === 'touch') return
    const r = boite.current!.getBoundingClientRect()
    setLens({ x: (e.clientX - r.left) / r.width, y: (e.clientY - r.top) / r.height, w: r.width, h: r.height })
  }
  return (
    <div className="media-image">
      <div className="cadre" ref={boite} onPointerMove={bouger} onPointerLeave={() => setLens(null)}>
        <img src={src} alt={alt} draggable={false} />
        {lens && !zoom && (
          <span
            className="lentille"
            style={{
              left: `${lens.x * 100}%`, top: `${lens.y * 100}%`,
              backgroundImage: `url(${src})`,
              backgroundSize: `${lens.w * LOUPE}px ${lens.h * LOUPE}px`,
              backgroundPosition: `${LENTILLE / 2 - lens.x * lens.w * LOUPE}px ${LENTILLE / 2 - lens.y * lens.h * LOUPE}px`,
            }}
            aria-hidden="true"
          />
        )}
        {zooms?.map((z) => (
          <button
            key={z.label}
            type="button"
            className="zone"
            style={{ left: `${z.x * 100}%`, top: `${z.y * 100}%`, width: `${z.r * 200}%`, height: `${z.r * 200}%` }}
            onClick={() => setZoom(z)}
            aria-label={`Zoomer : ${z.label}`}
          >
            <span className="pix">{z.label}</span>
          </button>
        ))}
        <AnimatePresence>
          {zoom && (
            <motion.button
              type="button"
              className="zoom-plein"
              onClick={() => setZoom(null)}
              initial={{ opacity: 0, scale: 0.6 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.6 }}
              transition={{ duration: 0.35, ease: [0.2, 0.7, 0.2, 1] }}
              style={{
                backgroundImage: `url(${src})`,
                backgroundSize: `${100 / (zoom.r * 2)}% auto`,
                backgroundPosition: `${pct(zoom.x, zoom.r)}% ${pct(zoom.y, zoom.r)}%`,
                transformOrigin: `${zoom.x * 100}% ${zoom.y * 100}%`,
              }}
              aria-label="Fermer le zoom"
            >
              <span className="pix etiquette">{zoom.label} · cliquer pour revenir</span>
            </motion.button>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}

/** Before / after slider across several scenes. */
function Comparateur({ media }: { media: Extract<Media, { type: 'comparer' }> }) {
  const [i, setI] = useState(0)
  const [pos, setPos] = useState(50)
  const boite = useRef<HTMLDivElement>(null)
  const glisse = useRef(false)
  const depuis = (e: React.PointerEvent) => {
    const r = boite.current!.getBoundingClientRect()
    setPos(Math.max(0, Math.min(100, ((e.clientX - r.left) / r.width) * 100)))
  }
  const s = media.scenes[i]
  return (
    <div className="media-comparer">
      <div
        className="comparer"
        ref={boite}
        style={{ '--pos': `${pos}%` } as React.CSSProperties}
        onPointerDown={(e) => { glisse.current = true; boite.current!.setPointerCapture(e.pointerId); depuis(e) }}
        onPointerMove={(e) => glisse.current && depuis(e)}
        onPointerUp={() => { glisse.current = false }}
        onPointerCancel={() => { glisse.current = false }}
      >
        <img src={s.avant} alt={`${s.nom}, ${media.avantNom}`} draggable={false} />
        <img className="apres" src={s.apres} alt={`${s.nom}, ${media.apresNom}`} draggable={false} />
        <span className="etiq g pix">{media.avantNom}</span>
        <span className="etiq d pix">{media.apresNom}</span>
        <span className="poignee" />
      </div>
      <div className="sous-barre">
        <div className="onglets" role="tablist" aria-label="Scènes">
          {media.scenes.map((sc, k) => (
            <button key={sc.nom} type="button" role="tab" aria-selected={k === i} className="onglet" onClick={() => setI(k)}>{sc.nom}</button>
          ))}
        </div>
        <label className="pix curseur-etiq">
          Séparation
          <input type="range" min={0} max={100} value={pos} onChange={(e) => setPos(+e.target.value)} />
        </label>
      </div>
    </div>
  )
}

const PALIERS = [
  { nom: 'Fer', sous: 'La forge', m: '#c9c3b8', titre: 'Lingot de fer',
    lignes: ["On chauffe, on martèle sur l'enclume d'Overgeared, et chaque pièce sort avec une qualité.", 'Plus la pièce est bien forgée, plus elle dure.'] },
  { nom: 'Acier', sous: 'Le four à alliage', m: '#8e9aa3', titre: 'Acier brut',
    lignes: ['3 fers et 1 charbon au four à alliage donnent 3 aciers bruts, à marteler.', "On n'en trouve plus tout prêt dans les coffres."] },
  { nom: 'Acier noir', sous: "L'enclume de maître", m: '#3f6a93', titre: "Lingot d'acier noir",
    lignes: ["Acier, magnétite et charbon de bois, martelé puis refroidi ; une teinte d'acier bleui.", "Il se forge sur l'enclume de maître. Seule sa pioche peut miner le diamant."] },
  { nom: 'Diamant', sous: 'Près de la lave profonde', m: '#7fd6d0', titre: 'Diamant brut',
    lignes: ['Les diamants ne viennent plus que de pierres brutes près de la lave, tout en bas.', "Le gabarit se gagne en réveillant le cœur ancien d'un monument des conifères."] },
  { nom: 'Orichalque', sous: 'Le cœur brûlant', m: '#7a4f3f', titre: "Lingot d'orichalque",
    lignes: ['Des débris antiques dorment près des lacs de lave, sous la couche −40.', "Le four se bâtit en magma ; Archie vend le gabarit une fois l'Obsidiceratops apprivoisé."] },
]
const DINOS = [
  ['Obsidiceratops', 'Chaleur', 'Approcher la lave', '#e07a4b'],
  ['Chorusoatl', 'Froid', 'Les hauteurs glacées', '#9fc6e8'],
  ['Coralosaur', 'Eau', 'Les fonds', '#5fb3a8'],
  ['Therimoss', 'Chaleur sèche', 'Les terres arides', '#d9b65e'],
]

function Echelle() {
  const [i, setI] = useState(2)
  const p = PALIERS[i]
  return (
    <div className="media-echelle">
      <div className="echelle" role="tablist" aria-label="Paliers du métal">
        {PALIERS.map((q, k) => (
          <button key={q.nom} type="button" role="tab" aria-selected={k === i} className="palier" onClick={() => setI(k)}>
            <span className="lingot" style={{ '--m': q.m } as React.CSSProperties} />
            <b>{q.nom}</b>
            <small>{q.sous}</small>
          </button>
        ))}
      </div>
      <AnimatePresence mode="wait">
        <motion.div key={p.nom} className="infobulle large" initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -6 }} transition={{ duration: 0.18 }}>
          <b>{p.titre}</b>
          {p.lignes.map((l) => <span key={l}>{l}</span>)}
          <span className="gris">Une pioche experte, parfaite ou de maître donne 10, 20 ou 30 % de chances d'un minerai en plus.</span>
        </motion.div>
      </AnimatePresence>
      <div className="dinos">
        <span className="pix">À côté : les armures de dinosaures, forgées à l'enclume</span>
        <div className="dinos-grille">
          {DINOS.map(([nom, el, usage, col]) => (
            <div key={nom} className="dino" style={{ '--c': col } as React.CSSProperties}>
              <span className="pix">{el}</span><b>{nom}</b><span>{usage}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

const TAMPONS = [
  ['Apercevoir', "L'avoir en vue, sans obstacle : la silhouette à l'encre prend ses couleurs."],
  ['Photographier', "La prendre en photo avec un appareil d'Exposure."],
  ['Apprivoiser ou héberger', "L'apprivoiser, la faire naître ou la mettre en lieu sûr."],
  ['Maîtriser', "Avoir les trois autres tampons et l'observer une minute."],
]
const MILIEUX: [string, number, string][] = [['Terre ferme', 130, '#6ea05a'], ['Grand bleu', 96, '#4f9fc2'], ['Plein ciel', 31, '#8fb7dc'], ['Bêtes noires', 8, '#9a5fc0']]

function Zoo() {
  const [poses, setPoses] = useState<boolean[]>([false, false, false, false])
  const [dernier, setDernier] = useState<number | null>(null)
  return (
    <div className="media-zoo">
      <div className="page-zoo">
        <div className="tampons">
          {TAMPONS.map(([nom], k) => (
            <button
              key={nom}
              type="button"
              aria-pressed={poses[k]}
              className={`tampon${poses[k] ? ' pose' : ''}`}
              onClick={() => { setPoses((p) => p.map((v, j) => (j === k ? !v : v))); setDernier(k) }}
            >
              <span className="encre">{k + 1}</span>
              <span className="pix">{nom}</span>
            </button>
          ))}
        </div>
        <p className="tampon-texte" aria-live="polite">
          {dernier === null ? 'Cliquez sur un tampon pour le poser.' : `${TAMPONS[dernier][0]} : ${TAMPONS[dernier][1]}`}
          {poses.every(Boolean) && ' La page est complète.'}
        </p>
      </div>
      <div className="milieux">
        {MILIEUX.map(([nom, n, col]) => (
          <div key={nom} className="milieu">
            <span>{nom}</span>
            <span className="barre"><motion.i style={{ background: col }} initial={{ width: 0 }} animate={{ width: `${(n / 130) * 100}%` }} transition={{ duration: 0.9, ease: 'easeOut' }} /></span>
            <b>{n}</b>
          </div>
        ))}
        <span className="gris">265 espèces comptées sur un serveur d'essai avec le pack complet.</span>
      </div>
    </div>
  )
}

function CarteMesure() {
  const [l, setL] = useState<{ titre: string; texte: string; fx: number; fy: number } | null>(null)
  const bouger = (e: React.PointerEvent<HTMLDivElement>) => {
    const r = e.currentTarget.getBoundingClientRect()
    const fx = (e.clientX - r.left) / r.width, fy = (e.clientY - r.top) / r.height
    setL({ ...lireCarte(fx, fy), fx, fy })
  }
  return (
    <div className="media-carte">
      <div className="carte-mesure" onPointerMove={bouger} onPointerDown={bouger}>
        <img src={CARTE_SRC} alt="Carte des biomes du disque" draggable={false} />
        <svg viewBox="0 0 100 100" aria-hidden="true">
          <circle cx="50" cy="50" r={(355 / 760) * 100} fill="none" stroke="var(--lanterne)" strokeWidth="0.3" strokeDasharray="1 1" />
          {l && <line x1="50" y1="50" x2={l.fx * 100} y2={l.fy * 100} stroke="var(--lanterne)" strokeWidth="0.5" />}
          <circle cx="50" cy="50" r="0.8" fill="var(--lanterne)" />
        </svg>
      </div>
      <div className="infobulle large">
        <b>{l ? l.titre : 'Mesurer'}</b>
        <span>{l ? l.texte : 'Passez sur la carte pour lire la distance au centre, le climat et le temps de marche.'}</span>
      </div>
    </div>
  )
}

export function MediaVue({ media }: { media: Media }) {
  switch (media.type) {
    case 'image': return <ImageLoupe src={media.src} alt={media.alt} zooms={media.zooms} />
    case 'comparer': return <Comparateur media={media} />
    case 'echelle': return <Echelle />
    case 'zoo': return <Zoo />
    case 'carte': return <CarteMesure />
  }
}
