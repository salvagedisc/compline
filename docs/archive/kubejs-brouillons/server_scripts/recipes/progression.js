// BROUILLON Tilth — verrous de progression Overgeared → Create → rail → aviation (plan/03 §3.4).
// Les identifiants Create sont ceux de Create 6 ; ceux des addons et d'Overgeared sont à lire dans les .jar.
// Les pièces tilth:* sont forgées chez Overgeared (recettes à écrire dans overgeared_pieces.js une fois
// les types de recettes d'Overgeared connus : overgeared:forging ? overgeared:alloy_smelting ?).

ServerEvents.recipes(event => {
  // Âge 0-1 : pas d'outils ni d'armures en métal à l'établi, Overgeared est le seul fabricant.
  const metals = ['iron', 'golden', 'diamond', 'netherite', 'chainmail']
  const pieces = ['sword', 'axe', 'pickaxe', 'shovel', 'hoe', 'helmet', 'chestplate', 'leggings', 'boots']
  metals.forEach(m => pieces.forEach(p => event.remove({ output: `minecraft:${m}_${p}`, type: 'minecraft:crafting_shaped' })))

  // Âge 2 : l'alliage d'andésite demande un rivet forgé.
  event.remove({ output: 'create:andesite_alloy', type: 'minecraft:crafting_shaped' })
  event.shaped('2x create:andesite_alloy', ['AR', 'RA'], { A: 'minecraft:andesite', R: 'tilth:rivet_forge' })
    .id('tilth:andesite_alloy_rivet')

  // Âge 2 : presse et mélangeur demandent un marteau d'atelier forgé.
  event.replaceInput({ output: 'create:mechanical_press' }, 'minecraft:iron_block', 'tilth:marteau_atelier')
  event.replaceInput({ output: 'create:mechanical_mixer' }, 'create:whisk', 'tilth:marteau_atelier') // à vérifier : garder le fouet ?

  // Âge 3 : le moteur à vapeur prend des plaques d'acier (Overgeared) à la place de l'or.
  event.replaceInput({ output: 'create:steam_engine' }, 'create:golden_sheet', '#c:plates/steel') // tag d'acier à vérifier

  // Âge 3 : le mécanisme de précision intègre un rouage forgé (assemblage séquencé).
  // event.recipes.create.sequenced_assembly(...) : à réécrire avec KubeJS Create une fois Create 6.0.10 confirmé.

  // Âge 5 : rails et bogies en acier.
  event.replaceInput({ output: 'create:track' }, '#c:nuggets/iron', '#c:nuggets/steel')
  // bogies railways:* : remplacer le caisson par un essieu forgé (identifiants à lire dans le port S'n'R)

  // Âge 6-7 : moteurs diesel et hélices (identifiants à lire dans les .jar).
  // event.replaceInput({ mod: 'createdieselgenerators', output: /engine/ }, '#c:plates/iron', 'tilth:bloc_moteur_forge')
  // event.replaceInput({ mod: 'aeronautics', output: /propeller/ }, '#c:plates/iron', 'tilth:pale_helice_forgee')
})
