"""Builds the fallow-interface resource pack from free-licensed packs.

Base: GUI Revision by Vixel and GUI Revision Compats by Fyoncle (both
CC BY-NC-SA 4.0), with the top frieze of Hollow GUI by Melee (CC BY 4.0).
The script downloads the pinned releases, keeps the files that concern
vanilla and the mods of the pack, and adapts them to Fallow:

- recolour: GUI Revision's greys become Fallow's mist, sage and moss;
- titles: the names drawn in the textures (plates, banners) are removed,
  since they cannot follow the game's language;
- screens GUI Revision paints in wood or purple are greyed first, so that
  they take the same palette;
- slots: redrawn over their whole cell, square with rounded corners or round,
  with a cast shadow; deep wells with a grid where creatures stand;
- arrows: one slim engraved arrow, the progress one in lantern light;
- details: Hollow GUI's frieze along the top of vanilla panels and a tight
  pattern of small leaves over every large panel;
- HUD: the hotbar as a bar with rounded ends;
- mods without a GUI Revision compat: their vanilla-grey screens, read from
  the jars cached by `npm run check-fr`, take the same colours, their own
  drawings keep theirs;
- Questlog: its journal is drawn by questlog.py in the same style.

Authoring tool, not run by CI: needs Python 3 and Pillow, and the jars in
.cache/jars (run `npm run check-fr` once first).
    python3 scripts/interface/build.py
"""
import hashlib
import io
import json
import re
import shutil
import urllib.request
import zipfile
from collections import Counter
from pathlib import Path

from PIL import Image

import questlog

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "pack" / "resourcepacks" / "fallow-interface"
CACHE = ROOT / ".cache" / "interface"
JARS = ROOT / ".cache" / "jars"

SOURCES = {
    "gui-revision": ("https://cdn.modrinth.com/data/orgtdk9U/versions/20jHoV4H/GUIRevision_2.2.0.zip",
                     "a25dbde8f0b834832ed532bd37e1caef759b81e9"),
    "gui-revision-compats": ("https://cdn.modrinth.com/data/STMnY6ly/versions/tZn6yME1/GUI%20Revision%20Compats.zip",
                             "3b271708462e750579eff55c86509bd76a8c1997"),
    "hollow-gui": ("https://cdn.modrinth.com/data/P3pQmBmF/versions/x0WWXDjf/HollowGUI.zip",
                   "77951b6430bb5e14c2ad7d97d8e3ed78205dae09"),
}

# Compats kept: the mods of the pack that GUI Revision Compats covers.
COMPAT_NAMESPACES = {
    "amendments", "chipped", "cold_sweat", "curios", "emi", "entity_features", "exposure",
    "farm_and_charm", "immersiveenchanting", "jei", "naturalist", "overgeared", "quark",
    "supplementaries",
}
# Mods whose own screens are recoloured when they use vanilla's greys.
# Config libraries, books and mods with a style of their own (Create and its
# addons) are left alone.
RECOLOUR_MODS = {
    "adorablehamsterpets", "alexsmobs", "ancient_remnants", "antique_atlas", "antique_transport",
    "companions", "crittersandcompanions",
    "eclipticseasons", "envelope", "exposure_space", "icys-better-horses", "kaleidoscope_cookery",
    "lootr", "mowziesmobs", "nomansland", "thirst", "treechop", "longwings",
}

# GUI Revision's palette -> Fallow (outline, darks, sage slots, mist panels, bone light).
LUT = {
    (28, 28, 31): (26, 31, 28),
    (54, 56, 56): (52, 60, 53),
    (85, 89, 89): (82, 92, 82),
    (98, 102, 102): (94, 104, 93),
    (100, 107, 107): (96, 108, 95),
    (109, 115, 115): (106, 117, 104),
    (125, 130, 130): (122, 131, 114),
    (131, 139, 139): (130, 140, 122),
    (162, 166, 166): (166, 170, 150),
    (193, 198, 198): (203, 202, 182),
    (218, 231, 230): (232, 229, 208),
}
FILL = LUT[(193, 198, 198)]
# Vanilla container greys -> Fallow, for the mods without a compat.
VANILLA = {
    (198, 198, 198): FILL,
    (139, 139, 139): LUT[(131, 139, 139)],
    (55, 55, 55): LUT[(54, 56, 56)],
    (85, 85, 85): LUT[(85, 89, 89)],
}
VANILLA_EDGES = {(255, 255, 255): LUT[(218, 231, 230)], (0, 0, 0): LUT[(28, 28, 31)]}

# frieze colours, one step from the panel
SOFT = (188, 188, 167)
ACCENT = (140, 149, 130)
# Background pattern: one small leaf, repeated tone on tone over the whole panel
# on a tight staggered grid, like the beans of a coffee sack.
LEAF_TONE = (195, 194, 173)
LEAF = [
    ".....##",
    "...####",
    "..###.#",
    ".##.##.",
    ".#.###.",
    ".####..",
    "#......",
]
LEAF_PITCH = (10, 8)

