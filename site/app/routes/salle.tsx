// A place of the disc, told as a short film in chapters. Its address alone opens the title card (the camera
// has just dived onto the place); /cuisine/planche opens a chapter. Arrows, keys and swipes move along the
// chapters and on to the next place.
import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router'
import type { Route } from './+types/salle'
import { LIEUX, STATUTS, type Fonction, type Lieu } from '../donnees/lieux'
import { ROMAINS } from '../donnees/details'
import { grande } from '../donnees/images'
import { metas } from '../donnees/site'
import { Icone, type NomIcone } from '../interface/icones'
import { lireEtat } from '../interface/etat'
import { prechargerPlan, SENS } from '../interface/transitions'
import { Pellicule } from '../interface/Pellicule'
import { Comparer } from '../interface/scenes/Comparer'
import { Mesure } from '../interface/scenes/Mesure'
import { Paliers } from '../interface/scenes/Paliers'
import { Plan } from '../interface/scenes/Plan'
import { Zoo } from '../interface/scenes/Zoo'
import { Demo } from '../interface/demos/Demo'
import Introuvable from './introuvable'

const trouver = (salle?: string, plan?: string) => {
  const lieu = LIEUX.find((l) => l.chemin === salle)
  const fonction = lieu?.fonctions.find((f) => f.id === plan)
  return { lieu, fonction }
}

/** The capture that stands for a chapter in link previews. */
function vignette(f: Fonction, l: Lieu): string {
  switch (f.media.type) {
    case 'image': return f.media.capture
    case 'comparer': return f.media.scenes[0].apres
    case 'zoo': return 'zoo-guide.jpg'
    case 'demo': return f.media.capture
    default: return l.fond
  }
}

export const meta: Route.MetaFunction = ({ params }) => {
  const { lieu, fonction } = trouver(params.salle, params.plan)
  if (!lieu) return metas('Perdu dans la brume', "Cette page n'existe pas.")
  if (fonction) return metas(`${fonction.titre} · ${lieu.nom}`, fonction.texte, grande(vignette(fonction, lieu)))
  return metas(`${lieu.nom}, ${lieu.theme.toLowerCase()}`, lieu.intro, grande(lieu.fond))
}

/** Every stop of the visit in order: each place's title card, then its chapters. */
const ETAPES = LIEUX.flatMap((l) => [`/${l.chemin}/`, ...l.fonctions.map((f) => `/${l.chemin}/${f.id}/`)])

/** The pictures a stop shows first, so the next and previous stops can be fetched ahead. */
function imagesDe(chemin: string): string[] {
  const [s, p] = chemin.split('/').filter(Boolean)
  const { lieu, fonction } = trouver(s, p)
  if (!lieu) return []
  if (!fonction) return [lieu.fond]
  const m = fonction.media
  switch (m.type) {
    case 'image': return [m.capture]
    case 'comparer': return [m.scenes[0].avant, m.scenes[0].apres]
    case 'echelle': return ['forge.jpg']
    case 'zoo': return ['zoo-guide.jpg']
    case 'demo': return [m.capture]
    default: return []
  }
}

function precharger(chemin: string) {
  for (const nom of imagesDe(chemin)) prechargerPlan(nom)
}

function Statut({ f }: { f: Fonction }) {
  return <span className={`statut s-${f.statut}`}><span className="statut-marque" aria-hidden="true" />{STATUTS[f.statut].nom}</span>
}

const rien = () => {}

