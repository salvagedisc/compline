// BROUILLON Fallow — constantes partagées par les scripts serveur.
// Tous les identifiants sont à confirmer dans les .jar (voir plan/08 §8.3).

global.FALLOW = {
  // Entités vanilla interdites dans l'Overworld (plan/05 §5.1)
  bannedVanilla: [
    'minecraft:zombie', 'minecraft:zombie_villager', 'minecraft:husk', 'minecraft:drowned',
    'minecraft:skeleton', 'minecraft:stray', 'minecraft:bogged', 'minecraft:creeper',
    'minecraft:spider', 'minecraft:cave_spider', 'minecraft:enderman', 'minecraft:witch',
    'minecraft:slime', 'minecraft:phantom', 'minecraft:silverfish', 'minecraft:guardian',
    'minecraft:elder_guardian', 'minecraft:pillager', 'minecraft:vindicator', 'minecraft:evoker',
    'minecraft:ravager', 'minecraft:vex', 'minecraft:illusioner', 'minecraft:breeze',
    'minecraft:warden', 'minecraft:skeleton_horse', 'minecraft:zombie_horse', 'minecraft:endermite'
  ],

  // Plans d'ingénieur : chaque plan débloque un lot de recettes (plan/02 §2.4, plan/03 §3.4).
  // Clé = identifiant de l'objet plan ; valeur = recettes verrouillées tant que le plan n'est pas lu.
  blueprints: {
    'fallow:plan_atelier':   ['create:crafting/kinetics/mechanical_press', 'create:crafting/kinetics/mechanical_mixer'],
    'fallow:plan_vapeur':    ['create:crafting/kinetics/steam_engine'],
    'fallow:plan_forage':    [/* foreuse et détecteur de Create Ore Excavation */],
    'fallow:plan_rail':      ['create:crafting/kinetics/track', /* bogies railways:* */],
    'fallow:plan_diesel':    [/* moteurs createdieselgenerators:* */],
    'fallow:plan_aerostat':  [/* brûleur, ballon aeronautics:* */],
    'fallow:plan_helice':    [/* hélices et paliers aeronautics:* */]
  }
}