# Slots: a rim dark at the top and left that fades towards the ends, the rim's
# shadow cast on the top of the well, a light lip at the bottom and right.
RIM = [(58, 68, 58), (74, 84, 72), (90, 100, 88)]
SHADE = [(100, 111, 98), (110, 120, 104), (120, 130, 114)]
SLOT_FILL = (130, 140, 122)
LIP = (224, 222, 202)
LIP_DARK = (214, 212, 192)
# deeper wells behind the player and the mounts, crossed by a fine grid
WELL_FILL = (112, 122, 106)
WELL_GRID = (120, 130, 113)

# The arrows between ingredients and result, engraved; the progress arrow in
# the Questlog's lantern light.
ARROW = [
    "..............#.......",
    "..............##......",
    "...............###....",
    ".#..............####..",
    "######################",
    "######################",
    ".#..............####..",
    "...............###....",
    "..............##......",
    "..............#.......",
]
# Vanilla's wide arrow, as mods copy it in slot grey: found and replaced by ours.
VANILLA_ARROW = [
    "..............#.......",
    "..............##......",
    "..............###.....",
    "..............####....",
    "..............#####...",
    "..............######..",
    "#####################.",
    "######################",
    "#####################.",
    "..............######..",
    "..............#####...",
    "..............####....",
    "..............###.....",
    "..............##......",
    "..............#.......",
]
ARROW_INK = (106, 117, 104)
ARROW_LIGHT = (232, 229, 208)
LANTERN_D = (168, 106, 44)
LANTERN = (226, 166, 78)
LANTERN_L = (248, 214, 140)
LANTERN_EDGE = (110, 70, 34)

# Vanilla screens GUI Revision paints in colours (wood, purple): brought back to
# its greys first so that they take Fallow's palette like the others.
GREYED = {"crafting_table.png", "loom.png", "cartography_table.png", "enchanting_table.png", "shulker_box.png"}
# Compat screens painted in colours: their own tones mapped to GUI Revision's greys,
# the rest greyed by brightness; slot cells given by their corner and top colours;
# `keep` boxes hold the mod's own drawings, left in their colours.
GREYED_COMPATS = {
    "assets/chipped/textures/gui/container/workbench.png": {
        "fixed": {
            (188, 152, 98): (193, 198, 198), (210, 183, 130): (218, 231, 230),
            (159, 127, 79): (162, 166, 166), (144, 113, 66): (131, 139, 139),
            (132, 103, 57): (109, 115, 115), (113, 88, 48): (85, 89, 89),
        },
        "cells": ((144, 113, 66), (113, 88, 48)),
        "keep": [(0, 147, 76, 256)],  # the tool rack
    },
    # the backpack's leather pocket becomes a darker mist pocket with bone stitches
    "assets/quark/textures/misc/backpack_gui.png": {
        "fixed": {(135, 63, 33): (162, 166, 166), (196, 92, 48): (218, 231, 230), (87, 41, 22): (85, 89, 89)},
        "cells": ((135, 63, 33), (87, 41, 22)),
    },
}
# Screens whose title GUI Revision draws on a plate over the top edge.
PLATED = {"furnace.png", "blast_furnace.png", "smoker.png", "crafting_table.png", "enchanting_table.png"}
# The traders' banner ("TRADES") and Supplementaries' "TRADE OFFER" box.
BANNERED = {"villager.png", "red_merchant.png", "orange_trader1.png"}
OFFER = {"red_merchant.png", "orange_trader1.png"}
# Slots that are not drawn as GUI Revision slots: item corner (x, y).
EXTRA_SLOTS = {
    "crafting_table.png": [(30 + 18 * c, 17 + 18 * r) for r in range(3) for c in range(3)] + [(124, 35)],
    "shulker_box.png": [(8 + 18 * c, 18 + 18 * r) for r in range(3) for c in range(9)],
    "loom.png": [(13, 26), (33, 26), (23, 45), (143, 58)],
    "cartography_table.png": [(15, 15), (15, 52), (145, 39)],
}
# Result slots, drawn round: item corner (x, y).
RESULTS = {
    "crafting_table.png": (124, 35), "inventory.png": (154, 28), "smithing.png": (98, 48),
    "furnace.png": (116, 35), "blast_furnace.png": (116, 35), "smoker.png": (116, 35),
    "stonecutter.png": (143, 33), "loom.png": (143, 58), "cartography_table.png": (145, 39),
    "grindstone.png": (129, 34), "anvil.png": (134, 47), "villager.png": (220, 37),
}
# Large wells (cell x, y, w, h): where the player and the mount stand, crossed by a grid.
WELLS = {
    "inventory.png": [(25, 7, 51, 72, "creature")],
    "horse.png": [(26, 16, 53, 55, "creature"), (79, 16, 90, 55, "square")],
}
# Small arrows GUI Revision paints in wood: the box to clear, the arrow's centre.
WOOD_ARROWS = {
    "crafting_table.png": ((102, 39, 107, 48), (104, 43)),
    "cartography_table.png": ((48, 38, 52, 46), (50, 42)),
}

