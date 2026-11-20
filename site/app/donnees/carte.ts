// Reading positions on the disc map (the 775 px image spans -6200..6200 blocks).
export const BORD = 6000
export const R_IMG = 6000 / 12400 // edge radius as a fraction of the map image
export const R_RIVAGE = 5700 / 12400 // open sea starts here

/** Reads a map position as distance from the centre and climate. */
export function lireCarte(fx: number, fy: number) {
  const dx = fx - 0.5, dy = fy - 0.5
  const d = Math.hypot(dx, dy) / R_IMG
  const blocs = Math.round((d * BORD) / 10) * 10
  if (blocs > BORD) return { titre: 'Au-delà du bord', texte: 'La brume ne laisse passer personne.' }
  const lat = dy / R_IMG
  const climat = lat < -0.35 ? 'froid' : lat > 0.35 ? 'chaud' : 'tempéré'
  const minutes = Math.max(1, Math.round(blocs / 4.3 / 60))
  return {
    titre: `${blocs.toLocaleString('fr-FR')} blocs du centre`,
    texte: blocs > 5700 ? 'Pleine mer, avant le bord.' : `Climat ${climat} · ${minutes} min de marche`,
  }
}
