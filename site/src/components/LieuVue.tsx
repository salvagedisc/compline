import { useEffect, useState } from 'react'
import { AnimatePresence, motion } from 'motion/react'
import { STATUTS, type Lieu } from '../data/lieux'
import { MediaVue } from './Medias'
import { Pixel } from './Pixel'

interface Props { lieu: Lieu; onFermer: () => void; reduit: boolean }

export function LieuVue({ lieu, onFermer, reduit }: Props) {
  const [i, setI] = useState(0)
  const f = lieu.fonctions[Math.min(i, lieu.fonctions.length - 1)]

  useEffect(() => { setI(0) }, [lieu.id])
  useEffect(() => {
    const touche = (e: KeyboardEvent) => {
      if ((e.target as HTMLElement)?.tagName === 'INPUT') return
      if (e.key === 'ArrowDown') { e.preventDefault(); setI((k) => (k + 1) % lieu.fonctions.length) }
      if (e.key === 'ArrowUp') { e.preventDefault(); setI((k) => (k - 1 + lieu.fonctions.length) % lieu.fonctions.length) }
    }
    window.addEventListener('keydown', touche)
    return () => window.removeEventListener('keydown', touche)
  }, [lieu])

  const t = reduit ? { duration: 0 } : { duration: 0.5, ease: [0.2, 0.7, 0.2, 1] as const }

  return (
    <motion.section
      className="lieu"
      aria-labelledby="lieu-titre"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1, transition: { ...t, delay: reduit ? 0 : 0.75 } }}
      exit={{ opacity: 0, transition: { duration: reduit ? 0 : 0.25 } }}
    >
      <div className="lieu-fond" style={{ backgroundImage: `url(${lieu.fond})` }} aria-hidden="true" />
      <div className="lieu-grille">
        <motion.aside className="lieu-rail" initial={{ x: -24, opacity: 0 }} animate={{ x: 0, opacity: 1, transition: { ...t, delay: reduit ? 0 : 0.85 } }}>
          <button type="button" className="retour" onClick={onFermer}>
            <span aria-hidden="true">←</span> La carte <kbd>Échap</kbd>
          </button>
          <div className="lieu-tete">
            <Pixel nom={lieu.id} taille={40} />
            <div>
              <span className="pix accent">{lieu.theme}</span>
              <h1 id="lieu-titre">{lieu.nom}</h1>
            </div>
          </div>
          <p className="lieu-intro">{lieu.intro}</p>
          <nav aria-label={`Fonctions : ${lieu.theme}`}>
            <ol className="fonctions">
              {lieu.fonctions.map((g, k) => (
                <li key={g.id}>
                  <button type="button" aria-current={k === i} className="fonction" onClick={() => setI(k)}>
                    <span className={`puce s-${g.statut}`} title={STATUTS[g.statut].nom} />
                    <span className="fonction-titre">{g.titre}</span>
                  </button>
                </li>
              ))}
            </ol>
          </nav>
          <p className="aide pix">↑ ↓ pour passer d'une fonction à l'autre</p>
        </motion.aside>

        <div className="lieu-scene">
          <AnimatePresence mode="wait">
            <motion.div
              key={`${lieu.id}-${f.id}`}
              className="scene"
              initial={{ opacity: 0, y: reduit ? 0 : 12 }}
              animate={{ opacity: 1, y: 0, transition: { ...t, delay: reduit ? 0 : 0.05 } }}
              exit={{ opacity: 0, y: reduit ? 0 : -8, transition: { duration: reduit ? 0 : 0.15 } }}
            >
              <div className="scene-media"><MediaVue media={f.media} /></div>
              <div className="scene-texte">
                <div className="scene-entete">
                  <h2>{f.titre}</h2>
                  <span className={`statut s-${f.statut}`}><span className="puce" />{STATUTS[f.statut].nom}</span>
                </div>
                <p>{f.texte}</p>
                {f.points && <ul className="points">{f.points.map((p) => <li key={p}>{p}</li>)}</ul>}
                {'provisoire' in f.media && f.media.provisoire && (
                  <p className="provisoire pix">Capture provisoire · la version HD arrive</p>
                )}
              </div>
            </motion.div>
          </AnimatePresence>
        </div>
      </div>
    </motion.section>
  )
}
