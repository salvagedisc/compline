// Shared by the pages and their transitions: picture sizes, fetching ahead, and which way the visit is going.
import { image } from '../donnees/images'

/** The sizes every full-screen picture is drawn at, shared with the code that fetches the next ones ahead. */
export const TAILLES_PLAN = '(max-width: 760px) 100vw, max(100vw, 178vh)'

/**
 * Which way the visit is going, read by the page transition (the picture slides that way). Set just before a
 * navigation; cleared once it has played.
 */
let minuterie = 0
export function SENS(sens: 'avant' | 'arriere' | null) {
  const d = document.documentElement.dataset
  if (sens) d.sens = sens; else delete d.sens
  window.clearTimeout(minuterie)
  minuterie = window.setTimeout(() => { delete d.sens }, 1500)
}

/** Fetches a full-screen picture ahead: a detached <picture> picks the same source the page will, and leaves it in the cache. */
const deja = new Set<string>()
export function prechargerPlan(nom: string) {
  if (deja.has(nom)) return
  deja.add(nom)
  const { sources, img } = image(nom)
  const pic = document.createElement('picture')
  for (const [f, srcset] of Object.entries(sources)) {
    if (f === 'jpeg' || f === 'jpg') continue
    const src = document.createElement('source')
    src.type = `image/${f}`; src.srcset = srcset; src.sizes = TAILLES_PLAN
    pic.append(src)
  }
  const i = new Image()
  i.decoding = 'async'
  pic.append(i)
  i.src = img.src
}
