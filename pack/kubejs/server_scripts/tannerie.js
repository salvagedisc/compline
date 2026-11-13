// Fallow : la tannerie, avec le mod Tannery (Beckstation). Recettes, retraits et écorce.
// Le butin est dans tannerie_butin.js, les sacoches cachées et les messages du tonneau
// traduits dans client_scripts/tannerie.js. Voir docs/plan/tannerie.md.
//
// La chaîne (celle du mod, inchangée) :
//   animal -> peau (vache, mouton, chèvre, cochon, cheval)
//   1. séchoir à peaux : la peau sèche à l'air -> peau séchée
//   2. écorce (bûche écorcée à la hache) x2 -> poudre tannique, versée dans le tonneau à tanner
//   3. tonneau à tanner : peau séchée -> cuir
// La chair putréfiée suit le même chemin : séchée, elle donne des tendons, de la viande
// séchée, ou du cuir au tonneau.

// Sacoches de Tannery : retirées et cachées d'EMI (client_scripts/tannerie.js, même liste).
// Les sacs à dos de Reliable Backpacks tiennent déjà ce rôle.
const TANNERIE_OBJETS = [
  'tannery:small_satchel', 'tannery:large_satchel'
]

// Recettes du pack qui donnent du cuir sans passer par la tannerie.
const TANNERIE_RECETTES_RETIREES = [
  // chair putréfiée séchée au soleil sur le plateau en bambou -> cuir
  'kaleidoscope_cookery:bamboo_tray/drying/rotten_flesh_to_leather',
  // six chairs putréfiées envoyées par pigeon -> cuir
  'envelope:mailing/automated_supply_service/leather',
  // peau de Naturalist cuite au four -> cuir en 10 secondes
  'naturalist:leather_from_smelting_hide',
  // deux peaux de kangourou -> cuir (remplacé plus bas par une peau de Tannery)
  'alexsmobs:kangaroo_hide_to_leather'
]

ServerEvents.recipes(event => {
  event.remove({ output: TANNERIE_OBJETS })
  TANNERIE_RECETTES_RETIREES.forEach(id => event.remove({ id: id }))

  // Les peaux de kangourou d'Alex's Mobs gardent leur rapport (2 pour 1),
  // mais donnent une peau à sécher au lieu du cuir.
  if (Platform.isLoaded('alexsmobs')) {
    event.shapeless('tannery:goat_hide', ['alexsmobs:kangaroo_hide', 'alexsmobs:kangaroo_hide'])
      .id('fallow:tannerie/peau_de_kangourou')
  }

  // Une peau de Naturalist d'un monde existant se sèche comme une peau de vache.
  if (Platform.isLoaded('naturalist')) {
    event.shapeless('tannery:cow_hide', ['naturalist:hide'])
      .id('fallow:tannerie/peau_de_naturalist')
  }
})

// Écorce : Tannery n'en donne qu'aux huit bûches vanilla de son tag `tannery:logs`, et sur
// tout bloc du tag, écorcé ou non. On y met chaque bûche ou bois qui a une version écorcée
// (Regions Unexplored et les autres essences), et rien de déjà écorcé : sinon un clic droit
// répété sur la même bûche donnerait de l'écorce sans fin.
ServerEvents.tags('block', event => {
  let ids = new Set()
  Block.getTypeList().forEach(id => ids.add(String(id)))
  let ajoutees = 0
  ids.forEach(id => {
    let [ns, path] = id.split(':')
    if (path.startsWith('stripped_') || !/_(log|wood|stem|hyphae)$/.test(path)) return
    if (!ids.has(`${ns}:stripped_${path}`)) return
    event.add('tannery:logs', id)
    ajoutees++
  })
  console.info(`[tannerie] ${ajoutees} bûches donnent de l'écorce`)
})

// Contrôle au chargement : liste dans le journal du serveur toutes les recettes
// qui donnent encore du cuir, pour repérer une source oubliée.
ServerEvents.afterRecipes(event => {
  event.forEachRecipe({ output: 'minecraft:leather' }, recipe => {
    console.info(`[tannerie] recette donnant du cuir : ${recipe.getOrCreateId()}`)
  })
})
