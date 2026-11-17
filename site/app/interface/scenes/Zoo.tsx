// A page of the guide to fill in, with the real stamps of the game: pick a family, then earn the stamps in
// the order the game asks. The beast stays an ink silhouette until it has been seen; "Maîtriser" waits for the others.
import { useState } from 'react'
import { FAMILLES, TAMPONS, TOTAL_ESPECES } from '../../donnees/details'
import { Capture } from '../Capture'

export function Zoo() {
  const [f, setF] = useState(0)
  const [poses, setPoses] = useState<boolean[]>([false, false, false, false])
  const [dernier, setDernier] = useState<number | null>(null)
  const famille = FAMILLES[f]
  const vue = poses[0]
  const autres = poses.slice(0, 3).every(Boolean)
  const complete = poses.every(Boolean)

  const poser = (k: number) => {
    if (poses[k]) { setDernier(k); return }
    if (k === 3 && !autres) { setDernier(-1); return }
    setPoses((p) => p.map((v, j) => (j === k ? true : v)))
    setDernier(k)
  }
  const changer = (n: number) => { setF(n); setPoses([false, false, false, false]); setDernier(null) }

  const message = dernier === -1
    ? "Pas encore : il faut d'abord les trois autres tampons."
    : dernier === null
      ? 'Touchez un tampon pour le gagner.'
      : TAMPONS[dernier].comment

  return (
    <div className="scene scene-zoo">
      <div className="scene-fond-sombre" aria-hidden="true"><Capture nom="zoo-guide.jpg" alt="" prioritaire /></div>
      <div className="zoo">
        <div className="zoo-familles" role="tablist" aria-label="Familles du guide">
          {FAMILLES.map((x, k) => (
            <button
              key={x.id}
              type="button"
              role="tab"
              id={`famille-${k}`}
              aria-selected={k === f}
              aria-controls="zoo-page"
              tabIndex={k === f ? 0 : -1}
              className="zoo-famille"
              style={{ '--c': x.couleur } as React.CSSProperties}
              onClick={() => changer(k)}
              onKeyDown={(e) => {
                const d = e.key === 'ArrowRight' || e.key === 'ArrowDown' ? 1 : e.key === 'ArrowLeft' || e.key === 'ArrowUp' ? -1 : 0
                if (!d) return
                e.preventDefault(); e.stopPropagation()
                const n = (k + d + FAMILLES.length) % FAMILLES.length
                changer(n)
                document.getElementById(`famille-${n}`)?.focus()
              }}
            >
              <img src={x.image} alt="" width={167} height={167} />
              <span className="zoo-famille-nom">{x.nom}</span>
              <span className="zoo-famille-n">{x.especes} espèces</span>
            </button>
          ))}
          <p className="zoo-total">{TOTAL_ESPECES} espèces au total, toutes dans le guide d'Archie.</p>
        </div>

        <div id="zoo-page" className={`zoo-page${complete ? ' complete' : ''}`} role="tabpanel" aria-labelledby={`famille-${f}`}>
          <div className="zoo-page-tete" style={{ '--c': famille.couleur } as React.CSSProperties}>
            <span className={`zoo-portrait${vue ? ' vue' : ''}`}>
              <img src={famille.image} alt={vue ? `Une bête de la famille ${famille.nom}` : 'Une silhouette à l’encre'} width={167} height={167} />
            </span>
            <div>
              <p className="petites-caps">{famille.nom}</p>
              <p className="zoo-nom">{vue ? 'Une bête aperçue' : '???'}</p>
              <p className="zoo-compte">{vue ? 1 : 0} / {famille.especes}</p>
            </div>
          </div>
          <ol className="zoo-tampons">
            {TAMPONS.map((t, k) => {
              const verrou = k === 3 && !autres && !poses[3]
              return (
                <li key={t.id}>
                  <button
                    type="button"
                    className={`zoo-tampon${poses[k] ? ' pose' : ''}${verrou ? ' verrou' : ''}`}
                    onClick={() => poser(k)}
                    aria-pressed={poses[k]}
                    aria-describedby="zoo-message"
                  >
                    <span className="zoo-tampon-image">
                      <img src={t.image} alt="" width={101} height={101} />
                    </span>
                    <span className="zoo-tampon-nom">{t.nom}</span>
                    <span className="zoo-tampon-geste">{poses[k] ? t.geste : verrou ? 'Après les trois autres' : 'À gagner'}</span>
                  </button>
                </li>
              )
            })}
          </ol>
          <div className="zoo-pied">
            <p id="zoo-message" className="zoo-message" aria-live="polite">
              {complete ? 'Page complète : la bête est maîtrisée, sans jamais avoir été tuée.' : message}
            </p>
            <button type="button" className="lien-trait zoo-encore" onClick={() => changer(f)} hidden={!poses.some(Boolean)}>Une autre bête</button>
          </div>
        </div>
      </div>
    </div>
  )
}
