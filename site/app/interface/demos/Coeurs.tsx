// The price of death, by the pack's own script: ten hearts to start, one lost at each death, never fewer than
// five; a life shard gives one back, refused at ten. The lines are the game's own messages.
import { useEffect, useRef, useState } from 'react'
import { Capture } from '../Capture'

const DEPART = 10, PLANCHER = 5

// Chances that one roll of a loot table gives a shard (sante_butin.js), the likeliest first.
const BUTIN: [string, number][] = [
  ['Temple de la jungle', 25], ['Trésor d’épave', 15], ['Grande ruine sous-marine', 12], ['Donjon', 10],
  ['Pyramide du désert', 5], ['Réserve d’épave', 5], ['Puits du désert, fouille', 5],
]

/** A heart of our own drawing, cell by cell: outline, fill, shine and shade. */
const COEUR = [
  '.kkk...kkk.',
  'kwwrk.krrdk',
  'kwrrrkrrrdk',
  'krrrrrrrrdk',
  '.krrrrrrdk.',
  '..krrrrdk..',
  '...krrdk...',
  '....kdk....',
  '.....k.....',
]
function Coeur({ etat }: { etat: 'plein' | 'perdu' | 'casse' | 'rendu' }) {
  return (
    <svg className={`coeur coeur-${etat}`} viewBox="0 0 11 9" shapeRendering="crispEdges" aria-hidden="true">
      {COEUR.flatMap((l, y) => [...l].map((c, x) => c !== '.' && <rect key={`${x}-${y}`} x={x} y={y} width="1.02" height="1.02" className={`c-${c}`} />))}
    </svg>
  )
}

export default function Coeurs() {
  const [coeurs, setCoeurs] = useState(DEPART)
  const [morts, setMorts] = useState(0)
  const [ecran, setEcran] = useState<null | 'mort'>(null)
  const [casse, setCasse] = useState<number | null>(null)
  const [rendu, setRendu] = useState<number | null>(null)
  const [ligne, setLigne] = useState<{ texte: string; ton: 'gris' | 'rouge'; n: number } | null>(null)
  const minuteries = useRef<number[]>([])
  useEffect(() => () => minuteries.current.forEach(clearTimeout), [])
  const plus_tard = (f: () => void, ms: number) => { minuteries.current.push(window.setTimeout(f, ms)) }
  const dire = (texte: string, ton: 'gris' | 'rouge') => setLigne((l) => ({ texte, ton, n: (l?.n ?? 0) + 1 }))

  const mourir = () => {
    if (ecran) return
    setEcran('mort')
    setMorts((m) => m + 1)
    plus_tard(() => {
      setEcran(null)
      if (coeurs > PLANCHER) {
        const reste = coeurs - 1
        setCasse(reste)
        setCoeurs(reste)
        dire(reste === PLANCHER
          ? `Vous vous réveillez. Il vous reste ${reste} cœurs, vous ne faiblirez pas davantage.`
          : `Vous vous réveillez plus faible. Il vous reste ${reste} cœurs.`, 'gris')
        plus_tard(() => setCasse(null), 900)
      } else dire(`Vous vous réveillez. Il vous reste ${coeurs} cœurs, vous ne faiblirez pas davantage.`, 'gris')
    }, 1500)
  }
  const eclat = () => {
    if (ecran) return
    if (coeurs >= DEPART) { dire(`Votre santé est déjà entière : ${DEPART} cœurs.`, 'gris'); return }
    setRendu(coeurs)
    setCoeurs(coeurs + 1)
    dire(`Un cœur vous revient : ${coeurs + 1} / ${DEPART}.`, 'rouge')
    plus_tard(() => setRendu(null), 900)
  }

  return (
    <div className="scene demo demo-coeurs">
      <div className="scene-fond-sombre" aria-hidden="true"><Capture nom="mort-coeur.jpg" alt="" prioritaire /></div>
      <div className="coeurs-scene">
        <div className="coeurs-barre" role="img" aria-label={`${coeurs} cœurs sur ${DEPART}`}>
          {Array.from({ length: DEPART }, (_, i) => (
            <Coeur key={i} etat={i === casse ? 'casse' : i === rendu ? 'rendu' : i < coeurs ? 'plein' : 'perdu'} />
          ))}
        </div>
        <p className="coeurs-compte"><b>{coeurs}</b> / {DEPART} cœurs<span>{morts ? ` · ${morts} mort${morts > 1 ? 's' : ''}` : ''}</span></p>
        <div className="coeurs-actions">
          <button type="button" className="demo-bouton mourir" onClick={mourir} disabled={!!ecran}>Mourir</button>
          <button type="button" className="demo-bouton" onClick={eclat} disabled={!!ecran}>
            <svg viewBox="0 0 8 8" width="18" height="18" shapeRendering="crispEdges" aria-hidden="true"><path d="M3 0h2v1h1v2h1v2H6v2H5v1H3V7H2V5H1V3h1V1h1Z" fill="#e0606a" /><path d="M3 1h1v2H3Z" fill="#ffd0d4" /></svg>
            Utiliser un éclat de vie
          </button>
        </div>
        <p key={ligne?.n} className={`coeurs-ligne ${ligne?.ton ?? ''}`} aria-live="polite">{ligne?.texte}</p>
        <div className="coeurs-plancher" aria-hidden="true"><span>plancher : 5 cœurs</span></div>
      </div>
      <div className="demo-panneau">
        <p className="petites-caps">Où trouver des éclats</p>
        <dl className="demo-mesures">
          {BUTIN.map(([lieu, p]) => <div key={lieu}><dt>{lieu}</dt><dd>{p} %</dd></div>)}
        </dl>
        <p className="demo-note">Et un éclat par joueur au réveil de chaque Cœur ancien des monuments. Aucune créature n'en lâche, aucune recette n'en fait.</p>
      </div>
      <div className={`coeurs-mort${ecran ? ' vue' : ''}`} aria-hidden={!ecran}>
        <p>Vous êtes mort !</p>
      </div>
    </div>
  )
}
