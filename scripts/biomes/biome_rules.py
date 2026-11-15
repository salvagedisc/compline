"""Makes the mods' own biome rules apply to the biomes other mods add (Python 3).

Several mods decide by a list of vanilla biome names where an animal spawns, which breed of
horse appears. A Regions Unexplored biome is in none of these lists, so
the rule ignores it: no horse in a prairie, no capybara in a bayou. This script gives each
Regions Unexplored biome the rules of the vanilla biome it resembles, with the same table as
the former mist (`MODELS` in models.py):

  - biome tags: for every tag of a mod that names vanilla biomes directly and knows no
    Regions Unexplored biome, it writes the additions in
    pack/kubejs/data/<namespace>/tags/worldgen/biome/. Tags that rely on the common tags
    (`#c:...`, `#minecraft:...`) are left alone: Regions Unexplored fills those itself.

The biomes of `OTHER_MODELS` in models.py (Hybrid Aquatic, the Streams Reflowing stream)
inherit the same way from their model, in the tags that know no biome of their mod. The
script stops if a mod of the pack has a biome that is in neither table.

Cave and Nether biomes inherit nothing: their model was chosen for the mist only. The tags
matched by `SKIPPED` are left alone too.

`PLACED` holds the placements chosen by hand, for what a mod puts in a cave biome or in a
closed dimension: they are written with the rest.

The script owns every entry of these mods' biomes in the pack's biome tags outside the
`compline` namespace: it rewrites them at each run.

Usage, from the repository root, after `npm run check-deps` has filled the jar cache:
  python3 scripts/biomes/biome_rules.py [folder of the pack's jars, default .cache/jars]
"""
import glob
import json
import os
import re
import sys
import zipfile

from models import MODELS, NAMESPACE, OTHER_MODELS, ROOT, UNDERGROUND, write_json

PACK_DATA = os.path.join(ROOT, "pack/kubejs/data")
MODS = os.path.join(ROOT, "pack/mods")

TAG = re.compile(r"^data/([^/]+)/tags/worldgen/biome/(.+)\.json$")
BIOME = re.compile(r"^data/([^/]+)/worldgen/biome/(.+)\.json$")

# Tags of these namespaces are shared conventions: Regions Unexplored fills them itself.
COMMON = {"minecraft", "c", "forge", "neoforge"}
OWN = {NAMESPACE, "compline"}
# Left as they are: Quark's fallen trunks are of vanilla woods, out of place under maples or redwoods.
SKIPPED = re.compile(r"^quark:has_(lower_)?fallen_")

# Placements chosen by hand (tag -> biomes), written with the inherited ones. Archie's Dinosaurs:
# the Lushsaur also lives in the bioshroom caves (lush caves are rare on Tectonic terrain); the
# Obsidiceratops of the Nether and the Chorusoatl of the End, both closed (decision 0004), move
# to the redstone caves and to the prismachasm.
PLACED = {
    "ec_ad:spawns_lushsaurs": ["regions_unexplored:bioshroom_caves"],
    "ec_ad:spawns_obsidiceratopses": ["regions_unexplored:redstone_caves"],
    "ec_ad:spawns_chorusoatls": ["regions_unexplored:prismachasm"],
}

# Every biome that inherits, with its model.
ALL_MODELS = {f"{NAMESPACE}:{name}": model for name, model in MODELS.items()}
ALL_MODELS.update(OTHER_MODELS)
# Tag namespaces a mod writes itself: its biomes take nothing there, the mod has made its choices.
FAMILY = {"hapi": "hybrid_aquatic"}
# The script owns the entries that start with one of these.
HEIRS = tuple(sorted({name.split(":")[0] + ":" for name in ALL_MODELS}))


def pack_jars(folder):
    """The jars of the mods listed in pack/mods, read in the jar cache."""
    names = []
    for toml in sorted(glob.glob(os.path.join(MODS, "*.pw.toml"))):
        with open(toml, encoding="utf-8") as source:
            match = re.search(r'^filename = "(.+)"$', source.read(), re.M)
        names.append(match.group(1))
    names += [os.path.basename(jar) for jar in glob.glob(os.path.join(MODS, "*.jar"))]
    missing = [name for name in names if not os.path.exists(os.path.join(folder, name))
               and not os.path.exists(os.path.join(MODS, name))]
    if missing:
        sys.exit(f"{len(missing)} jars are not in {folder} (run `npm run check-deps` first): {missing[:5]}")
    return [os.path.join(folder if os.path.exists(os.path.join(folder, name)) else MODS, name) for name in names]


def entry_id(entry):
    return entry["id"] if isinstance(entry, dict) else entry


