// BROUILLON Compline — masquer dans JEI ce qui n'existe pas dans le pack (KubeJS 2101 : RecipeViewerEvents).

RecipeViewerEvents.removeEntries('item', event => {
  ['minecraft:elytra', 'minecraft:ender_eye', 'minecraft:end_crystal', 'minecraft:dragon_egg',
   'minecraft:shulker_box', 'minecraft:end_rod', 'minecraft:chorus_fruit'].forEach(id => event.remove(id))
  event.remove(/^minecraft:.*_spawn_egg$/)
  // Cuisine de Farm & Charm masquée au profit de Kaleidoscope Cookery (décision 0002) : liste à dresser
  // event.remove(['farm_and_charm:stove', 'farm_and_charm:cooking_pot', 'farm_and_charm:roaster', 'farm_and_charm:pan'])
})
