// BROUILLON Fallow — ressources perdues avec les monstres vanilla et le Nether (plan/05 §5.4, plan/03 §3.8).
// À dimensionner après un passage sur les recettes des .jar (slime_ball, blaze_rod, gunpowder, string, bone…).

ServerEvents.recipes(event => {
  // Slime ball : indispensable à la Super Glue et au Sticker de Create, donc à Aeronautics.
  event.recipes.create.mixing('minecraft:slime_ball', ['minecraft:kelp', '#c:flours', Fluid.water(250)])
    .id('fallow:slime_ball_mixing')

  // Poudre à canon : charbon de bois + salpêtre (gravier lavé) + soufre.
  event.recipes.create.splashing([Item.of('fallow:salpetre').withChance(0.15)], 'minecraft:gravel')
    .id('fallow:salpetre_washing')
  event.recipes.create.mixing('minecraft:gunpowder', ['minecraft:charcoal', 'fallow:salpetre', 'fallow:salpetre'])
    .heated().id('fallow:gunpowder_mixing')

  // Ficelle : lin de Supplementaries (déjà retenu) ; la laine broyée de Create reste valable.
  // Os : ajoutés aux butins des grands animaux (loot/butin.js).

  // Blaze Burner sans blaze : un cœur de braise forgé (Nether fermé par défaut).
  event.shaped('create:blaze_burner', [' C ', 'IHI'], {
    C: 'fallow:coeur_de_braise', I: '#c:plates/iron', H: 'create:empty_blaze_burner'
  }).id('fallow:blaze_burner_coeur')
  // Le cœur de braise lui-même : forge Overgeared (charbon + bloc de magma), type de recette à confirmer.

  // Quartz : vérifier si Create en produit déjà dans l'Overworld ; sinon broyage de la diorite.
  // event.recipes.create.crushing([Item.of('minecraft:quartz').withChance(0.25)], 'minecraft:diorite')
})
