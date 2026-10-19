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
    'naturalist:mammoth_spawn_egg', 'naturalist:mammoth_meat', 'naturalist:cooked_mammoth_meat'
  ].forEach(id => event.remove(id))
})
