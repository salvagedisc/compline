// Tilth : où trouver l'« Éclat de vie ». Uniquement en explorant : jamais sur une créature,
// jamais par artisanat. Voir docs/plan/sante.md (taux, sources, réglage).
//
// Chaque taux est la chance qu'UN tirage de la table donne un éclat :
// un coffre ouvert ou un bloc suspect brossé. Avec Lootr, chaque joueur
// a son propre tirage par coffre. Tables du jeu de base (Minecraft 1.21.1).

const SANTE_BUTIN_ECLAT = 'tilth:eclat_de_vie'

// Coffres : l'éclat s'AJOUTE au butin normal.
const SANTE_BUTIN_COFFRES = {
  // Sous terre et dans les ruines de la surface
  'minecraft:chests/simple_dungeon': 0.10,          // donjon souterrain (un ou deux coffres)
  'minecraft:chests/jungle_temple': 0.25,           // temple de la jungle (rare)
  'minecraft:chests/desert_pyramid': 0.05,          // pyramide du désert (quatre coffres)
  // Sous l'eau
  'minecraft:chests/shipwreck_treasure': 0.15,      // coffre au trésor des épaves
  'minecraft:chests/shipwreck_supply': 0.05,        // coffre de provisions des épaves
  'minecraft:chests/underwater_ruin_big': 0.12,     // grandes ruines océaniques
  'minecraft:chests/underwater_ruin_small': 0.05,   // petites ruines océaniques
}

// Archéologie : un bloc suspect ne rend qu'UN objet, donc l'éclat REMPLACE la trouvaille.
const SANTE_BUTIN_ARCHEOLOGIE = {
  'minecraft:archaeology/trail_ruins_rare': 0.03,   // gravier suspect des ruines de sentier
  'minecraft:archaeology/desert_well': 0.05,        // sable suspect des puits du désert
  'minecraft:archaeology/ocean_ruin_cold': 0.04,    // sable et gravier suspects des ruines océaniques
  'minecraft:archaeology/ocean_ruin_warm': 0.04,
}

LootJS.modifiers(event => {
  Object.keys(SANTE_BUTIN_COFFRES).forEach(table => {
    event.addTableModifier(table)
      .randomChance(SANTE_BUTIN_COFFRES[table])
      .addLoot(SANTE_BUTIN_ECLAT)
  })

  Object.keys(SANTE_BUTIN_ARCHEOLOGIE).forEach(table => {
    event.addTableModifier(table)
      .randomChance(SANTE_BUTIN_ARCHEOLOGIE[table])
      .removeLoot('*')
      .addLoot(SANTE_BUTIN_ECLAT)
  })
})
