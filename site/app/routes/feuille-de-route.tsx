// The road map as a ledger in four columns, from what has been played to what is still only planned.
import { useState } from 'react'
import { Link } from 'react-router'
import type { Route } from './+types/feuille-de-route'
import { CHANTIERS, LIEUX, STATUTS, type Statut } from '../donnees/lieux'
import { MAJ, metas } from '../donnees/site'

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
        <p className="petites-caps">Mis à jour le {MAJ}</p>
        <h1 id="titre-route" className="page-titre">La feuille de route</h1>
        <p>Presque tout est construit et vérifié sur un banc d'essai, mais le pack n'a pas encore été joué du début à la fin. Prochaine étape : des sessions de test entre amis.</p>
        <div className="jauge" role="img" aria-label={ORDRE.map((s) => `${STATUTS[s].nom} : ${compte(s)}`).join(', ')}>
          {ORDRE.map((s, k) => (
            <i key={s} className={`s-${s}`} style={{ '--n': compte(s), '--k': k } as React.CSSProperties} />
          ))}
        </div>
      </header>
      <div className="onglets-route" role="tablist" aria-label="Colonnes">
        {ORDRE.map((s) => (
          <button key={s} type="button" role="tab" aria-selected={colonne === s} className={`onglet s-${s}`} onClick={() => setColonne(s)}>
            <span className="statut-marque" aria-hidden="true" />{STATUTS[s].court} <span className="nb">{compte(s)}</span>
          </button>
        ))}
      </div>
      <div className="colonnes">
        {ORDRE.map((s, k) => (
          <section
            key={s}
            className={`colonne s-${s}${colonne === s ? ' choisie' : ''}`}
            aria-labelledby={`col-${s}`}
            style={{ '--k': k } as React.CSSProperties}
          >
            <header>
              <h2 id={`col-${s}`}><span className="statut-marque" aria-hidden="true" />{STATUTS[s].nom}<span className="nb">{compte(s)}</span></h2>
              <p className="colonne-aide">{AIDE[s]}</p>
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
                        {c.quand && <span>{c.quand}</span>}
                        {lieu && <Link to={`/${lieu.chemin}/`}>{lieu.nom}</Link>}
                      </span>
                    )}
                  </li>
                )
              })}
            </ul>
          </section>
        ))}
      </div>
    </section>
  )
}
