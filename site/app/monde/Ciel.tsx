// The 3D sky behind every page: a canvas handed to the sky engine (moteur.ts), which draws the disc, moves the
// camera and the place pins. This component only passes on where to look.
import { useEffect, useRef } from 'react'
import { signalerMondePret } from '../interface/etat'
import { Monde, type Vue } from './moteur'

export type { Vue }

interface Props {
  vue: Vue
  calme: boolean // reduced motion: no drift, cuts instead of flights
  actif: boolean // false once a page covers the sky: no frame is drawn until something changes
  mobile: boolean
  base: string
  reperes: React.RefObject<(HTMLElement | null)[]> // pins of the places, moved over the canvas every frame
}

export default function Ciel({ vue, calme, actif, mobile, base, reperes }: Props) {
  const toile = useRef<HTMLCanvasElement>(null)
  const monde = useRef<Monde | null>(null)

  useEffect(() => {
    const m = new Monde(toile.current!, { mobile, base }, () => reperes.current, signalerMondePret)
    monde.current = m
    return () => { m.detruire(); monde.current = null }
  }, [mobile, base, reperes])

  useEffect(() => { monde.current?.regler({ vue, calme, actif }) }, [vue, calme, actif, mobile])

  return <canvas ref={toile} className="ciel-canvas" aria-hidden="true" />
}