# Container titles GUI Revision hides (its own en_us.json); hidden in French too.
HIDDEN_TITLES = [
    "container.inventory", "container.crafting", "container.enchant", "container.repair",
    "container.brewing", "container.loom", "container.grindstone_title", "container.cartography_table",
    "container.stonecutter", "container.upgrade", "container.dispenser", "container.shulkerBox",
    "container.hopper", "container.furnace", "container.smoker", "container.blast_furnace",
    "container.dropper", "container.chest", "container.chestDouble", "merchant.trades",
    "container.enderchest",
]


# ---------------------------------------------------------------- sources

def fetch(name):
    url, sha1 = SOURCES[name]
    path = CACHE / f"{name}.zip"
    if not path.exists() or hashlib.sha1(path.read_bytes()).hexdigest() != sha1:
        CACHE.mkdir(parents=True, exist_ok=True)
        data = urllib.request.urlopen(url).read()
        if hashlib.sha1(data).hexdigest() != sha1:
            raise SystemExit(f"{name}: checksum mismatch")
        path.write_bytes(data)
    return zipfile.ZipFile(path)


def lum(p):
    return 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]


def png(data):
    return Image.open(io.BytesIO(data)).convert("RGBA")


# ---------------------------------------------------------------- recolour

def recolour(im):
    px = im.load()
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            if a and (r, g, b) in LUT:
                px[x, y] = LUT[(r, g, b)] + (a,)
    return im


def recolour_vanilla(im):
    """Recolours vanilla's container greys; white and black only on panel edges."""
    src = im.copy().load()
    px = im.load()
    changed = False
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = src[x, y]
            if not a:
                continue
            if (r, g, b) in VANILLA:
                px[x, y] = VANILLA[(r, g, b)] + (a,)
                changed = True
            elif (r, g, b) in VANILLA_EDGES:
                near = [src[x + dx, y + dy][:3] for dx in (-1, 0, 1) for dy in (-1, 0, 1)
                        if 0 <= x + dx < im.width and 0 <= y + dy < im.height]
                if any(n in VANILLA for n in near):
                    px[x, y] = VANILLA_EDGES[(r, g, b)] + (a,)
                    changed = True
    if changed:
        # other flat greys of the screen follow the panel's tones, but only plain
        # rectangles (wells, frames, buttons): a mod's drawings keep their colours
        for region in flat_regions(im, src):
            for x, y in region:
                r, g, b, a = src[x, y]
                px[x, y] = tone(lum((r, g, b))) + (a,)
    return changed


def flat_regions(im, src):
    """Areas of one neutral grey, other than vanilla's, that nearly fill their bounding box."""
    seen = set()
    for y in range(im.height):
        for x in range(im.width):
            p = src[x, y]
            if (x, y) in seen or not p[3] or max(p[:3]) - min(p[:3]) > 3 or p[:3] in VANILLA \
                    or p[:3] in VANILLA_EDGES:
                continue
            region, todo = [], [(x, y)]
            seen.add((x, y))
            while todo:
                i, j = todo.pop()
                region.append((i, j))
                for k, l in ((i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)):
                    if 0 <= k < im.width and 0 <= l < im.height and (k, l) not in seen and src[k, l] == p:
                        seen.add((k, l))
                        todo.append((k, l))
            xs = [i for i, _ in region]
            ys = [j for _, j in region]
            w, h = max(xs) - min(xs) + 1, max(ys) - min(ys) + 1
            if len(region) >= 24 and min(w, h) >= 3 and len(region) >= 0.85 * w * h:
                yield region


def tone(v):
    """Fallow colour for a grey of luminance v, along GUI Revision's ramp."""
    ramp = sorted((lum(k), c) for k, c in LUT.items())
    if v <= ramp[0][0]:
        return ramp[0][1]
    for (l0, c0), (l1, c1) in zip(ramp, ramp[1:]):
        if v <= l1:
            t = (v - l0) / (l1 - l0)
            return tuple(round(a + (b - a) * t) for a, b in zip(c0, c1))
    return ramp[-1][1]


# ---------------------------------------------------------------- titles

