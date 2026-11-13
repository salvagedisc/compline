// Fallow : le rôle des monuments d'Ancient Remnants dans la survie. Voir docs/plan/monuments.md.
//
// 1. Cœurs endormis et gardiens. Quatre monuments sont gardés : leur Cœur ancien ne donne pas sa
//    bénédiction tant que leur gardien n'est pas vaincu. Le gardien apparaît au sol quand un joueur
//    approche ; à sa mort le Cœur s'éveille et bénit comme d'habitude (un joueur dans un rayon de 16).
//    Le Cerisier (vie) et la Sentinelle (moines) ne sont pas gardés.
// 2. Pages du codex. Les fragments de recherche d'Ancient Remnants (une page du codex chacun, qui dit
//    dans quels biomes chercher un monument) se trouvent aussi dans les ruines et les épaves.
//
// Lu dans le jar Ancient Remnants 1.5.2 : le Cœur garde sa bénédiction dans la clé NBT « Blessing » ;
// sans elle il ne bénit personne. Il la tire de « PossibleBlessings » à sa création, puis ne la change plus.

const MON_COEUR = 'ancient_remnants:elderheart'

// Bénédiction du monument → gardien (rapport 6 de la vérification, décision 15).
const MON_GARDIENS = {
  'ancient_remnants:warrior_wrath': 'mowziesmobs:ferrous_wroughtnaut',  // Monolithe du guerrier
  'ancient_remnants:architect_reach': 'mowziesmobs:frostmaw',           // Monument des conifères
  'ancient_remnants:hunter_instincts': 'mowziesmobs:umvuthi',           // Monolithe du chasseur
  'ancient_remnants:bountiful_harvest': 'companions:sacred_pontiff',    // Monument de la forêt
}

const MON_PERIODE = 40               // ticks entre deux recherches autour d'un joueur (2 s)
const MON_RAYON_EVEIL = 64           // blocs (à l'horizontale) : un joueur plus près réveille le gardien
const MON_HAUTEUR = 160              // blocs vers le haut et vers le bas : le Cœur d'un monolithe flotte haut
const MON_RAYON_GARDIEN = 128        // un gardien plus loin que ça de son Cœur ne compte plus
const MON_DELAI_GARDIEN = 1200       // ticks avant qu'un gardien disparu puisse revenir (1 min)
const MON_DISTANCE_APPARITION = 12   // blocs entre le joueur et le gardien qui apparaît, vers le monument

// Identifiants enregistrés dans les mondes : ne pas les changer sur un monde en cours.
const MON_TAG_GARDIEN = 'fallow_gardien'
const MON_CLE_BENEDICTION = 'fallow_benediction'
const MON_CLE_EVEILLE = 'fallow_eveille'
const MON_CLE_PROCHAIN = 'fallow_gardien_prochain'
const MON_CLE_COEUR = 'fallow_coeur'
const MON_CLE_EN_ATTENTE = 'fallow_coeurs_a_eveiller'   // données du serveur : Cœurs à éveiller hors chargement

const MON_HEIGHTMAP = Java.loadClass('net.minecraft.world.level.levelgen.Heightmap$Types')

function monGarde(coeur) {
  const pd = coeur.persistentData
  return !pd.getBoolean(MON_CLE_EVEILLE) && !!MON_GARDIENS[pd.getString(MON_CLE_BENEDICTION)]
}

// La bénédiction d'un Cœur d'après son monument (« PossibleBlessings », un tag par monument).
const MON_BENEDICTION_DU_MONUMENT = {
  'ancient_remnants:warrior_monolith': 'ancient_remnants:warrior_wrath',
  'ancient_remnants:coniferous_monument': 'ancient_remnants:architect_reach',
  'ancient_remnants:hunter_monolith': 'ancient_remnants:hunter_instincts',
  'ancient_remnants:oakwood_monument': 'ancient_remnants:bountiful_harvest',
}

