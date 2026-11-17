// The journal as a frieze: one station per milestone along a line drawn from the first day to today.
import { useEffect, useRef } from 'react'
import { Link } from 'react-router'
import { motion } from 'motion/react'
import type { Route } from './+types/journal'
import { JOURNAL, LIEUX } from '../donnees/lieux'
import { metas } from '../donnees/site'
import { estNouveau, useDejaVu } from '../interface/visite'
import { Titre } from '../interface/Titre'

export const meta: Route.MetaFunction = () => metas('Le journal', 'Les grandes étapes de Compline, du premier jour à aujourd’hui.')

const jours = (a: string, b: string) => Math.round((Date.parse(b) - Date.parse(a)) / 864e5)

export default function Journal() {
  const vu = useDejaVu()
  const frise = useRef<HTMLDivElement>(null)
  const debut = JOURNAL[0].iso, fin = JOURNAL[JOURNAL.length - 1].iso
  const total = jours(debut, fin)

  // Start on the newest milestone; the mouse wheel scrolls the frieze sideways.
  useEffect(() => {
    const f = frise.current
    if (!f) return
    f.scrollLeft = f.scrollWidth
    const roue = (e: WheelEvent) => {
      if (Math.abs(e.deltaY) > Math.abs(e.deltaX)) { f.scrollLeft += e.deltaY; e.preventDefault() }
    }
    f.addEventListener('wheel', roue, { passive: false })
    return () => f.removeEventListener('wheel', roue)
  }, [])

  return (
    <section className="page-journal" aria-labelledby="titre-journal">
      <header className="page-tete">
        <p className="pix accent">Jour {total + 1} du projet</p>
        <Titre id="titre-journal">Le journal</Titre>
        <p>Du dépôt vide du 23 septembre au pack d'aujourd'hui, les étapes qui ont compté.</p>
      </header>
      <div className="frise" ref={frise} tabIndex={0} role="region" aria-label="Frise des étapes, de la plus ancienne à la plus récente">
        <ol className="frise-piste">
          {JOURNAL.map((e, k) => {
            const lieu = LIEUX.find((l) => l.id === e.lieu)
            const neuf = estNouveau(e.iso, vu)
            return (
              <li key={e.iso} className={`etape${k % 2 ? ' bas' : ' haut'}${neuf ? ' nouveau' : ''}`}>
                <motion.div
                  className="etape-corps"
                  initial={{ opacity: 0, y: k % 2 ? -14 : 14 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.3 + k * 0.08, duration: 0.6 }}
                >
                  <time className="pix" dateTime={e.iso}>{e.date}</time>
                  <h2>{e.titre}</h2>
                  <p>{e.texte}</p>
                  {neuf && <span className="pastille pix">Nouveau</span>}
                  {lieu && <Link to={`/${lieu.chemin}`} className="lien-lieu">Voir {lieu.nom.toLowerCase()} →</Link>}
                </motion.div>
                <motion.span className="etape-borne" aria-hidden="true" initial={{ scale: 0 }} animate={{ scale: 1 }} transition={{ delay: 0.2 + k * 0.08, duration: 0.3 }} />
              </li>
            )
          })}
        </ol>
      </div>
      <p className="pix gris aide">Faire défiler la frise, ou les flèches du clavier</p>
    </section>
  )
}
