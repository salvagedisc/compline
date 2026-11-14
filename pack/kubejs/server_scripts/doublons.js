// Compline : quand deux mods ont la même espèce, on garde le meilleur modèle et on coupe
// l'autre. Voir docs/plan/doublons.md.
// - Naturalist se règle dans config/naturalist-server.toml ;
// - Hybrid Aquatic, par ses biome modifiers d'espèce remplacés par neoforge:none
//   (kubejs/data/hybrid_aquatic/neoforge/biome_modifier/) ;
// - Alex's Mobs et Critters and Companions, par kubejs/data/compline/neoforge/biome_modifier/doublons.json.
// Ce script fait le reste. Ids et objets lus dans les jars (Alex's Mobs 2.2.2, Hybrid
// Aquatic 1.7.3, Critters and Companions 2.7.0, Naturalist 2.0.4).

// Créatures coupées qui peuvent encore entrer dans le monde par un autre chemin que les
// listes de biomes : apparitions propres au mod (grottes luxuriantes de Critters and
// Companions), biomes hors de #minecraft:is_overworld, seaux restés en jeu.
const DOUBLONS_CREATURES = [
  'alexsmobs:grizzly_bear', 'alexsmobs:tiger', 'alexsmobs:elephant', 'alexsmobs:rhinoceros',
  'alexsmobs:komodo_dragon', 'alexsmobs:crocodile', 'alexsmobs:rattlesnake', 'alexsmobs:blue_jay',
  'alexsmobs:catfish', 'alexsmobs:blobfish', 'alexsmobs:hammerhead_shark', 'alexsmobs:comb_jelly',
  'alexsmobs:orca', 'alexsmobs:lobster', 'alexsmobs:flying_fish', 'alexsmobs:devils_hole_pupfish',
  'alexsmobs:mimic_octopus',
  'hybrid_aquatic:anglerfish', 'hybrid_aquatic:blobfish', 'hybrid_aquatic:giant_isopod',
  'hybrid_aquatic:frilled_shark', 'hybrid_aquatic:piranha', 'hybrid_aquatic:stingray',
  'hybrid_aquatic:otter', 'hybrid_aquatic:giant_squid', 'hybrid_aquatic:umbrella_octopus',
  'crittersandcompanions:dragonfly', 'crittersandcompanions:snail'
]

// Objets qui n'ont plus de source une fois ces créatures coupées. Ils sont aussi cachés
// d'EMI (client_scripts/doublons.js, même liste).
const DOUBLONS_OBJETS = [
  'alexsmobs:spawn_egg_grizzly_bear', 'alexsmobs:spawn_egg_tiger', 'alexsmobs:spawn_egg_elephant',
  'alexsmobs:spawn_egg_rhinoceros', 'alexsmobs:spawn_egg_komodo_dragon', 'alexsmobs:spawn_egg_crocodile',
  'alexsmobs:spawn_egg_rattlesnake', 'alexsmobs:spawn_egg_blue_jay', 'alexsmobs:spawn_egg_catfish',
  'alexsmobs:spawn_egg_blobfish', 'alexsmobs:spawn_egg_hammerhead_shark', 'alexsmobs:spawn_egg_comb_jelly',
  'alexsmobs:spawn_egg_orca', 'alexsmobs:spawn_egg_lobster', 'alexsmobs:spawn_egg_flying_fish',
  'alexsmobs:spawn_egg_devils_hole_pupfish', 'alexsmobs:spawn_egg_mimic_octopus',
  'alexsmobs:bear_dust', 'alexsmobs:crocodile_egg', 'alexsmobs:crocodile_scute',
  'alexsmobs:crocodile_chestplate', 'alexsmobs:rattlesnake_rattle', 'alexsmobs:komodo_spit',
  'alexsmobs:raw_catfish', 'alexsmobs:cooked_catfish', 'alexsmobs:small_catfish_bucket',
  'alexsmobs:medium_catfish_bucket', 'alexsmobs:large_catfish_bucket', 'alexsmobs:blobfish',
  'alexsmobs:blobfish_bucket', 'alexsmobs:fish_oil', 'alexsmobs:comb_jelly_bucket',
  'alexsmobs:rainbow_jelly', 'alexsmobs:rainbow_glass', 'alexsmobs:lobster_tail',
  'alexsmobs:cooked_lobster_tail', 'alexsmobs:lobster_bucket', 'alexsmobs:flying_fish',
  'alexsmobs:flying_fish_bucket', 'alexsmobs:flying_fish_boots', 'alexsmobs:devils_hole_pupfish_bucket',
  'alexsmobs:mimic_octopus_bucket',
  'hybrid_aquatic:anglerfish_spawn_egg', 'hybrid_aquatic:anglerfish', 'hybrid_aquatic:blobfish_spawn_egg',
  'hybrid_aquatic:blobfish', 'hybrid_aquatic:giant_isopod_spawn_egg', 'hybrid_aquatic:frilled_shark_spawn_egg',
  'hybrid_aquatic:piranha_spawn_egg', 'hybrid_aquatic:piranha', 'hybrid_aquatic:stingray_spawn_egg',
  'hybrid_aquatic:stingray', 'hybrid_aquatic:otter_spawn_egg', 'hybrid_aquatic:giant_squid_spawn_egg',
  'hybrid_aquatic:umbrella_octopus_spawn_egg',
  'crittersandcompanions:dragonfly_spawn_egg', 'crittersandcompanions:iron_dragonfly_armor',
  'crittersandcompanions:gold_dragonfly_armor', 'crittersandcompanions:diamond_dragonfly_armor',
  'crittersandcompanions:netherite_dragonfly_armor', 'crittersandcompanions:snail_spawn_egg',
  'crittersandcompanions:snail_slime_bottle',
  'naturalist:crab_spawn_egg', 'naturalist:crab', 'naturalist:great_white_shark_spawn_egg',
  'naturalist:jellyfish_spawn_egg', 'naturalist:jellyfish_bucket', 'naturalist:starfish_spawn_egg',
  'naturalist:starfish_bucket', 'naturalist:blue_starfish', 'naturalist:orange_starfish',
  'naturalist:purple_starfish', 'naturalist:red_starfish', 'naturalist:butterfly_spawn_egg',
  'naturalist:butterfly', 'naturalist:caterpillar_spawn_egg', 'naturalist:caterpillar',
  'naturalist:chrysalis'
]

DOUBLONS_CREATURES.forEach(id => {
  EntityEvents.spawned(id, event => {
    event.cancel()
  })
})

ServerEvents.recipes(event => {
  event.remove({ output: DOUBLONS_OBJETS })
})

// Le phoque d'Alex's Mobs offre encore des seaux, dont celui du homard coupé.
LootJS.lootTables(event => {
  event.forEachTable(table => {
    DOUBLONS_OBJETS.forEach(id => table.removeItem(id))
  })
})
