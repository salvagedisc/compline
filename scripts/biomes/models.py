"""The table that pairs each biome of Regions Unexplored, and the few biomes of other mods,
with the vanilla biome it resembles (Python 3). biome_rules.py gives each biome the mods'
rules of its model (animal spawns, horse breeds, wind).

The table first chose the mist of each biome in the ATMOSPHERICS preset of the former
`fallow-ambiance` resource pack (removed on 2026-11-13, decision 0024): hence the families
named below (misty, veiled, cave).
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

NAMESPACE = "regions_unexplored"
PREFIX = "ru_"

# Regions Unexplored biome -> vanilla biome whose mist profile it copies.
MODELS = {
    # Misty: forests, taigas, swamps, jungles.
    "alpha_grove": "forest",
    "ashen_woodland": "forest",
    "autumnal_maple_forest": "forest",
    "deciduous_forest": "forest",
    "magnolia_woodland": "forest",
    "maple_forest": "forest",
    "mauve_hills": "forest",
    "old_growth_forest": "forest",
    "orchard": "forest",
    "temperate_grove": "forest",
    "willow_forest": "forest",
    "wisteria_grove": "forest",
    "silver_birch_forest": "birch_forest",
    "windswept_maple_forest": "windswept_forest",
    "bamboo_forest": "bamboo_jungle",
    "blackwood_taiga": "taiga",
    "boreal_taiga": "taiga",
    "golden_boreal_taiga": "taiga",
    "pine_taiga": "taiga",
    "old_growth_boreal_taiga": "old_growth_spruce_taiga",
    "old_growth_golden_boreal_taiga": "old_growth_spruce_taiga",
    "redwoods": "old_growth_pine_taiga",
    "sparse_redwoods": "old_growth_pine_taiga",
    "cold_boreal_taiga": "snowy_taiga",
    "cold_deciduous_forest": "snowy_taiga",
    "frozen_pine_taiga": "snowy_taiga",
    "pine_slopes": "grove",
    "bayou": "swamp",
    "old_growth_bayou": "swamp",
    "fen": "swamp",
    "fungal_fen": "swamp",
    "marsh": "swamp",
    "eucalyptus_forest": "jungle",
    "rainforest": "jungle",
    # Veiled: open land, dry land, snow, heights, shores, water.
    "sparse_rainforest": "sparse_jungle",
    "tropics": "sparse_jungle",
    "barley_fields": "plains",
    "clover_plains": "plains",
    "flower_fields": "plains",
    "grassland": "plains",
    "poppy_fields": "plains",
    "prairie": "plains",
    "pumpkin_fields": "plains",
    "shrubland": "plains",
    "highland_fields": "meadow",
    "rocky_meadow": "meadow",
    "baobab_savanna": "savanna",
    "dry_bushland": "savanna",
    "steppe": "savanna",
    "joshua_desert": "desert",
    "saguaro_desert": "desert",
    "arid_mountains": "badlands",
    "outback": "badlands",
    "frozen_tundra": "snowy_plains",
    "tundra": "snowy_plains",
    "icy_heights": "frozen_peaks",
    "spires": "frozen_peaks",
    "mountains": "stony_peaks",
    "towering_cliffs": "stony_peaks",
    "chalk_cliffs": "stony_shore",
    "grassy_beach": "beach",
    "gravel_beach": "beach",
    "cold_river": "river",
    "muddy_river": "river",
    "tropical_river": "river",
    "hyacinth_deeps": "deep_cold_ocean",
    "rocky_reef": "lukewarm_ocean",
    # Caves and the Nether keep the mod's original cave and Nether profiles.
    "ancient_delta": "lush_caves",
    "bioshroom_caves": "lush_caves",
    "prismachasm": "lush_caves",
    "redstone_caves": "dripstone_caves",
    "inferno": "dripstone_caves",
    "scorching_caves": "dripstone_caves",
    "blackstone_basin": "nether_wastes",
    "glistering_meadow": "nether_wastes",
    "infernal_holt": "nether_wastes",
    "mycotoxic_undergrowth": "nether_wastes",
    "redstone_abyss": "nether_wastes",
}

UNDERGROUND = {"lush_caves", "dripstone_caves", "nether_wastes"}

# Biomes of the other mods of the pack ("namespace:biome") -> vanilla biome whose mist profile they copy.
# Their own colours are left alone. biome_rules.py checks this table against the jars.
OTHER_MODELS = {
    "hybrid_aquatic:cold_trench": "deep_cold_ocean",
    "hybrid_aquatic:coral_reef": "warm_ocean",
    "hybrid_aquatic:deep_coral_reef": "deep_lukewarm_ocean",
    "hybrid_aquatic:deep_warm_ocean": "deep_lukewarm_ocean",
    "hybrid_aquatic:frozen_trench": "deep_frozen_ocean",
    "hybrid_aquatic:lukewarm_trench": "deep_lukewarm_ocean",
    "hybrid_aquatic:red_meadow": "lukewarm_ocean",
    "hybrid_aquatic:seagrass_bed": "lukewarm_ocean",
    "hybrid_aquatic:sulfuric_caves": "dripstone_caves",
    "hybrid_aquatic:tide_pools": "beach",
    "hybrid_aquatic:trench": "deep_ocean",
    "hybrid_aquatic:tropical_deep_coral_reef": "deep_lukewarm_ocean",
    "hybrid_aquatic:tropical_river": "river",
    "hybrid_aquatic:warm_trench": "deep_lukewarm_ocean",
    "quark:glimmering_weald": "lush_caves",
    "spawn:ant_gardens": "meadow",
    "spawn:cold_island": "snowy_beach",
    "spawn:deep_warm_ocean": "deep_lukewarm_ocean",
    "spawn:dodo_island": "beach",
    "spawn:rocky_shore": "stony_shore",
    "spawn:sandy_island": "beach",
    "spawn:seagrass_meadow": "lukewarm_ocean",
    "spawn:tide_pool": "beach",
    "spawn:tropical_island": "beach",
    "spawn:volcanic_island": "stony_shore",
    "streamsreflowing:stream": "river",
}
OTHER_NAMESPACES = {name.split(":")[0] for name in OTHER_MODELS}


def write_json(path, data):
    with open(path, "w", encoding="utf-8") as out:
        json.dump(data, out, indent=2, ensure_ascii=False)
        out.write("\n")
