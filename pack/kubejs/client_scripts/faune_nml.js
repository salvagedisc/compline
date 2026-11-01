// Fallow : objets de No Man's Land retirés du jeu, cachés d'EMI. Mêmes listes que
// server_scripts/faune_nml.js : objets sans source une fois ses créatures coupées, puis
// objets retirés (viande de cheval, cuisses de grenouille, plats).
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'nomansland:deer_spawn_egg', 'nomansland:goose_spawn_egg',
    'nomansland:billhook_bass_spawn_egg', 'nomansland:tortoise_spawn_egg',
    'nomansland:raw_venison', 'nomansland:cooked_venison',
    'nomansland:billhook_bass', 'nomansland:cooked_billhook_bass',
    'nomansland:billhook_bass_bucket', 'nomansland:billhook_bass_barrel',
    'nomansland:sturdy_scute', 'nomansland:tortoise_shell', 'nomansland:tortoise_egg',
    'nomansland:pot_debug_stick', 'nomansland:no_mans_globe',

    'nomansland:raw_horse', 'nomansland:horse_steak',
    'nomansland:frog_leg', 'nomansland:cooked_frog_leg',
    'nomansland:pancake', 'nomansland:maple_tart', 'nomansland:sweet_tart',
    'nomansland:trail_mix', 'nomansland:syruped_pear', 'nomansland:honeyed_apple',
    'nomansland:hardtack'
  ].forEach(id => event.remove(id))
})
