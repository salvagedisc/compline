// Compline : Modonomicon ne reste que parce qu'Icy's Better Horses en a besoin pour démarrer.
// Son « Manuel de l'écurie » n'a plus de recette : le guide des chevaux passera dans
// Questlog. L'objet est aussi caché d'EMI (client_scripts/sans_manuel_ecurie.js).
ServerEvents.recipes(event => {
  event.remove({ id: 'icys-better-horses:stable_handbook' })
})