function Couverture({ lieu, i }: { lieu: Lieu; i: number }) {
  return (
    <>
      <Plan capture={lieu.fond} alt="" zoom={null} onZoom={rien} />
      <div className="voile-bas" aria-hidden="true" />
      <section className="couverture" aria-labelledby={`titre-${lieu.id}`}>
        <p className="couverture-num" aria-hidden="true">{ROMAINS[i]}</p>
        <p className="petites-caps couverture-theme">
          <Icone nom={lieu.id as NomIcone} taille={22} /> {lieu.theme}
        </p>
        <h1 id={`titre-${lieu.id}`} className="couverture-titre">{lieu.nom}</h1>
        <p className="couverture-intro">{lieu.intro}</p>
        <ol className="sommaire">
          {lieu.fonctions.map((f, k) => (
            <li key={f.id} style={{ '--k': k } as React.CSSProperties}>
              <Link prefetch="intent" to={`/${lieu.chemin}/${f.id}/`} onClick={() => SENS('avant')}>
                <span className="sommaire-num">{ROMAINS[k]}</span>
                <span className="sommaire-titre">{f.titre}</span>
                <span className={`sommaire-statut s-${f.statut}`} title={STATUTS[f.statut].nom}>
                  <span className="statut-marque" aria-hidden="true" />
                  <span className="visuellement-cache">{STATUTS[f.statut].nom}</span>
                </span>
              </Link>
            </li>
          ))}
        </ol>
      </section>
    </>
  )
}

function Chapitre({ lieu, f, i, k }: { lieu: Lieu; f: Fonction; i: number; k: number }) {
  const [zoom, setZoom] = useState<number | null>(null)
  const [scene, setScene] = useState(0)
  const [enJeu, setEnJeu] = useState(false) // a demonstration's chapter, showing the capture instead
  const m = f.media
  const zooms = m.type === 'image' ? m.zooms ?? [] : []

  let vue: React.ReactNode
  switch (m.type) {
    case 'image': vue = <Plan capture={m.capture} alt={m.alt} zooms={zooms} zoom={zoom} onZoom={setZoom} expose={m.mise === 'expose'} />; break
    case 'comparer': {
      const s = m.scenes[scene]
      vue = <Comparer key={s.nom} avant={s.avant} apres={s.apres} avantNom={m.avantNom} apresNom={m.apresNom} alt={s.nom} />
      break
    }
    case 'echelle': vue = <Paliers />; break
    case 'zoo': vue = <Zoo />; break
    case 'carte': vue = <Mesure />; break
    case 'demo': vue = (
      <>
        <Demo nom={m.demo} />
        {enJeu && <div className="demo-en-jeu"><Plan capture={m.capture} alt={m.alt} zoom={null} onZoom={rien} /></div>}
      </>
    ); break
  }

  const special = m.type === 'echelle' || m.type === 'zoo' || m.type === 'carte'
  return (
    <div className={`chapitre chapitre-${m.type}${m.type === 'demo' ? ` demo-${m.demo}` : ''}${(m.type === 'image' || m.type === 'demo') && m.mise ? ` mise-${m.mise}` : ''}${zoom !== null ? ' en-zoom' : ''}${enJeu ? ' montre-jeu' : ''}`}>
      {vue}
      {!special && <div className="voile-bas" aria-hidden="true" />}
      <section className={`cartouche${special ? ' a-cote' : ''}`} aria-labelledby={`titre-${f.id}`}>
        <p className="petites-caps cartouche-sur">
          <span aria-hidden="true">{ROMAINS[i]} · </span>{lieu.nom}
          <span className="cartouche-rang"> · chapitre {k + 1} sur {lieu.fonctions.length}</span>
        </p>
        <h1 id={`titre-${f.id}`} className="cartouche-titre">{f.titre}</h1>
        <p className="cartouche-texte">{f.texte}</p>
        {f.points && <ul className="cartouche-points">{f.points.map((p) => <li key={p}>{p}</li>)}</ul>}
        {m.type === 'comparer' && m.scenes.length > 1 && (
          <div className="choix-scenes" role="group" aria-label="Point de vue">
            {m.scenes.map((s, n) => (
              <button key={s.nom} type="button" aria-pressed={n === scene} onClick={() => setScene(n)}>{s.nom}</button>
            ))}
          </div>
        )}
        {zooms.length > 0 && (
          <div className="details-liste" role="group" aria-label="Regarder de près">
            <span className="petites-caps">De près</span>
            {zooms.map((z, n) => (
              <button key={z.label} type="button" aria-pressed={zoom === n} onClick={() => setZoom(zoom === n ? null : n)}>
                <span className="details-num" aria-hidden="true">{n + 1}</span>{z.label}
              </button>
            ))}
          </div>
        )}
        {m.type === 'demo' && (
          <button type="button" className="voir-en-jeu" aria-pressed={enJeu} onClick={() => setEnJeu(!enJeu)}>
            {enJeu ? 'Revenir à la démonstration' : 'Voir la capture en jeu'}
          </button>
        )}
        <p className="cartouche-pied">
          <Statut f={f} />
          {'provisoire' in m && m.provisoire && <span className="provisoire">Image provisoire, à refaire en jeu</span>}
        </p>
      </section>
    </div>
  )
}

