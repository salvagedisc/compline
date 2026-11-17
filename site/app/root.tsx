import { Suspense, lazy, useEffect, useRef, useState } from 'react'
import { Links, Meta, Outlet, Scripts, ScrollRestoration, isRouteErrorResponse, useLocation, useNavigate } from 'react-router'
import '@fontsource/cormorant-garamond/latin-300.css'
import '@fontsource/cormorant-garamond/latin-400.css'
import '@fontsource/cormorant-garamond/latin-500.css'
import '@fontsource/cormorant-garamond/latin-400-italic.css'
import '@fontsource/cormorant-sc/latin-500.css'
import '@fontsource/cormorant-sc/latin-600.css'
import '@fontsource/spectral/latin-300.css'
import '@fontsource/spectral/latin-400.css'
import '@fontsource/spectral/latin-300-italic.css'
import './styles.css'
import type { Route } from './+types/root'
import { LIEUX } from './donnees/lieux'
import type { Vue } from './monde/Ciel'
import { Entete } from './interface/Entete'
import { Entree } from './interface/Entree'
import { Index } from './interface/Index'
import { Reperes } from './interface/Reperes'
import { lireEtat, ouvrirIndex, useEtat } from './interface/etat'

const Ciel = lazy(() => import('./monde/Ciel'))
const BASE = import.meta.env.BASE_URL

export const links: Route.LinksFunction = () => [
  { rel: 'icon', type: 'image/svg+xml', href: `${BASE}favicon.svg` },
  { rel: 'preload', href: `${BASE}monde/couleurs.webp`, as: 'image', type: 'image/webp' },
]

export function Layout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="fr" suppressHydrationWarning>
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover" />
        <meta name="theme-color" content="#0f1217" />
        {/* Before the first paint: scripts are on, and a visitor who already came in during this visit skips the title screen. */}
        <script dangerouslySetInnerHTML={{ __html: "document.documentElement.classList.add('js');try{if(sessionStorage.getItem('compline-entre'))document.documentElement.dataset.entre=''}catch(e){}" }} />
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

/** Where the camera looks, from the address. */
function vueDe(chemin: string): Vue {
  const premier = chemin.split('/').filter(Boolean)[0]
  if (!premier) return { type: 'accueil' }
  const lieu = LIEUX.find((l) => l.chemin === premier)
  return lieu ? { type: 'lieu', lieu } : { type: 'haut' }
}

function useWebGL() {
  const [ok, setOk] = useState<boolean | null>(null)
  useEffect(() => {
    try {
      const c = document.createElement('canvas')
      setOk(!!c.getContext('webgl2'))
    } catch { setOk(false) }
  }, [])
  return ok
}

function useMedia(requete: string) {
  const [ok, setOk] = useState(false)
  useEffect(() => {
    const m = window.matchMedia(requete)
    setOk(m.matches)
    const f = () => setOk(m.matches)
    m.addEventListener('change', f)
    return () => m.removeEventListener('change', f)
  }, [requete])
  return ok
}

/** Keys: 1 to 7 for the places, J for the journal, R for the road map, I for the index. */
function useClavier() {
  const navigate = useNavigate()
  useEffect(() => {
    const f = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey) return
      const t = e.target as HTMLElement
      if (t.closest('input, textarea, [contenteditable]')) return
      const n = Number(e.key)
      if (n >= 1 && n <= LIEUX.length) { ouvrirIndex(false); navigate(`/${LIEUX[n - 1].chemin}/`); return }
      const k = e.key.toLowerCase()
      if (k === 'j') { ouvrirIndex(false); navigate('/journal/') }
      else if (k === 'r') { ouvrirIndex(false); navigate('/feuille-de-route/') }
      else if (k === 'i') ouvrirIndex(!lireEtat().index)
    }
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  }, [navigate])
}

export default function App() {
  const { pathname } = useLocation()
  const { entre, mondePret } = useEtat()
  const calme = useMedia('(prefers-reduced-motion: reduce)')
  const mobile = useMedia('(max-width: 760px), (pointer: coarse)')
  const webgl = useWebGL()
  const vue = vueDe(pathname)
  const main = useRef<HTMLElement>(null)
  const premier = useRef(true)
  const reperes = useRef<(HTMLElement | null)[]>([])
  useClavier()

  // The sky stops drawing a few seconds after a page has covered it, and starts again on the way back.
  const [actif, setActif] = useState(true)
  useEffect(() => {
    if (vue.type === 'accueil') { setActif(true); return }
    setActif(true)
    const t = window.setTimeout(() => setActif(false), 2600)
    return () => window.clearTimeout(t)
  }, [vue.type, pathname])

  // After each change of page, move focus to the new heading so keyboard and screen reader users follow.
  useEffect(() => {
    ouvrirIndex(false)
    if (premier.current) { premier.current = false; return }
    const h = main.current?.querySelector<HTMLElement>('h1')
    if (h) { h.tabIndex = -1; h.focus({ preventScroll: true }) }
  }, [pathname])

  return (
    <>
      <a className="evitement" href="#contenu">Aller au contenu</a>
      <div className={`ciel${webgl ? ' avec-3d' : ''}${webgl === false ? ' sans-3d' : ''}`} data-vue={vue.type} data-entre={entre || undefined} data-pret={mondePret || undefined}>
        <div className="ciel-statique" aria-hidden="true" />
        {webgl && (
          <Suspense fallback={null}>
            <Ciel vue={vue} calme={calme} actif={actif} mobile={mobile} base={BASE} reperes={reperes} />
          </Suspense>
        )}
        <div className="ciel-voile" aria-hidden="true" />
      </div>
      <Reperes refs={reperes} visibles={vue.type === 'accueil'} />
      <Entete />
      <main id="contenu" ref={main} className="contenu">
        <Outlet />
      </main>
      <Index />
      <Entree />
      <div className="grain" aria-hidden="true" />
    </>
  )
}

export function ErrorBoundary({ error }: Route.ErrorBoundaryProps) {
  const introuvable = isRouteErrorResponse(error) && error.status === 404
  return (
    <main id="contenu" className="contenu">
      <section className="perdu">
        <p className="petites-caps">{introuvable ? 'Erreur 404' : 'Erreur'}</p>
        <h1>{introuvable ? 'Perdu dans la brume' : 'Quelque chose a cassé'}</h1>
        <p>{introuvable ? "Cette page n'existe pas, ou plus." : "La page n'a pas pu s'afficher."}</p>
        <a className="lien-trait" href={BASE}>Revenir au disque</a>
      </section>
    </main>
  )
}
