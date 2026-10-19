// Fallow : créatures retirées par décision de l'équipe, hors doublons. Voir
// docs/plan/doublons.md, « Autres créatures retirées ».
// - Alex's Mobs : Murmur, Farseer, Skelewag, mouche, cafard (direction artistique, 5.3) ;
// - Friends & Foes : iceologer, illusioner, wildfire (hostiles, décision 0003), et leurs
//   quatre structures, coupées par leurs tags de biomes vidés
//   (kubejs/data/friendsandfoes/tags/worldgen/biome/has_structure/) ;
// - Naturalist : mammouth (préhistoire écartée), coupé dans config/naturalist-server.toml.
// Leurs apparitions par biome sont retirées par
// kubejs/data/fallow/neoforge/biome_modifier/faune_coupee.json. Ce script fait le reste.

// Créatures qui entrent aussi dans le monde par d'autres chemins : raids, évènements
// propres au mod, structures, ootheca de cafard.
const FAUNE_COUPEE_CREATURES = [
  'alexsmobs:murmur', 'alexsmobs:farseer', 'alexsmobs:skelewag', 'alexsmobs:fly',
  'alexsmobs:cockroach',
  'friendsandfoes:iceologer', 'friendsandfoes:illusioner', 'friendsandfoes:wildfire'
]

// Objets qui n'ont plus de source, et ceux qui ne se fabriquent qu'à partir d'eux
// (fouet en tendon, table de transmutation, couronne). Aussi cachés d'EMI
// (client_scripts/faune_coupee.js, même liste).
const FAUNE_COUPEE_OBJETS = [
  'alexsmobs:spawn_egg_murmur', 'alexsmobs:spawn_egg_farseer', 'alexsmobs:spawn_egg_skelewag',
  'alexsmobs:spawn_egg_fly', 'alexsmobs:spawn_egg_cockroach',
  'alexsmobs:unsettling_kimono', 'alexsmobs:elastic_tendon', 'alexsmobs:tendon_whip',
  'alexsmobs:farseer_arm', 'alexsmobs:transmutation_table', 'alexsmobs:skelewag_sword',
  'alexsmobs:novelty_hat',
  'friendsandfoes:iceologer_spawn_egg', 'friendsandfoes:illusioner_spawn_egg',
  'friendsandfoes:wildfire_spawn_egg', 'friendsandfoes:totem_of_freezing',
  'friendsandfoes:totem_of_illusion', 'friendsandfoes:wildfire_crown_fragment',
  'friendsandfoes:wildfire_crown',
  'naturalist:mammoth_spawn_egg', 'naturalist:mammoth_meat', 'naturalist:cooked_mammoth_meat'
]

FAUNE_COUPEE_CREATURES.forEach(id => {
  EntityEvents.spawned(id, event => {
    event.cancel()
  })
})

ServerEvents.recipes(event => {
  event.remove({ output: FAUNE_COUPEE_OBJETS })
})

LootJS.lootTables(event => {
  event.forEachTable(table => {
    FAUNE_COUPEE_OBJETS.forEach(id => table.removeItem(id))
  })
})
