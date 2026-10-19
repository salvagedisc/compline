// Fallow : infobulle de l'« Éclat de vie » (règles dans server_scripts/sante.js).
// Ligne fixe : « Rend un cœur perdu. » Ligne dynamique : les cœurs du joueur sur 10.
// Le compteur lit le modificateur fallow:mort, synchronisé avec les attributs du joueur.

const SANTE_CLIENT_COEURS_DEPART = 10   // doit rester égal à SANTE_COEURS_DEPART (server_scripts/sante.js)
const SANTE_CLIENT_ECLAT = 'fallow:eclat_de_vie'

ItemEvents.modifyTooltips(event => {
  event.add(SANTE_CLIENT_ECLAT, [Text.gray(Text.translatable('item.fallow.eclat_de_vie.tooltip'))])
  event.modify(SANTE_CLIENT_ECLAT, text => text.dynamic('fallow_sante_compteur'))
})

ItemEvents.dynamicTooltips('fallow_sante_compteur', event => {
  const player = Client.player
  if (!player) return
  let perdus = 0
  try {
    // let et non const : le Rhino de KubeJS refuse « const » dans un bloc try.
    let attribut = player.getAttribute('minecraft:generic.max_health')
    let modificateur = attribut ? attribut.getModifier('fallow:mort') : null
    if (modificateur) perdus = Math.round(-modificateur.amount() / 2)
  } catch (e) {
    // Repli : sans accès au modificateur, on déduit les cœurs du maximum affiché.
    perdus = Math.max(0, Math.round((SANTE_CLIENT_COEURS_DEPART * 2 - player.maxHealth) / 2))
  }
  const coeurs = Math.max(0, SANTE_CLIENT_COEURS_DEPART - perdus)
  event.add([Text.red(Text.translatable('item.fallow.eclat_de_vie.compteur', coeurs, SANTE_CLIENT_COEURS_DEPART))])
})
