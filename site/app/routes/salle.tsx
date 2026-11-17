// A place of the disc. Its address alone shows the arrival: the camera has dived onto the place and the
// panel lists what there is to see. With a feature in the address (/cuisine/planche) the feature takes the
// whole screen, and the arrows move from one feature to the next.
import { useEffect } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { AnimatePresence, motion } from 'motion/react'
import type { Route } from './+types/salle'
import { LIEUX, STATUTS, type Fonction, type Lieu } from '../donnees/lieux'
import { grande } from '../donnees/images'
import { metas } from '../donnees/site'
import { Capture } from '../interface/Capture'
import { MediaVue } from '../interface/Medias'
import { Pixel } from '../interface/Pixel'
import { Titre } from '../interface/Titre'
import Introuvable from './introuvable'

const trouver = (salle?: string, plan?: string) => {
  const lieu = LIEUX.find((l) => l.chemin === salle)
  const fonction = lieu?.fonctions.find((f) => f.id === plan)
  return { lieu, fonction }
}

/** The capture that stands for a feature in lists and link previews. */
export function vignette(f: Fonction, l: Lieu): string {
  switch (f.media.type) {
    case 'image': return f.media.capture
    case 'comparer': return f.media.scenes[0].apres
    default: return l.fond
  }
}

export const meta: Route.MetaFunction = ({ params }) => {
  const { lieu, fonction } = trouver(params.salle, params.plan)
  if (!lieu) return metas('Perdu dans la brume', "Cette page n'existe pas.")
  if (fonction) return metas(`${fonction.titre} · ${lieu.nom}`, fonction.texte, grande(vignette(fonction, lieu)))
  return metas(`${lieu.nom}, ${lieu.theme.toLowerCase()}`, lieu.intro, grande(lieu.fond))
}

function Statut({ f }: { f: Fonction }) {
  return <span className={`statut s-${f.statut}`}><span className="puce" aria-hidden="true" />{STATUTS[f.statut].nom}</span>
}

function Arrivee({ lieu }: { lieu: Lieu }) {
  const i = LIEUX.indexOf(lieu)
  const suivant = LIEUX[(i + 1) % LIEUX.length]
  return (
    <motion.section
      className="arrivee"
      aria-labelledby="titre-lieu"
      initial={{ opacity: 0, x: -30 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: -20 }}
      transition={{ duration: 0.7, delay: 0.35, ease: [0.2, 0.7, 0.2, 1] }}
    >
      <p className="sc accent sur-titre"><span className="touche pix">{lieu.touche}</span>{lieu.theme}</p>
      <Titre id="titre-lieu" key={lieu.id} delai={0.5}>{lieu.nom}</Titre>
      <p className="intro">{lieu.intro}</p>
      <ol className="plans">
        {lieu.fonctions.map((f, k) => (
          <motion.li key={f.id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.8 + k * 0.07, duration: 0.5 }}>
            <Link to={`/${lieu.chemin}/${f.id}`} className="plan-lien">
              <Capture nom={vignette(f, lieu)} alt="" sizes="9rem" className="plan-vignette" />
              <span className="plan-texte">
                <b>{f.titre}</b>
                <Statut f={f} />
              </span>
              <span className="sc plan-num" aria-hidden="true">{String(k + 1).padStart(2, '0')}</span>
            </Link>
          </motion.li>
        ))}
      </ol>
      <footer className="arrivee-pied">
        <Link to={`/${suivant.chemin}`} className="lien-suivant">
          <span className="sc gris">Lieu suivant</span>
          <span><Pixel nom={suivant.id} taille={18} /> {suivant.nom} →</span>
        </Link>
      </footer>
    </motion.section>
  )
}

function Plan({ lieu, fonction }: { lieu: Lieu; fonction: Fonction }) {
  const navigate = useNavigate()
  const k = lieu.fonctions.indexOf(fonction)
  const prec = lieu.fonctions[k - 1], suiv = lieu.fonctions[k + 1]

  useEffect(() => {
    const touche = (e: KeyboardEvent) => {
      if ((e.target as HTMLElement).closest('input, textarea, select')) return
      if (e.key === 'ArrowRight' && suiv) navigate(`/${lieu.chemin}/${suiv.id}`)
      if (e.key === 'ArrowLeft' && prec) navigate(`/${lieu.chemin}/${prec.id}`)
    }
    window.addEventListener('keydown', touche)
    return () => window.removeEventListener('keydown', touche)
  }, [lieu, prec, suiv, navigate])

  return (
    <section className="plan" aria-labelledby="titre-plan">
      <AnimatePresence mode="wait">
        <motion.div
          key={fonction.id}
          className="plan-scene"
          initial={{ opacity: 0, scale: 0.985, filter: 'blur(8px)' }}
          animate={{ opacity: 1, scale: 1, filter: 'blur(0px)' }}
          exit={{ opacity: 0, scale: 1.01, filter: 'blur(8px)' }}
          transition={{ duration: 0.45, ease: [0.4, 0, 0.2, 1] }}
        >
          <div className="plan-media"><MediaVue media={fonction.media} /></div>
          <div className="plan-legende">
            <p className="sc sur-titre">
              <Link to={`/${lieu.chemin}`} className="retour">← {lieu.nom}</Link>
              <span className="gris">{k + 1} / {lieu.fonctions.length}</span>
            </p>
            <Titre id="titre-plan" key={fonction.id} delai={0.1}>{fonction.titre}</Titre>
            <Statut f={fonction} />
            <p>{fonction.texte}</p>
            {fonction.points && <ul className="points">{fonction.points.map((p) => <li key={p}>{p}</li>)}</ul>}
            {'provisoire' in fonction.media && fonction.media.provisoire && (
              <p className="sc provisoire">Image provisoire, en attente d'une capture au banc</p>
            )}
          </div>
        </motion.div>
      </AnimatePresence>
      <nav className="plan-nav" aria-label={`Les plans de ${lieu.nom.toLowerCase()}`}>
        {prec ? <Link className="fleche" to={`/${lieu.chemin}/${prec.id}`} aria-label={`Précédent : ${prec.titre}`} aria-keyshortcuts="ArrowLeft">←</Link> : <span className="fleche vide" />}
        <ol className="pellicule">
          {lieu.fonctions.map((f) => (
            <li key={f.id}>
              <Link to={`/${lieu.chemin}/${f.id}`} aria-current={f === fonction ? 'page' : undefined} aria-label={f.titre}>
                <Capture nom={vignette(f, lieu)} alt="" sizes="6rem" />
              </Link>
            </li>
          ))}
        </ol>
        {suiv ? <Link className="fleche" to={`/${lieu.chemin}/${suiv.id}`} aria-label={`Suivant : ${suiv.titre}`} aria-keyshortcuts="ArrowRight">→</Link> : <span className="fleche vide" />}
      </nav>
    </section>
  )
}

export default function Salle() {
  const { salle, plan } = useParams()
  const { lieu, fonction } = trouver(salle, plan)
  if (!lieu || (plan && !fonction)) return <Introuvable />
  return (
    <AnimatePresence mode="wait">
      {fonction ? <Plan key="plan" lieu={lieu} fonction={fonction} /> : <Arrivee key={lieu.id} lieu={lieu} />}
    </AnimatePresence>
  )
}
