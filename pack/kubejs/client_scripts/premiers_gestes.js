// Tilth : outils en bois cachés d'EMI. Même liste que server_scripts/premiers_gestes.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'minecraft:wooden_sword', 'minecraft:wooden_shovel', 'minecraft:wooden_pickaxe',
    'minecraft:wooden_axe', 'minecraft:wooden_hoe'
  ].forEach(id => event.remove(id))
})
