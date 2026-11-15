// Compline : une seule cuisine, celle de Farmer's Delight, qui se joue à la main avec
// Hands-On Delight (décision 0030). Farm & Charm reste pour la ferme ; son réchaud, sa
// marmite, sa rôtissoire et son bol de préparation sortent du jeu (tri.js et
// client_scripts/tri.js les retirent et les cachent d'EMI).
//
// Ses plats propres ne sont pas perdus : chaque recette de ces quatre stations est
// recopiée vers Farmer's Delight, avec les mêmes ingrédients et le même résultat.
// - réchaud, marmite et rôtissoire -> marmite de Farmer's Delight (même récipient : bol
//   ou fiole) ;
// - bol de préparation (beurre, pâte, salade, porridge, pâtées) -> établi, sans cuisson.
// La soupe à l'oignon de Farm & Charm n'est pas recopiée : Farmer's Delight a la sienne.
// Les copies passent par fusionner() (fusions.js) : le bacon, le bœuf haché, les morceaux de
// poulet et d'agneau, les pâtes, la pâte et la farine de Farm & Charm y deviennent ceux de
// Farmer's Delight et de Create.

const CUISINE_TYPES_CUITS = [
  'farm_and_charm:pot_cooking', 'farm_and_charm:stove', 'farm_and_charm:roaster'
]
const CUISINE_BOL = 'farm_and_charm:crafting_bowl'
const CUISINE_PAS_RECOPIES = ['farm_and_charm:onion_soup']

ServerEvents.recipes(event => {
  const cuits = []
  const bol = []
  const lire = recipe => JSON.parse(fusionner(String(recipe.json.toString())))
  const nom = id => id.substring(id.lastIndexOf('/') + 1)

  CUISINE_TYPES_CUITS.forEach(type => {
    event.forEachRecipe({ type: type }, recipe => {
      const json = lire(recipe)
      if (CUISINE_PAS_RECOPIES.indexOf(json.result.id) >= 0) return
      const copie = {
        type: 'farmersdelight:cooking',
        ingredients: json.ingredients,
        result: json.result,
        cookingtime: 200,
        recipe_book_tab: json.container && json.container.id === 'minecraft:glass_bottle' ? 'drinks' : 'meals'
      }
      if (json.container) copie.container = json.container
      cuits.push({ id: 'compline:cuisine/' + nom(String(recipe.getId())), json: copie })
    })
  })

  event.forEachRecipe({ type: CUISINE_BOL }, recipe => {
    const json = lire(recipe)
    bol.push({ id: 'compline:cuisine/' + nom(String(recipe.getId())), json: json })
  })

  CUISINE_TYPES_CUITS.concat([CUISINE_BOL]).forEach(type => event.remove({ type: type }))
  cuits.forEach(copie => event.custom(copie.json).id(copie.id))
  bol.forEach(copie => event.custom({
    type: 'minecraft:crafting_shapeless',
    category: 'misc',
    ingredients: copie.json.ingredients,
    result: copie.json.result
  }).id(copie.id))
})

// Tout à la main (2026-11-15). Hands-On Delight assemble sur la planche à découper les plats
// de Farmer's Delight faits à l'établi ; config/handson_delight.properties
// (remove_crafting_recipes=true) les retire de l'établi, et le tag
// handson_delight:assembled (kubejs/data) y ajoute les plats des autres mods. Ici :
// - le pain ne se fait plus avec trois blés à l'établi : on pétrit la pâte sur la planche
//   (blé et seau d'eau, ou œuf), puis on la cuit au four, au fumoir, au feu de camp, à la
//   poêle ou sur le réchaud ;
// - la soupe de betterave et le ragoût de champignons se cuisent dans la marmite (Farmer's
//   Delight a déjà ces recettes), plus à l'établi ;
// - le concombre des hamsters se tranche au couteau sur la planche, comme les poissons, le
//   crabe et le homard d'Hybrid Aquatic, que l'on découpait à l'établi ;
// - l'orge sauvage de Regions Unexplored ne cuit plus en pain sans passer par la pâte ;
// - tout aliment qui cuit au fumoir peut aussi cuire au feu de camp, donc à la poêle et sur
//   le réchaud de Farmer's Delight, qui reprennent les recettes du feu de camp.

