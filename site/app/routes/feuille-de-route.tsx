// The road map as four columns, from what has been tried in game to what is still only planned.
import { useState } from 'react'
import { Link } from 'react-router'
import { motion } from 'motion/react'
import type { Route } from './+types/feuille-de-route'
import { CHANTIERS, LIEUX, STATUTS, type Statut } from '../donnees/lieux'
import { MAJ, metas } from '../donnees/site'
import { Titre } from '../interface/Titre'

export const meta: Route.MetaFunction = () => metas('La feuille de route', 'Ce qui est fait, ce qui reste à essayer en jeu, ce qui est en cours et ce qui est prévu.')

const ORDRE: Statut[] = ['teste', 'atester', 'encours', 'prevu']
const AIDE: Record<Statut, string> = {
  teste: 'Joué et validé en partie réelle.',
  atester: 'Construit et vérifié au banc, à jouer entre amis.',
  encours: 'En train de se construire.',
  prevu: 'Décidé, pas encore commencé.',
}

export default function FeuilleDeRoute() {
  const [colonne, setColonne] = useState<Statut>('atester') // shown alone on narrow screens
  const compte = (s: Statut) => CHANTIERS.filter((c) => c.statut === s).length
  return (
    <section className="page-route" aria-labelledby="titre-route">
      <header className="page-tete">
        <p className="sc accent">Mis à jour le {MAJ}</p>
        <Titre id="titre-route">La feuille de route</Titre>
        <p>Presque tout est construit et vérifié sur un banc d'essai, mais le pack n'a pas encore été joué du début à la fin. Prochaine étape : des sessions de test entre amis.</p>
        <div className="jauge" role="img" aria-label={ORDRE.map((s) => `${STATUTS[s].nom} : ${compte(s)}`).join(', ')}>
          {ORDRE.map((s, k) => (
            <motion.i key={s} className={`s-${s}`} initial={{ flexGrow: 0 }} animate={{ flexGrow: compte(s) }} transition={{ delay: 0.3 + k * 0.1, duration: 0.9, ease: [0.2, 0.7, 0.2, 1] }} />
          ))}
        </div>
      </header>
      <div className="onglets-route" role="tablist" aria-label="Colonnes">
        {ORDRE.map((s) => (
          <button key={s} type="button" role="tab" aria-selected={colonne === s} className={`onglet s-${s}`} onClick={() => setColonne(s)}>
            <span className="puce" aria-hidden="true" />{STATUTS[s].court} <span className="nb">{compte(s)}</span>
          </button>
        ))}
      </div>
      <div className="colonnes">
        {ORDRE.map((s, k) => (
          <motion.section
            key={s}
            className={`colonne s-${s}${colonne === s ? ' choisie' : ''}`}
            aria-labelledby={`col-${s}`}
            initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 + k * 0.1, duration: 0.6 }}
          >
            <header>
              <h2 id={`col-${s}`}><span className="puce" aria-hidden="true" />{STATUTS[s].nom}<span className="nb">{compte(s)}</span></h2>
              <p className="gris">{AIDE[s]}</p>
            </header>
            <ul>
              {CHANTIERS.filter((c) => c.statut === s).map((c) => {
                const lieu = LIEUX.find((l) => l.id === c.lieu)
                return (
                  <li key={c.titre} className="fiche">
                    <b>{c.titre}</b>
                    <span>{c.texte}</span>
                    {(c.quand || lieu) && (
                      <span className="fiche-pied">
                        {c.quand && <span className="gris">{c.quand}</span>}
                        {lieu && <Link to={`/${lieu.chemin}`}>{lieu.nom} →</Link>}
                      </span>
                    )}
                  </li>
                )
              })}
            </ul>
          </motion.section>
        ))}
      </div>
    </section>
  )
}
