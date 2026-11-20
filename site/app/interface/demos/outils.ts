// Shared pieces of the hands-on demonstrations: a drawing loop that sleeps while its page is hidden, a pixel
// canvas sized to its box, value noise, and the pointer in canvas pixels.
import { useEffect, useRef, useState } from 'react'

/** Deterministic noise in [0, 1) for an integer cell. */
export function hasard(x: number, y: number, graine = 0): number {
  let h = (x * 374761393 + y * 668265263 + graine * 2147483647) | 0
  h = Math.imul(h ^ (h >>> 13), 1274126177)
  h ^= h >>> 16
  return (h >>> 0) / 4294967296
}

/** Smooth value noise in [0, 1). */
export function bruit(x: number, y: number, graine = 0): number {
  const xi = Math.floor(x), yi = Math.floor(y)
  const fx = x - xi, fy = y - yi
  const sx = fx * fx * (3 - 2 * fx), sy = fy * fy * (3 - 2 * fy)
  const a = hasard(xi, yi, graine), b = hasard(xi + 1, yi, graine)
  const c = hasard(xi, yi + 1, graine), d = hasard(xi + 1, yi + 1, graine)
  return a + (b - a) * sx + (c - a) * sy + (a - b - c + d) * sx * sy
}

export const borner = (v: number, a = 0, b = 1) => Math.min(b, Math.max(a, v))
export const lisse = (a: number, b: number, v: number) => { const t = borner((v - a) / (b - a)); return t * t * (3 - 2 * t) }

/** Whether the visitor asked for less motion. */
export function useCalme() {
  const [calme, setCalme] = useState(false)
  useEffect(() => {
    const m = window.matchMedia('(prefers-reduced-motion: reduce)')
    const f = () => setCalme(m.matches)
    f()
    m.addEventListener('change', f)
    return () => m.removeEventListener('change', f)
  }, [])
  return calme
}

/** True while the page holding this element is hidden underneath the one on screen. */
const enAttente = (el: Element | null) => !!el?.closest('.couche.attente')

/**
 * Calls `image(dt, t)` on every frame while the element is on screen (dt in seconds, capped so a hidden tab does
 * not jump). The callback is read from a ref, so it can close over fresh state without restarting the loop.
 */
export function useBoucle(ref: React.RefObject<Element | null>, image: (dt: number, t: number) => void) {
  const cb = useRef(image)
  useEffect(() => { cb.current = image })
  useEffect(() => {
    let id = 0, avant = performance.now()
    const tour = (t: number) => {
      const dt = Math.min(0.1, (t - avant) / 1000)
      avant = t
      if (!enAttente(ref.current)) cb.current(dt, t / 1000)
      id = requestAnimationFrame(tour)
    }
    id = requestAnimationFrame(tour)
    return () => cancelAnimationFrame(id)
  }, [ref])
}

export interface Toile { w: number; h: number; px: number }

/**
 * A canvas whose pixels are `px` CSS pixels wide (drawn sharp, like the game's own texels), resized with its box.
 * Returns the size in canvas pixels; 0 until measured.
 */
export function useToile(ref: React.RefObject<HTMLCanvasElement | null>, px: (largeur: number) => number) {
  const [t, setT] = useState<Toile>({ w: 0, h: 0, px: 1 })
  const regle = useRef(px)
  useEffect(() => {
    const c = ref.current
    if (!c) return
    const mesurer = () => {
      // Layout size, not the drawn one: the page may still be scaling in.
      const lw = c.clientWidth, lh = c.clientHeight
      if (!lw || !lh) return
      const p = regle.current(lw)
      const w = Math.ceil(lw / p), h = Math.ceil(lh / p)
      if (c.width !== w || c.height !== h) { c.width = w; c.height = h }
      setT((v) => (v.w === w && v.h === h && v.px === p ? v : { w, h, px: p }))
    }
    mesurer()
    const ro = new ResizeObserver(mesurer)
    ro.observe(c)
    return () => ro.disconnect()
  }, [ref])
  return t
}

/** Pointer position in canvas pixels, from an event on the canvas or one of its parents. */
export function pointeur(c: HTMLCanvasElement, e: { clientX: number; clientY: number }) {
  const r = c.getBoundingClientRect()
  return { x: ((e.clientX - r.left) / r.width) * c.width, y: ((e.clientY - r.top) / r.height) * c.height }
}

/** French number with a thin space for thousands, as the game writes them. */
export const nombre = (n: number, decimales = 0) =>
  n.toLocaleString('fr-FR', { minimumFractionDigits: decimales, maximumFractionDigits: decimales }).replace(/ | /g, ' ')
