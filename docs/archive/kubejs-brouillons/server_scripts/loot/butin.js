// BROUILLON Fallow — butins (LootJS, syntaxe LootJS 3 pour 1.21.1 à vérifier).

LootJS.modifiers(event => {
  // Plus d'élytres ni d'yeux de l'Ender nulle part (décision 0004).
  event.addTableModifier(/.*/).removeLoot('minecraft:elytra').removeLoot('minecraft:ender_eye')

  // Os : les grands animaux en donnent (les squelettes ont disparu).
  event.addEntityModifier(['naturalist:bear', 'naturalist:deer', 'naturalist:lion', 'alexsmobs:moose', 'alexsmobs:bison'])
    .addLoot(LootEntry.of('minecraft:bone').setCount([1, 3]))

  // Plans d'ingénieur dans les lieux de civilisation perdue (identifiants des tables à lire dans les .jar).
  // event.addTableModifier('create_aeronautics_structures:chests/airship_wreck').addLoot(LootEntry.of('fallow:plan_aerostat').when(c => c.randomChance(0.5)))
  // event.addTableModifier(/create_new_beginnings:.*/).addLoot(LootEntry.of('fallow:plan_atelier').when(c => c.randomChance(0.3)))

  // Orbes spirituels de Paraglider : uniquement dans les sanctuaires et monuments de No Man's Land.
  // event.addTableModifier(/^(?!nomansland:).*/).removeLoot('paraglider:spirit_orb')
})
