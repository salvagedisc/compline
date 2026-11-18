// Fallow : tri du pack (docs/plan/tri-du-pack.md). Objets qui n'ont plus leur place :
// dimensions fermées (décision 0004), téléportation et repérage, armes de boss, doublons
// d'un autre mod. Ils perdent leurs recettes et sortent du butin ; ils sont aussi cachés
// d'EMI (client_scripts/tri.js, mêmes listes). Ids relevés dans le registre du serveur de test.

// Create, Aeronautics et Simulated : armes, outils créatifs, netherite, minerais broyés de métaux
// absents, pépites d'expérience.
const TRI_OBJETS_CREATE = [
  'create:potato_cannon', 'create:handheld_worldshaper', 'create:netherite_backtank',
  'create:netherite_backtank_placeable', 'create:netherite_diving_helmet',
  'create:netherite_diving_boots', 'create:crushed_raw_aluminum', 'create:crushed_raw_lead',
  'create:crushed_raw_nickel', 'create:crushed_raw_osmium', 'create:crushed_raw_platinum',
  'create:crushed_raw_quicksilver', 'create:crushed_raw_silver', 'create:crushed_raw_tin',
  'create:crushed_raw_uranium', 'create:experience_nugget', 'create:experience_block',
  'aeronautics:mounted_potato_cannon', 'simulated:plunger_launcher',
  'simulated:creative_physics_staff'
]

// Overgeared : l'argent, le moule du Nether, l'enclume du palier B (le palier A est l'enclume de maître de
// l'acier noir). La chaîne de la netherite revient avec le cœur brûlant (docs/plan/progression.md).
const TRI_OBJETS_OVERGEARED = [
  'overgeared:nether_tool_cast', 'overgeared:heated_silver_ingot', 'overgeared:tier_b_smithing_anvil'
]

// Farmer's Delight et Cold Sweat : salade du Nether, meubles et couteau venus du Nether, cuir
// de hoglin. La tomate et l'oignon de Farmer's Delight doublaient ceux de Farm & Charm : on
// garde ceux de Farm & Charm, qui suivent les saisons ; les recettes de Farmer's Delight les
// demandent par tag (c:crops/tomato, c:crops/onion) et les acceptent.
const TRI_OBJETS_CUISINE = [
  'farmersdelight:nether_salad', 'farmersdelight:netherite_knife',
  'farmersdelight:crimson_cabinet', 'farmersdelight:warped_cabinet',
  'farmersdelight:tomato', 'farmersdelight:tomato_seeds', 'farmersdelight:rotten_tomato',
  'farmersdelight:wild_tomatoes', 'farmersdelight:tomato_crate', 'farmersdelight:onion',
  'farmersdelight:wild_onions', 'farmersdelight:onion_crate',
  // Farm & Charm garde la ferme ; sa cuisine passe par Farmer's Delight (cuisine.js).
  'farm_and_charm:stove', 'farm_and_charm:cooking_pot', 'farm_and_charm:roaster',
  'farm_and_charm:crafting_bowl', 'farm_and_charm:onion_soup',
  // Sacs de Farm & Charm en double des caisses de Farmer's Delight (carotte, pomme de terre,
  // betterave) ou d'un objet fondu (fusions.js : pâtée pour chien, farine).
  'farm_and_charm:carrot_bag', 'farm_and_charm:potato_bag', 'farm_and_charm:beetroot_bag',
  'farm_and_charm:dog_food_bag', 'farm_and_charm:flour_bag',
  'cold_sweat:hoglin_hide', 'cold_sweat:hoglin_helmet', 'cold_sweat:hoglin_chestplate',
  'cold_sweat:hoglin_leggings', 'cold_sweat:hoglin_boots'
]

// Exposure, Envelope, Amendments, Supplementaries, Lootr : projecteur interplanaire, objets du
// Nether, objets coupés dans la config.
const TRI_OBJETS_DIVERS = [
  'exposure:interplanar_projector', 'exposure:broken_interplanar_projector',
  'envelope:crimson_pigeonhole', 'envelope:warped_pigeonhole', 'envelope:charred_pigeon_spawn_egg',
  'amendments:dragon_charge', 'supplementaries:sack', 'supplementaries:altimeter',
  'supplementaries:slice_map', 'supplementaries:candy', 'supplementaries:pancake',
  'supplementaries:sconce', 'supplementaries:sconce_soul', 'supplementaries:sconce_green',
  'supplementaries:sconce_lever', 'supplementaries:gravel_bricks',
  'supplementaries:netherite_door', 'supplementaries:netherite_trapdoor', 'lootr:lootr_shulker',
  'lootr:trophy'
]

