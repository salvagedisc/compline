import { Suspense, lazy, useEffect, useRef, useState } from 'react'
import { Links, Meta, Outlet, Scripts, ScrollRestoration, isRouteErrorResponse, useLocation, useNavigate } from 'react-router'
import { MotionConfig, useReducedMotion } from 'motion/react'
import '@fontsource/alegreya/latin-400.css'
import '@fontsource/alegreya/latin-500.css'
import '@fontsource/alegreya/latin-400-italic.css'
import '@fontsource/alegreya-sc/latin-500.css'
import '@fontsource/alegreya-sans/latin-400.css'
import '@fontsource/alegreya-sans/latin-500.css'
import '@fontsource/alegreya-sans/latin-700.css'
import '@fontsource/silkscreen/latin-400.css'
import './styles.css'
import type { Route } from './+types/root'
import { LIEUX, type Lieu } from './donnees/lieux'
import type { Vue } from './monde/Monde'
import { Barre } from './interface/Barre'
import { Entree } from './interface/Entree'
import { Tete } from './interface/Tete'

const Monde = lazy(() => import('./monde/Monde'))
const BASE = import.meta.env.BASE_URL

export const links: Route.LinksFunction = () => [
  { rel: 'icon', type: 'image/svg+xml', href: `${BASE}favicon.svg` },
  { rel: 'preload', href: `${BASE}monde/relief.bin`, as: 'fetch', crossOrigin: 'anonymous' },
]

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr" suppressHydrationWarning>
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
        <meta name="theme-color" content="#111513" />
        {/* Before the first paint: a visitor who already came in during this visit does not see the title screen flash again. */}
        <script dangerouslySetInnerHTML={{ __html: "try{if(sessionStorage.getItem('compline-entre'))document.documentElement.dataset.entre=''}catch(e){}" }} />
        <Meta />
        <Links />
      </head>
      <body>
        {children}
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  )
}

/** Which way the 3D camera looks, from the address. */
function vueDe(chemin: string): Vue {
  const premier = chemin.split('/').filter(Boolean)[0]
  if (!premier) return { type: 'accueil' }
  const lieu = LIEUX.find((l) => l.chemin === premier)
  if (lieu) return { type: 'lieu', lieu }
  return { type: 'haut' }
}

function useWebGL() {
  const [ok, setOk] = useState<boolean | null>(null)
  useEffect(() => {
    try {
      const c = document.createElement('canvas')
      setOk(!!(c.getContext('webgl2') || c.getContext('webgl')))
    } catch { setOk(false) }
  }, [])
  return ok
}

export default function App() {
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const calme = useReducedMotion() ?? false
  const webgl = useWebGL()
  const vue = vueDe(pathname)
  const plan = pathname.split('/').filter(Boolean).length > 1
  const main = useRef<HTMLElement>(null)
  const premier = useRef(true)

  // After each change of page, move focus to the new heading so keyboard and screen reader users follow.
  useEffect(() => {
    if (premier.current) { premier.current = false; return }
    const h = main.current?.querySelector<HTMLElement>('h1')
    if (h) { h.tabIndex = -1; h.focus({ preventScroll: true }) }
  }, [pathname])

  const choisir = (l: Lieu) => navigate(`/${l.chemin}`)

  return (
    <MotionConfig reducedMotion="user">
      <a className="evitement" href="#contenu">Aller au contenu</a>
      <div className={`scene${webgl === true ? ' avec-3d' : ''}${webgl === false ? ' sans-3d' : ''}`} data-vue={vue.type} data-plan={plan || undefined}>
        <div className="fond-statique" aria-hidden="true" />
        {webgl && (
          <Suspense fallback={null}>
            <Monde vue={vue} calme={calme} enPause={plan} onChoisir={choisir} reliefUrl={`${BASE}monde/relief.bin`} />
          </Suspense>
        )}
        <div className="assombri" aria-hidden="true" />
      </div>
      <Tete />
      <main id="contenu" ref={main} className="contenu">
        <Outlet />
      </main>
      <Barre />
      <Entree />
    </MotionConfig>
  )
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  const introuvable = isRouteErrorResponse(error) && error.status === 404
  return (
    <main id="contenu" className="contenu">
      <section className="perdu">
        <p className="sc accent">{introuvable ? 'Erreur 404' : 'Erreur'}</p>
        <h1>{introuvable ? 'Perdu dans la brume' : 'Quelque chose a cassé'}</h1>
        <p>{introuvable ? "Cette page n'existe pas, ou plus." : "La page n'a pas pu s'afficher."}</p>
        <a className="bouton" href={BASE}>Revenir au disque</a>
      </section>
    </main>
  )
}
