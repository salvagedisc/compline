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
import { Comparer } from '../interface/scenes/Comparer'
import { Mesure } from '../interface/scenes/Mesure'
import { Paliers } from '../interface/scenes/Paliers'
import { Plan } from '../interface/scenes/Plan'
import { Zoo } from '../interface/scenes/Zoo'
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

function Statut({ f }: { f: Fonction }) {
  return <span className={`statut s-${f.statut}`}><span className="statut-marque" aria-hidden="true" />{STATUTS[f.statut].nom}</span>
}

const rien = () => {}

function Couverture({ lieu, i }: { lieu: Lieu; i: number }) {
  return (
    <>
      <Plan capture={lieu.fond} alt="" zoom={null} onZoom={rien} />
      <div className="voile-bas" aria-hidden="true" />
      <section className="couverture" aria-labelledby="titre-lieu">
        <p className="couverture-num" aria-hidden="true">{ROMAINS[i]}</p>
        <p className="petites-caps couverture-theme">
          <Icone nom={lieu.id as NomIcone} taille={22} /> {lieu.theme}
        </p>
        <h1 id="titre-lieu" className="couverture-titre">{lieu.nom}</h1>
        <p className="couverture-intro">{lieu.intro}</p>
        <ol className="sommaire">
          {lieu.fonctions.map((f, k) => (
            <li key={f.id} style={{ '--k': k } as React.CSSProperties}>
              <Link to={`/${lieu.chemin}/${f.id}/`}>
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
  const m = f.media
  const zooms = m.type === 'image' ? m.zooms ?? [] : []

  let vue: React.ReactNode
  switch (m.type) {
    case 'image': vue = <Plan capture={m.capture} alt={m.alt} zooms={zooms} zoom={zoom} onZoom={setZoom} />; break
    case 'comparer': {
      const s = m.scenes[scene]
      vue = <Comparer key={s.nom} avant={s.avant} apres={s.apres} avantNom={m.avantNom} apresNom={m.apresNom} alt={s.nom} />
      break
    }
    case 'echelle': vue = <Paliers />; break
    case 'zoo': vue = <Zoo />; break
    case 'carte': vue = <Mesure />; break
  }

  const special = m.type === 'echelle' || m.type === 'zoo' || m.type === 'carte'
  return (
    <div className={`chapitre chapitre-${m.type}${zoom !== null ? ' en-zoom' : ''}`}>
      {vue}
      {!special && <div className="voile-bas" aria-hidden="true" />}
      <section className={`cartouche${special ? ' a-cote' : ''}`} aria-labelledby="titre-chapitre">
        <p className="petites-caps cartouche-sur">
          <span aria-hidden="true">{ROMAINS[i]} · </span>{lieu.nom}
          <span className="cartouche-rang"> · chapitre {k + 1} sur {lieu.fonctions.length}</span>
        </p>
        <h1 id="titre-chapitre" className="cartouche-titre">{f.titre}</h1>
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
        <p className="cartouche-pied">
          <Statut f={f} />
          {'provisoire' in m && m.provisoire && <span className="provisoire">Image provisoire, à refaire en jeu</span>}
        </p>
      </section>
    </div>
  )
}

function useBalayage(suivant: () => void, precedent: () => void) {
  const depart = useRef<{ x: number; y: number; t: number } | null>(null)
  useEffect(() => {
    const debut = (e: TouchEvent) => {
      const t = e.target as HTMLElement
      if (t.closest('.scene-comparer, .mesure-carte, .chapitre.en-zoom, button, a')) { depart.current = null; return }
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

export default function Salle() {
  const { salle, plan } = useParams()
  const { pathname } = useLocation()
  const navigate = useNavigate()
  const { lieu, fonction } = trouver(salle, plan)
  const ici = ETAPES.indexOf(pathname.endsWith('/') ? pathname : `${pathname}/`)
  const avant = ETAPES[(ici - 1 + ETAPES.length) % ETAPES.length]
  const apres = ETAPES[(ici + 1) % ETAPES.length]
  const suivant = useCallback(() => navigate(apres), [navigate, apres])
  const precedent = useCallback(() => navigate(avant), [navigate, avant])
  useBalayage(suivant, precedent)

  useEffect(() => {
    const f = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey || lireEtat().index) return
      if ((e.target as HTMLElement).closest('[role="slider"], [role="tab"], input')) return
      if (e.key === 'ArrowRight') { e.preventDefault(); suivant() }
      else if (e.key === 'ArrowLeft') { e.preventDefault(); precedent() }
      else if (e.key === 'Escape') navigate('/')
    }
    window.addEventListener('keydown', f)
    return () => window.removeEventListener('keydown', f)
  }, [suivant, precedent, navigate])

  // Arriving from the map, the picture waits for the camera to come through the clouds.
  const [plongee] = useState(() => lireEtat().plongee > 0 && performance.now() - lireEtat().plongee < 1500)

  if (!lieu || (plan && !fonction)) return <Introuvable />
  const i = LIEUX.indexOf(lieu)
  const k = fonction ? lieu.fonctions.indexOf(fonction) : -1
  const nomEtape = (chemin: string) => {
    const [s, p] = chemin.split('/').filter(Boolean)
    const l = LIEUX.find((x) => x.chemin === s)!
    return p ? l.fonctions.find((x) => x.id === p)!.titre : l.nom
  }

  return (
    <article className={`salle${plongee ? ' apres-plongee' : ''}`} key={pathname}>
      {fonction ? <Chapitre lieu={lieu} f={fonction} i={i} k={k} /> : <Couverture lieu={lieu} i={i} />}
      <nav className="film" aria-label="Chapitres">
        <Link to={avant} className="film-fleche" aria-label={`Précédent : ${nomEtape(avant)}`}>
          <Icone nom="gauche" taille={26} />
        </Link>
        <ol className="film-reperes">
          <li>
            <Link to={`/${lieu.chemin}/`} aria-current={k === -1 ? 'page' : undefined} aria-label={`${lieu.nom}, ouverture`} className="film-cran ouverture" />
          </li>
          {lieu.fonctions.map((x, n) => (
            <li key={x.id}>
              <Link to={`/${lieu.chemin}/${x.id}/`} aria-current={n === k ? 'page' : undefined} aria-label={x.titre} className="film-cran">
                <span className="film-bulle" aria-hidden="true">{x.titre}</span>
              </Link>
            </li>
          ))}
        </ol>
        <Link to={apres} className="film-fleche suivant" aria-label={`Suivant : ${nomEtape(apres)}`}>
          <span className="film-suivant-nom" aria-hidden="true">{nomEtape(apres)}</span>
          <Icone nom="droite" taille={26} />
        </Link>
      </nav>
    </article>
  )
}
