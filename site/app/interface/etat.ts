// Small client state shared between the 3D world and the interface, without a library.
import { useSyncExternalStore } from 'react'

type Etat = { mondePret: boolean; entre: boolean; son: boolean }
let etat: Etat = { mondePret: false, entre: false, son: false }
const abonnes = new Set<() => void>()

function changer(p: Partial<Etat>) {
  etat = { ...etat, ...p }
  abonnes.forEach((f) => f())
}

const abonner = (f: () => void) => { abonnes.add(f); return () => abonnes.delete(f) }
const SERVEUR: Etat = { mondePret: false, entre: false, son: false }

export const useEtat = () => useSyncExternalStore(abonner, () => etat, () => SERVEUR)
export const signalerMondePret = () => changer({ mondePret: true })
export const entrer = (son: boolean) => changer({ entre: true, son })
export const basculerSon = () => changer({ son: !etat.son })
