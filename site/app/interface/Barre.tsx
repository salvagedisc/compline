// The hotbar: nine slots like the game's, one per place plus the journal and the road map.
// Keys 1 to 9 jump to a slot from anywhere, as in game.
import { useEffect, useState } from 'react'
import { NavLink, useLocation, useNavigate } from 'react-router'
import { LIEUX } from '../donnees/lieux'
import { Pixel } from './Pixel'

const CASES = [
  ...LIEUX.map((l) => ({ chemin: `/${l.chemin}`, icone: l.id, nom: l.nom, sous: l.theme })),
  { chemin: '/journal', icone: 'journal', nom: 'Le journal', sous: 'Les grandes étapes' },
  { chemin: '/feuille-de-route', icone: 'avancement', nom: 'La feuille de route', sous: 'Fait, en cours, prévu' },
]

export function Barre() {
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const [survol, setSurvol] = useState<number | null>(null)

  useEffect(() => {
    const touche = (e: KeyboardEvent) => {
      if (e.ctrlKey || e.metaKey || e.altKey) return
      const t = e.target as HTMLElement
      if (t.closest('input, textarea, select, [contenteditable]')) return
      const n = Number(e.key)
      if (n >= 1 && n <= CASES.length) { e.preventDefault(); navigate(CASES[n - 1].chemin) }
      if (e.key === 'Escape' && pathname !== '/' && !document.querySelector('[data-echap]')) {
        const parties = pathname.split('/').filter(Boolean)
        navigate(parties.length > 1 ? `/${parties[0]}` : '/')
      }
    }
    window.addEventListener('keydown', touche)
    return () => window.removeEventListener('keydown', touche)
  }, [navigate, pathname])

  return (
    <nav className="barre" aria-label="Lieux du disque">
      <ol>
        {CASES.map((c, i) => (
          <li key={c.chemin}>
            <NavLink
              to={c.chemin}
              className="case"
              onPointerEnter={() => setSurvol(i)}
              onPointerLeave={() => setSurvol(null)}
              onFocus={() => setSurvol(i)}
              onBlur={() => setSurvol(null)}
              aria-label={`${c.nom}, ${c.sous}`}
              aria-keyshortcuts={String(i + 1)}
            >
              <Pixel nom={c.icone} taille={30} />
              <span className="case-num pix" aria-hidden="true">{i + 1}</span>
            </NavLink>
            {survol === i && (
              <span className="infobulle bulle-case" role="tooltip">
                <b>{c.nom}</b>
                <span>{c.sous}</span>
              </span>
            )}
          </li>
        ))}
      </ol>
    </nav>
  )
}
