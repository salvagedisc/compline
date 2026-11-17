// The full-screen index: the seven places as a table of contents, the journal and the road map.
// Hovering a place brings its picture up behind the list.
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router'
import { LIEUX } from '../donnees/lieux'
import { ROMAINS } from '../donnees/details'
import { Capture } from './Capture'
import { Icone, type NomIcone } from './icones'
import { ouvrirIndex, useEtat } from './etat'

export function Index() {
  const { index } = useEtat()
  const [survol, setSurvol] = useState(0)
  const boite = useRef<HTMLDivElement>(null)
  const avant = useRef<Element | null>(null)

  // Opened: focus the first place, Escape closes, focus goes back where it was.
  useEffect(() => {
    if (!index) return
    avant.current = document.activeElement
    boite.current?.querySelector<HTMLElement>('a')?.focus()
    const f = (e: KeyboardEvent) => { if (e.key === 'Escape') { e.stopImmediatePropagation(); ouvrirIndex(false) } }
    window.addEventListener('keydown', f, true)
    return () => {
      window.removeEventListener('keydown', f, true)
      ;(avant.current as HTMLElement | null)?.focus?.()
    }
  }, [index])

  const fermer = () => ouvrirIndex(false)
  const lieu = LIEUX[survol]
  return (
    <div id="index" ref={boite} className={`index${index ? ' ouvert' : ''}`} role="dialog" aria-modal="true" aria-label="Index" inert={!index} aria-hidden={!index || undefined}>
      <div className="index-fonds" aria-hidden="true">
        {LIEUX.map((l, i) => (
          <div key={l.id} className={`index-fond${i === survol ? ' vu' : ''}`}>
            {index && <Capture nom={l.fond} alt="" sizes="100vw" />}
          </div>
        ))}
      </div>
      <div className="index-corps">
        <p className="petites-caps index-sur">Les sept lieux du disque</p>
        <ol className="index-liste">
          {LIEUX.map((l, i) => (
            <li key={l.id} style={{ '--i': i } as React.CSSProperties}>
              <Link
                to={`/${l.chemin}/`}
                className={i === survol ? 'actif' : undefined}
                onClick={fermer}
                onPointerEnter={() => setSurvol(i)}
                onFocus={() => setSurvol(i)}
              >
                <span className="index-num">{ROMAINS[i]}</span>
                <span className="index-icone" aria-hidden="true"><Icone nom={l.id as NomIcone} taille={34} /></span>
                <span className="index-nom">{l.nom}</span>
                <span className="index-theme">{l.theme}</span>
                <kbd className="index-touche" aria-hidden="true">{l.touche}</kbd>
              </Link>
            </li>
          ))}
        </ol>
        <aside className="index-apercu" aria-live="polite">
          <p className="petites-caps">{lieu.theme} · {lieu.fonctions.length} chapitres</p>
          <p className="index-intro">{lieu.intro}</p>
          <ul className="index-chapitres">
            {lieu.fonctions.map((f, k) => (
              <li key={f.id}>
                <Link to={`/${lieu.chemin}/${f.id}/`} onClick={fermer} tabIndex={-1}>
                  <span>{ROMAINS[k]}</span> {f.titre}
                </Link>
              </li>
            ))}
          </ul>
        </aside>
        <div className="index-pied">
          <Link to="/journal/" onClick={fermer} className="index-annexe"><Icone nom="journal" taille={26} /> Journal <kbd aria-hidden="true">J</kbd></Link>
          <Link to="/feuille-de-route/" onClick={fermer} className="index-annexe"><Icone nom="route" taille={26} /> Feuille de route <kbd aria-hidden="true">R</kbd></Link>
          <Link to="/" onClick={fermer} className="index-annexe"><Icone nom="disque" taille={26} /> Le disque vu du ciel</Link>
        </div>
      </div>
    </div>
  )
}
