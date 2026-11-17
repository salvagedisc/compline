import { Link } from 'react-router'
import { motion } from 'motion/react'
import type { Route } from './+types/accueil'
import { CHANTIERS, JOURNAL, LIEUX } from '../donnees/lieux'
import { metas } from '../donnees/site'
import { grande } from '../donnees/images'
import { estNouveau, useDejaVu } from '../interface/visite'
import { Titre } from '../interface/Titre'

export const meta: Route.MetaFunction = () => metas(
  'Compline',
  "Un modpack Minecraft entre amis : un monde rond de douze kilomètres sous la brume. Où en est le projet.",
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
        <p className="sc accent">Minecraft 1.21.1 · NeoForge · en préparation</p>
        <Titre id="titre-accueil" className="geant">Compline</Titre>
        <motion.p className="accroche" initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.6, duration: 0.8 }}>
          Un seul continent, rond, large de douze kilomètres. Froid au nord, chaud au sud, et au bord une brume que personne ne traverse. On y survit à plusieurs, sans monstres de nuit mais avec le froid, la soif et le prix de chaque mort.
        </motion.p>
        <motion.div className="accueil-actions" initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: 0.9, duration: 0.8 }}>
          <Link className="bouton principal" to={`/${LIEUX[0].chemin}`}>Commencer la visite</Link>
          <Link className="bouton" to="/feuille-de-route">Où en est-on</Link>
        </motion.div>
        <p className="aide sc">Choisissez un lieu sur le disque<span className="clavier">, ou les touches 1 à 9</span></p>
        <p className="mention">Projet entre amis, non officiel, sans lien avec Mojang ni Microsoft.</p>
      </section>

      <motion.aside
        className="carte-nouveau"
        aria-labelledby="titre-nouveau"
        initial={{ opacity: 0, x: 24 }} animate={{ opacity: 1, x: 0 }} transition={{ delay: 1.1, duration: 0.8, ease: [0.2, 0.7, 0.2, 1] }}
      >
        <header>
          <h2 id="titre-nouveau" className="sc">Quoi de neuf</h2>
          {nouveaux > 0 ? <span className="pastille">{nouveaux} depuis ta visite</span> : <span className="sc date">{dernier.date}</span>}
        </header>
        <b>{dernier.titre}</b>
        <p>{dernier.texte}</p>
        <footer>
          <Link to="/journal">Tout le journal →</Link>
          <span className="gris">{faits} chantiers sur {CHANTIERS.length} construits</span>
        </footer>
      </motion.aside>
    </div>
  )
}
