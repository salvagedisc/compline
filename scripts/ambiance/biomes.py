"""Gives every Regions Unexplored biome its mist, its muted colours and its tag (Python 3).

The mist of the pack is set biome by biome in the ATMOSPHERICS preset. A biome the preset
does not know gets no mist at all, so each Regions Unexplored biome copies the profile of
the vanilla biome it resembles (the "model" below): same family (misty, veiled, cave),
same colours by climate. The few biomes of the other mods (`OTHER_MODELS`: Hybrid Aquatic,
the Streams Reflowing stream, Quark's glimmering weald) get their mist profile the same way,
and nothing else.

The script rewrites three things, from the table below and the biome files of the jar:
  - the `regions_unexplored:*` profiles, and those of `OTHER_MODELS`, of the single preset in
    pack/resourcepacks/fallow-ambiance/assets/atmospherics/preset/ (rename the file by hand
    to ship it: ATMOSPHERICS applies a preset once per file name);
  - one Polytone modifier per surface biome that sets its own grass, foliage or water
    colour (`ru_<biome>.json`), muted like the rest of the pack, with the snowflakes of the
    snowy biomes;
  - the Regions Unexplored part of the `fallow:brumeux` biome tag (the misty family), which
    the music pack reads.

Usage, from the repository root:
  python3 scripts/ambiance/biomes.py <regions-unexplored jar>
"""
import colorsys
import glob
import json
import os
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
AMBIANCE = os.path.join(ROOT, "pack/resourcepacks/fallow-ambiance/assets")
PRESETS = os.path.join(AMBIANCE, "atmospherics/preset")
MODIFIERS = os.path.join(AMBIANCE, "fallow/polytone/biome_modifiers")
MISTY_TAG = os.path.join(ROOT, "pack/kubejs/data/fallow/tags/worldgen/biome/brumeux.json")

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
    "streamsreflowing:stream": "river",
}
OTHER_NAMESPACES = {name.split(":")[0] for name in OTHER_MODELS}

# The misty family ends its daytime mist at 40 blocks, the veiled one at 72.
MISTY_END = 40.0

# Grass and foliage: (saturation, brightness) factors per family. Water: saturation only.
MUTED = {True: (0.72, 0.95), False: (0.85, 0.98)}
WATER_SATURATION = 0.80
VANILLA_WATER = 0x3F76E4  # left alone, so that the water does not change at a vanilla border

SNOWFLAKES = {"options": {"type": "minecraft:snowflake"}, "probability": 0.004}


def mute(colour, saturation, value):
    r, g, b = ((colour >> shift) & 0xFF for shift in (16, 8, 0))
    h, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
    return "#%02x%02x%02x" % tuple(round(c * 255) for c in colorsys.hsv_to_rgb(h, s * saturation, v * value))


def write_json(path, data):
    with open(path, "w", encoding="utf-8") as out:
        json.dump(data, out, indent=2, ensure_ascii=False)
        out.write("\n")


def read_biomes(jar_path):
    """Biome name -> its JSON, for every biome of the mod."""
    folder = f"data/{NAMESPACE}/worldgen/biome/"
    biomes = {}
    with zipfile.ZipFile(jar_path) as jar:
        for name in jar.namelist():
            if name.startswith(folder) and name.endswith(".json"):
                biomes[name[len(folder):-5]] = json.loads(jar.read(name))
    return biomes


def main(jar_path):
    biomes = read_biomes(jar_path)
    unknown = sorted(set(biomes) - set(MODELS))
    gone = sorted(set(MODELS) - set(biomes))
    if unknown or gone:
        sys.exit(f"MODELS does not match the jar: missing {unknown}, no longer in the mod {gone}")

    presets = glob.glob(os.path.join(PRESETS, "*.json"))
    if len(presets) != 1:
        sys.exit(f"expected a single preset in {PRESETS}, found {len(presets)}")
    with open(presets[0], encoding="utf-8") as source:
        preset = json.load(source)
    profiles = preset["biomes"]
    for key in [key for key in profiles if key.split(":")[0] in {NAMESPACE} | OTHER_NAMESPACES]:
        del profiles[key]

    for old in glob.glob(os.path.join(MODIFIERS, PREFIX + "*.json")):
        os.remove(old)

    misty = []
    modifiers = 0
    for name in sorted(biomes):
        model = MODELS[name]
        profile = profiles["minecraft:" + model]
        profiles[f"{NAMESPACE}:{name}"] = json.loads(json.dumps(profile))
        if model in UNDERGROUND:
            continue
        is_misty = profile["endDistance"] == MISTY_END
        if is_misty:
            misty.append(f"{NAMESPACE}:{name}")

        effects = biomes[name]["effects"]
        saturation, value = MUTED[is_misty]
        modifier = {"targets": [f"{NAMESPACE}:{name}"]}
        # A "swamp" or "dark_forest" grass modifier would ignore a fixed grass colour.
        if "grass_color" in effects and "grass_color_modifier" not in effects:
            modifier["grass_color"] = mute(effects["grass_color"], saturation, value)
        if "foliage_color" in effects:
            modifier["foliage_color"] = mute(effects["foliage_color"], saturation, value)
        if effects["water_color"] & 0xFFFFFF != VANILLA_WATER:
            modifier["water_color"] = mute(effects["water_color"], WATER_SATURATION, 1.0)
        if biomes[name].get("has_precipitation", True) and biomes[name]["temperature"] < 0.15:
            modifier["particle"] = SNOWFLAKES
        if len(modifier) > 1:
            write_json(os.path.join(MODIFIERS, f"{PREFIX}{name}.json"), modifier)
            modifiers += 1

    for name, model in sorted(OTHER_MODELS.items()):
        profile = profiles["minecraft:" + model]
        profiles[name] = json.loads(json.dumps(profile))
        if model not in UNDERGROUND and profile["endDistance"] == MISTY_END:
            misty.append(name)

    write_json(presets[0], preset)

    with open(MISTY_TAG, encoding="utf-8") as source:
        tag = json.load(source)
    tag["values"] = [value for value in tag["values"]
                     if value.split(":")[0] not in {NAMESPACE} | OTHER_NAMESPACES] + misty
    write_json(MISTY_TAG, tag)

    print(f"{len(biomes)} + {len(OTHER_MODELS)} mist profiles in {os.path.basename(presets[0])}, "
          f"{modifiers} colour modifiers, {len(misty)} misty biomes in the tag")


if __name__ == "__main__":
    if len(sys.argv) != 2:
        sys.exit(__doc__)
    main(sys.argv[1])
