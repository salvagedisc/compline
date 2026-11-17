// Engraved line icons in the manner of an old atlas legend, drawn for the site on a 48 unit grid.
// Strokes follow currentColor; the fine hatching uses a thinner stroke.

type Nom =
  | 'disque' | 'lac' | 'cimes' | 'foyer' | 'riviere' | 'forge' | 'zoo'
  | 'journal' | 'route' | 'son' | 'muet' | 'gauche' | 'droite' | 'fermer' | 'index' | 'loupe'

const TRAITS: Record<Nom, React.ReactNode> = {
  // The disc: coast, mist ring and a compass needle.
  disque: (
    <>
      <circle cx="24" cy="24" r="20.5" strokeDasharray="1.2 2.6" />
      <path d="M24 8.5c4.6.4 7.5 2.4 10.3 4.4 2.6 2 4.8 4.9 5.1 9.3.4 4.4-1.5 8.2-4.2 10.8-2.6 2.5-6.6 4.6-11.4 4.5-4.6-.1-8.2-2.1-10.6-4.9-2.5-2.9-3.9-6.4-3.6-10.4.3-3.7 2.1-6.9 4.7-9.3 2.6-2.3 5.7-4.6 9.7-4.4Z" />
      <path d="M24 13.5 26.6 24 24 34.5 21.4 24Z" />
      <path d="M24 13.5V34.5" className="fin" />
      <path d="M26.6 24H21.4" className="fin" />
      <path d="M22.2 17.5 21.4 24M22.7 20.2l.5 7.8M22.3 26.5l.8 5" className="fin" />
    </>
  ),
  // The lake: an evening sun on still water, the render's light.
  lac: (
    <>
      <path d="M13 26a11 11 0 0 1 22 0" />
      <path d="M24 8v4M12.3 13.3l2.8 2.8M35.7 13.3l-2.8 2.8M6 26h36" />
      <path d="M18.5 22a6 6 0 0 1 4-4" className="fin" />
      <path d="M11 31h10M26 31h11M15.5 35.5h7M26.5 35.5h6M20.5 40h7" className="fin" />
    </>
  ),

  // The peaks: cold and altitude.
  cimes: (
    <>
      <path d="M4 39 18.5 13 26 26.5l4.5-7.5L44 39Z" />
      <path d="m14.2 20.8 2.6 2.4 1.7-2.2 2 2.4 1.6-1.5M27.7 23.7l1.8 1.4 1.1-1.5 1.5 1.6" />
      <path d="M19.5 25l4 14M21.5 27.5l3.4 11.5M23.5 30l2.8 9M32 25.5l4.6 13.5M34 28.5l3.5 10.5" className="fin" />
      <path d="M38 8.5v7M34.6 10.5l6.8 3M34.6 13.5l6.8-3" className="fin" />
    </>
  ),
  // The hearth: a pot over a fire, steam rising.
  foyer: (
    <>
      <path d="M9.5 19h29" />
      <path d="M11.5 19c0 9.5 5.2 15.5 12.5 15.5S36.5 28.5 36.5 19" />
      <path d="M11.8 21.5c-2.8.2-4.1 2.4-3 4.6M36.2 21.5c2.8.2 4.1 2.4 3 4.6" />
      <path d="M17.5 33.6 16 37M30.5 33.6 32 37" />
      <path d="M12 43.5h24" />
      <path d="M20 43c-1.7-2.2-.9-4.4 1-6.2.2 1.9 1.4 2.7 1.4 4.4.9-1.7 2.4-2.6 2.2-5 2.4 2 3.4 4.6 2.2 6.8" />
      <path d="M19 14.5c-1.5-1.6 1.5-3 0-4.8M24 13.5c-1.5-1.6 1.5-3 0-4.8M29 14.5c-1.5-1.6 1.5-3 0-4.8" className="fin" />
      <path d="M30.5 22.5c0 4.2-1.6 7.4-4.4 9.2M33.6 22c-.2 5-2.2 8.6-5.4 10.6" className="fin" />
    </>
  ),

  // The river: a fish rising to a line.
  riviere: (
    <>
      <path d="M9 25.5c5-5.6 11.5-8.2 18-8.2 5 0 9 3 12 8.2-3 5.2-7 8.2-12 8.2-6.5 0-13-2.6-18-8.2Z" />
      <path d="M9 25.5 3.5 20v11Z" />
      <circle cx="33" cy="24.2" r="1.1" />
      <path d="M28.5 19.6c1.8 3.6 1.8 8.2 0 11.8" />
      <path d="M14.5 23.5c1.2 1.4 1.2 2.6 0 4M19 21.6c1.6 2.4 1.6 5.4 0 7.8M23.5 20.4c1.8 3 1.8 7.2 0 10.2" className="fin" />
      <path d="M43 4v14.5a3.2 3.2 0 1 1-6.4 0v-1.2l1.6 1.4" />
      <path d="M5 41c3 1.6 5 1.6 8 0s5-1.6 8 0 5 1.6 8 0 5-1.6 8 0 5 1.6 6 1" className="fin" />
    </>
  ),

  // The forge: anvil, hammer and sparks.
  forge: (
    <>
      <path d="M8 22h24c0 3.6 2.5 5.5 6 5.5H40v2.5H34c-1.5 0-2.5 1-2.5 2.5v2.5H17v-2.5c0-1.5-1-2.5-2.5-2.5H13c-3 0-5-2.5-5-5Z" />
      <path d="M14 39h21l-2.5-4.5H16.5Z" />
      <path d="m27 6 9 9M24.5 12.5l6-6 3 3-6 6Z" />
      <path d="M11 25.5h19M13.5 28h15" className="fin" />
      <path d="M38.5 18.5l2-1.5M40.5 21.5h2.5M15 15l-1.8-1.8M19 13.5V11" className="fin" />
    </>
  ),
  // The zoo: a pawprint under the study glass.
  zoo: (
    <>
      <circle cx="22" cy="22" r="14" />
      <path d="m32 32 10 10" strokeWidth="2.6" />
      <path d="M22 31.5c-3.4 0-6.2-1.6-6.2-4 0-2.8 3-5.5 6.2-5.5s6.2 2.7 6.2 5.5c0 2.4-2.8 4-6.2 4Z" />
      <ellipse cx="15.5" cy="18.5" rx="1.9" ry="2.5" transform="rotate(-20 15.5 18.5)" />
      <ellipse cx="19.8" cy="14.6" rx="1.9" ry="2.6" transform="rotate(-6 19.8 14.6)" />
      <ellipse cx="24.4" cy="14.6" rx="1.9" ry="2.6" transform="rotate(6 24.4 14.6)" />
      <ellipse cx="28.6" cy="18.5" rx="1.9" ry="2.5" transform="rotate(20 28.6 18.5)" />
      <path d="M12.5 13.5c1.8-2.3 4.4-3.9 7.4-4.4" className="fin" />
    </>
  ),
  journal: (
    <>
      <path d="M24 13c-4-2.6-10-3.5-17-2.5v26c7-1 13 0 17 2.5 4-2.5 10-3.5 17-2.5v-26c-7-1-13-.1-17 2.5Z" />
      <path d="M24 13v26" />
      <path d="M11 17c3-.4 6.5 0 9 1.2M11 21.5c3-.4 6.5 0 9 1.2M11 26c3-.4 6.5 0 9 1.2M28 18.2c2.5-1.2 6-1.6 9-1.2M28 22.7c2.5-1.2 6-1.6 9-1.2" className="fin" />
    </>
  ),
  route: (
    <>
      <path d="M8 40c6-1.5 7-6 4-9.5s-2.5-8 4-9 12.5-1.5 13.5-6.5" strokeDasharray="2 2.6" />
      <path d="M31 4.5v14M31 5h10l-3 3.2 3 3.3H31" />
      <circle cx="8" cy="40" r="1.6" />
      <path d="m36 30 6 6m0-6-6 6" />
    </>
  ),
  son: (
    <>
      <path d="M8 19h7l9-7v24l-9-7H8Z" />
      <path d="M30 18.5c1.8 1.6 2.8 3.4 2.8 5.5s-1 3.9-2.8 5.5M34.5 14c3 2.8 4.5 6.1 4.5 10s-1.5 7.2-4.5 10" />
    </>
  ),
  muet: (
    <>
      <path d="M8 19h7l9-7v24l-9-7H8Z" />
      <path d="m31 19 10 10m0-10L31 29" />
    </>
  ),
  gauche: <path d="M42 24H7m8-8-8 8 8 8" />,
  droite: <path d="M6 24h35m-8-8 8 8-8 8" />,
  fermer: <path d="m12 12 24 24m0-24L12 36" />,
  index: <path d="M9 15h30M9 24h30M9 33h30" />,
  loupe: (
    <>
      <circle cx="21" cy="21" r="11" />
      <path d="m29 29 11 11M21 16v10M16 21h10" />
    </>
  ),
}

export function Icone({ nom, taille = 28, titre }: { nom: Nom; taille?: number; titre?: string }) {
  return (
    <svg
      className={`icone icone-${nom}`}
      viewBox="0 0 48 48"
      width={taille}
      height={taille}
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      role={titre ? 'img' : undefined}
      aria-label={titre}
      aria-hidden={titre ? undefined : true}
    >
      {TRAITS[nom]}
    </svg>
  )
}

export type { Nom as NomIcone }
