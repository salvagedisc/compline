// BROUILLON Fallow — objets propres au pack (registre au démarrage).
// Textures dans kubejs/assets/fallow/textures/item/<nom>.png ; noms en français via lang/fr_fr.json.

StartupEvents.registry('item', event => {
  // Plans d'ingénieur (plan/02 §2.4), trouvés en exploration uniquement
  for (const plan of ['atelier', 'vapeur', 'forage', 'rail', 'diesel', 'aerostat', 'helice']) {
    event.create(`fallow:plan_${plan}`).maxStackSize(1).rarity('uncommon')
  }

  // Pièces forgées qui verrouillent les machines Create (plan/03 §3.4).
  // Alternative à étudier dans le .jar : utiliser directement des têtes ou pièces d'Overgeared avec leur qualité.
  event.create('fallow:rivet_forge')
  event.create('fallow:marteau_atelier').maxStackSize(1)
  event.create('fallow:rouage_forge')
  event.create('fallow:tete_foreuse_forgee').maxStackSize(1)
  event.create('fallow:essieu_forge')
  event.create('fallow:bloc_moteur_forge')
  event.create('fallow:pale_helice_forgee')

  // Remplacement du Blaze si le Nether est fermé (plan/03 §3.8)
  event.create('fallow:coeur_de_braise')

  // Remplacement du salpêtre pour la poudre à canon (plan/05 §5.4)
  event.create('fallow:salpetre')
})
