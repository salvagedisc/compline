// Compline : objets fondus dans celui d'un autre mod, cachés d'EMI. Mêmes ids que les clés de
// FUSIONS dans server_scripts/fusions.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'farm_and_charm:bacon', 'farm_and_charm:minced_beef', 'farm_and_charm:chicken_parts',
    'farm_and_charm:lamb_ham', 'farm_and_charm:raw_pasta', 'farm_and_charm:dough', 'create:dough',
    'farm_and_charm:flour', 'farm_and_charm:dog_food', 'naturalist:cooked_egg',
    'naturalist:clam_meat', 'naturalist:cooked_clam_meat', 'naturalist:crab_meat',
    'naturalist:cooked_crab_meat', 'farmersdelight:tree_bark', 'naturalist:fat',
    'farmersdelight:rope', 'farm_and_charm:rope', 'overgeared:copper_nugget',
    'overgeared:diamond_shard', 'hybrid_aquatic:glowstick', 'alexsmobs:shark_tooth',
    'minecraft:fishing_rod'
  ].forEach(id => event.remove(id))
})
