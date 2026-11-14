// Compline : plus d'expérience. Voir docs/plan/xp.md.
// Ce script coupe toutes les sources d'XP. Le module xp du mod compline fait le reste :
// l'enclume ne demande plus de niveaux et la barre d'expérience est cachée.
// Pour revenir en arrière : supprimer ce fichier et passer xp.enabled = false
// dans config/compline-common.toml.

// 1. Plus d'orbes d'expérience. Couvre le minage, la fonte, les créatures, l'élevage,
//    la pêche, le commerce, la meule, les fioles, les pépites de Create et les mods.
EntityEvents.spawned('minecraft:experience_orb', event => {
  event.cancel()
})

// 2. Plus de gain direct de points (succès, Overgeared, chambre noire d'Exposure,
//    /xp add … points). Seuls les gains sont bloqués : une dépense (montant négatif) passe.
MoreJS.playerXpChange(event => {
  if (event.amount > 0) event.cancel()
})

// 3. Un joueur qui a encore de l'XP (monde de test d'avant) revient à zéro.
PlayerEvents.loggedIn(event => {
  event.player.setXp(0)
})

// 4. Les pépites d'expérience de Create ne sortent plus du broyage, et le bloc
//    d'expérience n'a plus de recette. Chaque recette de broyage qui donne une pépite
//    est recopiée sans elle, sous le même identifiant (sorties, chances et durée gardées).
const SANS_XP_PEPITE = 'create:experience_nugget'

ServerEvents.recipes(event => {
  const copies = []
  event.forEachRecipe({ type: 'create:crushing' }, recipe => {
    const json = JSON.parse(recipe.json.toString())
    const results = json.results || []
    const kept = results.filter(result => (result.id || result.item) !== SANS_XP_PEPITE)
    if (kept.length === results.length) return
    delete json['neoforge:conditions'] // la recette est chargée : ses conditions sont remplies
    json.results = kept
    copies.push({ id: String(recipe.getId()), json: json })
  })

  copies.forEach(copy => event.remove({ id: copy.id }))
  copies.forEach(copy => {
    if (copy.json.results.length > 0) {
      event.custom(copy.json).id(copy.id)
    }
  })
  console.info(`[sans_xp] ${copies.length} recettes de broyage recopiées sans pépite d'expérience`)

  event.remove({ id: 'create:crafting/materials/experience_block' })
  event.remove({ id: 'create:crafting/materials/experience_nugget_from_block' })
})

// 5. Plus de fioles d'expérience dans les coffres, les caisses et les butins :
//    lancées, elles ne donneraient plus rien. On modifie les tables elles-mêmes.
LootJS.lootTables(event => {
  event.forEachTable(table => {
    table.removeItem('minecraft:experience_bottle')
  })
})
