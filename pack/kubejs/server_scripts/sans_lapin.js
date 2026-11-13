// Tilth : le lapin est retiré du jeu. Fresh Animations n'a pas de modèle pour lui : il
// détonnait au milieu des animaux animés. L'historique est dans docs/archive/plan/faune-nml.md.
// Ses apparitions sont retirées de tous les biomes de l'Overworld par
// kubejs/data/tilth/neoforge/biome_modifier/sans_lapin.json. Ce script retire ses objets.

// Objets qui n'ont plus de source sans lapin. Aussi cachés d'EMI
// (client_scripts/sans_lapin.js, même liste).
const SANS_LAPIN_OBJETS = [
  'minecraft:rabbit_spawn_egg', 'minecraft:rabbit', 'minecraft:cooked_rabbit',
  'minecraft:rabbit_stew', 'minecraft:rabbit_foot', 'minecraft:rabbit_hide'
]

ServerEvents.recipes(event => {
  event.remove({ output: SANS_LAPIN_OBJETS })
})

// La peau et la patte tombent encore d'autres tables, dont le cadeau matinal du chat.
LootJS.lootTables(event => {
  event.forEachTable(table => {
    SANS_LAPIN_OBJETS.forEach(id => table.removeItem(id))
  })
})