// Rend le Cœur muet (sans « Blessing ») en gardant sa bénédiction de côté.
// Attention : écrire coeur.nbt recharge aussi ses données persistantes, d'où l'ordre ci-dessous.
function monEndormir(coeur) {
  if (coeur.persistentData.getBoolean(MON_CLE_EVEILLE)) return
  const nbt = coeur.nbt
  let benediction = coeur.persistentData.getString(MON_CLE_BENEDICTION)
  if (nbt.contains('Blessing')) benediction = nbt.getString('Blessing')
  if (!benediction) benediction = MON_BENEDICTION_DU_MONUMENT[nbt.getString('PossibleBlessings')] || ''
  if (!MON_GARDIENS[benediction]) return
  if (nbt.contains('Blessing')) {
    nbt.remove('Blessing')
    coeur.nbt = nbt
  }
  coeur.persistentData.putString(MON_CLE_BENEDICTION, benediction)
}

function monEveiller(coeur) {
  const benediction = coeur.persistentData.getString(MON_CLE_BENEDICTION)
  const nbt = coeur.nbt
  nbt.putString('Blessing', benediction)
  coeur.nbt = nbt
  coeur.persistentData.putBoolean(MON_CLE_EVEILLE, true)
  coeur.level.getPlayers().forEach(joueur => {
    const dx = joueur.x - coeur.x, dz = joueur.z - coeur.z
    if (dx * dx + dz * dz > MON_RAYON_EVEIL * MON_RAYON_EVEIL) return
    joueur.tell(Text.lightPurple(Text.translatable('fallow.monument.eveil')))
  })
  coeur.playSound('minecraft:block.beacon.activate', 2.0, 0.8)
}

function monEnAttente(server) {
  const s = server.persistentData.getString(MON_CLE_EN_ATTENTE)
  return s ? s.split(';') : []
}

// Le Cœur arrive dans le monde (création du monument ou chargement du chunk).
EntityEvents.spawned(MON_COEUR, event => {
  const coeur = event.entity
  const attente = monEnAttente(event.server)
  const i = attente.indexOf(coeur.stringUUID)
  if (i >= 0 && coeur.persistentData.getString(MON_CLE_BENEDICTION)) {
    attente.splice(i, 1)
    event.server.persistentData.putString(MON_CLE_EN_ATTENTE, attente.join(';'))
    monEveiller(coeur)
    return
  }
  monEndormir(coeur)
})

function monGardienPresent(coeur) {
  const r = MON_RAYON_GARDIEN
  const zone = AABB.of(coeur.x - r, coeur.y - MON_HAUTEUR, coeur.z - r, coeur.x + r, coeur.y + MON_HAUTEUR, coeur.z + r)
  let present = false
  coeur.level.getEntitiesWithin(zone).forEach(e => {
    if (present || !e.alive) return
    if (e.tags.contains(MON_TAG_GARDIEN) && e.persistentData.getString(MON_CLE_COEUR) == coeur.stringUUID) present = true
  })
  return present
}

function monAppeler(coeur, joueur) {
  const level = coeur.level
  const type = MON_GARDIENS[coeur.persistentData.getString(MON_CLE_BENEDICTION)]
  // Au sol, entre le joueur et le monument.
  let dx = coeur.x - joueur.x, dz = coeur.z - joueur.z
  const d = Math.sqrt(dx * dx + dz * dz)
  if (d < 1) { dx = 1; dz = 0 } else { dx /= d; dz /= d }
  const x = Math.floor(joueur.x + dx * MON_DISTANCE_APPARITION)
  const z = Math.floor(joueur.z + dz * MON_DISTANCE_APPARITION)
  const y = level.getHeight(MON_HEIGHTMAP.MOTION_BLOCKING_NO_LEAVES, x, z)
  const gardien = level.createEntity(type)
  if (!gardien) {
    console.warn(`[Fallow] monuments.js : gardien ${type} introuvable`)
    return
  }
  gardien.setPos(x + 0.5, y, z + 0.5)
  gardien.addTag(MON_TAG_GARDIEN)
  gardien.persistentData.putString(MON_CLE_COEUR, coeur.stringUUID)
  gardien.mergeNbt({ PersistenceRequired: true })
  gardien.spawn()
  level.getPlayers().forEach(p => {
    const ex = p.x - x, ez = p.z - z
    if (ex * ex + ez * ez <= MON_RAYON_EVEIL * MON_RAYON_EVEIL) p.tell(Text.gold(Text.translatable('fallow.monument.gardien')))
  })
}

