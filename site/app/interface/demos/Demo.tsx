// Picks a chapter's hands-on demonstration; each one is fetched only when its chapter is reached.
import { Suspense, lazy } from 'react'
import type { NomDemo } from '../../donnees/lieux'

const DEMOS: Record<NomDemo, React.LazyExoticComponent<() => React.JSX.Element>> = {
  traces: lazy(() => import('./Traces')),
  lumiere: lazy(() => import('./Lumiere')),
  bord: lazy(() => import('./Bord')),
  peche: lazy(() => import('./Peche')),
  coeurs: lazy(() => import('./Coeurs')),
  interface: lazy(() => import('./Interface')),
  patine: lazy(() => import('./Patine')),
}

export function Demo({ nom }: { nom: NomDemo }) {
  const D = DEMOS[nom]
  return (
    <Suspense fallback={<div className="scene demo" />}>
      <D />
    </Suspense>
  )
}
