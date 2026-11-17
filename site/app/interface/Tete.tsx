import { Link } from 'react-router'
import { basculerSon, useEtat } from './etat'
import { couperSon, demarrerSon } from './son'

export function Tete() {
  const { son, entre } = useEtat()
  const changer = () => {
    if (son) couperSon(); else demarrerSon()
    basculerSon()
  }
  return (
    <header className="tete">
      <Link to="/" className="marque" aria-label="Compline, retour au disque">
        <span className="marque-nom">Compline</span>
        <span className="pix marque-sous">Un modpack entre amis</span>
      </Link>
      {entre && (
        <button type="button" className="bouton-son" aria-pressed={son} onClick={changer}>
          <span className={`ondes${son ? ' actives' : ''}`} aria-hidden="true"><i /><i /><i /><i /></span>
          <span className="pix">{son ? 'Son' : 'Muet'}</span>
        </button>
      )}
    </header>
  )
}
