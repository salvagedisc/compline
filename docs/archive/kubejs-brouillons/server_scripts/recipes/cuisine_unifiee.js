// BROUILLON Compline — Farm & Charm cantonné à la ferme, KC seul système de cuisine (décision 0002, plan/04 §4.7).
// Kaleidoscope Compat fait déjà l'essentiel (fusion des doublons, recettes de marmite rejouées au stockpot KC).
// Ce script ne couvre que ce qu'il laisse : postes de cuisine F&C et mangeoire. Identifiants à lire dans le .jar F&C.

ServerEvents.recipes(event => {
  // Postes de cuisine de F&C : plus fabricables (masqués dans JEI par client_scripts/visionneuse.js).
  ;['farm_and_charm:stove', 'farm_and_charm:cooking_pot', 'farm_and_charm:roaster', 'farm_and_charm:crafting_bowl']
    .forEach(id => event.remove({ output: id }))

  // Mangeoire F&C : elle met les animaux en amour automatiquement (reproduction autonome refusée en 0002).
  event.remove({ output: 'farm_and_charm:feeding_trough' })
})

ServerEvents.tags('item', event => {
  // Tags de saison pour les cultures de F&C, au même format que KC (sereneseasons:* lus aussi par Ecliptic).
  event.add('sereneseasons:summer_crops', ['farm_and_charm:corn', 'farm_and_charm:tomato_seeds'])
  event.add('sereneseasons:autumn_crops', ['farm_and_charm:barley_seeds', 'farm_and_charm:oat_seeds'])
  event.add('sereneseasons:spring_crops', ['farm_and_charm:strawberry_seeds', 'farm_and_charm:onion'])
  // Liste à compléter et à vérifier (Seasonal Let's Do sert de modèle).
})
