// Shared by the pages and their transitions: picture sizes, and which way the visit is going.

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
