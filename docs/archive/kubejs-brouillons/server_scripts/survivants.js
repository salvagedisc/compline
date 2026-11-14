// BROUILLON Compline — règles des survivants (décision 0004, plan/03 §3.6).
// Le Mending est retiré des échanges par les tags data/minecraft/tags/enchantment/*.json.
// Dépend de MoreJS (version 1.21.1 à confirmer) ; syntaxe à vérifier avec ProbeJS.

// 1. Pas de reproduction des villageois : chaque survivant est précieux.
NativeEvents.onEvent('net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent', event => {
  const parent = event.getParentA()
  if (parent && parent.getType() == 'minecraft:villager') event.setCanceled(true) // comparaison de type à vérifier
})

// 2. Échanges : Overgeared reste le seul fabricant d'outils, pas de cartes de structures.
MoreJSEvents.villagerTrades(event => {
  // Les forgerons ne vendent plus d'outils ni d'armures : on retire tous leurs échanges vanilla…
  for (const profession of ['minecraft:toolsmith', 'minecraft:armorer', 'minecraft:weaponsmith']) {
    for (let level = 1; level <= 5; level++) event.removeVanillaTrades(profession, level)
  }
  // …et ils deviennent des débouchés pour la production de la colonie (exemples, prix à équilibrer).
  event.addTrade('minecraft:toolsmith', 1, ['minecraft:coal', 'minecraft:coal'], 'minecraft:emerald')
  event.addTrade('minecraft:armorer', 1, ['minecraft:leather', 'minecraft:leather', 'minecraft:leather'], 'minecraft:emerald')

  // Le cartographe ne vend plus de cartes menant aux structures.
  for (let level = 2; level <= 3; level++) event.removeVanillaTrades('minecraft:cartographer', level)
})
