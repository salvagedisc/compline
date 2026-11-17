// Every page of the site, rendered to HTML at build time.
import { LIEUX } from './lieux'

export const CHEMINS = [
  ...LIEUX.flatMap((l) => [`/${l.chemin}`, ...l.fonctions.map((f) => `/${l.chemin}/${f.id}`)]),
  '/journal',
  '/feuille-de-route',
]
