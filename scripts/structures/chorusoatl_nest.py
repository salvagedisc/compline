"""Writes the nest of the Chorusoatl (Python 3, no dependency).

Archie's Dinosaurs puts its Lushsaur, Therimoss and Obsidiceratops in the world with a tiny
structure: a floor, an air pocket and the animal. The Chorusoatl has none, because in the End it
spawns like any animal. Moved to the prismachasm (decision 0021) it would almost never appear:
the animals of the surface fill the spawn cap. This script writes the same kind of structure for
it: a 7 x 7 floor of deepslate prismoss, five blocks of air and one Chorusoatl.

Usage, from the repository root:
  python3 scripts/structures/chorusoatl_nest.py
"""
import gzip
import os
import struct

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
TARGET = os.path.join(ROOT, "pack/kubejs/data/compline/structure/chorusoatl.nbt")
DATA_VERSION = 3955  # Minecraft 1.21.1
SIZE = (7, 6, 7)
FLOOR, AIR = "regions_unexplored:deepslate_prismoss", "minecraft:air"

END, INT, DOUBLE, STRING, LIST, COMPOUND = 0, 3, 6, 8, 9, 10


def text(value):
    data = value.encode("utf-8")
    return struct.pack(">H", len(data)) + data


def payload(kind, value):
    if kind == INT:
        return struct.pack(">i", value)
    if kind == DOUBLE:
        return struct.pack(">d", value)
    if kind == STRING:
        return text(value)
    if kind == LIST:
        inner, items = value
        return struct.pack(">bi", inner, len(items)) + b"".join(payload(inner, item) for item in items)
    return b"".join(struct.pack(">b", k) + text(name) + payload(k, v) for name, (k, v) in value.items()) + b"\x00"


def ints(*values):
    return (LIST, (INT, list(values)))


blocks = [{"pos": ints(x, y, z), "state": (INT, 0 if y == 0 else 1)}
          for y in range(SIZE[1]) for z in range(SIZE[2]) for x in range(SIZE[0])]
structure = {
    "DataVersion": (INT, DATA_VERSION),
    "size": ints(*SIZE),
    "palette": (LIST, (COMPOUND, [{"Name": (STRING, FLOOR)}, {"Name": (STRING, AIR)}])),
    "blocks": (LIST, (COMPOUND, blocks)),
    "entities": (LIST, (COMPOUND, [{
        "pos": (LIST, (DOUBLE, [3.5, 1.0, 3.5])),
        "blockPos": ints(3, 1, 3),
        "nbt": (COMPOUND, {"id": (STRING, "ec_ad:chorusoatl")}),
    }])),
}

os.makedirs(os.path.dirname(TARGET), exist_ok=True)
with open(TARGET, "wb") as target:
    # mtime=0: the same bytes at each run.
    with gzip.GzipFile(fileobj=target, mode="wb", mtime=0) as packed:
        packed.write(struct.pack(">b", COMPOUND) + text("") + payload(COMPOUND, structure))
print(f"{os.path.relpath(TARGET, ROOT)} written")
