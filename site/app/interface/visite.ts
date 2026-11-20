// Remembers, in this browser only, the newest journal entry already seen, to flag what is new since.
import { useEffect, useState } from 'react'
import { JOURNAL } from '../donnees/lieux'

const CLE = 'compline-vu'
const DERNIER = JOURNAL[JOURNAL.length - 1].iso

/** The ISO date of the newest entry seen on the previous visit, or null on a first visit. */
export function useDejaVu() {
  const [vu, setVu] = useState<string | null | undefined>(undefined)
  useEffect(() => {
    let avant: string | null = null
    try { avant = localStorage.getItem(CLE) } catch { /* storage blocked */ }
    setVu(avant)
    const t = window.setTimeout(() => { try { localStorage.setItem(CLE, DERNIER) } catch { /* storage blocked */ } }, 4000)
    return () => window.clearTimeout(t)
  }, [])
  return vu
}

export const estNouveau = (iso: string, vu: string | null | undefined) => !!vu && iso > vu
