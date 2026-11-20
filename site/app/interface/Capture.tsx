import { useState } from 'react'
import { apercu, image } from '../donnees/images'

interface Props {
  nom: string
  alt: string
  sizes?: string
  className?: string
  prioritaire?: boolean // the main picture of the view: loaded at once, not lazily
  draggable?: boolean
}

/** A bench capture as a responsive <picture> (AVIF, WebP, JPEG), over its blurred preview until it loads. */
export function Capture({ nom, alt, sizes = '100vw', className, prioritaire, draggable = false }: Props) {
  const p = image(nom)
  const [charge, setCharge] = useState(false)
  const flou = apercu(nom)
  return (
    <picture
      className={`capture${charge ? ' chargee' : ''}${className ? ` ${className}` : ''}`}
      style={flou ? { backgroundImage: `url(${flou})` } : undefined}
    >
      {Object.entries(p.sources).filter(([f]) => f !== 'jpeg' && f !== 'jpg').map(([f, srcset]) => (
        <source key={f} type={`image/${f}`} srcSet={srcset} sizes={sizes} />
      ))}
      <img
        src={p.img.src}
        width={p.img.w}
        height={p.img.h}
        alt={alt}
        loading={prioritaire ? 'eager' : 'lazy'}
        decoding="async"
        fetchPriority={prioritaire ? 'high' : 'auto'}
        draggable={draggable}
        onLoad={() => setCharge(true)}
        ref={(el) => { if (el?.complete && el.naturalWidth) setCharge(true) }}
      />
    </picture>
  )
}
