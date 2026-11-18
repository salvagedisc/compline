// The seven places pinned on the disc. With the 3D sky, the sky moves each pin every frame; without it,
// the pins sit on the flat map by their map position, so the home page works with no script and no WebGL.
import { useState } from 'react'
import { Link } from 'react-router'
import { LIEUX } from '../donnees/lieux'
import { Capture } from './Capture'
import { ROMAINS } from '../donnees/details'
import { Icone, type NomIcone } from './icones'
import { signalerPlongee, useEtat } from './etat'
import { prechargerPlan } from './transitions'

export function Reperes({ refs, visibles, inerte }: { refs: React.RefObject<(HTMLElement | null)[]>; visibles: boolean; inerte: boolean }) {
  const { entre } = useEtat()
  // A pin shows a small picture of its place while pointed at; the picture is only fetched the first time,
  // with the full-screen one the dive will open on, so it is there when the camera lands.
  const [vus, setVus] = useState<ReadonlySet<string>>(new Set())
  const voir = (i: number) => {
    const l = LIEUX[i]
    prechargerPlan(l.fond)
    setVus((v) => (v.has(l.id) ? v : new Set(v).add(l.id)))
  }
  return (
    <nav className={`reperes${visibles ? '' : ' caches'}${entre ? ' entres' : ''}`} aria-label="Les sept lieux du disque" aria-hidden={!visibles || undefined} inert={!visibles || inerte}>
      {LIEUX.map((l, i) => (
        <Link viewTransition prefetch="render"
          key={l.id}
          to={`/${l.chemin}/`}
          ref={(el) => { refs.current[i] = el }}
          className="repere"
          style={{ '--x': l.x, '--y': l.y, '--i': i } as React.CSSProperties}
          onClick={() => signalerPlongee()}
          onPointerEnter={() => voir(i)}
          onFocus={() => voir(i)}
        >
          <span className="repere-carte" aria-hidden="true">
            {vus.has(l.id) && <Capture nom={l.fond} alt="" sizes="240px" prioritaire />}
          </span>
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
