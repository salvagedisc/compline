// Fallow : états que le paquet musical TAM « fallow » ne peut pas voir depuis le client.
// Voir docs/plan/musique.md. Le script pose ou retire deux tags sur chaque joueur ; les
// prédicats personnalisés du paquet (trueadaptivemusicpacks/fallow/predicates/*.json) sont
// des conditions de butin que TAM fait évaluer par le serveur, et lisent ces tags.
//
// - fallow_monument : un Cœur ancien d'Ancient Remnants est proche (silence à l'approche) ;
// - fallow_a_bord   : le joueur est sur un engin Sable (debout sur le pont ou assis).
//
// Classe lue dans le jar : Sable 2.0.6 (Sable.HELPER, ActiveSableCompanion.getTrackingOrVehicleSubLevel).

const MUSIQUE_PERIODE = 40                 // ticks entre deux mises à jour (2 s)
const MUSIQUE_RAYON_MONUMENT = 96          // blocs ; la nappe du Cœur ancien porte à 256
const MUSIQUE_COEUR_ANCIEN = 'ancient_remnants:elderheart'

let MUSIQUE_SABLE = null
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
  server.getPlayerList().getPlayers().forEach(player => {
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