// Handcrafted : trophées, four, confiture, meubles en bois du Nether.
const TRI_OBJETS_HANDCRAFTED = [
  'handcrafted:bear_trophy', 'handcrafted:blaze_trophy', 'handcrafted:creeper_trophy',
  'handcrafted:evoker_trophy', 'handcrafted:fox_trophy', 'handcrafted:goat_trophy',
  'handcrafted:phantom_trophy', 'handcrafted:pillager_trophy', 'handcrafted:pufferfish_trophy',
  'handcrafted:salmon_trophy', 'handcrafted:silverfish_trophy',
  'handcrafted:skeleton_horse_trophy', 'handcrafted:skeleton_trophy', 'handcrafted:spider_trophy',
  'handcrafted:tropical_fish_trophy', 'handcrafted:vindicator_trophy', 'handcrafted:witch_trophy',
  'handcrafted:wither_skeleton_trophy', 'handcrafted:wolf_trophy', 'handcrafted:oven',
  'handcrafted:berry_jam_jar', 'handcrafted:crimson_bench', 'handcrafted:crimson_chair',
  'handcrafted:crimson_corner_trim', 'handcrafted:crimson_couch', 'handcrafted:crimson_counter',
  'handcrafted:crimson_cupboard', 'handcrafted:crimson_desk', 'handcrafted:crimson_dining_bench',
  'handcrafted:crimson_drawer', 'handcrafted:crimson_fancy_bed', 'handcrafted:crimson_nightstand',
  'handcrafted:crimson_pillar_trim', 'handcrafted:crimson_shelf', 'handcrafted:crimson_side_table',
  'handcrafted:crimson_table', 'handcrafted:warped_bench', 'handcrafted:warped_chair',
  'handcrafted:warped_corner_trim', 'handcrafted:warped_couch', 'handcrafted:warped_counter',
  'handcrafted:warped_cupboard', 'handcrafted:warped_desk', 'handcrafted:warped_dining_bench',
  'handcrafted:warped_drawer', 'handcrafted:warped_fancy_bed', 'handcrafted:warped_nightstand',
  'handcrafted:warped_pillar_trim', 'handcrafted:warped_shelf', 'handcrafted:warped_side_table',
  'handcrafted:warped_table'
]

// Twigs : tables (Handcrafted les a), pierre de sang et blocs du Nether, doublons du tuf vanilla,
// rhyolite qui ne se génère plus.
const TRI_OBJETS_TWIGS = [
  'twigs:oak_table', 'twigs:spruce_table', 'twigs:birch_table', 'twigs:jungle_table',
  'twigs:acacia_table', 'twigs:dark_oak_table', 'twigs:mangrove_table', 'twigs:cherry_table',
  'twigs:bamboo_table', 'twigs:crimson_table', 'twigs:warped_table', 'twigs:bloodstone',
  'twigs:bloodstone_slab', 'twigs:bloodstone_stairs', 'twigs:bloodstone_wall',
  'twigs:cracked_polished_bloodstone_bricks', 'twigs:cracked_polished_rhyolite_bricks',
  'twigs:cracked_polished_tuff_bricks', 'twigs:polished_bloodstone',
  'twigs:polished_bloodstone_brick_slab', 'twigs:polished_bloodstone_brick_stairs',
  'twigs:polished_bloodstone_brick_wall', 'twigs:polished_bloodstone_bricks',
  'twigs:polished_bloodstone_slab', 'twigs:polished_bloodstone_stairs', 'twigs:polished_rhyolite',
  'twigs:polished_rhyolite_brick_slab', 'twigs:polished_rhyolite_brick_stairs',
  'twigs:polished_rhyolite_brick_wall', 'twigs:polished_rhyolite_bricks',
  'twigs:polished_rhyolite_slab', 'twigs:polished_rhyolite_stairs', 'twigs:polished_tuff',
  'twigs:polished_tuff_brick_slab', 'twigs:polished_tuff_brick_stairs',
  'twigs:polished_tuff_brick_wall', 'twigs:polished_tuff_bricks', 'twigs:polished_tuff_slab',
  'twigs:polished_tuff_stairs', 'twigs:rhyolite', 'twigs:rhyolite_slab', 'twigs:rhyolite_stairs',
  'twigs:rhyolite_wall', 'twigs:tuff_slab', 'twigs:tuff_stairs', 'twigs:tuff_wall',
  'twigs:crimson_shroomlamp', 'twigs:warped_shroomlamp', 'twigs:crimson_roots_paper_lantern',
  'twigs:twisting_polished_blackstone_brick_slab',
  'twigs:twisting_polished_blackstone_brick_stairs',
  'twigs:twisting_polished_blackstone_brick_wall', 'twigs:twisting_polished_blackstone_bricks',
  'twigs:weeping_polished_blackstone_brick_slab', 'twigs:weeping_polished_blackstone_brick_stairs',
  'twigs:weeping_polished_blackstone_brick_wall', 'twigs:weeping_polished_blackstone_bricks'
]

