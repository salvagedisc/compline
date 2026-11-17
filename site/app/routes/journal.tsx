// The journal as a reel: one milestone at a time over its picture, the milestones strung along a line at the
// bottom from the first day to today. Arrows and keys move along it; it opens on the newest.
import { useCallback, useEffect, useLayoutEffect, useRef, useState } from 'react'
import { flushSync } from 'react-dom'
import { Link } from 'react-router'
import type { Route } from './+types/journal'
import { JOURNAL, LIEUX } from '../donnees/lieux'
import { metas } from '../donnees/site'
import { Icone } from '../interface/icones'
import { Plan } from '../interface/scenes/Plan'
import { lireEtat } from '../interface/etat'
import { estNouveau, useDejaVu } from '../interface/visite'
import { SENS } from '../interface/transitions'

export const meta: Route.MetaFunction = () => metas('Le journal', 'Les grandes étapes de Compline, du premier jour à aujourd’hui.')

const jours = (a: string, b: string) => Math.round((Date.parse(b) - Date.parse(a)) / 864e5)
const rien = () => {}

export default function Journal() {
  const vu = useDejaVu()
  const [k, setKBrut] = useState(JOURNAL.length - 1)
  const actuel = useRef(k)
  useLayoutEffect(() => { actuel.current = k }, [k])
  // Moving along the reel plays the same transition as a change of page, the picture sliding the way we go.
  const setK = useCallback((suite: number | ((n: number) => number)) => {
    const n = typeof suite === 'function' ? suite(actuel.current) : suite
    if (n === actuel.current) return
    const changer = () => flushSync(() => setKBrut(n))
    SENS(n > actuel.current ? 'avant' : 'arriere')
    if (document.startViewTransition && !matchMedia('(prefers-reduced-motion: reduce)').matches) document.startViewTransition(changer)
    else changer()
  }, [])
  const e = JOURNAL[k]
  const lieu = LIEUX.find((l) => l.id === e.lieu)
  const debut = JOURNAL[0].iso

  useEffect(() => {
    const f = (ev: KeyboardEvent) => {
      if (ev.metaKey || ev.ctrlKey || ev.altKey || lireEtat().index) return
      if (ev.key === 'ArrowLeft') { ev.preventDefault(); setK((n) => Math.max(0, n - 1)) }
      if (ev.key === 'ArrowRight') { ev.preventDefault(); setK((n) => Math.min(JOURNAL.length - 1, n + 1)) }
    }
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  }, [setK])

  return (
    <article className="journal">
      <div className="journal-image" key={e.iso}>
        <Plan capture={e.image} alt="" zoom={null} onZoom={rien} />
      </div>
      <div className="voile-bas" aria-hidden="true" />
      <section className="cartouche journal-cartouche" aria-labelledby="titre-journal">
        <p className="petites-caps cartouche-sur">Journal · jour {jours(debut, e.iso) + 1} du projet</p>
        <h1 id="titre-journal" className="visuellement-cache">Le journal</h1>
        <div key={e.iso} className="journal-entree" aria-live="polite">
          <time className="journal-date" dateTime={e.iso}>{e.date}</time>
          <h2 className="cartouche-titre">{e.titre}</h2>
          <p className="cartouche-texte">{e.texte}</p>
          {lieu && <Link viewTransition prefetch="intent" to={`/${lieu.chemin}/`} className="lien-trait">Voir {lieu.nom.toLowerCase()}</Link>}
        </div>
      </section>
      <nav className="journal-frise" aria-label="Étapes du journal">
        <button type="button" className="film-fleche" onClick={() => setK(Math.max(0, k - 1))} disabled={k === 0} aria-label="Étape précédente">
          <Icone nom="gauche" taille={26} />
        </button>
        <ol>
          {JOURNAL.map((x, n) => (
            <li key={x.iso}>
              <button
                type="button"
                className={`journal-cran${estNouveau(x.iso, vu) ? ' nouveau' : ''}`}
                aria-current={n === k ? 'step' : undefined}
                onClick={() => setK(n)}
              >
                <span className="journal-cran-date">{x.date}</span>
                <span className="journal-cran-titre">{x.titre}</span>
              </button>
            </li>
          ))}
        </ol>
        <button type="button" className="film-fleche" onClick={() => setK(Math.min(JOURNAL.length - 1, k + 1))} disabled={k === JOURNAL.length - 1} aria-label="Étape suivante">
          <Icone nom="droite" taille={26} />
        </button>
      </nav>
    </article>
  )
}
