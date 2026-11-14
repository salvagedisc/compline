// Compline : santé maximale perdue à la mort, regagnée en explorant.
// Ce fichier enregistre l'objet « Éclat de vie ». Les règles sont dans
// server_scripts/sante.js, le butin dans server_scripts/sante_butin.js.
// Voir docs/plan/sante.md.

// Texture vanilla réutilisée (aucun PNG copié) et teinte rouge douce appliquée au calque 0.
const SANTE_ECLAT_TEXTURE = 'minecraft:item/quartz'
const SANTE_ECLAT_TEINTE = 0xE8868A
const SANTE_ECLAT_PILE_MAX = 16

StartupEvents.registry('item', event => {
  event.create('compline:eclat_de_vie')
    .texture(SANTE_ECLAT_TEXTURE)
    .color(0, SANTE_ECLAT_TEINTE)
    .maxStackSize(SANTE_ECLAT_PILE_MAX)
    .rarity('uncommon')
})
