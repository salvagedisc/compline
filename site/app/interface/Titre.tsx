// A heading whose letters come out of the mist one by one, in a scattered order.
// Pure CSS: the letters are in the prerendered HTML with their animation, so the title plays without
// waiting for JavaScript, and simply appears when the visitor prefers less motion (see styles.css).

interface Props { children: string; id?: string; className?: string; niveau?: 1 | 2; delai?: number }

const PAS = 0.035 // seconds between two letters

// Stable scattered order: stepping through the letters by a stride prime to their count visits each
// rank once, so the same title always comes out of the mist the same way.
const rang = (k: number, n: number) => {
  const pas = [7, 11, 13, 5, 3].find((p) => n % p !== 0) ?? 1
  return (k * pas + 3) % n
}

export function Titre({ children, id, className, niveau = 1, delai = 0.15 }: Props) {
  const H = niveau === 1 ? 'h1' : 'h2'
  const lettres = children.replace(/\s/g, '').length
  let k = 0
  return (
    <H id={id} className={`titre-brume${className ? ` ${className}` : ''}`}>
      <span className="lecteur">{children}</span>
      <span aria-hidden="true">
        {children.split(' ').map((mot, m) => (
          <span key={m}>
            {m > 0 && ' '}
            <span className="mot">
              {[...mot].map((c) => {
                const i = k++
                const d = delai + rang(i, lettres) * PAS
                return <span key={i} className="lettre" style={{ animationDelay: `${d.toFixed(3)}s` }}>{c}</span>
              })}
            </span>
          </span>
        ))}
      </span>
    </H>
  )
}
