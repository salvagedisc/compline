// Fallow : objets que deux mods ont chacun en double, fondus en un seul (vérification du
// 2026-11-15, décision 0030). Pour chaque paire, l'objet de gauche disparaît : les recettes
// qui le donnaient ou le demandaient donnent ou demandent celui de droite, le butin aussi, et
// il est caché d'EMI (client_scripts/fusions.js, mêmes ids). Les recettes gardent leur
// station : le hachoir de Farm & Charm donne désormais le bacon de Farmer's Delight, à côté du
// couteau sur la planche. cuisine.js passe aussi par fusionner() pour les plats de Farm &
// Charm qu'il recopie. Ids lus dans les jars (Farm & Charm 1.1.25, Farmer's Delight 1.3.4,
// Naturalist 2.0.7, Overgeared 1.6.19, Hybrid Aquatic 1.7.4, Alex's Mobs 2.2.4).

const FUSIONS = {
  // Cuisine : on garde les morceaux, les pâtes et la pâte de Farmer's Delight, que la planche
  // de Hands-On Delight sait faire, et la farine de Create, que la meule et le hachoir font.
  'farm_and_charm:bacon': 'farmersdelight:bacon',
  'farm_and_charm:minced_beef': 'farmersdelight:minced_beef',
  'farm_and_charm:chicken_parts': 'farmersdelight:chicken_cuts',
  'farm_and_charm:lamb_ham': 'farmersdelight:mutton_chops',
  'farm_and_charm:raw_pasta': 'farmersdelight:raw_pasta',
  'farm_and_charm:dough': 'farmersdelight:wheat_dough',
  'create:dough': 'farmersdelight:wheat_dough',
  'farm_and_charm:flour': 'create:wheat_flour',
  'farm_and_charm:dog_food': 'farmersdelight:dog_food',
  'naturalist:cooked_egg': 'farmersdelight:fried_egg',
  // Palourde de Naturalist et palourdes d'Hybrid Aquatic ; le crabe de Naturalist est coupé
  // (doublons.js), sa chair rejoint celle du crabe d'Hybrid Aquatic.
  'naturalist:clam_meat': 'hybrid_aquatic:clam',
  'naturalist:cooked_clam_meat': 'hybrid_aquatic:cooked_clam',
  'naturalist:crab_meat': 'hybrid_aquatic:raw_crab',
  'naturalist:cooked_crab_meat': 'hybrid_aquatic:cooked_crab',
  // Autres : une seule corde (celle de Supplementaries, en lin, tri_recettes.js), une pépite
  // de cuivre, un éclat de diamant, un bâton lumineux, une dent de requin.
  'farmersdelight:rope': 'supplementaries:rope',
  'farm_and_charm:rope': 'supplementaries:rope',
  'overgeared:copper_nugget': 'create:copper_nugget',
  'overgeared:diamond_shard': 'spelunkery:diamond_shard',
  'hybrid_aquatic:glowstick': 'spelunkery:glowstick',
  'alexsmobs:shark_tooth': 'hybrid_aquatic:shark_tooth',
  // Pêche (décision 0034) : une seule canne, celle de Starcatcher, qui lance le mini-jeu et
  // porte hameçon, flotteur et appât. La canne vanilla pêchait encore au hasard dans la table
  // de butin ; la carotte et le champignon sur un bâton demandent désormais celle de Starcatcher.
  'minecraft:fishing_rod': 'starcatcher:starcatcher_rod'
}

// Recettes retirées sans être fondues : elles referaient un raccourci ou un doublon.
const FUSIONS_RECETTES_RETIREES = [
  // cordes de paille : la corde se fait en lin
  'farmersdelight:rope', 'farm_and_charm:rope',
  // pain de la pâte de Create : Farmer's Delight et cuisine.js cuisent déjà la pâte
  'create:smelting/bread', 'create:smoking/bread', 'create:campfire_cooking/bread',
  // pépites -> lingot : Create a la même
  'overgeared:copper_ingot_from_nuggets',
  // sacs de Farm & Charm retirés (tri.js)
  'farm_and_charm:dog_food_bag', 'farm_and_charm:dog_food_from_bag',
  'farm_and_charm:flour_bag', 'farm_and_charm:flour_from_bag',
  'farm_and_charm:carrot_from_bag', 'farm_and_charm:potato_from_bag',
  'farm_and_charm:beetroot_from_bag',
  // canne vanilla : trois bâtons et deux ficelles redonneraient une canne sans hameçon
  'minecraft:fishing_rod'
]

// Remplace chaque id fondu dans le texte JSON d'une recette, là où il nomme un objet
// ("item" ou "id"). Un tag qui porte le même nom (farm_and_charm:flour, farm_and_charm:dough)
// reste tel quel.
function fusionner(texte) {
  Object.keys(FUSIONS).forEach(ancien => {
    const motif = new RegExp('("(?:item|id)"\\s*:\\s*)"' + ancien.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + '"', 'g')
    texte = texte.replace(motif, '$1"' + FUSIONS[ancien] + '"')
  })
  return texte
}

// Vrai si la recette nomme un objet fondu.
function fusionAFaire(texte) {
  return fusionner(texte) !== texte
}

ServerEvents.tags('item', event => {
  // Les recettes de Farm & Charm demandent ces tags ; on y met les objets gardés.
  event.add('c:doughs', 'farmersdelight:wheat_dough')
  event.add('c:flours', 'create:wheat_flour')
  event.add('c:pasta', 'farmersdelight:raw_pasta')
  event.add('c:raw_bacon', 'farmersdelight:bacon')
  event.add('c:raw_pork', 'farmersdelight:bacon')
  event.add('c:raw_beef', 'farmersdelight:minced_beef')
  event.add('c:raw_chicken', 'farmersdelight:chicken_cuts')
  event.add('c:raw_mutton', 'farmersdelight:mutton_chops')
  event.add('c:ropes', 'supplementaries:rope')
})

ServerEvents.recipes(event => {
  FUSIONS_RECETTES_RETIREES.forEach(id => event.remove({ id: id }))

  // Les stations de cuisine de Farm & Charm sont recopiées par cuisine.js, qui fond lui-même.
  const fusionsIgnorees = CUISINE_TYPES_CUITS.concat([CUISINE_BOL])
  const anciens = Object.keys(FUSIONS)
  const aRefaire = []
  event.forEachRecipe({}, recipe => {
    const id = String(recipe.getId())
    if (FUSIONS_RECETTES_RETIREES.indexOf(id) >= 0) return
    if (fusionsIgnorees.indexOf(String(recipe.getType())) >= 0) return
    const texte = String(recipe.json.toString())
    if (!anciens.some(ancien => texte.indexOf('"' + ancien + '"') >= 0) || !fusionAFaire(texte)) return
    aRefaire.push({ id: id, json: JSON.parse(fusionner(texte)) })
  })
  aRefaire.forEach(recette => {
    event.remove({ id: recette.id })
    event.custom(recette.json).id('fallow:fusion/' + recette.id.replace(':', '/'))
  })
  console.info('[fusions] ' + aRefaire.length + ' recettes refaites avec les objets gardés')
})

LootJS.lootTables(event => {
  event.forEachTable(table => {
    Object.keys(FUSIONS).forEach(ancien => table.replaceItem(ancien, FUSIONS[ancien]))
  })
})
