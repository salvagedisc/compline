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

const CUISINE_TYPES_CUITS = [
  'farm_and_charm:pot_cooking', 'farm_and_charm:stove', 'farm_and_charm:roaster'
]
const CUISINE_BOL = 'farm_and_charm:crafting_bowl'
const CUISINE_PAS_RECOPIES = ['farm_and_charm:onion_soup']

ServerEvents.recipes(event => {
  const cuits = []
  const bol = []
  const lire = recipe => JSON.parse(recipe.json.toString())
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
