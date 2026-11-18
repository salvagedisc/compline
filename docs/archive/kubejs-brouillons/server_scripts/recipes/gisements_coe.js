// BROUILLON Fallow — gisements finis de Create Ore Excavation, liés aux biomes (plan/03 §3.3).
// Syntaxe d'après le README de COE pour 1.21 ; tags de biomes à vérifier avec No Man's Land et Biolith.

ServerEvents.recipes(event => {
  const coe = event.recipes.createoreexcavation

  // Zinc : biomes humides.
  coe.vein('{"text":"Gisement de zinc"}', 'create:raw_zinc')
    .placement(64, 24, 91827).biomeWhitelist('#c:is_wet/overworld')
    .alwaysFinite().veinSize(4, 8).id('fallow:vein_zinc')
  coe.drilling([coeutil.processingOutput('create:raw_zinc', 1), coeutil.processingOutput('minecraft:gravel', 0.3)],
      'fallow:vein_zinc', 400)
    .drill('fallow:tete_foreuse_forgee').stress(512).id('fallow:drill_zinc')

  // Fer : montagnes et collines.
  coe.vein('{"text":"Gisement de fer"}', 'minecraft:raw_iron')
    .placement(48, 16, 55120).biomeWhitelist('#c:is_mountain')
    .alwaysFinite().veinSize(6, 10).id('fallow:vein_iron')
  coe.drilling([coeutil.processingOutput('minecraft:raw_iron', 1)], 'fallow:vein_iron', 400)
    .drill('fallow:tete_foreuse_forgee').stress(512).id('fallow:drill_iron')

  // À compléter : cuivre (côtes), charbon (forêts), or (badlands), pétrole pour Diesel Generators (extracting).
})
