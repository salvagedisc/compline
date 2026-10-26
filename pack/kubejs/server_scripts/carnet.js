// Fallow : le Carnet du voyageur, livre Modonomicon de guides (data/fallow/modonomicon/books/carnet).
// Donné à la première connexion ; un livre et un charbon de bois en refont un s'il est perdu.
// Les pages verrouillées s'ouvrent par les progrès cachés data/fallow/advancement/carnet/.
// Voir docs/plan/carnet.md.

const CARNET_OBJET = 'modonomicon:modonomicon[modonomicon:book_id="fallow:carnet"]'
const CARNET_CLE_DONNE = 'fallow_carnet_donne'

PlayerEvents.loggedIn(event => {
  const player = event.player
  if (player.persistentData.getBoolean(CARNET_CLE_DONNE)) return
  player.persistentData.putBoolean(CARNET_CLE_DONNE, true)
  player.server.runCommandSilent(`give ${player.username} ${CARNET_OBJET}`)
})

ServerEvents.recipes(event => {
  event.shapeless(Item.of(CARNET_OBJET), ['minecraft:book', 'minecraft:charcoal']).id('fallow:carnet')
})
