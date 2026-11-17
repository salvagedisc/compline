// Two captures of the same view laid over each other across the whole screen; a line splits them and
// follows the pointer while it is held, or the arrow keys.
import { useRef, useState } from 'react'
import { image } from '../../donnees/images'
import { Capture } from '../Capture'

interface Props {
  avant: string
  apres: string
  avantNom: string
  apresNom: string
  alt: string
}

export function Comparer({ avant, apres, avantNom, apresNom, alt }: Props) {
  const [part, setPart] = useState(0.5) // share of the screen width showing the "before" picture
  const [glisse, setGlisse] = useState(false)
  const zone = useRef<HTMLDivElement>(null)
  const { w, h } = image(apres).img

  const suivre = (e: React.PointerEvent) => {
    const r = zone.current!.getBoundingClientRect()
    setPart(Math.min(0.98, Math.max(0.02, (e.clientX - r.left) / r.width)))
  }

  return (
    <div
      ref={zone}
      className={`scene scene-comparer${glisse ? ' glisse' : ''}`}
      style={{ '--part': part, '--a': w / h } as React.CSSProperties}
      onPointerDown={(e) => { if (e.pointerType === 'mouse' && e.button !== 0) return; setGlisse(true); e.currentTarget.setPointerCapture(e.pointerId); suivre(e) }}
      onPointerMove={(e) => { if (glisse) suivre(e) }}
      onPointerUp={() => setGlisse(false)}
      onPointerCancel={() => setGlisse(false)}
    >
      <div className="cadre">
        <Capture nom={apres} alt={`${apresNom} : ${alt}`} prioritaire sizes="max(100vw, 178vh)" />
      </div>
      <div className="comparer-avant" aria-hidden="true">
        <div className="cadre">
          <Capture nom={avant} alt="" prioritaire sizes="max(100vw, 178vh)" />
        </div>
      </div>
      <span className="comparer-nom gauche" aria-hidden="true">{avantNom}</span>
      <span className="comparer-nom droite" aria-hidden="true">{apresNom}</span>
      <div
        className="comparer-ligne"
        role="slider"
        tabIndex={0}
        aria-label={`Séparation entre ${avantNom.toLowerCase()} et ${apresNom.toLowerCase()}`}
        aria-valuemin={0}
        aria-valuemax={100}
        aria-valuenow={Math.round(part * 100)}
        aria-valuetext={`${Math.round(part * 100)} % ${avantNom.toLowerCase()}`}
        onKeyDown={(e) => {
          const pas = e.shiftKey ? 0.1 : 0.03
          if (e.key === 'ArrowLeft') { e.preventDefault(); e.stopPropagation(); setPart((p) => Math.max(0.02, p - pas)) }
          if (e.key === 'ArrowRight') { e.preventDefault(); e.stopPropagation(); setPart((p) => Math.min(0.98, p + pas)) }
        }}
      >
        <span className="comparer-poignee" aria-hidden="true">
          <svg viewBox="0 0 48 48" width="22" height="22" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="m18 15-9 9 9 9M30 15l9 9-9 9" />
          </svg>
        </span>
      </div>
    </div>
  )
}
