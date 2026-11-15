// Compline : l'End n'existe plus et le Nether est fermé (décision 0004).
// Les forteresses de l'End ne se génèrent plus (tag vide
// data/minecraft/tags/worldgen/biome/has_structure/stronghold.json).
// Pour rouvrir le Nether : retirer 'minecraft:the_nether' de la liste et passer
// DIMENSIONS_NETHER_FERME à false.

const DIMENSIONS_NETHER_FERME = true
const DIMENSIONS_FERMEES = DIMENSIONS_NETHER_FERME
  ? ['minecraft:the_end', 'minecraft:the_nether']
  : ['minecraft:the_end']

// 1. Plus d'œil de l'Ender ni de cristal de l'End.
ServerEvents.recipes(event => {
  event.remove({ output: 'minecraft:ender_eye' })
  event.remove({ output: 'minecraft:end_crystal' })
})

// 2. Un portail du Nether ne s'allume plus (briquet, boule de feu, lave).
//    Les portails en ruine restent des ruines.
if (DIMENSIONS_NETHER_FERME) {
  NativeEvents.onEvent('net.neoforged.neoforge.event.level.BlockEvent$PortalSpawnEvent', event => {
    event.setCanceled(true)
  })
}

// 3. Filet de sécurité : aucun voyage vers une dimension fermée.
NativeEvents.onEvent('net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent', event => {
  if (DIMENSIONS_FERMEES.includes(String(event.getDimension().location()))) event.setCanceled(true)
})
