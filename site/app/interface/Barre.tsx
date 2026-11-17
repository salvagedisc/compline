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
  const actif = CASES.findIndex((c) => pathname === c.chemin || pathname.startsWith(`${c.chemin}/`))

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

  // As in game, the name of the held slot shows above the bar when it changes, then fades;
  // pointing at a slot shows its name for as long as the pointer stays.
  const montre = survol ?? (actif >= 0 ? actif : null)
  return (
    <nav className="barre" aria-label="Lieux du disque">
      {montre !== null && (
        <p key={survol === null ? `actif-${pathname}` : `survol-${survol}`} className={`nom-tenu${survol === null ? ' s-efface' : ''}`} aria-hidden="true">
          <b>{CASES[montre].nom}</b>
          <span>{CASES[montre].sous}</span>
        </p>
      )}
      <ol>
        {CASES.map((c, i) => (
          <li key={c.chemin}>
            <NavLink
              to={c.chemin}
              className="case"
              onPointerEnter={(e) => { if (e.pointerType === 'mouse') setSurvol(i) }}
              onPointerLeave={() => setSurvol(null)}
              onFocus={() => setSurvol(i)}
              onBlur={() => setSurvol(null)}
              aria-label={`${c.nom}, ${c.sous}`}
              aria-keyshortcuts={String(i + 1)}
            >
              <Pixel nom={c.icone} taille={30} />
              <span className="case-num pix" aria-hidden="true">{i + 1}</span>
            </NavLink>
          </li>
        ))}
      </ol>
    </nav>
  )
}
