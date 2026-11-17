import { Link } from 'react-router'
import { metas } from '../donnees/site'

export const meta = () => metas('Perdu dans la brume', "Cette page n'existe pas.")

export default function Introuvable() {
  return (
    <section className="perdu" aria-labelledby="titre-perdu">
      <p className="sc accent">Erreur 404</p>
      <h1 id="titre-perdu">Perdu dans la brume</h1>
      <p>Cette page n'existe pas, ou plus. Le disque, lui, est toujours là.</p>
      <Link className="bouton principal" to="/">Revenir au disque</Link>
    </section>
  )
}
