// A capture filling the screen like a film still. Its details are marked inside the picture itself;
// choosing one moves the camera into the picture, onto that detail.
import { useEffect } from 'react'
import type { Zoom } from '../../donnees/lieux'
import { apercu, image } from '../../donnees/images'
import { Capture } from '../Capture'

interface Props {
  capture: string
  alt: string
  zooms?: Zoom[]
  zoom: number | null
  onZoom: (i: number | null) => void
  prioritaire?: boolean
}

/** Scale used to bring a detail of half-width r (fraction of the picture width) up close. */
const echelle = (r: number) => Math.min(3.4, Math.max(1.6, 0.3 / r))

export function Plan({ capture, alt, zooms = [], zoom, onZoom, prioritaire = true }: Props) {
  const { w, h } = image(capture).img
  const a = w / h
  const z = zoom === null ? null : zooms[zoom]

  useEffect(() => {
    if (zoom === null) return
    const f = (e: KeyboardEvent) => { if (e.key === 'Escape') { e.stopImmediatePropagation(); onZoom(null) } }
    window.addEventListener('keydown', f, true)
    return () => window.removeEventListener('keydown', f, true)
  }, [zoom, onZoom])

  const style = { '--a': a } as Record<string, string | number>
  if (z) {
    const s = echelle(z.r)
    style['--s'] = s
    style['--zx'] = `${((0.5 - z.x) * s * 100).toFixed(2)}%`
    style['--zy'] = `${((0.5 - z.y) * s * 100).toFixed(2)}%`
  }
  const flou = apercu(capture)

  return (
    <div className={`scene scene-plan${z ? ' zoomee' : ''}`} onClick={z ? () => onZoom(null) : undefined}>
      {flou && <div className="scene-flou" style={{ backgroundImage: `url(${flou})` }} aria-hidden="true" />}
      <div className="cadre" style={style as React.CSSProperties}>
        <div className="derive">
          <Capture nom={capture} alt={alt} prioritaire={prioritaire} sizes="(max-width: 760px) 100vw, max(100vw, 178vh)" />
          {zooms.map((d, i) => (
            <button
              key={d.label}
              type="button"
              className={`detail${zoom === i ? ' actif' : ''}`}
              style={{
                '--x': d.x, '--y': d.y, '--r': d.r, '--ratio': d.ratio ?? 1,
              } as React.CSSProperties}
              onClick={(e) => { e.stopPropagation(); onZoom(zoom === i ? null : i) }}
              aria-label={`Regarder de près : ${d.label}`}
              aria-pressed={zoom === i}
              tabIndex={-1}
            >
              <span className="detail-cadre" aria-hidden="true" />
              <span className="detail-point" aria-hidden="true">{i + 1}</span>
              <span className="detail-nom" aria-hidden="true">{d.label}</span>
            </button>
          ))}
        </div>
      </div>
      {z && (
        <p className="zoom-legende" aria-live="polite">
          <span>{z.label}</span>
          <span className="zoom-sortir">Échap ou clic pour revenir</span>
        </p>
      )}
    </div>
  )
}