const CUISINE_RECETTES_RETIREES = [
  'minecraft:bread', 'minecraft:beetroot_soup', 'minecraft:mushroom_stew',
  'adorablehamsterpets:sliced_cucumber', 'regions_unexplored:barley_smelting',
  'regions_unexplored:barley_smoking'
]

// Découpe d'Hybrid Aquatic : [recette retirée de l'établi, ingrédient, résultat, nombre, os].
const CUISINE_DECOUPES = [
  ['hybrid_aquatic:raw_fish_meat_small', { tag: 'hybrid_aquatic:small_fish' }, 'hybrid_aquatic:raw_fish_meat', 1, true],
  ['hybrid_aquatic:raw_fish_meat_medium', { tag: 'hybrid_aquatic:medium_fish' }, 'hybrid_aquatic:raw_fish_meat', 2, true],
  ['hybrid_aquatic:raw_fish_steak', { tag: 'hybrid_aquatic:large_fish' }, 'hybrid_aquatic:raw_fish_steak', 2, true],
  ['hybrid_aquatic:raw_crab', { tag: 'hybrid_aquatic:crab_claw' }, 'hybrid_aquatic:raw_crab', 1, false],
  ['hybrid_aquatic:raw_lobster', { item: 'hybrid_aquatic:lobster_claw' }, 'hybrid_aquatic:raw_lobster', 1, false]
]
const CUISINE_COUTEAU = [{ type: 'farmersdelight:item_ability', action: 'knife_dig' }, { tag: 'c:tools/knife' }]

ServerEvents.recipes(event => {
  CUISINE_RECETTES_RETIREES.forEach(id => event.remove({ id: id }))

  event.custom({
    type: 'minecraft:campfire_cooking',
    category: 'food',
    ingredient: { item: 'farmersdelight:wheat_dough' },
    result: { id: 'minecraft:bread', count: 1 },
    cookingtime: 600,
    experience: 0
  }).id('compline:cuisine/pain_au_feu')

  event.custom({
    type: 'farmersdelight:cutting',
    ingredients: [{ item: 'adorablehamsterpets:cucumber' }],
    tool: CUISINE_COUTEAU,
    result: [{ item: { id: 'adorablehamsterpets:sliced_cucumber', count: 3 } }]
  }).id('compline:cuisine/concombre_tranche')

  // Comme la morue et le saumon de Farmer's Delight, les poissons rendent de la poudre d'os.
  CUISINE_DECOUPES.forEach(decoupe => {
    event.remove({ id: decoupe[0] })
    const morceaux = [{ item: { id: decoupe[2], count: decoupe[3] } }]
    if (decoupe[4]) morceaux.push({ item: { id: 'minecraft:bone_meal', count: 1 } })
    event.custom({
      type: 'farmersdelight:cutting',
      ingredients: [decoupe[1]],
      tool: CUISINE_COUTEAU,
      result: morceaux
    }).id('compline:cuisine/decoupe/' + decoupe[0].substring(decoupe[0].indexOf(':') + 1))
  })

  // Fumoir -> feu de camp, quand le feu de camp ne sait pas déjà cuire cet ingrédient.
  const auFeu = {}
  event.forEachRecipe({ type: 'minecraft:campfire_cooking' }, recipe => {
    auFeu[JSON.stringify(JSON.parse(recipe.json.toString()).ingredient)] = true
  })
  const aCuire = []
  event.forEachRecipe({ type: 'minecraft:smoking' }, recipe => {
    const fume = JSON.parse(recipe.json.toString())
    const cle = JSON.stringify(fume.ingredient)
    if (auFeu[cle]) return
    auFeu[cle] = true
    aCuire.push({
      id: 'compline:cuisine/au_feu/' + String(recipe.getId()).replace(':', '/'),
      json: {
        type: 'minecraft:campfire_cooking',
        category: 'food',
        ingredient: fume.ingredient,
        result: fume.result,
        cookingtime: (fume.cookingtime || 100) * 6,
        experience: 0
      }
    })
  })
  aCuire.forEach(copie => event.custom(copie.json).id(copie.id))
  console.info('[cuisine] ' + aCuire.length + ' aliments du fumoir cuisent aussi au feu de camp')
})
