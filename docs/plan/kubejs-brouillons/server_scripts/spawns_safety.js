// BROUILLON Fallow — filet de sécurité contre les monstres vanilla (plan/05 §5.1).
// Couvre ce que les biome modifiers et In Control! laissent passer :
// conversions, entités placées à la génération, spawn_overrides de structures.
// À vérifier : annulabilité de EntityEvents.spawned dans KubeJS 2101.7, accès à entity.tags.

EntityEvents.spawned(event => {
  const entity = event.entity
  if (!global.FALLOW.bannedVanilla.includes(String(entity.type))) return
  if (String(entity.level.dimension) !== 'minecraft:overworld') return
  if (entity.tags.contains('fallow_allowed')) return // /summon d'un admin avec Tags:["fallow_allowed"]
  event.cancel()
})
