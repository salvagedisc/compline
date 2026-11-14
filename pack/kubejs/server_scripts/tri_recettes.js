// Compline : recettes corrigées par le tri du pack (docs/plan/tri-du-pack.md).
// Chacune répare une chaîne qui passait par le Nether ou l'End, fermés (décision 0004),
// ou un raccourci qui contournait la forge et Create (recettes de base vérifiées dans les
// mods : aucun ne couvre les cordes, la poulie ni les couteaux, on le fait ici).

// Le hachoir de Farm & Charm : ses recettes METAL, STONE et WOOD tripleraient les minerais
// et donneraient pierre, bois et cuir sans outil. On garde la viande (MEAT), la farine, les
// pâtes et les tranches de melon, qui ne court-circuitent rien.
const TRI_HACHOIR_GARDE = [
  'farm_and_charm:mincer/flour', 'farm_and_charm:mincer/raw_pasta',
  'farm_and_charm:mincer/melon_slice'
]

ServerEvents.recipes(event => {
  const hachoir = []
  event.forEachRecipe({ type: 'farm_and_charm:mincer' }, recipe => {
    const id = String(recipe.getId())
    const json = JSON.parse(recipe.json.toString())
    if (json.recipe_type !== 'MEAT' && TRI_HACHOIR_GARDE.indexOf(id) < 0) hachoir.push(id)
  })
  hachoir.forEach(id => event.remove({ id: id }))

  // Lévitite : la poudre venait de la pierre de l'End. Elle vient désormais de la calcite
  // (géodes d'améthyste, pics rocheux), broyée par Create, avec la moitié rendue.
  event.remove({ id: 'aeronautics:crushing/end_stone_powder' })
  event.custom({
    type: 'create:crushing',
    ingredients: [{ item: 'minecraft:calcite' }],
    processing_time: 250,
    results: [
      { id: 'aeronautics:end_stone_powder' },
      { id: 'minecraft:calcite', chance: 0.5 }
    ]
  }).id('compline:tri/poudre_de_levitite')

  // Foyer de Cold Sweat : briques ordinaires et deux fours au lieu des briques du Nether
  // et du sable des âmes.
  event.remove({ id: 'cold_sweat:hearth' })
  event.shaped('cold_sweat:hearth', [' # ', 'BBB', 'SIS'], {
    '#': 'cold_sweat:smokestack',
    B: 'minecraft:brick',
    S: 'minecraft:furnace',
    I: '#c:ingots/iron'
  }).id('compline:tri/foyer')

  // Lentille télescopique excellente d'Exposure: Space : un mécanisme de précision de Create
  // à la place du débris de netherite.
  event.replaceInput({ id: 'exposure_space:excellent_telescopic_lens' },
    'minecraft:netherite_scrap', 'create:precision_mechanism')

  // Escalade et levage. La corde coûte plus cher (3 lin pour 2 cordes au lieu de 3), la
  // flèche à corde n'en emporte que 8 (config de Supplementaries), et la poulie demande un
  // engrenage de Create : on ne monte pas de charges avant d'avoir ouvert l'atelier.
  event.remove({ id: 'supplementaries:rope' })
  event.shaped('2x supplementaries:rope', ['#', '#', '#'], { '#': '#c:crops/flax' })
    .id('compline:tri/corde')
  event.remove({ id: 'supplementaries:pulley' })
  event.shaped('supplementaries:pulley_block', ['PSP', 'PCP', 'PSP'], {
    P: '#minecraft:planks',
    S: '#minecraft:wooden_slabs',
    C: 'create:cogwheel'
  }).id('compline:tri/poulie')

  // Couteaux de cuisine de Kaleidoscope : ils se faisaient à l'établi avec des lingots, à
  // côté de la forge d'Overgeared. Le fer passe par une plaque forgée, l'or par une lame
  // forgée, le diamant par la table de forgeron comme les autres outils en diamant.
  event.remove({ id: 'kaleidoscope_cookery:iron_kitchen_knife' })
  event.remove({ id: 'kaleidoscope_cookery:gold_kitchen_knife' })
  event.remove({ id: 'kaleidoscope_cookery:diamond_kitchen_knife' })
  event.shaped('kaleidoscope_cookery:iron_kitchen_knife', ['#', 'S'], {
    '#': 'overgeared:iron_plate',
    S: 'minecraft:stick'
  }).id('compline:tri/couteau_fer')
  event.shaped('kaleidoscope_cookery:gold_kitchen_knife', ['#', 'S'], {
    '#': 'overgeared:golden_sword_blade',
    S: 'minecraft:stick'
  }).id('compline:tri/couteau_or')
  event.smithing('kaleidoscope_cookery:diamond_kitchen_knife',
    'overgeared:diamond_upgrade_smithing_template',
    'kaleidoscope_cookery:iron_kitchen_knife', 'minecraft:diamond').id('compline:tri/couteau_diamant')
})
