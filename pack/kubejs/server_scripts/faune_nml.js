// Fallow : la faune de No Man's Land revient au vanilla, pour Fresh Animations.
// Voir docs/plan/faune-nml.md. Les données (pack/kubejs/data/nomansland/) coupent les
// apparitions du cerf, de l'oie, du bar à serpe et de la tortue, les textures de NML sur
// les animaux vanilla et les terriers de tortue ; config/mixed_litter-startup.toml coupe
// les remodèles. Ce script fait le reste.
// Lu dans le jar de NML 1.6.1 : MobSpawnEvents.onFinalizeMobSpawn, NMLItems, recettes,
// tables et modificateurs de butin.

// Objets qui n'ont plus de source une fois ces créatures coupées. Ils sont aussi cachés
// d'EMI (client_scripts/faune_nml.js, même liste).
const FAUNE_NML_OBJETS = [
  'nomansland:deer_spawn_egg', 'nomansland:goose_spawn_egg',
  'nomansland:billhook_bass_spawn_egg', 'nomansland:tortoise_spawn_egg',
  'nomansland:raw_venison', 'nomansland:cooked_venison',
  'nomansland:billhook_bass', 'nomansland:cooked_billhook_bass',
  'nomansland:billhook_bass_bucket', 'nomansland:billhook_bass_barrel',
  'nomansland:sturdy_scute', 'nomansland:tortoise_shell', 'nomansland:tortoise_egg',
  'nomansland:pot_debug_stick', 'nomansland:no_mans_globe'
]

// Objets de NML retirés du jeu : viande de cheval, cuisses de grenouille, plats. Les
// explosifs, les bombes et la flèche incendiaire restent, avec la TNT de NML (quatre
// explosifs). Les objets retirés tombent aussi de butins : NML les ajoute aux tables
// vanilla par des modificateurs qui tirent ses propres tables (entities/*_addition,
// chests/hardtack_addition…), ou les place dans ses tables et dans simple_dungeon.
// Aussi cachés d'EMI (client_scripts/faune_nml.js, même liste).
const FAUNE_NML_OBJETS_RETIRES = [
  'nomansland:raw_horse', 'nomansland:horse_steak',
  'nomansland:frog_leg', 'nomansland:cooked_frog_leg',
  'nomansland:pancake', 'nomansland:maple_tart', 'nomansland:sweet_tart',
  'nomansland:trail_mix', 'nomansland:syruped_pear', 'nomansland:honeyed_apple',
  'nomansland:hardtack'
]

// NML remplace 1 % des endermen naturels de l'Overworld par un Frienderman, ajouté par
// addFreshEntity, sans réglage dans sa config. Ce spawn passe par EntityJoinLevelEvent.
EntityEvents.spawned('nomansland:frienderman', event => {
  event.cancel()
})

ServerEvents.recipes(event => {
  event.remove({ output: FAUNE_NML_OBJETS })
  event.remove({ output: FAUNE_NML_OBJETS_RETIRES })
})

LootJS.lootTables(event => {
  event.forEachTable(table => {
    FAUNE_NML_OBJETS_RETIRES.forEach(id => table.removeItem(id))
  })
})
