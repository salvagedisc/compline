// BROUILLON Tilth — plans d'ingénieur (plan/02 §2.4).
// Principe : doLimitedCrafting = true (fonction tilth:init). À chaque connexion, le joueur reçoit toutes
// les recettes, puis on lui retire celles des plans qu'il n'a pas encore lus. Lire un plan (clic droit)
// le mémorise dans persistentData et débloque son lot. Quark « Automatic Recipe Unlock » doit être désactivé.
// À vérifier : player.unlockRecipes / lockRecipes ou commandes /recipe dans KubeJS 2101.

function tilthSyncRecipes(player) {
  const server = player.server
  const name = player.username
  server.runCommandSilent(`recipe give ${name} *`)
  const read = player.persistentData.getCompound('tilth_plans')
  for (const [plan, recipes] of Object.entries(global.TILTH.blueprints)) {
    if (read.getBoolean(plan)) continue
    recipes.forEach(id => server.runCommandSilent(`recipe take ${name} ${id}`))
  }
}

PlayerEvents.loggedIn(event => tilthSyncRecipes(event.player))

ItemEvents.rightClicked(event => {
  const plan = String(event.item.id)
  const recipes = global.TILTH.blueprints[plan]
  if (!recipes) return
  const player = event.player
  const read = player.persistentData.getCompound('tilth_plans')
  if (read.getBoolean(plan)) {
    player.tell(Text.gray('Vous connaissez déjà ce plan.'))
    return
  }
  read.putBoolean(plan, true)
  player.persistentData.put('tilth_plans', read)
  recipes.forEach(id => player.server.runCommandSilent(`recipe give ${player.username} ${id}`))
  player.tell(Text.gold('Vous étudiez le plan. De nouvelles fabrications vous sont accessibles.'))
  event.item.count-- // le plan est consommé ; le garder si on veut qu'il se transmette entre joueurs
  event.cancel()
})