def clear_plate(im):
    """Removes the title plate drawn over the top edge: each of its columns takes
    the column of the panel it hides (or the mirror of a frame running under it)."""
    px = im.load()
    span = [x for x in range(176) if px[x, 0][3]]
    if not span:
        return
    span = range(span[0] - 3, span[-1] + 4)
    ref = 20  # a plain column of the top edge
    rows, probe = range(0, 15), 15

    def plain(c):
        return px[c, probe] == px[ref, probe]

    src = {}
    for x in span:
        if plain(x):
            src[x] = ref
            continue
        g0 = g1 = x
        while g0 > 2 and not plain(g0 - 1):
            g0 -= 1
        while g1 < 173 and not plain(g1 + 1):
            g1 += 1
        m = g0 + g1 - x
        if m not in span:
            src[x] = m
        else:
            inner = [c for c in range(g0 + 3, g1 - 2) if c not in span]
            src[x] = min(inner, key=lambda c: abs(c - x)) if inner else ref
    def column(c):
        # a frame's own rows (the run touching the probe row) over the plain top edge
        top = probe
        while top > probe - 4 and px[c, top - 1] != px[ref, top - 1]:
            top -= 1
        return [px[c if y >= top else ref, y] for y in rows]

    cols = {x: column(c) for x, c in src.items()}
    for x, col in cols.items():
        for y, p in zip(rows, col):
            px[x, y] = p


def clear_banner(im, name):
    """Removes the traders' "TRADES" banner, and Supplementaries' "TRADE OFFER" box."""
    px = im.load()
    for y in range(3, 18):
        for x in range(3, 99):
            px[x, y] = px[x, 20]
    if name in OFFER:
        for y in range(4, 24):
            for x in range(120, 249):
                px[x, y] = (193, 198, 198, 255)


# ---------------------------------------------------------------- arrows

def template(im, box, bg):
    px = im.load()
    return {(x - box[0], y - box[1]): px[x, y][:3] for y in range(box[1], box[3])
            for x in range(box[0], box[2]) if px[x, y][3] and px[x, y][:3] != bg}


def matches(im, tpl):
    px = im.load()
    width, height = im.size
    k0 = min(tpl)
    found = []
    for y in range(height):
        for x in range(width):
            if not px[x, y][3] or px[x, y][:3] != tpl[k0]:
                continue
            ox, oy = x - k0[0], y - k0[1]
            if all(0 <= ox + dx < width and 0 <= oy + dy < height and px[ox + dx, oy + dy][3]
                   and px[ox + dx, oy + dy][:3] == c for (dx, dy), c in tpl.items()):
                found.append((ox, oy))
    return found


def draw_arrow(im, x0, y0, ink, light, top=None):
    """Fallow's arrow, 22x10, its tip to the right."""
    px = im.load()
    pts = {(x, y) for y, row in enumerate(ARROW) for x, c in enumerate(row) if c == "#"}
    for x, y in pts:
        if (x, y + 1) not in pts and light:
            px[x0 + x, y0 + y + 1] = light + (255,)
    for x, y in pts:
        px[x0 + x, y0 + y] = (top if top and (x, y - 1) not in pts else ink) + (255,)


def progress_arrow():
    """The 24x16 progress sprite of furnaces: the arrow in lantern light."""
    im = Image.new("RGBA", (24, 16))
    draw_arrow(im, 1, 3, LANTERN, LANTERN_D, LANTERN_L)
    return im


def replace_arrows(im, arrows):
    """GUI Revision's arrows (a wide one, a small triangle) and the progress arrow
    stored next to them become Fallow's arrow; returns where they were."""
    px = im.load()
    big, small, filled = arrows
    out = []
    for tpl in (big, small):
        for ox, oy in matches(im, tpl):
            for dx, dy in tpl:
                px[ox + dx, oy + dy] = (193, 198, 198, 255)
            if tpl is big:
                x0, y0 = ox - 4, oy + 1  # same place as the progress sprite's arrow
            else:
                x0, y0 = ox + 2 - 11, oy + 4 - 4
            out.append((x0, y0))
    for ox, oy in matches(im, filled):
        for dx, dy in filled:
            px[ox + dx, oy + dy] = (0, 0, 0, 0)
        im.alpha_composite(progress_arrow(), (ox, oy))
    return out


# ---------------------------------------------------------------- details

def frieze_mask(hollow):
    """Hollow GUI's top frieze (rows 2-12 of its chest), s soft, a accent."""
    im = png(hollow.read("assets/minecraft/textures/gui/container/generic_54.png"))
    px = im.load()
    mask = {}
    for y in range(2, 13):
        for x in range(3, 173):
            p = px[x, y][:3]
            if p == (76, 99, 122):
                mask[(x, y)] = SOFT
            elif p == (246, 247, 248):
                mask[(x, y)] = ACCENT
    return mask


def frieze_top(im):
    """First row of a 176-wide panel, or None."""
    px = im.load()
    w = min(im.width, 180)
    return next((y for y in range(min(im.height, 40))
                 if 168 <= sum(px[x, y][3] > 0 for x in range(w)) <= 178), None)


