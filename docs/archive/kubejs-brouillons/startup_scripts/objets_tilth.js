// BROUILLON Tilth — objets propres au pack (registre au démarrage).
// Textures dans kubejs/assets/tilth/textures/item/<nom>.png ; noms en français via lang/fr_fr.json.

StartupEvents.registry('item', event => {
  // Plans d'ingénieur (plan/02 §2.4), trouvés en exploration uniquement
  for (const plan of ['atelier', 'vapeur', 'forage', 'rail', 'diesel', 'aerostat', 'helice']) {
    event.create(`tilth:plan_${plan}`).maxStackSize(1).rarity('uncommon')
  }

  // Pièces forgées qui verrouillent les machines Create (plan/03 §3.4).
  // Alternative à étudier dans le .jar : utiliser directement des têtes ou pièces d'Overgeared avec leur qualité.
  event.create('tilth:rivet_forge')
  event.create('tilth:marteau_atelier').maxStackSize(1)
  event.create('tilth:rouage_forge')
  event.create('tilth:tete_foreuse_forgee').maxStackSize(1)
  event.create('tilth:essieu_forge')
  event.create('tilth:bloc_moteur_forge')
  event.create('tilth:pale_helice_forgee')

  // Remplacement du Blaze si le Nether est fermé (plan/03 §3.8)
  event.create('tilth:coeur_de_braise')

  // Remplacement du salpêtre pour la poudre à canon (plan/05 §5.4)
  event.create('tilth:salpetre')
})
