// Compline : créatures retirées par décision de l'équipe, hors doublons. Voir
// docs/plan/doublons.md, « Autres créatures retirées ».
// - Alex's Mobs : Murmur, Farseer, Skelewag, mouche, cafard (direction artistique, 5.3) ;
// - Friends & Foes : iceologer, illusioner, wildfire (hostiles, décision 0003), et leurs
//   quatre structures, coupées par leurs tags de biomes vidés
//   (kubejs/data/friendsandfoes/tags/worldgen/biome/has_structure/) ;
// - Naturalist : mammouth (préhistoire écartée), coupé dans config/naturalist-server.toml.
// - animations trop faibles à côté de Fresh Animations (2026-10-28) : limace banane, tortue
//   alligator, mungus, triops, oiseau-soleil et guster d'Alex's Mobs, moobloom de Friends &
//   Foes (aussi coupée dans config/friendsandfoes.json), koï de Critters and Companions ;
//   puis les cas limites : rocky roller, requin-lutin et potoo d'Alex's Mobs, glare (aussi
//   coupé dans config/friendsandfoes.json), lapin de mer, et oursin, cténophore, anguille
//   jardinière, nautile, limace et concombre de mer, étoile de mer d'Hybrid Aquatic (fichiers
//   d'apparition remplacés dans kubejs/data/hybrid_aquatic/neoforge/biome_modifier/).
// Leurs apparitions par biome sont retirées par
// kubejs/data/compline/neoforge/biome_modifier/faune_coupee.json. Ce script fait le reste.

// Créatures qui entrent aussi dans le monde par d'autres chemins : raids, évènements
// propres au mod, structures, ootheca de cafard.
const FAUNE_COUPEE_CREATURES = [
  'alexsmobs:murmur', 'alexsmobs:farseer', 'alexsmobs:skelewag', 'alexsmobs:fly',
  'alexsmobs:cockroach',
  'friendsandfoes:iceologer', 'friendsandfoes:illusioner', 'friendsandfoes:wildfire',
  'alexsmobs:banana_slug', 'alexsmobs:alligator_snapping_turtle', 'alexsmobs:mungus',
  'alexsmobs:triops', 'alexsmobs:sunbird', 'alexsmobs:guster', 'friendsandfoes:moobloom',
  'crittersandcompanions:koi_fish',
  'alexsmobs:rocky_roller', 'alexsmobs:frilled_shark', 'alexsmobs:potoo',
  'friendsandfoes:glare', 'crittersandcompanions:sea_bunny', 'hybrid_aquatic:sea_urchin',
  'hybrid_aquatic:comb_jelly', 'hybrid_aquatic:garden_eel', 'hybrid_aquatic:nautilus',
  'hybrid_aquatic:sea_slug', 'hybrid_aquatic:sea_cucumber', 'hybrid_aquatic:starfish'
]

// Objets qui n'ont plus de source, et ceux qui ne se fabriquent qu'à partir d'eux
// (fouet en tendon, table de transmutation, couronne, carapace épineuse, gustmaker,
// ragoût anti-moustiques). Aussi cachés d'EMI
// (client_scripts/faune_coupee.js, même liste).
const FAUNE_COUPEE_OBJETS = [
  'alexsmobs:spawn_egg_murmur', 'alexsmobs:spawn_egg_farseer', 'alexsmobs:spawn_egg_skelewag',
  'alexsmobs:spawn_egg_fly', 'alexsmobs:spawn_egg_cockroach',
  'alexsmobs:unsettling_kimono', 'alexsmobs:elastic_tendon', 'alexsmobs:tendon_whip',
  'alexsmobs:farseer_arm', 'alexsmobs:transmutation_table', 'alexsmobs:skelewag_sword',
  'alexsmobs:novelty_hat',
  'friendsandfoes:iceologer_spawn_egg', 'friendsandfoes:illusioner_spawn_egg',
  'friendsandfoes:wildfire_spawn_egg', 'friendsandfoes:totem_of_freezing',
  'friendsandfoes:totem_of_illusion', 'friendsandfoes:wildfire_crown_fragment',
  'friendsandfoes:wildfire_crown',
  'naturalist:mammoth_spawn_egg', 'naturalist:mammoth_meat', 'naturalist:cooked_mammoth_meat',
  'alexsmobs:spawn_egg_banana_slug', 'alexsmobs:banana_slug_slime',
  'alexsmobs:banana_slug_slime_block', 'alexsmobs:crystalized_banana_slug_mucus',
  'alexsmobs:spawn_egg_alligator_snapping_turtle', 'alexsmobs:spiked_scute',
  'alexsmobs:spiked_turtle_shell', 'alexsmobs:spawn_egg_mungus', 'alexsmobs:mungal_spores',
  'alexsmobs:spawn_egg_triops', 'alexsmobs:triops_bucket', 'alexsmobs:triops_eggs',
  'alexsmobs:mosquito_repellent_stew', 'alexsmobs:spawn_egg_sunbird', 'alexsmobs:spawn_egg_guster',
  'alexsmobs:guster_eye', 'alexsmobs:pocket_sand', 'alexsmobs:gustmaker',
  'friendsandfoes:moobloom_spawn_egg',
  'crittersandcompanions:koi_fish', 'crittersandcompanions:koi_fish_bucket',
  'crittersandcompanions:koi_fish_spawn_egg',
  'alexsmobs:spawn_egg_rocky_roller', 'alexsmobs:rocky_shell', 'alexsmobs:rocky_chestplate',
  'alexsmobs:spawn_egg_frilled_shark', 'alexsmobs:frilled_shark_bucket',
  'alexsmobs:spawn_egg_potoo', 'friendsandfoes:glare_spawn_egg',
  'crittersandcompanions:sea_bunny_spawn_egg', 'crittersandcompanions:sea_bunny_bucket',
  'crittersandcompanions:sea_bunny_slime_bottle',
  'crittersandcompanions:sea_bunny_slime_block', 'hybrid_aquatic:sea_urchin_spawn_egg',
  'hybrid_aquatic:sea_urchin_spine', 'hybrid_aquatic:comb_jelly_spawn_egg',
  'hybrid_aquatic:garden_eel_spawn_egg', 'hybrid_aquatic:nautilus_spawn_egg',
  'hybrid_aquatic:sea_slug_spawn_egg', 'hybrid_aquatic:sea_cucumber_spawn_egg',
  'hybrid_aquatic:starfish_spawn_egg', 'hybrid_aquatic:starfish'
]

FAUNE_COUPEE_CREATURES.forEach(id => {
  EntityEvents.spawned(id, event => {
    event.cancel()
  })
})

ServerEvents.recipes(event => {
  event.remove({ output: FAUNE_COUPEE_OBJETS })
})

LootJS.lootTables(event => {
  event.forEachTable(table => {
    FAUNE_COUPEE_OBJETS.forEach(id => table.removeItem(id))
  })
})
