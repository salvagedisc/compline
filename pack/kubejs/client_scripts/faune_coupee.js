// Fallow : objets des créatures retirées hors doublons, cachés d'EMI. Même liste que
// server_scripts/faune_coupee.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'alexsmobs:spawn_egg_murmur', 'alexsmobs:spawn_egg_farseer', 'alexsmobs:spawn_egg_skelewag',
    'alexsmobs:spawn_egg_fly', 'alexsmobs:spawn_egg_cockroach',
    'alexsmobs:unsettling_kimono', 'alexsmobs:elastic_tendon', 'alexsmobs:tendon_whip',
    'alexsmobs:farseer_arm', 'alexsmobs:transmutation_table', 'alexsmobs:skelewag_sword',
    'alexsmobs:novelty_hat',
    'friendsandfoes:iceologer_spawn_egg', 'friendsandfoes:illusioner_spawn_egg',
    'friendsandfoes:wildfire_spawn_egg', 'friendsandfoes:totem_of_freezing',
    'friendsandfoes:totem_of_illusion', 'friendsandfoes:wildfire_crown_fragment',
    'friendsandfoes:wildfire_crown',
    'naturalist:mammoth_spawn_egg', 'naturalist:mammoth_meat', 'naturalist:cooked_mammoth_meat',
    'alexsmobs:spawn_egg_banana_slug', 'alexsmobs:banana_slug_slime',
    'alexsmobs:banana_slug_slime_block', 'alexsmobs:crystalized_banana_slug_mucus',
    'alexsmobs:spawn_egg_alligator_snapping_turtle', 'alexsmobs:spiked_scute',
    'alexsmobs:spiked_turtle_shell', 'alexsmobs:spawn_egg_mungus', 'alexsmobs:mungal_spores',
    'alexsmobs:spawn_egg_triops', 'alexsmobs:triops_bucket', 'alexsmobs:triops_eggs',
    'alexsmobs:mosquito_repellent_stew', 'alexsmobs:spawn_egg_sunbird', 'alexsmobs:spawn_egg_guster',
    'alexsmobs:guster_eye', 'alexsmobs:pocket_sand', 'alexsmobs:gustmaker',
    'friendsandfoes:moobloom_spawn_egg',
    'crittersandcompanions:koi_fish', 'crittersandcompanions:koi_fish_bucket',
    'crittersandcompanions:koi_fish_spawn_egg',
    'alexsmobs:spawn_egg_rocky_roller', 'alexsmobs:rocky_shell', 'alexsmobs:rocky_chestplate',
    'alexsmobs:spawn_egg_frilled_shark', 'alexsmobs:frilled_shark_bucket',
    'alexsmobs:spawn_egg_potoo', 'friendsandfoes:glare_spawn_egg',
    'crittersandcompanions:sea_bunny_spawn_egg', 'crittersandcompanions:sea_bunny_bucket',
    'crittersandcompanions:sea_bunny_slime_bottle',
    'crittersandcompanions:sea_bunny_slime_block', 'hybrid_aquatic:sea_urchin_spawn_egg',
    'hybrid_aquatic:sea_urchin_spine', 'hybrid_aquatic:comb_jelly_spawn_egg',
    'hybrid_aquatic:garden_eel_spawn_egg', 'hybrid_aquatic:nautilus_spawn_egg',
    'hybrid_aquatic:sea_slug_spawn_egg', 'hybrid_aquatic:sea_cucumber_spawn_egg',
    'hybrid_aquatic:starfish_spawn_egg', 'hybrid_aquatic:starfish',
    'hybrid_aquatic:starfish_projectile_spawn_egg'
  ].forEach(id => event.remove(id))
})