// Alex's Mobs : téléportation, repérage, élytres, objets et œufs des créatures du Nether et de
// l'End, objets techniques.
const TRI_OBJETS_ALEXSMOBS = [
  'alexsmobs:dimensional_carver', 'alexsmobs:shattered_dimensional_carver',
  'alexsmobs:dimensional_carver_shard_0', 'alexsmobs:dimensional_carver_shard_1',
  'alexsmobs:dimensional_carver_shard_2', 'alexsmobs:dimensional_carver_shard_3',
  'alexsmobs:dimensional_carver_shard_4', 'alexsmobs:dimensional_carver_shard_5',
  'alexsmobs:dimensional_carver_shard_6', 'alexsmobs:dimensional_carver_shard_7',
  'alexsmobs:dimensional_carver_shard_8', 'alexsmobs:dimensional_carver_shard_9',
  'alexsmobs:dimensional_carver_shard_10', 'alexsmobs:void_worm_mandible',
  'alexsmobs:void_worm_eye', 'alexsmobs:mysterious_worm', 'alexsmobs:void_worm_beak',
  'alexsmobs:void_worm_effigy', 'alexsmobs:echolocator', 'alexsmobs:endolocator',
  'alexsmobs:pupfish_locator', 'alexsmobs:tarantula_hawk_elytra', 'alexsmobs:tarantula_hawk_wing',
  'alexsmobs:tarantula_hawk_wing_fragment', 'minecraft:elytra', 'alexsmobs:soul_heart',
  'alexsmobs:warped_muscle', 'alexsmobs:warped_mixture', 'alexsmobs:hemolymph_sac',
  'alexsmobs:hemolymph_blaster', 'alexsmobs:straddlite', 'alexsmobs:straddlite_block',
  'alexsmobs:straddleboard', 'alexsmobs:straddle_helmet', 'alexsmobs:straddle_saddle',
  'alexsmobs:stradpole_bucket', 'alexsmobs:cosmic_cod', 'alexsmobs:cosmic_cod_bucket',
  'alexsmobs:enderiophage_rocket', 'alexsmobs:capsid', 'alexsmobs:ender_residue',
  'alexsmobs:chorus_on_a_stick', 'alexsmobs:spawn_egg_bone_serpent',
  'alexsmobs:spawn_egg_soul_vulture', 'alexsmobs:spawn_egg_warped_toad',
  'alexsmobs:spawn_egg_warped_mosco', 'alexsmobs:spawn_egg_crimson_mosquito',
  'alexsmobs:spawn_egg_straddler', 'alexsmobs:spawn_egg_stradpole',
  'alexsmobs:spawn_egg_laviathan', 'alexsmobs:spawn_egg_endergrade',
  'alexsmobs:spawn_egg_enderiophage', 'alexsmobs:spawn_egg_cosmaw',
  'alexsmobs:spawn_egg_cosmic_cod', 'alexsmobs:spawn_egg_void_worm',
  'alexsmobs:spawn_egg_mimicube', 'alexsmobs:spawn_egg_spectre', 'alexsmobs:tab_icon',
  'alexsmobs:fancy_item', 'alexsmobs:effect_item', 'alexsmobs:falconry_glove_hand',
  'alexsmobs:falconry_glove_inventory', 'alexsmobs:vine_lasso_hand',
  'alexsmobs:vine_lasso_inventory', 'alexsmobs:stink_ray_hand', 'alexsmobs:stink_ray_inventory',
  'alexsmobs:stink_ray_empty_hand', 'alexsmobs:stink_ray_empty_inventory',
  'alexsmobs:skelewag_sword_hand', 'alexsmobs:skelewag_sword_inventory'
]

