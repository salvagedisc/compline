// BROUILLON Fallow — conversion d'une zone en biome fallow:clairiere (plan/10 §10.3).
// /fillbiome est limité à 32 768 blocs par commande en 1.21.1 : on découpe en colonnes de 8 × 64 × 8
// (4 096 blocs) et on étale le travail sur plusieurs ticks. Le biome est écrit dans les chunks une fois pour
// toutes : aucun coût continu. Casser le feu ne rend pas la brume (décision 10.10-1, défaut « permanent »).

const CLAIRIERE = {
  biome: 'fallow:clairiere',
  filtre: '#fallow:peut_devenir_clairiere', // biomes terrestres seulement
  colonne: 8,       // côté d'une colonne, en blocs
  dessous: 24,      // blocs sous le feu
  dessus: 40,       // blocs au-dessus du feu
  parTick: 4        // colonnes converties par tick
}

BlockEvents.placed(event => {
  const rayon = global.FALLOW_CLAIRIERES[String(event.block.id)]
  if (!rayon) return
  const level = event.level
  if (String(level.dimension) !== 'minecraft:overworld') return

  const c = event.block.pos
  const s = CLAIRIERE.colonne
  const colonnes = []
  for (let dx = -rayon; dx < rayon; dx += s) {
    for (let dz = -rayon; dz < rayon; dz += s) {
      // garder les colonnes dont le centre est dans le cercle
      const mx = dx + s / 2, mz = dz + s / 2
      if (mx * mx + mz * mz > rayon * rayon) continue
      colonnes.push([c.x + dx, c.z + dz])
    }
  }

  const y1 = c.y - CLAIRIERE.dessous, y2 = c.y + CLAIRIERE.dessus
  const server = event.server
  let i = 0
  const etape = () => {
    for (let n = 0; n < CLAIRIERE.parTick && i < colonnes.length; n++, i++) {
      const [x, z] = colonnes[i]
      server.runCommandSilent(
        `execute in minecraft:overworld run fillbiome ${x} ${y1} ${z} ${x + s - 1} ${y2} ${z + s - 1} ${CLAIRIERE.biome} replace ${CLAIRIERE.filtre}`)
    }
    if (i < colonnes.length) server.scheduleInTicks(1, etape)
    else event.player?.tell(Text.gray('La brume se retire.'))
  }
  etape()
})