/**
 * The mouse wheel or a trackpad swipe turns the page, one stop per gesture, unless what is under the pointer can
 * still scroll that way itself (a list, a long caption) or takes the wheel for its own use (a demonstration).
 */
function useMolette(suivant: () => void, precedent: () => void) {
  const actions = useRef({ suivant, precedent })
  useEffect(() => { actions.current = { suivant, precedent } })
  useEffect(() => {
    let cumul = 0, dernier = 0, bloque = 0
    const f = (e: WheelEvent) => {
      if (lireEtat().index || e.ctrlKey) return
      const t = e.target as HTMLElement
      if (t.closest('[data-molette], .chapitre.en-zoom')) return
      const maintenant = performance.now()
      const d = Math.abs(e.deltaY) >= Math.abs(e.deltaX) ? e.deltaY : e.deltaX
      // Something under the pointer still scrolls this way: it takes the whole gesture, and the page only
      // turns on a new gesture once it has reached its end.
      for (let el: HTMLElement | null = t; el && el !== document.body; el = el.parentElement) {
        if (!/(auto|scroll)/.test(getComputedStyle(el).overflowY) || el.scrollHeight <= el.clientHeight + 1) continue
        if ((d > 0 && el.scrollTop + el.clientHeight < el.scrollHeight - 1) || (d < 0 && el.scrollTop > 0)) {
          bloque = maintenant + 350
          return
        }
      }
      if (maintenant < bloque) { bloque = Math.max(bloque, maintenant + 160); return } // the tail of a gesture already used
      if (maintenant - dernier > 250) cumul = 0
      dernier = maintenant
      cumul += e.deltaMode === 1 ? d * 30 : d
      if (Math.abs(cumul) < 70) return
      ;(cumul > 0 ? actions.current.suivant : actions.current.precedent)()
      cumul = 0
      bloque = maintenant + 900
    }
    window.addEventListener('wheel', f, { passive: true })
    return () => window.removeEventListener('wheel', f)
  }, [])
}

function useBalayage(suivant: () => void, precedent: () => void) {
  const depart = useRef<{ x: number; y: number; t: number } | null>(null)
  useEffect(() => {
    const debut = (e: TouchEvent) => {
      const t = e.target as HTMLElement
      if (t.closest('.scene-comparer, .mesure-carte, .demo, .chapitre.en-zoom, button, a')) { depart.current = null; return }
      depart.current = { x: e.touches[0].clientX, y: e.touches[0].clientY, t: performance.now() }
    }
    const fin = (e: TouchEvent) => {
      const d = depart.current
      depart.current = null
      if (!d) return
      const dx = e.changedTouches[0].clientX - d.x, dy = e.changedTouches[0].clientY - d.y
      if (Math.abs(dx) > 60 && Math.abs(dx) > Math.abs(dy) * 1.6 && performance.now() - d.t < 700) (dx < 0 ? suivant : precedent)()
    }
    window.addEventListener('touchstart', debut, { passive: true })
    window.addEventListener('touchend', fin, { passive: true })
    return () => { window.removeEventListener('touchstart', debut); window.removeEventListener('touchend', fin) }
  }, [suivant, precedent])
}

/** Position of a stop in the visit. */
const ordre = (chemin: string) => ETAPES.indexOf(chemin)

