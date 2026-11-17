// Home: the disc seen from the evening sky. The title sits low on the left, the seven places are pinned on the
// map (see Reperes), and the latest news waits in a corner.
import { Link } from 'react-router'
import type { Route } from './+types/accueil'
import { CHANTIERS, JOURNAL } from '../donnees/lieux'
import { metas } from '../donnees/site'
import { grande } from '../donnees/images'
import { estNouveau, useDejaVu } from '../interface/visite'
import { ouvrirIndex } from '../interface/etat'

export const meta: Route.MetaFunction = () => metas(
  'Compline',
  "Un modpack Minecraft entre amis : un monde rond de douze kilomètres sous la brume. Ce qu'on y trouve, et où en est le projet.",
  grande('bru-vallee.jpg'),
)

export default function Accueil() {
  const vu = useDejaVu()
  const dernier = JOURNAL[JOURNAL.length - 1]
  const nouveaux = JOURNAL.filter((e) => estNouveau(e.iso, vu)).length
  const faits = CHANTIERS.filter((c) => c.statut === 'teste' || c.statut === 'atester').length
  return (
    <div className="accueil">
      <section className="accueil-titre" aria-labelledby="titre-accueil">
        <p className="petites-caps accueil-sur">Modpack Minecraft · 1.21.1 · en préparation</p>
        <h1 id="titre-accueil" className="titre-geant">Compline</h1>
        <p className="accueil-accroche">
          Un seul continent, rond, large de douze kilomètres. Froid au nord, chaud au sud, et au bord une brume
          que personne ne traverse. On y survit à plusieurs : pas de monstres la nuit, mais le froid, la soif et le prix de chaque mort.
        </p>
        <p className="accueil-aide">
          <span className="seul-pointeur">Choisissez un lieu sur le disque</span>
          <button type="button" className="lien-trait seul-js" onClick={() => ouvrirIndex(true)}>Ouvrir l'index</button>
        </p>
      </section>

      <aside className="accueil-nouveau" aria-labelledby="titre-nouveau">
        <p className="petites-caps">
          <span id="titre-nouveau">Dernière page du journal</span>
          {nouveaux > 0 && <span className="pastille">{nouveaux} depuis votre visite</span>}
        </p>
        <Link to="/journal/" className="accueil-nouveau-lien">
          <span className="date">{dernier.date}</span>
          <b>{dernier.titre}</b>
          <span>{dernier.texte}</span>
        </Link>
        <Link to="/feuille-de-route/" className="accueil-avance">
          <span className="jauge" aria-hidden="true"><i style={{ width: `${(faits / CHANTIERS.length) * 100}%` }} /></span>
          {faits} chantiers sur {CHANTIERS.length} construits
        </Link>
      </aside>
      <p className="mention">Projet entre amis, non officiel, sans lien avec Mojang ni Microsoft.</p>
    </div>
  )
}
