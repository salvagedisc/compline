// Fallow : objets de No Man's Land sans source une fois ses créatures coupées, cachés
// d'EMI. Même liste que server_scripts/faune_nml.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'nomansland:deer_spawn_egg', 'nomansland:goose_spawn_egg',
    'nomansland:billhook_bass_spawn_egg', 'nomansland:tortoise_spawn_egg',
    'nomansland:raw_venison', 'nomansland:cooked_venison',
    'nomansland:billhook_bass', 'nomansland:cooked_billhook_bass',
    'nomansland:billhook_bass_bucket', 'nomansland:billhook_bass_barrel',
    'nomansland:sturdy_scute', 'nomansland:tortoise_shell', 'nomansland:tortoise_egg',
    'nomansland:pot_debug_stick', 'nomansland:no_mans_globe'
  ].forEach(id => event.remove(id))
})
