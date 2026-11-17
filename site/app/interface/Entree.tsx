// The title screen: the disc comes out of the mist while it loads, then the visitor chooses sound or silence.
import { useEffect } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { entrer, useEtat } from './etat'
import { demarrerSon } from './son'

const CLE = 'compline-entre'

export function Entree() {
  const { entre, mondePret } = useEtat()

  // Within one visit the title screen shows once; later pages open directly, without sound.
  useEffect(() => {
    try { if (sessionStorage.getItem(CLE)) entrer(false) } catch { /* storage blocked */ }
  }, [])

  const choisir = (son: boolean) => {
    try { sessionStorage.setItem(CLE, '1') } catch { /* storage blocked */ }
    if (son) demarrerSon()
    entrer(son)
  }

  return (
    <AnimatePresence>
      {!entre && (
        <motion.div
          className="entree"
          role="dialog"
          aria-modal="true"
          aria-labelledby="entree-titre"
          exit={{ opacity: 0, filter: 'blur(6px)' }}
          transition={{ duration: 1.1, ease: [0.4, 0, 0.2, 1] }}
        >
          <noscript><style>{'.entree{display:none}'}</style></noscript>
          <div className="entree-corps">
            <p className="sc entree-sur">Un modpack Minecraft entre amis</p>
            <h1 id="entree-titre" className="entree-titre">Compline</h1>
            <p className="entree-texte">Un monde rond de douze kilomètres, et la brume tout autour.</p>
            <div className="entree-charge" aria-live="polite">
              <span className={`entree-jauge${mondePret ? ' pleine' : ''}`} aria-hidden="true"><i /></span>
              <span className="pix">{mondePret ? 'Le disque est sorti de la brume' : 'Le disque sort de la brume…'}</span>
            </div>
            <div className="entree-choix">
              <button type="button" className="bouton principal" onClick={() => choisir(true)} autoFocus>Entrer avec le son</button>
              <button type="button" className="bouton" onClick={() => choisir(false)}>Entrer sans le son</button>
            </div>
            <p className="sc entree-pied">Le son : du vent et des cloches lointaines, fabriqués dans le navigateur.</p>
          </div>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
