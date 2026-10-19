// Fallow : la tannerie. Recettes, retraits et infobulles.
// Les objets sont dans startup_scripts/tannerie.js, le butin dans tannerie_butin.js.
// Voir docs/plan/tannerie.md.
//
// La chaîne :
//   animal -> peau brute
//   1. établi : peau brute + cendres (Supplementaries) -> peau préparée
//   2. plateau en bambou (Kaleidoscope Cookery), mode « humidification » :
//      la peau préparée trempe sous la pluie, ou sous un stalactite qui goutte
//      de l'eau, et devient du cuir au bout de TANNERIE_DUREE_TREMPAGE ticks.

// 6000 ticks = 5 minutes de pluie (ou de gouttes) sur le plateau.
const TANNERIE_DUREE_TREMPAGE = 6000

// Source sûre de cendres : du charbon de bois brûlé sur un feu de camp.
// Les cendres viennent aussi des blocs qui brûlent et des créatures mortes en feu.
const TANNERIE_DUREE_CENDRES = 600

// Secours, éteint par défaut : si le bambou s'avère trop rare dans le monde,
// passer à true pour qu'on puisse aussi fumer la peau préparée sur un feu de camp.
const TANNERIE_SECOURS_FUMAGE = false
const TANNERIE_DUREE_FUMAGE = 2400

// Recettes du pack qui donnent du cuir trop facilement, ou hors de la chaîne.
const TANNERIE_RECETTES_RETIREES = [
  // chair putréfiée séchée au soleil sur le plateau en bambou -> cuir
  'kaleidoscope_cookery:bamboo_tray/drying/rotten_flesh_to_leather',
  // le cuir laissé sous la pluie pourrit : retiré pour ne pas perdre le cuir
  // tanné qu'on oublie sur le plateau (il y trempe déjà dans la même eau)
  'kaleidoscope_cookery:bamboo_tray/wetting/leather_to_rotten_flesh',
  // six chairs putréfiées envoyées par pigeon -> cuir
  'envelope:mailing/automated_supply_service/leather',
  // peau de Naturalist cuite au four -> cuir en 10 secondes
  'naturalist:leather_from_smelting_hide',
  // deux peaux de kangourou -> cuir (remplacé plus bas par une peau brute)
  'alexsmobs:kangaroo_hide_to_leather'
]

ServerEvents.recipes(event => {
  TANNERIE_RECETTES_RETIREES.forEach(id => event.remove({ id: id }))

  // Étape 1 : écharner et saler.
  event.shapeless('fallow:peau_preparee', ['#fallow:peaux_brutes', '#c:dusts/ash'])
    .id('fallow:tannerie/echarner_et_saler')

  // Étape 2 : tremper sur le plateau en bambou (même format que les recettes du mod).
  event.custom({
    type: 'kaleidoscope_cookery:bamboo_tray',
    subtype: 'wetting',
    duration: TANNERIE_DUREE_TREMPAGE,
    ingredient: { item: 'fallow:peau_preparee' },
    result: { id: 'minecraft:leather', count: 1 }
  }).id('fallow:tannerie/tremper')

  // Les peaux de kangourou d'Alex's Mobs gardent leur rapport (2 pour 1),
  // mais donnent une peau brute au lieu du cuir.
  event.shapeless('fallow:peau_brute', ['alexsmobs:kangaroo_hide', 'alexsmobs:kangaroo_hide'])
    .id('fallow:tannerie/peau_de_kangourou')

  // Cendres : charbon de bois brûlé sur un feu de camp.
  event.campfireCooking('supplementaries:ash', 'minecraft:charcoal')
    .cookingTime(TANNERIE_DUREE_CENDRES)
    .id('fallow:tannerie/cendres_de_charbon')

  if (TANNERIE_SECOURS_FUMAGE) {
    event.campfireCooking('minecraft:leather', 'fallow:peau_preparee')
      .cookingTime(TANNERIE_DUREE_FUMAGE)
      .id('fallow:tannerie/fumer')
  }
})

// Contrôle au chargement : liste dans le journal du serveur toutes les recettes
// qui donnent encore du cuir, pour repérer une source oubliée.
ServerEvents.afterRecipes(event => {
  event.forEachRecipe({ output: 'minecraft:leather' }, recipe => {
    console.info(`[tannerie] recette donnant du cuir : ${recipe.getOrCreateId()}`)
  })
})

// Une ligne d'aide sur chaque peau, pour comprendre sans wiki.
ItemEvents.modifyTooltips(event => {
  event.add('fallow:peau_brute', [Text.gray(Text.translatable('tooltip.fallow.peau_brute'))])
  event.add('fallow:peau_preparee', [Text.gray(Text.translatable('tooltip.fallow.peau_preparee'))])
})
