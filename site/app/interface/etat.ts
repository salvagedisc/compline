// Small client state shared between the sky, the interface and the pages, without a library.
import { useSyncExternalStore } from 'react'

type Etat = {
  mondePret: boolean // the sky has drawn its first frames
  entre: boolean // the visitor went past the title screen
  son: boolean
  index: boolean // the full-screen index is open
  plongee: number // time of the latest dive from the map onto a place, 0 when the page was opened directly
}
let etat: Etat = { mondePret: false, entre: false, son: false, index: false, plongee: 0 }
const abonnes = new Set<() => void>()

function changer(p: Partial<Etat>) {
  etat = { ...etat, ...p }
  abonnes.forEach((f) => f())
}

const abonner = (f: () => void) => { abonnes.add(f); return () => abonnes.delete(f) }
const SERVEUR: Etat = { mondePret: false, entre: false, son: false, index: false, plongee: 0 }

export const useEtat = () => useSyncExternalStore(abonner, () => etat, () => SERVEUR)
export const lireEtat = () => etat
export const signalerMondePret = () => changer({ mondePret: true })
export const entrer = (son: boolean) => changer({ entre: true, son })
export const basculerSon = () => changer({ son: !etat.son })
export const ouvrirIndex = (index: boolean) => changer({ index })
export const signalerPlongee = () => changer({ plongee: performance.now() })