// Companions! : rappel, totem de réapparition, objets du Nether et de l'End, équipement de combat
// magique.
const TRI_OBJETS_COMPANIONS = [
  'companions:recall_platform_block', 'companions:respawn_totem_block', 'companions:nether_coin',
  'companions:end_coin', 'companions:netherite_chains', 'companions:netherite_dagger',
  'companions:soul_gem', 'companions:soul_furnace_block', 'companions:shade_bat_altar',
  'companions:shade_maw_altar', 'companions:shade_sword_altar', 'companions:mage_coat',
  'companions:mage_hat', 'companions:mage_leggings', 'companions:mage_staff',
  'companions:holy_robe_coat', 'companions:holy_robe_leggings', 'companions:holy_robe_mask',
  'companions:crystallized_blood', 'companions:crystallized_blood_axe',
  'companions:crystallized_blood_boots', 'companions:crystallized_blood_chestplate',
  'companions:crystallized_blood_helmet', 'companions:crystallized_blood_leggings',
  'companions:crystallized_blood_scythe', 'companions:crystallized_blood_sword',
  'companions:blade_arm', 'companions:cannon_arm', 'companions:whip_arm', 'companions:mutant_arm',
  'companions:book_black_hole', 'companions:book_brace', 'companions:book_fire_mark',
  'companions:book_heal_ring', 'companions:book_ice_shard', 'companions:book_ice_tornado',
  'companions:book_magic_ray', 'companions:book_naginata', 'companions:book_stone_spikes'
]

// Mowzie's Mobs : armes et armures des boss (les gardiens donnent une bénédiction, pas une arme),
// objets techniques.
const TRI_OBJETS_MOWZIE = [
  'mowziesmobs:wrought_axe', 'mowziesmobs:wrought_helmet', 'mowziesmobs:earthrend_gauntlet',
  'mowziesmobs:sculptor_staff', 'mowziesmobs:sol_visage', 'mowziesmobs:geomancer_beads',
  'mowziesmobs:geomancer_belt', 'mowziesmobs:geomancer_robe', 'mowziesmobs:geomancer_sandals',
  'mowziesmobs:mob_remover', 'mowziesmobs:grant_suns_blessing', 'mowziesmobs:logo'
]

// Autres mods de faune : orphelins et doublons.
const TRI_OBJETS_FAUNE = [
  'hybrid_aquatic:uni', 'naturalist:silverfish', 'naturalist:endermite',
  'adorablehamsterpets:hamster_armor_netherite',
  'adorablehamsterpets:hamster_armor_trim_smithing_template_netherite',
  'adorablehamsterpets:announcement_bell_icon', 'adorablehamsterpets:upside_down_hamster_bed_icon',
  'crittersandcompanions:dragonfly_wing', 'crittersandcompanions:acorn_hat'
]

// Spelunkery : l'expérience en réserve (décision 0009), la hauteur et la position en chiffres, le
// repérage des géodes et des créatures, le Nether, l'End et la netherite (décision 0004), les
// objets inachevés du mod, les minerais et pépites des métaux absents du pack, la pépite de cuivre
// (Create a la sienne).
const TRI_OBJETS_SPELUNKERY = [
  'spelunkery:nephrite_charm', 'spelunkery:carved_nephrite', 'spelunkery:nephrite_siphon',
  'spelunkery:nephrite_fountain', 'spelunkery:nephrite_diode', 'spelunkery:depth_gauge',
  'spelunkery:magnetic_compass', 'spelunkery:tuning_fork', 'spelunkery:echo_fork',
  'spelunkery:compression_blast_miner', 'spelunkery:sulfur', 'spelunkery:sulfur_block',
  'spelunkery:end_stone_pebble', 'spelunkery:true_crown', 'spelunkery:eggplant',
  'spelunkery:iron_pick_on_a_stick',
  'spelunkery:copper_nugget', 'spelunkery:raw_lead_nugget',
  'spelunkery:raw_silver_nugget', 'spelunkery:raw_bismuth_nugget', 'spelunkery:raw_tin_nugget',
  'spelunkery:bismuth_nugget', 'spelunkery:rough_jade_shard', 'spelunkery:jade_shard',
  'spelunkery:andesite_lead_ore', 'spelunkery:diorite_lead_ore', 'spelunkery:granite_lead_ore',
  'spelunkery:tuff_lead_ore', 'spelunkery:andesite_silver_ore', 'spelunkery:diorite_silver_ore',
  'spelunkery:granite_silver_ore', 'spelunkery:tuff_silver_ore', 'spelunkery:andesite_jade_ore',
  'spelunkery:diorite_jade_ore', 'spelunkery:granite_jade_ore', 'spelunkery:tuff_jade_ore'
]