def add_frieze(im, mask):
    """Along the top of a 176-wide panel, behind the slots."""
    px = im.load()
    top = frieze_top(im)
    if top is None:
        return
    for (x, y), c in mask.items():
        if px[x, y + top][:3] == FILL:
            px[x, y + top] = c + (255,)


def frieze_band(im, vanilla):
    top = frieze_top(im) if vanilla else None
    return [] if top is None else [(0, top, 176, top + 14)]


def add_pattern(im, skip=()):
    """The leaf repeated over every large stretch of bare panel, at the same spacing
    everywhere; slots, wells and the frieze stay clear."""
    px = im.load()
    width, height = im.size
    seen = set()
    pw, ph = LEAF_PITCH
    for y in range(height):
        for x in range(width):
            if (x, y) in seen or px[x, y] != FILL + (255,):
                continue
            region, todo = [], [(x, y)]
            seen.add((x, y))
            while todo:
                i, j = todo.pop()
                region.append((i, j))
                for k, l in ((i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)):
                    if 0 <= k < width and 0 <= l < height and (k, l) not in seen and px[k, l] == FILL + (255,):
                        seen.add((k, l))
                        todo.append((k, l))
            if len(region) < 300:
                continue
            for i, j in region:
                if any(a <= i < c and b <= j < d for a, b, c, d in skip):
                    continue
                row = j // ph
                u = (i - (pw // 2 if row % 2 else 0)) % pw
                v = j % ph
                if v < len(LEAF) and u < len(LEAF[0]) and LEAF[v][u] == "#":
                    px[i, j] = LEAF_TONE + (255,)


# ---------------------------------------------------------------- slots

def greyify(im, fixed=None, rare=0, keep=()):
    """Maps a coloured screen onto GUI Revision's greys, its main colour to the panel grey.

    `fixed` maps a screen's own tones (its panel first) to given greys; otherwise the
    panel colour is read in the left margin. Colours seen fewer than `rare` times are
    icons and keep their colour, as does everything in the `keep` boxes."""
    greys = [g for g in LUT if g not in ((100, 107, 107), (125, 130, 130))]
    levels = [lum(g) for g in greys]
    px = im.load()
    count = Counter(p[:3] for p in im.get_flattened_data() if p[3])
    margin = Counter(px[x, y][:3] for x in range(3, 7) for y in range(20, 60) if px[x, y][3])
    fixed = fixed or {}
    panel = next(iter(fixed), None) or margin.most_common(1)[0][0]
    scale = lum(fixed.get(panel, (193, 198, 198))) / lum(panel)
    for y in range(im.height):
        for x in range(im.width):
            p = px[x, y]
            if any(a <= x < c and b <= y < d for a, b, c, d in keep):
                continue
            if p[3] and p[:3] in fixed:
                px[x, y] = fixed[p[:3]] + (p[3],)
                continue
            if not p[3] or p[:3] in greys or count[p[:3]] < rare:
                continue
            v = lum(p) * scale
            px[x, y] = greys[min(range(len(levels)), key=lambda k: abs(levels[k] - v))] + (p[3],)


def find_cells(im, corner, top):
    """16x16 slot cells whose top row is a corner, fourteen top pixels and a corner."""
    px = im.load()
    found = []
    for y in range(im.height - 15):
        for x in range(im.width - 15):
            if (px[x, y][:3] == corner and px[x + 15, y][:3] == corner
                    and all(px[x + i, y][:3] == top for i in range(1, 15))):
                found.append((x, y, 16, 16))
    return found


def find_slots(im):
    """GUI Revision slots: a dark top run between slot-grey corners, light bottom corners."""
    px = im.load()
    width, height = im.size
    dark, slot, side, low = (85, 89, 89), (131, 139, 139), (109, 115, 115), (162, 166, 166)
    found = []
    for y in range(height - 2):
        for x in range(width - 2):
            if px[x, y][:3] != slot or px[x + 1, y][:3] != dark:
                continue
            w = 1  # a placeholder icon may cross the dark run in the side grey
            while x + w < width and px[x + w, y][:3] in (dark, side):
                w += 1
            if x + w >= width or px[x + w, y][:3] != slot or w < 7:
                continue
            w += 1
            h = 1
            while y + h < height and px[x, y + h][:3] in (side, slot):
                h += 1
            if y + h >= height or px[x, y + h][:3] != low or h < 7:
                continue
            h += 1
            if px[x + w - 1, y + h - 1][:3] == low:
                found.append((x, y, w, h))
    return found


def find_vanilla_slots(im):
    """Vanilla slots: 18x18 (26x26 for outputs), dark top-left edge, white bottom-right edge;
    also flat 16x16 slot-grey cells drawn in a grid without those edges."""
    px = im.load()
    width, height = im.size
    dark, slot, white = (55, 55, 55), (139, 139, 139), (255, 255, 255)
    found = []
    for n in (18, 26):
        for y in range(height - n + 1):
            for x in range(width - n + 1):
                if (px[x, y][:3] == dark and px[x + n - 2, y][:3] == dark and px[x + n - 1, y][:3] == slot
                        and px[x, y + n - 2][:3] == dark and px[x, y + n - 1][:3] == slot
                        and px[x + n - 1, y + n - 1][:3] == white and px[x + 1, y + 1][:3] == slot
                        and all(px[x + i, y][:3] == dark for i in range(n - 1))):
                    found.append((x, y, n, n))

    def taken(x, y):
        return any(a <= x < a + w and b <= y < b + h for a, b, w, h in found)

    flat = []
    for y in range(1, height - 16):
        for x in range(1, width - 16):
            if (px[x - 1, y][:3] != slot and px[x, y - 1][:3] != slot and px[x + 16, y][:3] != slot
                    and px[x, y + 16][:3] != slot and not taken(x, y)
                    and all(px[x + i, y + j][:3] == slot for j in range(16) for i in range(16))):
                flat.append((x - 1, y - 1, 18, 18))
    return found + flat


def draw_slot(im, x, y, w, h, shape="square", around=FILL + (255,)):
    """Redraws the slot cell (x, y, w, h): a well with rounded corners ("square"), a
    disc ("round") or a deep well crossed by a grid ("creature"). Its rim is dark at
    the top and left and fades towards the ends, casts a shadow on the top of the
    well, and a light lip runs along the bottom and right. A placeholder drawn
    inside the slot (a banner, a dye, a blueprint) is kept."""
    if x < 0 or y < 0 or x + w > im.width or y + h > im.height:
        return
    px = im.load()
    before = {(i, j): px[x + i, y + j] for j in range(h) for i in range(w)}
    ground = Counter(before.values()).most_common(1)[0][0]
    r = 2 if max(w, h) <= 18 else 3 if max(w, h) <= 30 else 4

    def inside(i, j):
        if not (0 <= i < w and 0 <= j < h):
            return False
        if shape == "round":
            return (i + 0.5 - w / 2) ** 2 + (j + 0.5 - h / 2) ** 2 <= (w / 2) ** 2 - 0.3
        dx = max(r - i, i - (w - 1 - r), 0)
        dy = max(r - j, j - (h - 1 - r), 0)
        return dx * dx + dy * dy <= r * r + 0.5

    def run(i, j, di, dj):
        k = 1
        while inside(i + di * k, j + dj * k):
            k += 1
        return k

    fill = WELL_FILL if shape == "creature" else SLOT_FILL
    for j in range(h):
        for i in range(w):
            if not inside(i, j):
                px[x + i, y + j] = tuple(around)
                continue
            up, left = not inside(i, j - 1), not inside(i - 1, j)
            down, right = not inside(i, j + 1), not inside(i + 1, j)
            if up:
                c = RIM[0] if i < w - 3 else RIM[1] if i < w - 1 else RIM[2]
            elif left:
                c = RIM[0] if j < h - 5 else RIM[1] if j < h - 3 else RIM[2]
            elif right and j < 3:
                c = RIM[2]
            elif down or right:
                c = LIP_DARK if down and i < 3 else LIP
            else:
                d, dl, dr, db = run(i, j, 0, -1), run(i, j, -1, 0), run(i, j, 1, 0), run(i, j, 0, 1)
                if d == 1:
                    c = SHADE[0]
                elif d == 2:
                    c = SHADE[1]
                elif d == 3 and (dl <= 2 or dr <= 2) or db == 1 and (dl == 1 or dr == 1):
                    c = SHADE[2]
                elif shape == "creature" and ((x + i) % 6 == 0 or (y + j) % 6 == 0):
                    c = WELL_GRID
                elif before[i, j] != ground and shape != "creature" and max(w, h) <= 18:
                    c = before[i, j][:3]
                else:
                    c = fill
            px[x + i, y + j] = c + (255,)


def slot_shapes(cells, results=()):
    """Round for the results and for the hotbar row (the lowest row of nine);
    the large wells of a screen keep their square shape."""
    rows = Counter(y for x, y, w, h in cells if w == 18)
    hotbar = max((y for y, n in rows.items() if n >= 9), default=None)
    shapes = []
    for x, y, w, h in cells:
        if (x + 1, y + 1) in results or 22 <= w <= 30 and w == h or y == hotbar and w == 18:
            shapes.append("round")
        else:
            shapes.append("square")
    return shapes


def find_black_wells(im):
    """Black rectangles of vanilla-style screens where a creature is shown."""
    px = im.load()
    seen = set()
    wells = []
    for y in range(im.height):
        for x in range(im.width):
            if (x, y) in seen or px[x, y] != (0, 0, 0, 255):
                continue
            region, todo = 0, [(x, y)]
            seen.add((x, y))
            x0 = x1 = x
            y0 = y1 = y
            while todo:
                i, j = todo.pop()
                region += 1
                x0, x1, y0, y1 = min(x0, i), max(x1, i), min(y0, j), max(y1, j)
                for k, l in ((i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)):
                    if 0 <= k < im.width and 0 <= l < im.height and (k, l) not in seen and px[k, l] == (0, 0, 0, 255):
                        seen.add((k, l))
                        todo.append((k, l))
            w, h = x1 - x0 + 1, y1 - y0 + 1
            if min(w, h) >= 30 and region >= 0.95 * w * h:
                wells.append((x0 - 1, y0 - 1, w + 2, h + 2))  # with its bevel
    return wells


def rounded(w, h, r):
    """Mask of a rectangle with corners of radius r."""
    def inside(i, j):
        dx = max(r - i, i - (w - 1 - r), 0)
        dy = max(r - j, j - (h - 1 - r), 0)
        return 0 <= i < w and 0 <= j < h and dx * dx + dy * dy <= r * r + 0.5
    return inside


def draw_bar(im, x0, y0, w, h):
    """A mist bar with rounded ends, moss outline and a soft shadow under it."""
    px = im.load()
    inside = rounded(w, h, 5)
    for j in range(h + 1):
        for i in range(w):
            if inside(i, j):
                edge = any(not inside(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                c = RIM[0] if edge else (232, 229, 208) if not inside(i, j - 2) else \
                    (166, 170, 150) if not inside(i, j + 2) else FILL
                px[x0 + i, y0 + j] = c + (255,)
            elif inside(i, j - 1):
                px[x0 + i, y0 + j] = RIM[0] + (110,)


def draw_hud(hud):
    """The hotbar as a bar with rounded ends and rounded slots; the selection and
    the offhand slot to match, in the Questlog's lantern light."""
    bar = Image.new("RGBA", (182, 22))
    draw_bar(bar, 0, 0, 182, 21)
    for i in range(9):
        draw_slot(bar, 2 + 20 * i, 2, 18, 18, "square")
    bar.save(hud / "hotbar.png")

    sel = Image.new("RGBA", (24, 23))
    px = sel.load()
    outer, inner = rounded(24, 23, 5), rounded(18, 18, 2)
    for j in range(23):
        for i in range(24):
            if not outer(i, j) or inner(i - 3, j - 3):
                continue
            edge = any(not outer(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            ring = any(inner(i - 3 + a, j - 3 + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            c = LANTERN_EDGE if edge else LANTERN_L if ring and j < 12 else LANTERN_D if ring else LANTERN
            px[i, j] = c + (255,)
    sel.save(hud / "hotbar_selection.png")

    for name, x0 in (("hotbar_offhand_left.png", 0), ("hotbar_offhand_right.png", 7)):
        im = Image.new("RGBA", (29, 24))
        draw_bar(im, x0, 1, 22, 21)
        draw_slot(im, x0 + 2, 3, 18, 18, "square")
        im.save(hud / name)


def big_panel(path, im):
    if "/sprites/" in path and "/sprites/container/" not in path:
        return False
    px = im.load()
    return sum(1 for y in range(0, im.height, 2) for x in range(0, im.width, 2)
               if px[x, y][:3] == FILL and px[x, y][3]) * 4 >= 1500


# ---------------------------------------------------------------- build

def main():
    rev = fetch("gui-revision")
    compats = fetch("gui-revision-compats")
    hollow = fetch("hollow-gui")
    mask = frieze_mask(hollow)

    files = {}
    for n in rev.namelist():
        if n.startswith("assets/minecraft/") and not n.endswith("/") and "/lang/" not in n:
            files[n] = rev.read(n)
    for prefix in ("", "overlay_34/"):
        for n in compats.namelist():
            m = re.match(prefix + r"assets/([^/]+)/(.+)$", n)
            if m and not n.endswith("/") and m.group(1) in COMPAT_NAMESPACES:
                files[f"assets/{m.group(1)}/{m.group(2)}"] = compats.read(n)

    container = "assets/minecraft/textures/gui/container/"
    furnace = png(files[container + "furnace.png"])
    anvil = png(files[container + "anvil.png"])
    progress = png(files["assets/minecraft/textures/gui/sprites/container/furnace/burn_progress.png"])
    grey = (193, 198, 198)
    arrows = (template(furnace, (84, 36, 99, 49), grey), template(anvil, (110, 50, 115, 59), grey),
              template(progress, (0, 0, 24, 16), None))

    if OUT.exists():
        shutil.rmtree(OUT / "assets", ignore_errors=True)
    OUT.mkdir(parents=True, exist_ok=True)

    for path, data in sorted(files.items()):
        dest = OUT / path
        dest.parent.mkdir(parents=True, exist_ok=True)
        if not path.endswith(".png"):
            dest.write_bytes(data)
            continue
        im = png(data)
        name = path.rsplit("/", 1)[1]
        vanilla = path.startswith(container)
        if vanilla and name in PLATED:
            clear_plate(im)
        if (vanilla or path.startswith("assets/supplementaries/textures/gui/")) and name in BANNERED:
            clear_banner(im, name)
        spec = GREYED_COMPATS.get(path, {})
        cells = find_cells(im, *spec["cells"]) if "cells" in spec else []
        if (vanilla and name in GREYED) or spec:
            greyify(im, spec.get("fixed"), rare=30 if spec else 0, keep=spec.get("keep", ()))
        if name == "anvil.png":  # GUI Revision's red mark under the name field
            px = im.load()
            for y in range(im.height):
                for x in range(im.width):
                    if px[x, y] == (255, 0, 0, 255):
                        px[x, y] = grey + (255,)
        slots = [(x - 1, y - 1, w + 2, h + 2) for x, y, w, h in find_slots(im) + cells]
        if vanilla:
            slots += [(x - 1, y - 1, 18, 18) for x, y in EXTRA_SLOTS.get(name, [])]
        drawn = replace_arrows(im, arrows) if "/gui/" in path or "/misc/" in path else []
        if vanilla and name in WOOD_ARROWS:
            (a, b, c, d), (cx, cy) = WOOD_ARROWS[name]
            px = im.load()
            for y in range(b, d):
                for x in range(a, c):
                    px[x, y] = px[a - 2, y]
            drawn.append((cx - 11, cy - 4))
        recolour(im)
        for x0, y0 in drawn:
            draw_arrow(im, x0, y0, ARROW_INK, ARROW_LIGHT)
        if vanilla:
            add_frieze(im, mask)
        shapes = slot_shapes(slots, [RESULTS[name]] if vanilla and name in RESULTS else [])
        for (x, y, w, h), shape in zip(slots, shapes):
            draw_slot(im, x, y, w, h, "square" if min(w, h) >= 30 else shape)
        if vanilla:
            for x, y, w, h, shape in WELLS.get(name, []):
                draw_slot(im, x, y, w, h, shape)
                slots.append((x, y, w, h))
        if big_panel(path, im):
            add_pattern(im, [(x, y, x + w, y + h) for x, y, w, h in slots] + frieze_band(im, vanilla))
        im.save(dest)

    # vanilla-grey screens of the mods without a compat
    for jar in sorted(JARS.glob("*.jar")):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                m = re.match(r"assets/([^/]+)/textures/gui/(.+\.png)$", n)
                if not m or m.group(1) not in RECOLOUR_MODS:
                    continue
                im = png(z.read(n))
                slots = find_vanilla_slots(im)
                wells = find_black_wells(im)
                tpl = {(x, y): (139, 139, 139) for y, row in enumerate(VANILLA_ARROW)
                       for x, c in enumerate(row) if c == "#"}
                drawn = []
                for ox, oy in matches(im, tpl):
                    for dx, dy in tpl:
                        im.putpixel((ox + dx, oy + dy), (198, 198, 198, 255))
                    drawn.append((ox, oy + 3))
                if recolour_vanilla(im):
                    for x0, y0 in drawn:
                        draw_arrow(im, x0, y0, ARROW_INK, ARROW_LIGHT)
                    px = im.load()
                    shapes = slot_shapes(slots)
                    for (x, y, w, h), shape in zip(slots, shapes):
                        clear = x == 0 or px[x - 1, y + h // 2][3] == 0
                        draw_slot(im, x, y, w, h, shape, (0, 0, 0, 0) if clear else FILL + (255,))
                    for x, y, w, h in wells:
                        draw_slot(im, x, y, w, h, "creature")
                    if big_panel(n, im):
                        add_pattern(im, [(x, y, x + w, y + h) for x, y, w, h in slots + wells])
                    dest = OUT / n
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    im.save(dest)

    lang = OUT / "assets" / "minecraft" / "lang"
    lang.mkdir(parents=True, exist_ok=True)
    blank = {k: " " for k in HIDDEN_TITLES}
    for code in ("fr_fr", "en_us"):
        (lang / f"{code}.json").write_text(json.dumps(blank, indent=2, ensure_ascii=False) + "\n")

    draw_hud(OUT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud")

    questlog.draw(OUT)
    for f in (OUT / "assets" / "questlog" / "textures" / "gui").glob("*.png"):
        im = Image.open(f).convert("RGBA")
        add_pattern(im)
        im.save(f)


if __name__ == "__main__":
    main()
