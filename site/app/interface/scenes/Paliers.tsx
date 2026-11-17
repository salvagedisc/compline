// The five tiers of metal as ingots on a rising stair, over the forge. Choose one to read how it is made.
import { useState } from 'react'
import { DINOS, PALIERS, PIOCHE, ROMAINS } from '../../donnees/details'
import { Capture } from '../Capture'

function Lingot({ teinte }: { teinte: string }) {
  return (
    <svg className="lingot" viewBox="0 0 120 64" aria-hidden="true">
      <defs>
        <linearGradient id={`l-${teinte.slice(1)}`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor="#fff" stopOpacity="0.55" />
          <stop offset="0.45" stopColor="#fff" stopOpacity="0" />
          <stop offset="1" stopColor="#000" stopOpacity="0.35" />
        </linearGradient>
      </defs>
      {/* Top face, front face, side face of a cast ingot. */}
      <path d="M22 14h76l14 18H8Z" fill={teinte} />
      <path d="M22 14h76l14 18H8Z" fill={`url(#l-${teinte.slice(1)})`} />
      <path d="M8 32h104l-6 22H14Z" fill={teinte} />
      <path d="M8 32h104l-6 22H14Z" fill="#000" fillOpacity="0.28" />
      <g fill="none" stroke="#120f0c" strokeWidth="1.4" strokeLinejoin="round">
        <path d="M22 14h76l14 18-6 22H14L8 32Z" />
        <path d="M8 32h104" />
      </g>
      <g stroke="#120f0c" strokeOpacity="0.45" strokeWidth="0.8">
        <path d="M20 37l-2 12M30 37l-1.5 13M40 37l-1 13M50 37l-.5 13M80 37l.5 13M90 37l1 13M100 37l1.5 12" />
      </g>
      <path d="M30 19h40" stroke="#fff" strokeOpacity="0.6" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  )
}

export function Paliers() {
  const [i, setI] = useState(2)
  const p = PALIERS[i]
  return (
    <div className="scene scene-paliers">
      <div className="scene-fond-sombre" aria-hidden="true"><Capture nom="forge.jpg" alt="" prioritaire /></div>
      <div className="paliers">
        <div className="paliers-escalier" role="tablist" aria-label="Paliers du métal">
          {PALIERS.map((q, k) => (
            <button
              key={q.nom}
              type="button"
              role="tab"
              id={`palier-${k}`}
              aria-selected={k === i}
              aria-controls="palier-fiche"
              tabIndex={k === i ? 0 : -1}
              className="palier"
              style={{ '--k': k } as React.CSSProperties}
              onClick={() => setI(k)}
              onKeyDown={(e) => {
                const d = e.key === 'ArrowRight' || e.key === 'ArrowUp' ? 1 : e.key === 'ArrowLeft' || e.key === 'ArrowDown' ? -1 : 0
                if (!d) return
                e.preventDefault(); e.stopPropagation()
                const n = (k + d + PALIERS.length) % PALIERS.length
                setI(n)
                document.getElementById(`palier-${n}`)?.focus()
              }}
            >
              <Lingot teinte={q.teinte} />
              <span className="palier-num">{ROMAINS[k]}</span>
              <span className="palier-nom">{q.nom}</span>
            </button>
          ))}
        </div>
        <div id="palier-fiche" className="palier-fiche" role="tabpanel" aria-labelledby={`palier-${i}`} key={p.nom}>
          <p className="petites-caps">{p.ou}</p>
          <h2>{p.objet}</h2>
          {p.lignes.map((l) => <p key={l}>{l}</p>)}
          <p className="palier-note">{PIOCHE}</p>
        </div>
        <div className="paliers-dinos">
          <p className="petites-caps">À côté du métal : les armures de dinosaures</p>
          <ul>
            {DINOS.map((d) => (
              <li key={d.nom}><b>{d.nom}</b><span>{d.milieu}</span><span>{d.ouvre}</span></li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  )
}
