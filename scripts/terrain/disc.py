"""Writes the disc world: the shape of the land, its climate and the edge of the world
(Python 3, no dependency).

The world of the pack is one continent on a disc, cold in the north and hot in the south, with
open sea around it up to an edge nobody crosses (decision 0022). Nothing is painted: Tectonic and
the biome mods still generate the terrain from the seed. Two Lithostitched modifiers wrap two of
their inputs:

- `tectonic:noise/raw_continents` (how far inland a place is). Inside the disc a floor turns most
  of the sea the seed would have put there into low land, a slow noise keeps a few inland seas, and
  near the coast a cap brings everything down to deep sea, whatever the seed says. The cap never
  goes below -0.47: under -0.5 Tectonic switches to its island mode and makes land again.
- `minecraft:overworld/temperature`: the latitude replaces most of the seed's temperature.

The same numbers give the edge of the world, read by the `bord` module of the `fallow` mod
(`data/fallow/monde/bord.json`).

Usage, from the repository root:
  python3 scripts/terrain/disc.py [key=value ...]   (keys: see PARAMS; all distances in blocks)
"""
import json
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
DATA = os.path.join(ROOT, "pack/kubejs/data/fallow")

PARAMS = {
    "edge": 6000,        # radius of the edge of the world: nobody goes further
    "coast": 5700,       # mean radius where the cap reaches the coast; the land ends about 100 to 400 blocks before
    "big": 220,          # the coast wanders by about this much over 2,000 blocks,
    "mid": 90,           # this much over 500 blocks,
    "small": 20,         # and this much over 130 blocks
    "bias": 0.25,        # push toward land added to the seed's continents, in the middle of the disc
    "floor": -0.12,      # lowest "continents" value inside the disc (the coast is near -0.19),
    "floor_amp": 0.45,   # moved by a slow noise of this amplitude: where it dips, an inland sea may remain
    "latitude": 0.78,    # temperature added from the centre to the coast (north cold, south hot)
    "cx": 0, "cz": 0,    # centre of the disc
}


def add(*terms):
    result = terms[0]
    for term in terms[1:]:
        result = {"type": "minecraft:add", "argument1": result, "argument2": term}
    return result


def axis(name, centre):
    return add(-float(centre), {"type": "lithostitched:axis", "axis": name})


def square(function):
    return {"type": "minecraft:mul", "argument1": function, "argument2": function}


def noise(name, scale, amplitude):
    return {"type": "minecraft:mul", "argument1": float(amplitude),
            "argument2": {"type": "minecraft:noise", "noise": name, "xz_scale": scale, "y_scale": 0}}


def cached(function):
    # Without both caches the generation is four times slower.
    return {"type": "minecraft:flat_cache", "argument": {"type": "minecraft:cache_2d", "argument": function}}


def spline(points):
    return {"type": "minecraft:spline", "spline": {
        "coordinate": "fallow:disque/distance",
        "points": [{"location": float(a), "value": float(b), "derivative": 0} for a, b in points]}}


def build(p):
    coast = float(p["coast"])
    # Blocks from the coast line: negative inside. The three noises make the coast wander.
    distance = cached(add(
        {"type": "lithostitched:sqrt", "argument": add(square(axis("x", p["cx"])), square(axis("z", p["cz"])))},
        noise("minecraft:erosion", 0.25, p["big"]), noise("minecraft:ridge", 0.25, p["mid"]),
        noise("minecraft:ridge", 1.0, p["small"]), -coast))
    bias = spline([(-coast, p["bias"]), (-1500, p["bias"]), (-400, 0.0)])
    floor = add(spline([(-coast, p["floor"]), (-900, p["floor"]), (-350, -1.0)]),
                noise("minecraft:vegetation", 0.125, p["floor_amp"]))
    cap = spline([(-550, 2.0), (-300, 0.35), (-110, -0.05), (0, -0.19), (140, -0.40), (350, -0.47)])
    continents = {
        "type": "lithostitched:wrap_density_function",
        "target_function": "tectonic:noise/raw_continents",
        "wrapper_function": cached({"type": "minecraft:min", "argument1": cap, "argument2": {
            "type": "minecraft:clamp", "min": -0.49, "max": 2.0, "input": {
                "type": "minecraft:max",
                "argument1": add({"type": "lithostitched:wrapped_marker"}, bias),
                "argument2": floor}}}),
    }
    latitude = {"type": "minecraft:mul", "argument1": p["latitude"] / coast, "argument2": axis("z", p["cz"])}
    climate = {
        "type": "lithostitched:wrap_density_function",
        "target_function": "minecraft:overworld/temperature",
        "wrapper_function": {"type": "minecraft:flat_cache", "argument": {
            "type": "minecraft:clamp", "min": -1.0, "max": 1.0, "input": add(
                {"type": "minecraft:mul", "argument1": 0.3, "argument2": {"type": "lithostitched:wrapped_marker"}},
                latitude,
                # three noises of different scales bend the limits between climates
                noise("minecraft:temperature", 1.0, 0.22), noise("minecraft:erosion", 5, 0.07),
                noise("minecraft:offset", 1, 0.012))}},
    }
    edge = {"center_x": p["cx"], "center_z": p["cz"], "radius": p["edge"]}
    return {
        "worldgen/density_function/disque/distance.json": distance,
        "lithostitched/worldgen_modifier/disque_continents.json": continents,
        "lithostitched/worldgen_modifier/disque_climat.json": climate,
        "monde/bord.json": edge,
    }


def main(arguments, target=DATA):
    params = dict(PARAMS)
    for argument in arguments:
        key, _, value = argument.partition("=")
        if key not in params:
            sys.exit(f"unknown key {key}; known: {', '.join(params)}")
        params[key] = float(value) if "." in value else int(value)
    if params["coast"] + 150 > params["edge"]:
        sys.exit("the coast must stay at least 150 blocks inside the edge")
    for path, content in build(params).items():
        full = os.path.join(target, path)
        os.makedirs(os.path.dirname(full), exist_ok=True)
        with open(full, "w", encoding="utf-8") as handle:
            json.dump(content, handle, indent=2)
            handle.write("\n")
    print(f"disc written: coast {params['coast']}, edge {params['edge']}, centre {params['cx']}, {params['cz']}")


if __name__ == "__main__":
    main(sys.argv[1:])
