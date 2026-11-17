// The disc as an engraved chart: the map sampled on the bench server, the coast and edge rings, a compass and
// a scale. Moving over it measures the distance to the centre and the climate.
import { useState } from 'react'
import { BORD, R_IMG, R_RIVAGE, lireCarte } from '../../donnees/carte'
import { LIEUX } from '../../donnees/lieux'
import { ROMAINS } from '../../donnees/details'

const CARTE = `${import.meta.env.BASE_URL}monde/couleurs.webp`

export function Mesure() {
  const [l, setL] = useState<{ titre: string; texte: string; fx: number; fy: number } | null>(null)
  const bouger = (e: React.PointerEvent<HTMLDivElement>) => {
    const r = e.currentTarget.getBoundingClientRect()
    const fx = (e.clientX - r.left) / r.width, fy = (e.clientY - r.top) / r.height
    setL({ ...lireCarte(fx, fy), fx, fy })
  }
  const mille = (1000 / (BORD * 2)) * (R_IMG * 2) * 100 // 1 000 blocks as a share of the map width, in %
  return (
    <div className="scene scene-mesure">
      <div className="mesure">
        <div className="mesure-carte" onPointerMove={bouger} onPointerDown={bouger} onPointerLeave={() => setL(null)}>
          <img src={CARTE} alt="Carte du disque relevée sur le serveur d'essai : montagnes enneigées au nord, forêts et prairies au centre, lacs et rivières" draggable={false} width={775} height={775} />
          <svg viewBox="0 0 100 100" aria-hidden="true">
            <circle cx="50" cy="50" r={R_IMG * 100} className="mesure-bord" />
            <circle cx="50" cy="50" r={R_RIVAGE * 100} className="mesure-rivage" />
            {[0.25, 0.5, 0.75].map((f) => <circle key={f} cx="50" cy="50" r={R_IMG * 100 * f} className="mesure-cercle" />)}
            <path d="M50 1v4M50 95v4M1 50h4M95 50h4" className="mesure-cercle" />
            {l && Math.hypot(l.fx - 0.5, l.fy - 0.5) <= R_IMG + 0.02 && (
              <>
                <line x1="50" y1="50" x2={l.fx * 100} y2={l.fy * 100} className="mesure-ligne" />
                <circle cx={l.fx * 100} cy={l.fy * 100} r="0.9" className="mesure-point" />
              </>
            )}
            <circle cx="50" cy="50" r="0.7" className="mesure-point" />
            {LIEUX.map((p, i) => (
              <g key={p.id} transform={`translate(${p.x * 100} ${p.y * 100})`}>
                <circle r="1.6" className="mesure-lieu" />
                <text y="0.55" className="mesure-lieu-num">{ROMAINS[i]}</text>
              </g>
            ))}
          </svg>
          <span className="mesure-n petites-caps" aria-hidden="true">Nord · froid</span>
          <span className="mesure-s petites-caps" aria-hidden="true">Sud · chaud</span>
          <div className="mesure-echelle" aria-hidden="true" style={{ width: `${mille * 3}%` }}>
            <span><i /><i /><i /></span>
            <small>3 000 blocs</small>
          </div>
        </div>
        <div className="mesure-legende">
          <p className="mesure-lecture" aria-live="polite">
            <b>{l ? l.titre : '6 000 blocs du centre au bord'}</b>
            <span>{l ? l.texte : 'Survolez la carte pour mesurer.'}</span>
          </p>
          <ul className="mesure-cles">
            <li><span className="cle-rivage" aria-hidden="true" />La côte, vers 5 700 blocs</li>
            <li><span className="cle-bord" aria-hidden="true" />Le bord, à 6 000 blocs</li>
          </ul>
        </div>
      </div>
    </div>
  )
}
