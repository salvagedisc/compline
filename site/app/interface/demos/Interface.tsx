// The game's bar and gauges fade out after six seconds without a gesture and come back in a quarter of a second,
// unless something is wrong. Here the page does the same with two real captures of the same view, with and without
// the interface: stop moving and watch it go.
import { useEffect, useRef, useState } from 'react'
import { Capture } from '../Capture'

const REPOS = 6 // idleSeconds

const DANGERS = [
  { id: 'vie', nom: 'Vie à 30 %' },
  { id: 'soif', nom: 'Soif à 6' },
  { id: 'froid', nom: 'Froid à −50' },
] as const
type Danger = (typeof DANGERS)[number]['id']

export default function Interface() {
  const racine = useRef<HTMLDivElement>(null)
  const [reste, setReste] = useState(REPOS)
  const [dangers, setDangers] = useState<Set<Danger>>(new Set())
  const dernier = useRef(0)

  useEffect(() => {
    dernier.current = performance.now()
    // Any gesture on the page counts, as a key or a click does in the game.
    const geste = () => { dernier.current = performance.now(); setReste(REPOS) }
    const evts = ['pointermove', 'pointerdown', 'keydown', 'wheel', 'touchstart'] as const
    evts.forEach((e) => window.addEventListener(e, geste, { passive: true }))
    const id = window.setInterval(() => {
      if (!racine.current?.closest('.couche.la, .couche.entre')) return
      setReste(Math.max(0, REPOS - (performance.now() - dernier.current) / 1000))
    }, 100)
    return () => { evts.forEach((e) => window.removeEventListener(e, geste)); window.clearInterval(id) }
  }, [])

  const garde = dangers.size > 0
  const visible = garde || reste > 0
  const basculer = (d: Danger) => setDangers((s) => { const n = new Set(s); if (n.has(d)) n.delete(d); else n.add(d); return n })

  return (
    <div ref={racine} className={`scene demo demo-interface${visible ? '' : ' au-repos'}`}>
      <div className="scene-fond-sombre" aria-hidden="true"><Capture nom="ecran-sans.jpg" alt="" prioritaire /></div>
      <div className="interface-zone">
        <div className="interface-cadre">
          <Capture nom="ecran-sans.jpg" alt="La même clairière, sans rien à l'écran" prioritaire sizes="(max-width: 760px) 100vw, 60vw" />
          <div className="interface-avec"><Capture nom="ecran-avec.jpg" alt="La clairière avec la barre d'action et les jauges" prioritaire sizes="(max-width: 760px) 100vw, 60vw" /></div>
        </div>
        <div className="interface-etat" aria-live="polite">
          <div className="interface-jauge" style={{ '--p': garde ? 1 : reste / REPOS } as React.CSSProperties}><span /></div>
          <p>
            {garde ? <><b>L'interface reste</b> tant qu'il y a un danger.</>
              : visible ? <><b>Ne bougez plus</b> : elle s'efface dans {Math.ceil(reste)} s.</>
                : <><b>Au repos</b> : rien à l'écran. Bougez pour la rappeler.</>}
          </p>
        </div>
        <div className="interface-dangers">
          <span className="petites-caps">Ce qui la garde</span>
          <div className="demo-choix" role="group" aria-label="Dangers">
            {DANGERS.map((d) => (
              <button key={d.id} type="button" aria-pressed={dangers.has(d.id)} onClick={() => basculer(d.id)}>{d.nom}</button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
