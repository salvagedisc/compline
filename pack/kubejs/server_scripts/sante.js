// Tilth : santé maximale perdue à la mort, regagnée en explorant.
// Chaque mort retire un cœur de santé maximale, jusqu'à un plancher. L'« Éclat de vie »,
// trouvé en explorant, en rend un. Voir docs/plan/sante.md.
//
// Le script ne touche qu'à SON modificateur (SANTE_MODIFICATEUR) de l'attribut
// minecraft:generic.max_health. Les autres sources (effet Bonus de vie, etc.) gardent
// leurs propres modificateurs et s'ajoutent.
//
// Persistance : le nombre de cœurs perdus vit dans les données persistantes KubeJS du
// joueur (sauvegardées avec le joueur, recopiées par KubeJS à chaque réapparition).
// Le modificateur est transitoire : il est reposé à la connexion, au clonage et au réveil.

// ---- Réglages ----
const SANTE_COEURS_DEPART = 10            // cœurs d'un joueur neuf (vanilla : 10)
const SANTE_COEURS_PLANCHER = 5           // jamais moins de 5 cœurs
const SANTE_COEURS_PERDUS_PAR_MORT = 1
const SANTE_COEURS_RENDUS_PAR_ECLAT = 1
const SANTE_SOIN_A_L_USAGE = 2            // points de vie soignés à l'usage (2 = le cœur rendu est plein)
const SANTE_DELAI_ECLAT = 20              // ticks d'attente entre deux éclats (1 s)

// Cœur ancien (ancient_remnants:elderheart) : un éclat au premier passage près de chaque monument.
const SANTE_COEUR_ANCIEN_ECLATS = 1
const SANTE_COEUR_ANCIEN_RAYON = 16       // le même rayon que la bénédiction d'Ancient Remnants
const SANTE_COEUR_ANCIEN_INTERVALLE = 100 // ticks entre deux vérifications par joueur (5 s)

// ---- Identifiants (ne pas changer sur un monde en cours) ----
const SANTE_ATTRIBUT = 'minecraft:generic.max_health'
const SANTE_MODIFICATEUR = 'tilth:mort'
const SANTE_CLE_PERDUS = 'tilth_sante_perdus'
const SANTE_CLE_REVEIL = 'tilth_sante_reveil'
const SANTE_CLE_COEURS_ANCIENS = 'tilth_sante_coeurs_anciens'
const SANTE_ECLAT = 'tilth:eclat_de_vie'
const SANTE_COEUR_ANCIEN = 'ancient_remnants:elderheart'
const SANTE_SON = 'minecraft:block.amethyst_block.chime'

const SANTE_PERTE_MAX = SANTE_COEURS_DEPART - SANTE_COEURS_PLANCHER

function santePerdus(player) {
  const n = player.persistentData.getInt(SANTE_CLE_PERDUS)
  return Math.max(0, Math.min(SANTE_PERTE_MAX, n))
}

function santeFixerPerdus(player, n) {
  player.persistentData.putInt(SANTE_CLE_PERDUS, Math.max(0, Math.min(SANTE_PERTE_MAX, n)))
}

function santeCoeursRestants(player) {
  return SANTE_COEURS_DEPART - santePerdus(player)
}

// Pose (ou retire) le modificateur tilth:mort selon les cœurs perdus.
function santeAppliquer(player) {
  const perdus = santePerdus(player)
  if (perdus > 0) {
    player.modifyAttribute(SANTE_ATTRIBUT, SANTE_MODIFICATEUR, -2 * perdus, 'add_value')
  } else {
    player.removeAttribute(SANTE_ATTRIBUT, SANTE_MODIFICATEUR)
  }
  if (player.health > player.maxHealth) player.health = player.maxHealth
}

// Son et particules vanilla, joués à la position du joueur.
function santeEffets(player) {
  const nom = player.username
  player.server.runCommandSilent(`execute at ${nom} run playsound ${SANTE_SON} player @a ~ ~ ~ 1 0.8`)
  player.server.runCommandSilent(`execute at ${nom} run particle minecraft:heart ~ ~1.2 ~ 0.4 0.3 0.4 0 6`)
}

