// Compline : outils en bois cachés d'EMI. Même liste que server_scripts/premiers_gestes.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'minecraft:wooden_sword', 'minecraft:wooden_shovel', 'minecraft:wooden_pickaxe',
    'minecraft:wooden_axe', 'minecraft:wooden_hoe'
  ].forEach(id => {
    // Un identifiant disparu ne doit pas arrêter le reste de la liste.
    try { event.remove(id) } catch (e) { console.warn(`EMI : ${id} introuvable, ignoré`) }
  })
})
