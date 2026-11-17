"""Fits Starcatcher's fishing entries to Compline (Python 3).

Starcatcher reads its catches from a datapack registry (`data/<namespace>/starcatcher/fish/`),
and so does the Hybrid Aquatic + Starcatcher Compatibility addon. A datapack can override an
entry, or remove it with the NeoForge condition `neoforge:false`. This script writes, from the
entries read in the jars:

  - removed: the End catches (the End no longer exists, decision 0004), Starcatcher's stonefish
    and sea bass (Hybrid Aquatic's are fished instead), and the Hybrid Aquatic species the pack
    cut for another mod's model whose item does not exist (stingray, ocean sunfish, seahorse);
  - moved: the Nether lava catches (the Nether is closed) are fished in the Overworld's lava,
    below y = -40, where the deep heat already is;
  - swapped: Hybrid Aquatic's anglerfish, blobfish and piranha give Naturalist's, the model the
    pack keeps (docs/plan/doublons.md);
  - added (namespace compline): Naturalist's bass and catfish and Spawn's bluefish, fish of the
    pack that no entry knew.

Usage, from the repository root, after `npm run check-deps` has filled the jar cache:
  python3 scripts/fishing/starcatcher_entries.py [folder of the pack's jars, default .cache/jars]
"""
import copy
import glob
import json
import os
import shutil
import sys
import zipfile

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA = os.path.join(ROOT, "pack/kubejs/data")

REMOVED = {
    "starcatcher": [
        "charfish", "chorus_crab", "chorus_minnow", "dreamliner", "end_glow", "nebula_squid",
        "purple_carp", "spacejelly", "voidbiter", "voidfin",
        "stonefish", "sea_bass",
    ],
    "hybrid_aquatic": ["stingray", "ocean_sunfish", "seahorse"],
}
# Naturalist keeps these species (doublons.js); its item and model replace Hybrid Aquatic's.
SWAPPED = {
    "anglerfish": "naturalist:anglerfish",
    "blobfish": "naturalist:blobfish",
    "piranha": "naturalist:piranha",
}
DEEP_LAVA = -40
# New entries: (name, item and entity, entry it copies the minigame and size from, restrictions).
OVERWORLD = {"type": "starcatcher:dimension", "dimension_entry": "overworld",
             "translation_override": "dimension.minecraft.overworld"}
WATER = {"type": "starcatcher:fluid", "fluids": ["minecraft:water"],
         "translation_override": "fluid.starcatcher.water"}
ADDED = [
    ("bass", "naturalist:bass", ("hybrid_aquatic", "trout"), "common", 20, [
        OVERWORLD,
        {"type": "starcatcher:biome", "biomes": ["#starcatcher:is_river", "#minecraft:is_forest",
                                                 "#starcatcher:is_swamp"],
         "blacklist": ["#starcatcher:is_cold_river", "#starcatcher:is_ocean"]},
        WATER,
    ], {"average_size_cm": 40.0, "average_weight_grams": 1500.0, "deviation_size_cm": 10.0,
        "deviation_weight_grams": 500.0, "golden_chance": 0.02}),
    ("catfish", "naturalist:catfish", ("starcatcher", "ripple_catfish"), "uncommon", 14, [
        OVERWORLD,
        {"type": "starcatcher:biome", "biomes": ["#starcatcher:is_river", "#starcatcher:is_swamp"],
         "blacklist": ["#starcatcher:is_cold_river"]},
        {"type": "starcatcher:daytime_restriction", "ranges": [{"max": 23000, "min": 13000}],
         "translation_override": "gui.guide.night"},
        WATER,
    ], {"average_size_cm": 70.0, "average_weight_grams": 4000.0, "deviation_size_cm": 20.0,
        "deviation_weight_grams": 1500.0, "golden_chance": 0.02}),
    ("bluefish", "spawn:bluefish", ("hybrid_aquatic", "mackerel"), "common", 18, [
        OVERWORLD,
        {"type": "starcatcher:biome", "biomes": ["#starcatcher:is_normal_ocean",
                                                 "#starcatcher:is_beach"],
         "blacklist": ["#starcatcher:is_frozen_ocean"]},
        WATER,
    ], {"average_size_cm": 50.0, "average_weight_grams": 2500.0, "deviation_size_cm": 15.0,
        "deviation_weight_grams": 900.0, "golden_chance": 0.02}),
]
DISABLED = {"neoforge:conditions": [{"type": "neoforge:false"}]}