// Archie's Dinosaurs : les griffes en netherite.
const TRI_OBJETS_DINOSAURES = [
  'ec_ad:theri_claw_netherite'
]

// Starcatcher (décision 0034) : le bac de vente, sans monnaie dans Fallow ; les prises de
// l'End, qui n'existe plus ; sa rascasse et son bar, qu'Hybrid Aquatic a déjà. Leurs entrées de
// pêche sont retirées par scripts/fishing/starcatcher_entries.py.
const TRI_OBJETS_PECHE = [
  'selling_bin:selling_bin', 'starcatcher:charfish', 'starcatcher:chorus_crab',
  'starcatcher:chorus_minnow', 'starcatcher:dreamliner', 'starcatcher:end_glow',
  'starcatcher:nebula_squid', 'starcatcher:purple_carp', 'starcatcher:spacejelly',
  'starcatcher:voidbiter', 'starcatcher:voidfin', 'starcatcher:stonefish',
  'starcatcher:sea_bass'
]

// Surface Samples : les affleurements du Nether et des métaux absents du pack. Les affleurements
// gardés sont posés au-dessus des filons (docs/plan/progression.md).
const TRI_OBJETS_AFFLEUREMENTS = [
  'surfacesamples:aluminum_ore_sample',
  'surfacesamples:lead_ore_sample', 'surfacesamples:nickel_ore_sample', 'surfacesamples:osmium_ore_sample',
  'surfacesamples:silver_ore_sample', 'surfacesamples:tin_ore_sample', 'surfacesamples:uranium_ore_sample',
  'surfacesamples:fluorite_ore_sample', 'surfacesamples:nether_gold_ore_sample',
  'surfacesamples:nether_quartz_ore_sample'
]

const TRI_TOUT = [].concat(
  TRI_OBJETS_CREATE, TRI_OBJETS_OVERGEARED, TRI_OBJETS_CUISINE, TRI_OBJETS_DIVERS,
  TRI_OBJETS_HANDCRAFTED, TRI_OBJETS_TWIGS, TRI_OBJETS_ALEXSMOBS, TRI_OBJETS_COMPANIONS,
  TRI_OBJETS_MOWZIE, TRI_OBJETS_FAUNE, TRI_OBJETS_SPELUNKERY, TRI_OBJETS_DINOSAURES,
  TRI_OBJETS_PECHE, TRI_OBJETS_AFFLEUREMENTS
)

// Steam 'n' Rails : variantes décoratives. Voies de 3 bois (chêne, épicéa, bouleau), en
// andésite, monorail et sans traverses gardées ; les autres bois, le Nether et l'End sortent.
// Locométal et bandes de danger : seule la version sans teinte garde une recette, le pinceau
// peint toujours les blocs posés. Recettes seulement : ces objets ne sont dans aucun butin.
const TRI_MOTIFS = [
  /^railways:track_(incomplete_)?(acacia|bamboo|stripped_bamboo|blackstone|cherry|crimson|dark_oak|ender|jungle|mangrove|phantom|quark_ancient|quark_azalea|quark_blossom|warped)(_narrow|_wide)?$/,
  /^railways:(white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black|chartreuse|maroon|olive_green|pine_green|royal_blue|sea_green|turquoise|vermilion|ochrum|scorchia|limestone|dripstone|tuff|granite|diorite)_(\w*locometal\w*|hazard_stripes_\w+)$/
]

ServerEvents.recipes(event => {
  event.remove({ output: TRI_TOUT })
  // La poudre à canon de Spelunkery demandait son soufre, qui ne se trouve que dans le Nether.
  event.remove({ id: 'spelunkery:gunpowder' })
  TRI_MOTIFS.forEach(motif => event.remove({ output: motif }))
})

LootJS.lootTables(event => {
  event.forEachTable(table => {
    TRI_TOUT.forEach(id => table.removeItem(id))
  })
})
