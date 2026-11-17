// The seven places pinned on the disc. With the 3D sky, the sky moves each pin every frame; without it,
// the pins sit on the flat map by their map position, so the home page works with no script and no WebGL.
import { Link } from 'react-router'
import { LIEUX } from '../donnees/lieux'
import { ROMAINS } from '../donnees/details'
import { Icone, type NomIcone } from './icones'
import { signalerPlongee, useEtat } from './etat'

export function Reperes({ refs, visibles }: { refs: React.RefObject<(HTMLElement | null)[]>; visibles: boolean }) {
  const { entre } = useEtat()
  return (
    <nav className={`reperes${visibles ? '' : ' caches'}${entre ? ' entres' : ''}`} aria-label="Les sept lieux du disque" aria-hidden={!visibles || undefined} inert={!visibles}>
      {LIEUX.map((l, i) => (
        <Link
          key={l.id}
          to={`/${l.chemin}/`}
          ref={(el) => { refs.current[i] = el }}
          className="repere"
          style={{ '--x': l.x, '--y': l.y, '--i': i } as React.CSSProperties}
          onClick={() => signalerPlongee()}
        >
          <span className="repere-pied" aria-hidden="true" />
          <span className="repere-marque" aria-hidden="true">
            <Icone nom={l.id as NomIcone} taille={22} />
          </span>
          <span className="repere-nom">
            <span className="repere-num" aria-hidden="true">{ROMAINS[i]}</span>
            <span className="repere-titre">{l.nom}</span>
            <span className="repere-theme">{l.theme}</span>
          </span>
        </Link>
      ))}
    </nav>
  )
}
