// The title screen: the disc lies under thick mist while it loads; entering lifts the mist.
// The visitor chooses sound or silence; within one visit the screen shows only once.
import { useEffect, useState } from 'react'
import { entrer, useEtat } from './etat'
import { demarrerSon } from './son'

const CLE = 'compline-entre'

export function Entree() {
  const { entre, mondePret } = useEtat()
  const [parti, setParti] = useState(false)

  useEffect(() => {
    try { if (sessionStorage.getItem(CLE)) { entrer(false); setParti(true) } } catch { /* storage blocked */ }
  }, [])

  useEffect(() => {
    if (!entre) return
    const t = window.setTimeout(() => setParti(true), 1600)
    return () => window.clearTimeout(t)
  }, [entre])

  const choisir = (son: boolean) => {
    try { sessionStorage.setItem(CLE, '1') } catch { /* storage blocked */ }
    if (son) demarrerSon()
    entrer(son)
  }

  if (parti) return null
  return (
    <div className={`entree${entre ? ' sortie' : ''}`} role="dialog" aria-modal="true" aria-labelledby="entree-titre">
      <div className="entree-corps">
        <p className="petites-caps entree-sur">Un modpack Minecraft entre amis</p>
        <p id="entree-titre" className="entree-titre" aria-label="Compline">
          {'Compline'.split('').map((c, i) => <span key={i} style={{ '--i': i } as React.CSSProperties} aria-hidden="true">{c}</span>)}
        </p>
        <p className="entree-texte">Un monde rond de douze kilomètres, le soir qui tombe, et la brume tout autour.</p>
        <div className="entree-choix">
          <button type="button" className="bouton-plein" onClick={() => choisir(true)} autoFocus disabled={entre}>
            Entrer
          </button>
          <button type="button" className="lien-trait" onClick={() => choisir(false)} disabled={entre}>
            Entrer sans le son
          </button>
        </div>
        <p className="entree-etat" aria-live="polite">
          <span className={`entree-jauge${mondePret ? ' pleine' : ''}`} aria-hidden="true" />
          {mondePret ? 'Le disque attend sous la brume' : 'Le disque se dessine…'}
        </p>
      </div>
      <p className="entree-pied">Le son : le vent et des cloches lointaines, joués par le navigateur.</p>
    </div>
  )
}
