// Fallow : états que le paquet musical TAM « fallow » ne peut pas voir depuis le client.
// Voir docs/plan/musique.md. Le script pose ou retire trois tags sur chaque joueur ; les
// prédicats personnalisés du paquet (trueadaptivemusicpacks/fallow/predicates/*.json) sont
// des conditions de butin que TAM fait évaluer par le serveur, et lisent ces tags.
//
// - fallow_monument : un Cœur ancien d'Ancient Remnants est proche (un morceau à l'approche) ;
// - fallow_lune     : une lune d'Enhanced Celestials est levée (lune bleue, des moissons…) ;
// - fallow_a_bord   : le joueur est sur un engin Sable (debout sur le pont ou assis).
//
// Classes lues dans les jars : Enhanced Celestials 2 Core 2.0.3.3 (mêmes appels que lune.js),
// Sable 2.0.6 (Sable.HELPER, ActiveSableCompanion.getTrackingOrVehicleSubLevel).

const MUSIQUE_PERIODE = 40                 // ticks entre deux mises à jour (2 s)
const MUSIQUE_RAYON_MONUMENT = 96          // blocs ; la nappe du Cœur ancien porte à 256
const MUSIQUE_COEUR_ANCIEN = 'ancient_remnants:elderheart'

let MUSIQUE_EC = null
let MUSIQUE_SABLE = null
try {
  MUSIQUE_EC = Java.loadClass('dev.corgitaco.enhancedcelestials2core.EnhancedCelestials')
} catch (e) {
  console.warn(`[Fallow] musique.js : Enhanced Celestials introuvable, pas de musique de lune (${e})`)
}
try {
  MUSIQUE_SABLE = Java.loadClass('dev.ryanhcode.sable.Sable')
} catch (e) {
  console.warn(`[Fallow] musique.js : Sable introuvable, pas de musique de voyage (${e})`)
}

let musiqueTicks = 0
let musiqueEnPanne = false

function musiqueTag(player, tag, actif) {
  if (actif) player.addTag(tag)
  else player.removeTag(tag)
}

// Vrai si l'évènement lunaire de cette nuit n'est pas la lune ordinaire.
function musiqueLuneLevee(server) {
  if (!MUSIQUE_EC) return false
  let previsionOpt = MUSIQUE_EC.lunarForecastWorldData(server.overworld())
  if (!previsionOpt.isPresent()) return false
  let prevision = previsionOpt.get()
  let actuelleOpt = prevision.currentLunarEventHolder().unwrapKey()
  if (!actuelleOpt.isPresent()) return false
  let ordinaire = prevision.getDimensionSettings().defaultEvent()
  return String(ordinaire.location()) !== String(actuelleOpt.get().location())
}

function musiqueABord(player) {
  if (!MUSIQUE_SABLE) return false
  return MUSIQUE_SABLE.HELPER.getTrackingOrVehicleSubLevel(player) != null
}

function musiquePresDUnMonument(player) {
  const r = MUSIQUE_RAYON_MONUMENT
  const zone = AABB.of(player.x - r, player.y - r, player.z - r, player.x + r, player.y + r, player.z + r)
  return player.level.getEntitiesWithin(zone).some(entite => entite.type == MUSIQUE_COEUR_ANCIEN)
}

function musiqueMettreAJour(server) {
  const lune = musiqueLuneLevee(server)
  server.getPlayerList().getPlayers().forEach(player => {
    const overworld = String(player.level.dimension) === 'minecraft:overworld'
    musiqueTag(player, 'fallow_lune', lune && overworld)
    musiqueTag(player, 'fallow_a_bord', musiqueABord(player))
    musiqueTag(player, 'fallow_monument', musiquePresDUnMonument(player))
  })
}

ServerEvents.tick(event => {
  if (musiqueEnPanne) return
  if (++musiqueTicks < MUSIQUE_PERIODE) return
  musiqueTicks = 0
  try {
    musiqueMettreAJour(event.server)
  } catch (e) {
    musiqueEnPanne = true
    console.error(`[Fallow] musique.js : états musicaux coupés après une erreur (${e})`)
  }
})