/** One stop of the visit, from its address: a place's title card or one of its chapters. */
function rendu(chemin: string) {
  const [s, p] = chemin.split('/').filter(Boolean)
  const { lieu, fonction } = trouver(s, p)
  if (!lieu) return null
  const i = LIEUX.indexOf(lieu)
  return fonction
    ? <Chapitre key={chemin} lieu={lieu} f={fonction} i={i} k={lieu.fonctions.indexOf(fonction)} />
    : <Couverture key={chemin} lieu={lieu} i={i} />
}

export default function Salle() {
  const { salle, plan } = useParams()
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { lieu, fonction } = trouver(salle, plan)
  const ici = ETAPES.indexOf(pathname.endsWith('/') ? pathname : `${pathname}/`)
  const avant = ETAPES[(ici - 1 + ETAPES.length) % ETAPES.length]
  const apres = ETAPES[(ici + 1) % ETAPES.length]
  const suivant = useCallback(() => { SENS('avant'); navigate(apres) }, [navigate, apres])
  const precedent = useCallback(() => { SENS('arriere'); navigate(avant) }, [navigate, avant])
  useBalayage(suivant, precedent)
  useMolette(suivant, precedent)

  useEffect(() => {
    const f = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey || lireEtat().index) return
      if ((e.target as HTMLElement).closest('[role="slider"], [role="tab"], input, [data-clavier]')) return
      if (e.key === 'ArrowRight') { e.preventDefault(); suivant() }
      else if (e.key === 'ArrowLeft') { e.preventDefault(); precedent() }
      else if (e.key === 'Escape') navigate('/', { viewTransition: true })
    }
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  }, [suivant, precedent, navigate])

  // Once this stop is shown, fetch the pictures of the stops on either side.
  useEffect(() => {
    const t = window.setTimeout(() => { precharger(apres); precharger(avant) }, 900)
    return () => window.clearTimeout(t)
  }, [apres, avant])

  // Arriving from the map, the picture waits for the camera to come through the clouds.
  const [plongee] = useState(() => lireEtat().plongee > 0 && performance.now() - lireEtat().plongee < 1500)

  if (!lieu || (plan && !fonction)) return <Introuvable />
  const chemin = pathname.endsWith('/') ? pathname : `${pathname}/`
  const k = fonction ? lieu.fonctions.indexOf(fonction) : -1
  const nomEtape = (chemin: string) => {
    const [s, p] = chemin.split('/').filter(Boolean)
    const l = LIEUX.find((x) => x.chemin === s)!
    return p ? l.fonctions.find((x) => x.id === p)!.titre : l.nom
  }

  return (
    <>
      <Pellicule chemin={chemin} ordre={ordre} rendu={rendu} classe={plongee ? 'apres-plongee' : undefined} />
      {/* Outside the page that changes, so the reel stays in place from one chapter to the next. */}
      <nav className="film" aria-label="Chapitres">
        <Link prefetch="intent" to={avant} onClick={() => SENS('arriere')} className="film-fleche" aria-label={`Précédent : ${nomEtape(avant)}`}>
          <Icone nom="gauche" taille={26} />
        </Link>
        <ol className="film-reperes">
          <li>
            <Link prefetch="intent" to={`/${lieu.chemin}/`} onClick={() => SENS(k > -1 ? 'arriere' : null)} aria-current={k === -1 ? 'page' : undefined} aria-label={`${lieu.nom}, ouverture`} className="film-cran ouverture" />
          </li>
          {lieu.fonctions.map((x, n) => (
            <li key={x.id}>
              <Link prefetch="intent" to={`/${lieu.chemin}/${x.id}/`} onClick={() => SENS(n > k ? 'avant' : n < k ? 'arriere' : null)} aria-current={n === k ? 'page' : undefined} aria-label={x.titre} className="film-cran">
                <span className="film-bulle" aria-hidden="true">{x.titre}</span>
              </Link>
            </li>
          ))}
        </ol>
        <Link prefetch="intent" to={apres} onClick={() => SENS('avant')} className="film-fleche suivant" aria-label={`Suivant : ${nomEtape(apres)}`}>
          <span className="film-suivant-nom" aria-hidden="true">{nomEtape(apres)}</span>
          <Icone nom="droite" taille={26} />
        </Link>
      </nav>
    </>
  )
}
