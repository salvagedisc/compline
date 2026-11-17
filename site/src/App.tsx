import { useCallback, useEffect, useState } from 'react'
import { AnimatePresence, motion, useReducedMotion } from 'motion/react'
import { LIEUX, type Lieu } from './data/lieux'
import { Carte } from './components/Carte'
import { LieuVue } from './components/LieuVue'
import { Avancement, Journal } from './components/Panneaux'
import { Pixel } from './components/Pixel'

type Panneau = 'avancement' | 'journal' | null

// The address keeps the open place or panel (#zoo, #avancement) so a link can be shared.
function lireAdresse(): { lieu: Lieu | null; panneau: Panneau } {
  const h = window.location.hash.slice(1)
  if (h === 'avancement' || h === 'journal') return { lieu: null, panneau: h }
  return { lieu: LIEUX.find((l) => l.id === h) ?? null, panneau: null }
}

export default function App() {
  const reduit = useReducedMotion() ?? false
  const [etat, setEtat] = useState(lireAdresse)
  // The opening (mist over everything, then the disc appears) plays when arriving on the map.
  const [intro] = useState(() => window.location.hash === '' || window.location.hash === '#')
  const [introFinie, setIntroFinie] = useState(!intro)
  useEffect(() => {
    if (!intro) return
    const t = window.setTimeout(() => setIntroFinie(true), reduit ? 0 : 2600)
    return () => window.clearTimeout(t)
  }, [intro, reduit])
  const { lieu, panneau } = etat

  const aller = useCallback((cible: string) => {
    if (window.location.hash.slice(1) !== cible) window.location.hash = cible
    else setEtat(lireAdresse())
  }, [])
  const fermer = useCallback(() => {
    history.pushState(null, '', window.location.pathname + window.location.search)
    setEtat({ lieu: null, panneau: null })
  }, [])

  useEffect(() => {
    const maj = () => setEtat(lireAdresse())
    window.addEventListener('hashchange', maj)
    window.addEventListener('popstate', maj)
    return () => { window.removeEventListener('hashchange', maj); window.removeEventListener('popstate', maj) }
  }, [])

  // Hotbar keys, like the game: 1 to 7 for the places, 8 and 9 for the panels.
  useEffect(() => {
    const touche = (e: KeyboardEvent) => {
      if (e.metaKey || e.ctrlKey || e.altKey || (e.target as HTMLElement)?.tagName === 'INPUT') return
      if (e.key === 'Escape') { if (lieu || panneau) fermer(); return }
      const n = Number(e.key)
      if (n >= 1 && n <= 7) aller(LIEUX[n - 1].id)
      if (n === 8) aller('avancement')
      if (n === 9) aller('journal')
    }
    window.addEventListener('keydown', touche)
    return () => window.removeEventListener('keydown', touche)
  }, [lieu, panneau, aller, fermer])

  useEffect(() => {
    document.title = lieu ? `${lieu.nom} · Compline` : panneau === 'avancement' ? 'Avancement · Compline' : panneau === 'journal' ? 'Journal · Compline' : 'Compline'
  }, [lieu, panneau])

  const fentes: { id: string; nom: string; icone: string }[] = [
    ...LIEUX.map((l) => ({ id: l.id, nom: `${l.nom} · ${l.theme}`, icone: l.id })),
    { id: 'avancement', nom: 'Avancement', icone: 'avancement' },
    { id: 'journal', nom: 'Journal', icone: 'journal' },
  ]
  const choisi = lieu?.id ?? panneau

  return (
    <div className={`app${lieu ? ' dans-lieu' : ''}`}>
      <Carte intro={intro} actif={lieu} onChoisir={(l) => aller(l.id)} reduit={reduit} />

      <AnimatePresence>
        {!introFinie && (
          <motion.div
            className="ouverture"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1, transition: { duration: 0.8 } }}
            exit={{ opacity: 0, scale: 1.04, transition: { duration: 0.9 } }}
            onClick={() => setIntroFinie(true)}
          >
            <span className="ouverture-nom">COMPLINE</span>
            <span className="pix">Un monde sous la brume</span>
          </motion.div>
        )}
      </AnimatePresence>

      <header className={`titre-site${lieu ? ' discret' : ''}${introFinie ? '' : ' cache'}`}>
        <button type="button" className="marque" onClick={fermer} aria-label="Compline, retour à la carte">COMPLINE</button>
        {!lieu && (
          <div className="accroche">
            <p className="devise">Les complies, c'est le dernier office du jour, quand la lumière baisse et que la brume monte.</p>
            <p>Un modpack Minecraft entre amis : un monde rond de douze kilomètres, presque vide, à reconquérir sur la brume. Choisissez un lieu sur la carte.</p>
          </div>
        )}
      </header>

      <AnimatePresence>
        {lieu && <LieuVue key={lieu.id} lieu={lieu} onFermer={fermer} reduit={reduit} />}
      </AnimatePresence>
      <AnimatePresence>
        {panneau === 'avancement' && <Avancement key="a" onFermer={fermer} onLieu={aller} reduit={reduit} />}
        {panneau === 'journal' && <Journal key="j" onFermer={fermer} onLieu={aller} reduit={reduit} />}
      </AnimatePresence>

      <nav className="hotbar" aria-label="Lieux et panneaux">
        {fentes.map((f, k) => (
          <button
            key={f.id}
            type="button"
            className={`fente${choisi === f.id ? ' choisie' : ''}${k === 7 ? ' separee' : ''}`}
            onClick={() => (choisi === f.id ? fermer() : aller(f.id))}
            aria-label={`${f.nom} (touche ${k + 1})`}
            aria-current={choisi === f.id}
          >
            <Pixel nom={f.icone} taille={30} />
            <span className="num">{k + 1}</span>
            <span className="fente-nom" role="tooltip">{f.nom}</span>
          </button>
        ))}
      </nav>
    </div>
  )
}
