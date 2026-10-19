// Fallow : objets du lapin cachés d'EMI. Même liste que server_scripts/sans_lapin.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'minecraft:rabbit_spawn_egg', 'minecraft:rabbit', 'minecraft:cooked_rabbit',
    'minecraft:rabbit_stew', 'minecraft:rabbit_foot', 'minecraft:rabbit_hide'
  ].forEach(id => event.remove(id))
})
