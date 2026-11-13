// Tilth : la tannerie, côté client. Voir docs/plan/tannerie.md.

// Sacoches de Tannery cachées d'EMI. Même liste que server_scripts/tannerie.js.
RecipeViewerEvents.removeEntriesCompletely('item', event => {
  [
    'tannery:small_satchel', 'tannery:large_satchel'
  ].forEach(id => event.remove(id))
})

// Le tonneau à tanner écrit ses messages en anglais, en dur (Component.literal), au-dessus
// de la barre d'objets. Le serveur les envoie comme messages système : on les remplace
// à l'arrivée par leur traduction (assets/tilth/lang).
const TANNERIE_MESSAGES = {
  'Barrel needs Tannic Powder': 'message.tilth.tannerie.poudre_manquante',
  'Barrel Full': 'message.tilth.tannerie.tonneau_plein'
}

try {
  NativeEvents.onEvent(Java.loadClass('net.neoforged.neoforge.client.event.ClientChatReceivedEvent$System'), event => {
    if (!event.isOverlay()) return
    let cle = TANNERIE_MESSAGES[event.getMessage().getString()]
    if (cle) event.setMessage(Text.translatable(cle))
  })
} catch (e) {
  console.warn(`[tannerie] messages du tonneau non traduits : ${e}`)
}
