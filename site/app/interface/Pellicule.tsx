// Moving from one stop of a place to the next without a cut. The page being left stays on screen while the
// next one is drawn hidden underneath; once its pictures are decoded (or after a short wait), the new one slides
// in over the old, picture, veil and caption together, and the old one is dropped. One page, one set of layers:
// nothing pops in on its own, unlike a view transition, which captures whatever state the page happens to be in.
import { useEffect, useLayoutEffect, useRef, useState } from 'react'

type Etat = 'attente' | 'entre' | 'la' | 'sort'
interface Couche { chemin: string; etat: Etat; sens: 'avant' | 'arriere' | 'aucun'; premier?: boolean }

interface Props {
  chemin: string
  ordre: (chemin: string) => number // position of a stop in the visit, to tell the direction
  rendu: (chemin: string) => React.ReactNode
  classe?: string // extra class of the first page only (the dive from the map)
}

/** Longest wait for the next pictures before sliding anyway. */
const ATTENTE_MAX = 650

export function Pellicule({ chemin, ordre, rendu, classe }: Props) {
  const [couches, setCouches] = useState<Couche[]>([{ chemin, etat: 'la', sens: 'aucun', premier: true }])
  const elements = useRef(new Map<string, HTMLElement>())

  // A new address: draw it hidden, keep what is on screen.
  useLayoutEffect(() => {
    setCouches((cs) => {
      const visible = cs.find((c) => c.etat === 'la' || c.etat === 'entre')
      if (visible?.chemin === chemin) return cs.filter((c) => c.etat !== 'attente')
      const voulu = document.documentElement.dataset.sens
      const sens = voulu === 'avant' || voulu === 'arriere' ? voulu
        : visible ? (ordre(chemin) > ordre(visible.chemin) ? 'avant' : 'arriere') : 'aucun'
      return [...cs.filter((c) => c.etat !== 'attente' && c.chemin !== chemin), { chemin, etat: 'attente', sens }]
    })
  }, [chemin, ordre])

  // When the hidden page is ready, it takes the screen.
  const attente = couches.find((c) => c.etat === 'attente')?.chemin
  useEffect(() => {
    if (!attente) return
    let fait = false
    const basculer = () => {
      if (fait) return
      fait = true
      setCouches((cs) => {
        const sens = cs.find((c) => c.chemin === attente)?.sens ?? 'aucun'
        return cs.map((c) => (c.chemin === attente ? { ...c, etat: 'entre' }
          : c.etat === 'la' || c.etat === 'entre' ? { ...c, etat: 'sort', sens } : c))
      })
    }
    const el = elements.current.get(attente)
    const images = el ? [...el.querySelectorAll<HTMLImageElement>('img[loading="eager"]')] : []
    void Promise.all(images.map((i) => i.decode().catch(() => undefined))).then(basculer)
    const t = window.setTimeout(basculer, ATTENTE_MAX)
    return () => { fait = true; window.clearTimeout(t) }
  }, [attente])

  const finir = (c: Couche) => (e: React.AnimationEvent) => {
    if (e.target !== e.currentTarget) return
    setCouches((cs) => (c.etat === 'sort' ? cs.filter((x) => !(x.chemin === c.chemin && x.etat === 'sort'))
      : c.etat === 'entre' ? cs.map((x) => (x.chemin === c.chemin && x.etat === 'entre' ? { ...x, etat: 'la' } : x)) : cs))
  }

  return (
    <>
      {couches.map((c) => {
        const actif = c.etat === 'la' || c.etat === 'entre'
        return (
          <article
            key={c.chemin}
            ref={(el) => { if (el) elements.current.set(c.chemin, el); else elements.current.delete(c.chemin) }}
            className={`salle couche ${c.etat}${c.premier && classe ? ` ${classe}` : ''}`}
            data-sens={c.sens}
            aria-hidden={!actif || undefined}
            inert={!actif}
            onAnimationEnd={finir(c)}
          >
            {rendu(c.chemin)}
          </article>
        )
      })}
    </>
  )
}
