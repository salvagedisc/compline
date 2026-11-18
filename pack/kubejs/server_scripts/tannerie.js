// Fallow : la tannerie maison (module tannerie de fallow). Recettes ; le butin est
// dans tannerie_butin.js. Voir docs/plan/tannerie.md.
//
// La chaîne :
//   animal -> peau brute (fallow:peau_brute)
//   1. chevalet à écharner : la peau se racle au couteau -> peau raclée (parfois du gras)
//   2. cuve à tan : un seau d'eau, une écorce par peau, quatre peaux au plus, 5 minutes
//      -> cuir mouillé
//   3. séchoir à cuir : il sèche au soleil, à l'ombre, plus vite près d'un feu, pas sous
//      la pluie -> cuir
// L'écorce tombe des bûches écorcées à la hache (module tannerie) ; c'est celle de
// Farmer's Delight, qui sort aussi de sa planche à découper.

// Recettes du pack qui donnent du cuir sans passer par la tannerie.
const TANNERIE_RECETTES_RETIREES = [
  // six chairs putréfiées envoyées par pigeon -> cuir
  'envelope:mailing/automated_supply_service/leather',
  // peau de Naturalist cuite au four -> cuir en 10 secondes
  'naturalist:leather_from_smelting_hide',
  // deux peaux de kangourou -> cuir (remplacé plus bas par une peau brute)
  'alexsmobs:kangaroo_hide_to_leather',
  // vieille botte pêchée (Starcatcher) -> cuir
  'starcatcher:leather_from_boot',
  // chair putréfiée salée (Spelunkery) -> 4 cuirs
  'spelunkery:leather',
  // quatre peaux de reptile (Koopa's Critters) -> cuir (remplacé plus bas par une peau brute)
  'koopascritters:reptilehiderecipe'
]

ServerEvents.recipes(event => {
  TANNERIE_RECETTES_RETIREES.forEach(id => event.remove({ id: id }))

  // Les peaux de kangourou d'Alex's Mobs gardent leur rapport (2 pour 1),
  // mais donnent une peau brute au lieu du cuir.
  if (Platform.isLoaded('alexsmobs')) {
    event.shapeless('fallow:peau_brute', ['alexsmobs:kangaroo_hide', 'alexsmobs:kangaroo_hide'])
      .id('fallow:tannerie/peau_de_kangourou')
  }

  // Quatre peaux de reptile de Koopa's Critters font une peau brute.
  if (Platform.isLoaded('koopascritters')) {
    event.shapeless('fallow:peau_brute', ['4x koopascritters:reptile_hide'])
      .id('fallow:tannerie/peau_de_reptile')
  }

  // Une peau de Naturalist d'un monde existant devient une peau brute.
  if (Platform.isLoaded('naturalist')) {
    event.shapeless('fallow:peau_brute', ['naturalist:hide'])
      .id('fallow:tannerie/peau_de_naturalist')
    // Le gras que laisse le raclage : deux torches avec un bâton, une bougie avec une ficelle.
    event.shapeless('2x minecraft:torch', ['naturalist:fat', '#c:rods/wooden'])
      .id('fallow:tannerie/torches_au_gras')
    event.shapeless('minecraft:candle', ['naturalist:fat', '#c:strings'])
      .id('fallow:tannerie/chandelle_de_suif')
  }
})

// Contrôle au chargement : liste dans le journal du serveur toutes les recettes
// qui donnent encore du cuir, pour repérer une source oubliée.
ServerEvents.afterRecipes(event => {
  event.forEachRecipe({ output: 'minecraft:leather' }, recipe => {
    console.info(`[tannerie] recette donnant du cuir : ${recipe.getOrCreateId()}`)
  })
})
