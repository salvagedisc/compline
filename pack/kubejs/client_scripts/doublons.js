// Tilth : objets des créatures coupées comme doublons, cachés d'EMI. Même liste que
// server_scripts/doublons.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'alexsmobs:spawn_egg_grizzly_bear', 'alexsmobs:spawn_egg_tiger',
    'alexsmobs:spawn_egg_elephant', 'alexsmobs:spawn_egg_rhinoceros',
    'alexsmobs:spawn_egg_komodo_dragon', 'alexsmobs:spawn_egg_crocodile',
    'alexsmobs:spawn_egg_rattlesnake', 'alexsmobs:spawn_egg_blue_jay',
    'alexsmobs:spawn_egg_catfish', 'alexsmobs:spawn_egg_blobfish',
    'alexsmobs:spawn_egg_hammerhead_shark', 'alexsmobs:spawn_egg_comb_jelly',
    'alexsmobs:spawn_egg_orca', 'alexsmobs:spawn_egg_lobster', 'alexsmobs:spawn_egg_flying_fish',
    'alexsmobs:spawn_egg_devils_hole_pupfish', 'alexsmobs:spawn_egg_mimic_octopus',
    'alexsmobs:bear_dust', 'alexsmobs:crocodile_egg', 'alexsmobs:crocodile_scute',
    'alexsmobs:crocodile_chestplate', 'alexsmobs:rattlesnake_rattle', 'alexsmobs:komodo_spit',
    'alexsmobs:raw_catfish', 'alexsmobs:cooked_catfish', 'alexsmobs:small_catfish_bucket',
    'alexsmobs:medium_catfish_bucket', 'alexsmobs:large_catfish_bucket', 'alexsmobs:blobfish',
    'alexsmobs:blobfish_bucket', 'alexsmobs:fish_oil', 'alexsmobs:comb_jelly_bucket',
    'alexsmobs:rainbow_jelly', 'alexsmobs:rainbow_glass', 'alexsmobs:lobster_tail',
    'alexsmobs:cooked_lobster_tail', 'alexsmobs:lobster_bucket', 'alexsmobs:flying_fish',
    'alexsmobs:flying_fish_bucket', 'alexsmobs:flying_fish_boots',
    'alexsmobs:devils_hole_pupfish_bucket', 'alexsmobs:mimic_octopus_bucket',
    'hybrid_aquatic:anglerfish_spawn_egg', 'hybrid_aquatic:anglerfish',
    'hybrid_aquatic:blobfish_spawn_egg', 'hybrid_aquatic:blobfish',
    'hybrid_aquatic:giant_isopod_spawn_egg', 'hybrid_aquatic:frilled_shark_spawn_egg',
    'hybrid_aquatic:piranha_spawn_egg', 'hybrid_aquatic:piranha',
    'hybrid_aquatic:stingray_spawn_egg', 'hybrid_aquatic:stingray',
    'hybrid_aquatic:otter_spawn_egg', 'hybrid_aquatic:giant_squid_spawn_egg',
    'hybrid_aquatic:umbrella_octopus_spawn_egg', 'crittersandcompanions:dragonfly_spawn_egg',
    'crittersandcompanions:iron_dragonfly_armor', 'crittersandcompanions:gold_dragonfly_armor',
    'crittersandcompanions:diamond_dragonfly_armor',
    'crittersandcompanions:netherite_dragonfly_armor', 'crittersandcompanions:snail_spawn_egg',
    'crittersandcompanions:snail_slime_bottle', 'naturalist:crab_spawn_egg', 'naturalist:crab',
    'naturalist:great_white_shark_spawn_egg', 'naturalist:jellyfish_spawn_egg',
    'naturalist:jellyfish_bucket', 'naturalist:starfish_spawn_egg', 'naturalist:starfish_bucket',
    'naturalist:blue_starfish', 'naturalist:orange_starfish', 'naturalist:purple_starfish',
    'naturalist:red_starfish', 'naturalist:butterfly_spawn_egg', 'naturalist:butterfly',
    'naturalist:caterpillar_spawn_egg', 'naturalist:caterpillar', 'naturalist:chrysalis'
  ].forEach(id => event.remove(id))
})