// ---- Connexion : le modificateur transitoire n'est pas sauvegardé, on le repose ----
PlayerEvents.loggedIn(event => {
  santeAppliquer(event.player)
})

// ---- Mort : KubeJS a déjà recopié les données persistantes dans le nouveau joueur ----
PlayerEvents.cloned(event => {
  const player = event.player
  if (!event.keepData) {
    santeFixerPerdus(player, santePerdus(player) + SANTE_COEURS_PERDUS_PAR_MORT)
    player.persistentData.putBoolean(SANTE_CLE_REVEIL, true)
  }
  santeAppliquer(player)
})

// ---- Réveil : santé pleine (du nouveau maximum) et message court ----
PlayerEvents.respawned(event => {
  const player = event.player
  santeAppliquer(player)
  player.health = player.maxHealth
  if (player.persistentData.getBoolean(SANTE_CLE_REVEIL)) {
    player.persistentData.remove(SANTE_CLE_REVEIL)
    const cle = santePerdus(player) >= SANTE_PERTE_MAX ? 'tilth.sante.reveil_plancher' : 'tilth.sante.reveil'
    player.tell(Text.gray(Text.translatable(cle, santeCoeursRestants(player))))
  }
})

// ---- Éclat de vie : un clic droit rend un cœur ----
ItemEvents.rightClicked(SANTE_ECLAT, event => {
  const player = event.player
  const objet = event.item
  if (player.cooldowns.isOnCooldown(objet.item)) return

  const perdus = santePerdus(player)
  if (perdus <= 0) {
    player.setStatusMessage(Text.gray(Text.translatable('tilth.sante.plein', SANTE_COEURS_DEPART)))
    return event.cancel()
  }

  santeFixerPerdus(player, perdus - SANTE_COEURS_RENDUS_PAR_ECLAT)
  santeAppliquer(player)
  player.heal(SANTE_SOIN_A_L_USAGE)
  player.addItemCooldown(objet.item, SANTE_DELAI_ECLAT)
  player.swing(event.hand, true)
  if (!player.isCreative()) objet.shrink(1)
  santeEffets(player)
  player.setStatusMessage(Text.red(Text.translatable('tilth.sante.rendu', santeCoeursRestants(player), SANTE_COEURS_DEPART)))
  return event.cancel()
})

// ---- Cœur ancien d'Ancient Remnants : un éclat au premier passage par monument ----
// Les monuments n'ont aucun coffre (vérifié dans le jar 1.5.2) : la récompense vient donc
// du Cœur ancien lui-même, quand le joueur arrive dans le rayon de la bénédiction.
PlayerEvents.tick(event => {
  const player = event.player
  if (player.tickCount % SANTE_COEUR_ANCIEN_INTERVALLE != 0) return
  if (player.isSpectator()) return

  const r = SANTE_COEUR_ANCIEN_RAYON
  const zone = AABB.of(player.x - r, player.y - r, player.z - r, player.x + r, player.y + r, player.z + r)
  player.level.getEntitiesWithin(zone).forEach(entite => {
    if (entite.type != SANTE_COEUR_ANCIEN) return
    // Distance calculée à la main : player.distanceTo(entite) est ambigu pour Rhino.
    const dx = entite.x - player.x, dy = entite.y - player.y, dz = entite.z - player.z
    if (dx * dx + dy * dy + dz * dz > r * r) return
    const id = entite.stringUUID
    const vus = player.persistentData.getString(SANTE_CLE_COEURS_ANCIENS)
    if ((';' + vus + ';').indexOf(';' + id + ';') >= 0) return

    player.persistentData.putString(SANTE_CLE_COEURS_ANCIENS, vus ? vus + ';' + id : id)
    player.give(Item.of(SANTE_ECLAT, SANTE_COEUR_ANCIEN_ECLATS))
    santeEffets(player)
    player.tell(Text.gray(Text.translatable('tilth.sante.coeur_ancien')))
  })
})
