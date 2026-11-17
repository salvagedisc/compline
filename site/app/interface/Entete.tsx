// The top line of every page: the name, where we are, the sound and the index.
import { Link, useLocation } from 'react-router'
import { LIEUX } from '../donnees/lieux'
import { ROMAINS } from '../donnees/details'
import { Icone } from './icones'
import { basculerSon, ouvrirIndex, useEtat } from './etat'
import { couperSon, demarrerSon } from './son'

export function Entete({ inerte }: { inerte: boolean }) {
  const { pathname } = useLocation()
  const { son, index } = useEtat()
  const premier = pathname.split('/').filter(Boolean)[0]
  const i = LIEUX.findIndex((l) => l.chemin === premier)
  const ici = i >= 0 ? `${ROMAINS[i]} · ${LIEUX[i].nom}` : premier === 'journal' ? 'Journal' : premier === 'feuille-de-route' ? 'Feuille de route' : null

  const sonner = () => {
    if (son) couperSon(); else demarrerSon()
    basculerSon()
  }

  return (
    <header className="entete" inert={inerte || undefined}>
      <Link viewTransition prefetch="intent" to="/" className="marque" aria-label="Compline, retour au disque">Compline</Link>
      {ici && <span className="ici" aria-hidden="true">{ici}</span>}
      <div className="entete-actions">
        <button type="button" className="entete-bouton seul-js" onClick={sonner} aria-pressed={son} aria-label={son ? 'Couper le son' : 'Mettre le son'}>
          <Icone nom={son ? 'son' : 'muet'} taille={22} />
        </button>
        <button type="button" className="entete-bouton avec-texte seul-js" onClick={() => ouvrirIndex(!index)} aria-expanded={index} aria-controls="index">
          <Icone nom={index ? 'fermer' : 'index'} taille={22} />
          <span>{index ? 'Fermer' : 'Index'}</span>
        </button>
      </div>
    </header>
  )
}