PlayerEvents.tick(event => {
  const joueur = event.player
  if (joueur.tickCount % MON_PERIODE != 0 || joueur.isSpectator() || joueur.isCreative()) return
  const r = MON_RAYON_EVEIL
  const zone = AABB.of(joueur.x - r, joueur.y - MON_HAUTEUR, joueur.z - r, joueur.x + r, joueur.y + MON_HAUTEUR, joueur.z + r)
  joueur.level.getEntitiesWithin(zone).forEach(coeur => {
    if (coeur.type != MON_COEUR) return
    monEndormir(coeur) // au cas où le Cœur serait arrivé sans passer par l'événement
    if (!monGarde(coeur)) return
    const dx = coeur.x - joueur.x, dz = coeur.z - joueur.z
    if (dx * dx + dz * dz > r * r) return
    const maintenant = joueur.level.getGameTime()
    const pd = coeur.persistentData
    if (maintenant < pd.getLong(MON_CLE_PROCHAIN)) return
    pd.putLong(MON_CLE_PROCHAIN, maintenant + MON_DELAI_GARDIEN)
    if (monGardienPresent(coeur)) return
    monAppeler(coeur, joueur)
  })
})

EntityEvents.death(event => {
  const gardien = event.entity
  if (!gardien.tags.contains(MON_TAG_GARDIEN)) return
  const id = gardien.persistentData.getString(MON_CLE_COEUR)
  if (!id) return
  // Rhino prend level.getEntity(UUID) pour getEntity(int) : on cherche le Cœur autour du gardien.
  const r = MON_RAYON_GARDIEN
  let coeur = null
  gardien.level.getEntitiesWithin(AABB.of(gardien.x - r, gardien.y - MON_HAUTEUR, gardien.z - r,
    gardien.x + r, gardien.y + MON_HAUTEUR, gardien.z + r)).forEach(e => {
    if (!coeur && e.type == MON_COEUR && e.stringUUID == id) coeur = e
  })
  if (coeur && coeur.type == MON_COEUR) {
    if (monGarde(coeur)) monEveiller(coeur)
    return
  }
  // Le Cœur n'est pas chargé : il s'éveillera à son retour.
  const attente = monEnAttente(event.server)
  if (attente.indexOf(id) < 0) attente.push(id)
  event.server.persistentData.putString(MON_CLE_EN_ATTENTE, attente.join(';'))
})

// ---- Pages du codex dans les ruines (fragments de recherche) ----
// La table d'Ancient Remnants tire une des six pages au hasard. Taux = chance par coffre ouvert
// (chaque joueur a son tirage avec Lootr) ou par bloc suspect brossé (la page remplace la trouvaille).
const MON_PAGE = 'ancient_remnants:random_research_scrap'
const MON_PAGES_COFFRES = {
  'minecraft:chests/simple_dungeon': 0.25,
  'minecraft:chests/abandoned_mineshaft': 0.10,
  'minecraft:chests/ruined_portal': 0.20,
  'minecraft:chests/igloo_chest': 0.50,
  'minecraft:chests/shipwreck_map': 0.40,
  'minecraft:chests/underwater_ruin_big': 0.30,
  'minecraft:chests/underwater_ruin_small': 0.15,
  'minecraft:chests/jungle_temple': 0.50,
  'minecraft:chests/desert_pyramid': 0.25,
}
// Seulement des tables que sante_butin.js ne touche pas : les deux remplacent la trouvaille.
const MON_PAGES_ARCHEOLOGIE = {
  'minecraft:archaeology/trail_ruins_common': 0.05,
}

LootJS.modifiers(event => {
  Object.keys(MON_PAGES_COFFRES).forEach(table => {
    event.addTableModifier(table)
      .randomChance(MON_PAGES_COFFRES[table])
      .addLoot(LootEntry.reference(MON_PAGE))
  })
  Object.keys(MON_PAGES_ARCHEOLOGIE).forEach(table => {
    event.addTableModifier(table)
      .randomChance(MON_PAGES_ARCHEOLOGIE[table])
      .removeLoot('*')
      .addLoot(LootEntry.reference(MON_PAGE))
  })
})
