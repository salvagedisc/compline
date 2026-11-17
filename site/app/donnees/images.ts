// Every capture of the bench, optimised at build time: AVIF and WebP at three widths, plus a tiny blurred preview.

export interface Picture {
  sources: Record<string, string>
  img: { src: string; w: number; h: number }
}

const pleines = import.meta.glob<Picture>('../captures/*.{jpg,png}', {
  eager: true, import: 'default', query: { w: '640;1280;1920', format: 'avif;webp;jpg', as: 'picture' },
})
const floues = import.meta.glob<string>('../captures/*.{jpg,png}', {
  eager: true, import: 'default', query: { w: '24', format: 'webp', quality: '50', inline: '' },
})

const cle = (chemin: string) => chemin.slice(chemin.lastIndexOf('/') + 1)
const PLEINES = Object.fromEntries(Object.entries(pleines).map(([k, v]) => [cle(k), v]))
const FLOUES = Object.fromEntries(Object.entries(floues).map(([k, v]) => [cle(k), v]))

export function image(nom: string): Picture {
  const p = PLEINES[nom]
  if (!p) throw new Error(`Unknown capture: ${nom}`)
  return p
}

export const apercu = (nom: string): string | undefined => FLOUES[nom]

/** The largest JPEG of a capture, for link previews and zoomed details. */
export const grande = (nom: string) => image(nom).img.src
