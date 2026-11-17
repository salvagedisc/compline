import { useState } from 'react'
import { motion } from 'motion/react'
import { CHANTIERS, JOURNAL, LIEUX, STATUTS, type Statut } from '../data/lieux'

interface Props { onFermer: () => void; onLieu: (id: string) => void; reduit: boolean }

function Cadre({ titre, sous, onFermer, reduit, children }: { titre: string; sous: string; onFermer: () => void; reduit: boolean; children: React.ReactNode }) {
  return (
    <motion.div
      className="voile-panneau"
      initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
      transition={{ duration: reduit ? 0 : 0.2 }}
      onClick={(e) => { if (e.target === e.currentTarget) onFermer() }}
    >
      <motion.div
        className="panneau" role="dialog" aria-modal="true" aria-labelledby="panneau-titre"
        initial={{ y: reduit ? 0 : 24, opacity: 0 }} animate={{ y: 0, opacity: 1 }} exit={{ y: reduit ? 0 : 16, opacity: 0 }}
        transition={{ duration: reduit ? 0 : 0.3, ease: [0.2, 0.7, 0.2, 1] }}
      >
        <header className="panneau-tete">
          <div><span className="pix accent">{sous}</span><h1 id="panneau-titre">{titre}</h1></div>
          <button type="button" className="fermer" onClick={onFermer}>Fermer <kbd>Échap</kbd></button>
        </header>
        <div className="panneau-corps">{children}</div>
      </motion.div>
    </motion.div>
  )
}

export function Avancement({ onFermer, onLieu, reduit }: Props) {
  const [filtre, setFiltre] = useState<Statut | 'tous'>('tous')
  const compte = (s: Statut) => CHANTIERS.filter((c) => c.statut === s).length
  const ordre: Statut[] = ['teste', 'atester', 'encours', 'prevu']
  const nomLieu = (id?: string) => LIEUX.find((l) => l.id === id)
  return (
    <Cadre titre="Avancement" sous="Où en est-on · 17 novembre 2026" onFermer={onFermer} reduit={reduit}>
      <p className="panneau-intro">Presque tout est construit et vérifié sur un banc d'essai, mais le pack n'a pas encore été joué du début à la fin. Prochaine étape : des sessions de test entre amis.</p>
      <div className="jauge" role="img" aria-label={ordre.map((s) => `${STATUTS[s].nom} : ${compte(s)}`).join(', ')}>
        {ordre.map((s) => <i key={s} className={`s-${s}`} style={{ flexGrow: compte(s) }} />)}
      </div>
      <div className="filtres" role="group" aria-label="Filtrer par état">
        <button type="button" className="filtre" aria-pressed={filtre === 'tous'} onClick={() => setFiltre('tous')}>Tout <span className="nb">{CHANTIERS.length}</span></button>
        {ordre.map((s) => (
          <button key={s} type="button" className="filtre" aria-pressed={filtre === s} onClick={() => setFiltre(s)}>
            <span className={`puce s-${s}`} />{STATUTS[s].nom} <span className="nb">{compte(s)}</span>
          </button>
        ))}
      </div>
      <ul className="chantiers">
        {CHANTIERS.filter((c) => filtre === 'tous' || c.statut === filtre).map((c) => {
          const l = nomLieu(c.lieu)
          return (
            <li key={c.titre} className={`chantier s-${c.statut}`}>
              <span className="pix etat">{STATUTS[c.statut].nom}</span>
              <b>{c.titre}</b>
              <span>{c.texte}</span>
              <span className="pied-chantier">
                {c.quand && <span className="gris">{c.quand}</span>}
                {l && <button type="button" className="lien-lieu" onClick={() => onLieu(l.id)}>Voir {l.nom.toLowerCase()} →</button>}
              </span>
            </li>
          )
        })}
      </ul>
    </Cadre>
  )
}

export function Journal({ onFermer, reduit }: Props) {
  return (
    <Cadre titre="Journal" sous="Les grandes étapes" onFermer={onFermer} reduit={reduit}>
      <ol className="journal">
        {JOURNAL.map((j) => (
          <li key={j.date}><span className="pix accent">{j.date}</span><p><b>{j.titre}.</b> {j.texte}</p></li>
        ))}
      </ol>
    </Cadre>
  )
}
