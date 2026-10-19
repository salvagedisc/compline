// Fallow : où trouver l'« Éclat de vie ». Uniquement en explorant : jamais sur une créature,
// jamais par artisanat. Voir docs/plan/sante.md (taux, sources, réglage).
//
// Chaque taux est la chance qu'UN tirage de la table donne un éclat :
// un coffre ouvert, une jarre brisée ou un bloc suspect brossé. Avec Lootr, chaque joueur
// a son propre tirage par coffre. Tables vérifiées dans les jars (NML 1.6.1, Minecraft 1.21.1).

const SANTE_BUTIN_ECLAT = 'fallow:eclat_de_vie'

// Coffres et jarres : l'éclat s'AJOUTE au butin normal.
const SANTE_BUTIN_COFFRES = {
  // No Man's Land
  'nomansland:chests/small_dungeon_core': 0.10,     // cœur des petits donjons souterrains (structure fréquente)
  'nomansland:chests/water_well': 0.20,             // fond des puits profonds
  'nomansland:chests/sniffer_monument': 0.25,       // monument du renifleur (jungle, rare)
  'nomansland:chests/alchemist_pot_treasure': 0.03, // jarres « trésor » des ruines d'alchimiste et sanctuaires à cloche (elles se regarnissent)
  // Vanilla, sous l'eau (si les océans vanilla de NML portent ces structures)
  'minecraft:chests/shipwreck_treasure': 0.15,      // coffre au trésor des épaves
  'minecraft:chests/shipwreck_supply': 0.05,        // coffre de provisions des épaves
  'minecraft:chests/underwater_ruin_big': 0.12,     // grandes ruines océaniques
  'minecraft:chests/underwater_ruin_small': 0.05,   // petites ruines océaniques
}

// Archéologie : un bloc suspect ne rend qu'UN objet, donc l'éclat REMPLACE la trouvaille.
const SANTE_BUTIN_ARCHEOLOGIE = {
  'nomansland:archaeology/alchemist_ruins': 0.03,   // gravier suspect des ruines d'alchimiste et des sanctuaires à cloche
  'nomansland:archaeology/well': 0.05,              // gravier suspect des puits profonds
  'nomansland:archaeology/sniffer_monument': 0.08,  // monument du renifleur
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
