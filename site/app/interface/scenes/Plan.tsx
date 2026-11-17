// A capture filling the screen like a film still. Its details are marked inside the picture itself;
// choosing one moves the camera into the picture, onto that detail.
import { useEffect, useRef } from 'react'
import type { Zoom } from '../../donnees/lieux'
import { apercu, image } from '../../donnees/images'
import { Capture } from '../Capture'
import { TAILLES_PLAN } from '../transitions'

interface Props {
  capture: string
  alt: string
  zooms?: Zoom[]
  zoom: number | null
  onZoom: (i: number | null) => void
  prioritaire?: boolean
}

/** Whatever stands over the picture: a detail point behind one of these is hidden (it stays in the caption's list). */
const PAR_DESSUS = '.cartouche > *, .film, .journal-frise, .entete-actions, .marque, .ici, .comparer-nom'

/** Hides the points that something else on the page covers, and shows them again when the layout changes. */
function useDegager(scene: React.RefObject<HTMLDivElement | null>, n: number) {
  useEffect(() => {
    const el = scene.current
    if (!el || n === 0) return
    const mesurer = () => {
      const v = el.getBoundingClientRect()
      const cadre = { left: Math.max(0, v.left), top: Math.max(0, v.top), right: Math.min(innerWidth, v.right), bottom: Math.min(innerHeight, v.bottom) }
      const obstacles = [...document.querySelectorAll(PAR_DESSUS)].map((o) => o.getBoundingClientRect()).filter((r) => r.width > 0)
      el.querySelectorAll<HTMLElement>('.detail').forEach((d) => {
        const p = d.querySelector('.detail-point')!.getBoundingClientRect()
        const m = 10
        const cache = obstacles.some((r) => p.right + m > r.left && p.left - m < r.right && p.bottom + m > r.top && p.top - m < r.bottom)
          || p.left < cadre.left || p.top < cadre.top || p.right > cadre.right || p.bottom > cadre.bottom
        d.classList.toggle('sous', cache)
      })
    }
    mesurer()
    // Again once the caption has finished rising into place.
    const t = [window.setTimeout(mesurer, 700), window.setTimeout(mesurer, 2400)]
    const ro = new ResizeObserver(mesurer)
    ro.observe(el)
    return () => { t.forEach(clearTimeout); ro.disconnect() }
  }, [scene, n])
}

/** Scale used to bring a detail of half-width r (fraction of the picture width) up close. */
const echelle = (r: number) => Math.min(3.4, Math.max(1.6, 0.3 / r))

export function Plan({ capture, alt, zooms = [], zoom, onZoom, prioritaire = true }: Props) {
  const { w, h } = image(capture).img
  const a = w / h
  const z = zoom === null ? null : zooms[zoom]
  const scene = useRef<HTMLDivElement>(null)
  useDegager(scene, zooms.length)

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
    <div ref={scene} className={`scene scene-plan${z ? ' zoomee' : ''}`} onClick={z ? () => onZoom(null) : undefined}>
      {flou && <div className="scene-flou" style={{ backgroundImage: `url(${flou})` }} aria-hidden="true" />}
      <div className="cadre" style={style as React.CSSProperties}>
        <div className="derive">
          <Capture nom={capture} alt={alt} prioritaire={prioritaire} sizes={TAILLES_PLAN} />
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
