// BROUILLON Tilth — pont Ecliptic Seasons → Cold Sweat (décision 0002, plan/04 §4.6).
// Ecliptic expose EclipticSeasonsApi à KubeJS ; Cold Sweat expose des bindings coldsweat.* (createModifier,
// addModifier…). Les signatures exactes sont à lire avec ProbeJS ; les valeurs sont à étalonner en jeu
// (unités internes de Cold Sweat : 1.0 ≈ l'écart entre une zone tempérée et un désert).

// Décalage de température ressentie par sous-saison (6 par saison dans Ecliptic, à confirmer).
const TILTH_SEASON_OFFSET = {
  EARLY_SPRING: -0.10, MID_SPRING: 0.00, LATE_SPRING: 0.05,
  EARLY_SUMMER: 0.15, MID_SUMMER: 0.30, LATE_SUMMER: 0.20,
  EARLY_AUTUMN: 0.05, MID_AUTUMN: -0.10, LATE_AUTUMN: -0.20,
  EARLY_WINTER: -0.30, MID_WINTER: -0.40, LATE_WINTER: -0.30
}

function tilthSeasonOffset(level) {
  const subSeason = String(EclipticSeasonsApi.getSubSeason(level)) // à vérifier : enum ou chaîne
  return TILTH_SEASON_OFFSET[subSeason] ?? 0
}

// Une fois par minute de jeu, on remplace le modificateur saisonnier de chaque joueur de l'Overworld.
ServerEvents.tick(event => {
  if (event.server.tickCount % 1200 !== 0) return
  event.server.players.forEach(player => {
    if (String(player.level.dimension) !== 'minecraft:overworld') return
    const offset = tilthSeasonOffset(player.level)
    // Hypothèse d'API : un modificateur constant identifié par 'tilth:season', sur le trait « world ».
    const modifier = coldsweat.createModifier('tilth:season', offset)
    coldsweat.addModifier(player, modifier, 'world', /* remplacer l'existant */ true)
  })
})
