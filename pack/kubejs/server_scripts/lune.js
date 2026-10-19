// Fallow : la Lune Amie de No Man's Land est le mystère central du pack.
// Voir docs/plan/lune-amie.md (fonctionnement, réglages, tests en jeu).
//
// 1. Une seule lune à la fois. La Lune Amie apparaît au Point de rencontre, n'importe quelle
//    nuit, dès qu'un joueur s'y trouve. Si une lune d'Enhanced Celestials est levée cette
//    nuit-là (lune blanche, lune bleue, lune des moissons, super lune…), elle se couche :
//    la Lune Amie prend toute la nuit. Le prochain évènement d'Enhanced Celestials
//    reste prévu à sa date.
// 2. Un indice dans les ruines des alchimistes : une note sur la lune gravée, dans les jarres.
//
// Classes lues dans les jars : Enhanced Celestials 2 Core 2.0.3.3
// (EnhancedCelestials.lunarForecastWorldData, LunarForecast.currentLunarEventHolder /
// getDimensionSettings / setLunarEventTonight, celle qu'appelle /ec setLunarEvent,
// EnhancedCelestialsRegistry.LUNAR_EVENT_KEY) et No Man's Land 1.6.1
// (FriendMoon.getOrDefault / isAwake / getMeetingPointPosition).

const LUNE_PERIODE = 40          // ticks entre deux vérifications (2 s)
const LUNE_RAYON = 64            // blocs autour du Point de rencontre (distance à plat)
const LUNE_NOTE_CHANCE = 0.04    // chance qu'une jarre « alchimie » contienne la note

let LUNE_EC = null
let LUNE_EC_REGISTRE = null
let LUNE_AMIE = null
try {
  LUNE_EC = Java.loadClass('dev.corgitaco.enhancedcelestials2core.EnhancedCelestials')
  LUNE_EC_REGISTRE = Java.loadClass('dev.corgitaco.enhancedcelestials2core.api.EnhancedCelestialsRegistry')
  LUNE_AMIE = Java.loadClass('com.farcr.nomansland.common.friend.FriendMoon')
} catch (e) {
  console.warn(`[Fallow] lune.js : Enhanced Celestials ou No Man's Land introuvable, cohabitation des lunes coupée (${e})`)
}

let luneTicks = 0
let luneEnPanne = false

// Les joueurs de l'Overworld. « level.players() » ne marche pas : KubeJS y voit une propriété.
function luneJoueursOverworld(server) {
  let tous = server.getPlayerList().getPlayers()
  let liste = []
  for (let i = 0; i < tous.size(); i++) {
    let p = tous.get(i)
    if (String(p.serverLevel().dimension().location()) === 'minecraft:overworld') liste.push(p)
  }
  return liste
}

// Vrai si la Lune Amie est éveillée, ou si un joueur est près du Point de rencontre
// (elle commence à apparaître avant de s'éveiller).
function luneAmiePresente(server, level) {
  if (LUNE_AMIE.getOrDefault(level).isAwake()) return true
  let centre = LUNE_AMIE.getMeetingPointPosition(level)
  if (!centre) return false
  let joueurs = luneJoueursOverworld(server)
  for (let i = 0; i < joueurs.length; i++) {
    let p = joueurs[i]
    if (p.isSpectator()) continue
    let dx = p.getX() - centre.getX()
    let dz = p.getZ() - centre.getZ()
    if (dx * dx + dz * dz <= LUNE_RAYON * LUNE_RAYON) return true
  }
  return false
}

// Une vérification. Pièges de Rhino (KubeJS 2101) vus sur le serveur de test :
// - un « const » dans le try du rappel donnait « redeclaration of var level » ;
// - « clé == null » sur une ResourceKey lève une NullPointerException (égalité spéciale
//   de KubeJS), d'où les Optional testés avec isPresent() ;
// - ResourceKey n'expose pas equals() : on compare les identifiants en texte.
function luneVerifier(server) {
  let level = server.overworld()
  let previsionOpt = LUNE_EC.lunarForecastWorldData(level)
  if (!previsionOpt.isPresent()) return
  let prevision = previsionOpt.get()

  // Lune ordinaire (ou jour, ou pluie) : rien à faire. C'est le cas presque toujours.
  let ordinaire = prevision.getDimensionSettings().defaultEvent()
  let actuelleOpt = prevision.currentLunarEventHolder().unwrapKey()
  if (!actuelleOpt.isPresent()) return
  let actuelle = actuelleOpt.get()
  if (String(ordinaire.location()) === String(actuelle.location())) return

  if (!luneAmiePresente(server, level)) return

  // Même appel que /ec setLunarEvent vers la lune ordinaire : l'évènement de la nuit est
  // coupé, la suite de la prévision ne bouge pas, Enhanced Celestials affiche son message de fin.
  let lunes = level.registryAccess().registry(LUNE_EC_REGISTRE.LUNAR_EVENT_KEY).orElseThrow()
  prevision.setLunarEventTonight(lunes.getHolderOrThrow(ordinaire))
  let message = Text.gray(Text.translatable('fallow.lune.efface'))
  let joueurs = luneJoueursOverworld(server)
  for (let i = 0; i < joueurs.length; i++) joueurs[i].displayClientMessage(message, true)
  console.info(`[Fallow] lune.js : ${actuelle.location()} couchée, la Lune Amie est au Point de rencontre`)
}

ServerEvents.tick(event => {
  if (!LUNE_EC || !LUNE_AMIE || luneEnPanne) return
  if (++luneTicks < LUNE_PERIODE) return
  luneTicks = 0
  try {
    luneVerifier(event.server)
  } catch (e) {
    luneEnPanne = true
    console.error(`[Fallow] lune.js : cohabitation des lunes coupée après une erreur (${e})`)
  }
})

// L'indice : une note trouvée dans les jarres des ruines des alchimistes, où se trouve
// le sanctuaire de la lune gravée. Elle s'ajoute au butin normal.
LootJS.modifiers(event => {
  event.addTableModifier('nomansland:chests/alchemist_pot_alchemy')
    .randomChance(LUNE_NOTE_CHANCE)
    .addLoot(
      LootEntry.of('minecraft:paper')
        .setName(Text.translatable('fallow.lune.note'))
        .addLore(
          Text.gray(Text.translatable('fallow.lune.note.1')),
          Text.gray(Text.translatable('fallow.lune.note.2')),
          Text.gray(Text.translatable('fallow.lune.note.3'))
        )
    )
})
