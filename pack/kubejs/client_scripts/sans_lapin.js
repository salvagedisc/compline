// Fallow : objets du lapin cachés d'EMI. Même liste que server_scripts/sans_lapin.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'minecraft:rabbit_spawn_egg', 'minecraft:rabbit', 'minecraft:cooked_rabbit',
    'minecraft:rabbit_stew', 'minecraft:rabbit_foot', 'minecraft:rabbit_hide'
  ].forEach(id => {
    // Un identifiant disparu ne doit pas arrêter le reste de la liste.
    try { event.remove(id) } catch (e) { console.warn(`EMI : ${id} introuvable, ignoré`) }
  })
})
