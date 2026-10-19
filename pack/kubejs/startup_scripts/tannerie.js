// Fallow : la tannerie. Le cuir se gagne sur la faune, en deux temps.
// Ce fichier enregistre les deux étapes de la peau. Les recettes et les infobulles
// sont dans server_scripts/tannerie.js, le butin dans server_scripts/tannerie_butin.js.
// Voir docs/plan/tannerie.md.
//
// Textures vanilla réutilisées (aucun PNG copié), teintées au calque 0 :
// la peau brute est rosée (chair encore attachée), la peau préparée est grise (cendres).

const TANNERIE_TEXTURE = 'minecraft:item/rabbit_hide'
const TANNERIE_TEINTE_BRUTE = 0xE3A493
const TANNERIE_TEINTE_PREPAREE = 0xB9B2A6

StartupEvents.registry('item', event => {
  // Étape 0 : ce que lâche l'animal à la place du cuir.
  event.create('fallow:peau_brute')
    .texture(TANNERIE_TEXTURE)
    .color(0, TANNERIE_TEINTE_BRUTE)

  // Étape 1 : peau écharnée et frottée de cendres, prête à tremper.
  event.create('fallow:peau_preparee')
    .texture(TANNERIE_TEXTURE)
    .color(0, TANNERIE_TEINTE_PREPAREE)
})