def entries(folder):
    """Every fishing entry of the jars, as {(namespace, name): json}."""
    found = {}
    for jar in sorted(glob.glob(os.path.join(folder, "*.jar"))):
        with zipfile.ZipFile(jar) as archive:
            for path in archive.namelist():
                parts = path.split("/")
                if (len(parts) == 5 and parts[0] == "data" and parts[2] == "starcatcher"
                        and parts[3] == "fish" and parts[4].endswith(".json")):
                    found[(parts[1], parts[4][:-5])] = json.loads(archive.read(path))
    return found


def write(namespace, name, content):
    path = os.path.join(DATA, namespace, "starcatcher", "fish", name + ".json")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as target:
        json.dump(content, target, indent=2, ensure_ascii=False)
        target.write("\n")


def moved_to_deep_lava(entry):
    """A Nether lava catch, fished in the Overworld's lava below DEEP_LAVA."""
    moved = copy.deepcopy(entry)
    moved["restrictions"] = [OVERWORLD, {
        "type": "starcatcher:fluid", "fluids": ["minecraft:lava"],
        "translation_override": "fluid.starcatcher.lava",
    }, {
        "type": "starcatcher:elevation_restriction", "max_y": DEEP_LAVA, "min_y": -2147483648,
        "translation_override": "gui.guide.elevation.deepslate",
    }] + [r for r in entry.get("restrictions", [])
          if r["type"] not in ("starcatcher:dimension", "starcatcher:fluid",
                               "starcatcher:biome", "starcatcher:elevation_restriction")]
    return moved


def main():
    folder = sys.argv[1] if len(sys.argv) > 1 else os.path.join(ROOT, ".cache/jars")
    found = entries(folder)
    for namespace in ("starcatcher", "hybrid_aquatic", "compline"):
        shutil.rmtree(os.path.join(DATA, namespace, "starcatcher", "fish"), ignore_errors=True)
    counts = {"removed": 0, "moved": 0, "swapped": 0, "added": 0}
    for namespace, names in REMOVED.items():
        for name in names:
            if (namespace, name) not in found:
                sys.exit(f"no entry {namespace}:{name} in the jars")
            write(namespace, name, DISABLED)
            counts["removed"] += 1
    for (namespace, name), entry in sorted(found.items()):
        dimensions = [r.get("dimension_entry") for r in entry.get("restrictions", [])
                      if r["type"] == "starcatcher:dimension"]
        if namespace == "starcatcher" and dimensions == ["the_nether"]:
            write(namespace, name, moved_to_deep_lava(entry))
            counts["moved"] += 1
    for name, item in SWAPPED.items():
        entry = copy.deepcopy(found[("hybrid_aquatic", name)])
        entry["catch_info"]["item"]["identifier"] = item
        entry["catch_info"]["entity"] = item
        write("hybrid_aquatic", name, entry)
        counts["swapped"] += 1
    for name, item, model, rarity, chance, restrictions, size in ADDED:
        entry = copy.deepcopy(found[model])
        entry["catch_info"] = {"entity": item,
                               "fish_bucket": {"identifier": "starcatcher:starcaught_bucket"},
                               "item": {"identifier": item}}
        entry["rarity"] = rarity
        entry["base_chance"] = chance
        entry["restrictions"] = restrictions
        entry["size_and_weight"] = size
        write("compline", name, entry)
        counts["added"] += 1
    print(", ".join(f"{count} {kind}" for kind, count in counts.items()))


if __name__ == "__main__":
    main()