def read_jars(jars):
    """Biome tags (tag -> entries, merged like the game does) and biomes of the jars."""
    tags, biomes = {}, set()
    for path in jars:
        with zipfile.ZipFile(path) as jar:
            for name in jar.namelist():
                tag, biome = TAG.match(name), BIOME.match(name)
                if biome and biome.group(1) not in ("minecraft", "compline"):
                    biomes.add(f"{biome.group(1)}:{biome.group(2)}")
                if not tag:
                    continue
                try:
                    data = json.loads(jar.read(name))
                except ValueError:
                    continue
                entries = tags.setdefault(f"{tag.group(1)}:{tag.group(2)}", [])
                if data.get("replace"):
                    entries.clear()
                entries += [entry_id(entry) for entry in data.get("values", [])]
    return tags, biomes


def pack_tag_files():
    """Path -> (tag, JSON) of the biome tags the pack already overrides, outside our own namespaces."""
    files = {}
    for path in glob.glob(os.path.join(PACK_DATA, "*/tags/worldgen/biome/**/*.json"), recursive=True):
        tag = TAG.match("data/" + os.path.relpath(path, PACK_DATA).replace(os.sep, "/"))
        if tag.group(1) in COMMON | OWN:
            continue
        with open(path, encoding="utf-8") as source:
            files[path] = (f"{tag.group(1)}:{tag.group(2)}", json.load(source))
    return files


def resolve(tag, tags, seen=()):
    """Every biome of a tag, or None when it relies on a common tag."""
    biomes = set()
    for entry in tags.get(tag, []):
        if not entry.startswith("#"):
            biomes.add(entry)
            continue
        inner = entry[1:]
        if inner.split(":")[0] in COMMON:
            return None
        if inner not in seen:
            nested = resolve(inner, tags, seen + (tag,))
            if nested is None:
                return None
            biomes |= nested
    return biomes


def heirs(vanilla, known=()):
    """Surface biomes whose model is one of these vanilla biomes, except those of the mods in `known`."""
    return sorted(name for name, model in ALL_MODELS.items()
                  if model not in UNDERGROUND and "minecraft:" + model in vanilla
                  and name.split(":")[0] not in known)


def main(folder):
    tags, biomes = read_jars(pack_jars(folder))
    unknown, gone = sorted(biomes - set(ALL_MODELS)), sorted(set(ALL_MODELS) - biomes)
    if unknown or gone:
        sys.exit(f"MODELS and OTHER_MODELS (models.py) do not match the jars: missing {unknown}, "
                 f"no longer in a mod {gone}")

    # The pack's own tag files come last, without the entries of a previous run.
    files = pack_tag_files()
    for path, (tag, data) in files.items():
        data["values"] = [entry for entry in data.get("values", []) if not entry_id(entry).startswith(HEIRS)]
        entries = tags.setdefault(tag, [])
        if data.get("replace"):
            entries.clear()
        entries += [entry_id(entry) for entry in data["values"]]

    additions = {}
    for tag in sorted(tags):
        if tag.split(":")[0] in COMMON | OWN or SKIPPED.match(tag):
            continue
        named = resolve(tag, tags)
        if not named:
            continue
        # So has a mod that already names one of its biomes in the tag.
        namespace = tag.split(":")[0]
        known = {biome.split(":")[0] for biome in named} | {namespace, FAMILY.get(namespace)}
        new = heirs(set(tags[tag]), known)
        if new:
            additions[tag] = new
    for tag, placed in PLACED.items():
        if tag not in tags:
            sys.exit(f"PLACED names {tag}, which no mod of the pack has")
        additions[tag] = sorted(set(additions.get(tag, [])) | set(placed))

    written = 0
    for path, (tag, data) in files.items():
        data["values"] += [{"id": biome, "required": False} for biome in additions.pop(tag, [])]
        if data["values"] or data.get("replace"):
            write_json(path, data)
            written += any(entry_id(entry).startswith(HEIRS) for entry in data["values"])
        else:
            os.remove(path)
    for tag, new in additions.items():
        namespace, name = tag.split(":")
        path = os.path.join(PACK_DATA, namespace, "tags/worldgen/biome", name + ".json")
        os.makedirs(os.path.dirname(path), exist_ok=True)
        write_json(path, {"replace": False, "values": [{"id": biome, "required": False} for biome in new]})
        written += 1

    print(f"{written} biome tags now include the biomes of other mods")


if __name__ == "__main__":
    if len(sys.argv) > 2:
        sys.exit(__doc__)
    main(sys.argv[1] if len(sys.argv) == 2 else os.path.join(ROOT, ".cache/jars"))
