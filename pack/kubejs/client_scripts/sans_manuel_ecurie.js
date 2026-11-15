// Compline : le livre de Modonomicon (le « Manuel de l'écurie » d'Icy's Better Horses) caché
// d'EMI. Voir server_scripts/sans_manuel_ecurie.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  event.remove('modonomicon:modonomicon')
})
