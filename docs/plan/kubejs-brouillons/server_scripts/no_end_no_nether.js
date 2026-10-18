// BROUILLON Fallow — End supprimé (décision 0004), Nether fermé par défaut (plan/03 §3.8).
// Les strongholds ne se génèrent plus (tag data/minecraft/tags/worldgen/biome/has_structure/stronghold.json).
// À vérifier : NativeEvents dans KubeJS 2101 et les noms exacts des événements NeoForge.

const FALLOW_NETHER_CLOSED = true // passer à false si la décision sur le Nether change
const FALLOW_CLOSED_DIMENSIONS = FALLOW_NETHER_CLOSED ? ['minecraft:the_end', 'minecraft:the_nether'] : ['minecraft:the_end']

ServerEvents.recipes(event => {
  event.remove({ output: 'minecraft:ender_eye' })
  event.remove({ output: 'minecraft:end_crystal' })
})

BlockEvents.rightClicked('minecraft:end_portal_frame', event => {
  if (event.item.id == 'minecraft:ender_eye') event.cancel()
})

// Portail du Nether : il ne s'allume plus (briquet, boule de feu ou lave). Les portails en ruine restent des ruines.
if (FALLOW_NETHER_CLOSED) {
  NativeEvents.onEvent('net.neoforged.neoforge.event.level.BlockEvent$PortalSpawnEvent', event => event.setCanceled(true))
}

// Filet de sécurité : aucun voyage vers une dimension fermée.
NativeEvents.onEvent('net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent', event => {
  const target = String(event.getDimension().location())
  if (FALLOW_CLOSED_DIMENSIONS.includes(target)) event.setCanceled(true)
})
