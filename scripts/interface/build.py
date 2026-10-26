"""Builds the fallow-interface resource pack from free-licensed packs.

Base: GUI Revision by Vixel and GUI Revision Compats by Fyoncle (both
CC BY-NC-SA 4.0), with the top frieze of Hollow GUI by Melee (CC BY 4.0).
The script downloads the pinned releases, keeps the files that concern
vanilla and the mods of the pack, and adapts them to Fallow:

- recolour: GUI Revision's greys become Fallow's mist, sage and moss;
- titles: the titles drawn in the textures are redrawn in French with
  the letters of GUI Revision's own plates;
- screens GUI Revision paints in wood or purple are greyed first, so that
  they take the same palette;
- slots: redrawn as rounded, outlined wells, the hotbar's cells too;
- details: Hollow GUI's frieze along the top of vanilla panels and a
  regular pattern of small leaves on every large panel;
- mods without a GUI Revision compat: their vanilla-grey screens, read from
  the jars cached by `npm run check-fr`, take the same colours;
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
# Background pattern: one leaf with its midrib, repeated on a staggered grid,
# tone on tone like the beans of a coffee sack.
LEAF_TONE = (194, 193, 172)
LEAF = [
    ".....dd",
    "...dddd",
    "..ddd.d",
    ".dd.dd.",
    ".d.ddd.",
    ".dddd..",
    "d......",
]
LEAF_PITCH = 12

# Rounded slots: outline, sunk top-left, lit bottom-right.
SLOT_EDGE = (66, 76, 66)
SLOT_SHADOW = (106, 117, 104)
SLOT_SHADOW_2 = (118, 128, 112)
SLOT_FILL = (130, 140, 122)
SLOT_RIGHT = (140, 149, 130)
SLOT_LIGHT = (152, 160, 140)
ROUND = {2: [2, 1], 3: [3, 1, 1]}

# Vanilla screens GUI Revision paints in colours (wood, purple): brought back to
# its greys first so that they take Fallow's palette like the others.
GREYED = {"crafting_table.png", "loom.png", "cartography_table.png", "enchanting_table.png", "shulker_box.png"}
# Compat screens painted in colours: their own tones mapped to GUI Revision's greys,
# the rest greyed by brightness; slot cells given by their corner and top colours.
GREYED_COMPATS = {
    "assets/chipped/textures/gui/container/workbench.png": {
        "fixed": {
            (188, 152, 98): (193, 198, 198), (210, 183, 130): (218, 231, 230),
            (159, 127, 79): (162, 166, 166), (144, 113, 66): (131, 139, 139),
            (132, 103, 57): (109, 115, 115), (113, 88, 48): (85, 89, 89),
        },
        "cells": ((144, 113, 66), (113, 88, 48)),
    },
    # the backpack's leather pocket becomes a darker mist pocket with bone stitches
    "assets/quark/textures/misc/backpack_gui.png": {
        "fixed": {(135, 63, 33): (162, 166, 166), (196, 92, 48): (218, 231, 230), (87, 41, 22): (85, 89, 89)},
        "cells": ((135, 63, 33), (87, 41, 22)),
    },
}
# The traders' crimson and gold banner, in moss and bone.
BANNER = {
    (71, 8, 30): (38, 50, 37), (84, 10, 36): (46, 60, 44), (92, 11, 41): (54, 70, 51),
    (111, 12, 46): (66, 84, 62), (133, 15, 50): (80, 100, 74),
    (106, 60, 11): (122, 120, 96), (166, 105, 22): (176, 172, 146),
    (227, 167, 43): (214, 210, 186), (255, 225, 122): (240, 236, 214),
}
# The gold frame of the selected hotbar slot and of the offhand slot, in the
# Questlog's lantern light.
LANTERN = {
    (37, 34, 14): (58, 42, 26), (74, 68, 28): (110, 70, 34), (122, 108, 42): (168, 106, 44),
    (162, 133, 52): (198, 136, 60), (210, 161, 55): (226, 166, 78), (236, 179, 9): (236, 186, 104),
    (255, 207, 87): (248, 214, 140),
}
# Slots that are not drawn as GUI Revision slots: item corner (x, y) of 16x16 areas.
EXTRA_SLOTS = {
    "crafting_table.png": [(30 + 18 * c, 17 + 18 * r) for r in range(3) for c in range(3)] + [(124, 35)],
    "shulker_box.png": [(8 + 18 * c, 18 + 18 * r) for r in range(3) for c in range(9)],
    "loom.png": [(13, 26), (33, 26), (23, 45), (143, 58)],
    "cartography_table.png": [(15, 15), (15, 52), (145, 39)],
}

# Titles drawn in GUI Revision's textures: French word, plate span, text rows.
TITLES = {
    "furnace.png": ("FOURNEAU", (56, 120), (4, 10)),
    "blast_furnace.png": ("HAUT FOURNEAU", (48, 129), (4, 10)),
    "smoker.png": ("FUMOIR", (59, 118), (4, 10)),
    "crafting_table.png": ("ÉTABLI", (50, 128), (4, 10)),
    "enchanting_table.png": ("ENCHANTEMENT", (44, 132), (3, 9)),
}
GLYPH_SOURCES = [
    ("furnace.png", "FURNACE", (56, 120), (4, 10)),
    ("blast_furnace.png", "BLAST FURNACE", (48, 129), (4, 10)),
    ("smoker.png", "SMOKER", (59, 118), (4, 10)),
    ("crafting_table.png", "CRAFTING TABLE", (50, 128), (4, 10)),
    ("enchanting_table.png", "ENCHANTING TABLE", (44, 132), (3, 9)),
]

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
        # other flat greys of the same screen (wells, frames) follow the panel's tones
        count = Counter(p[:3] for p in im.get_flattened_data() if p[3])
        for y in range(im.height):
            for x in range(im.width):
                r, g, b, a = src[x, y]
                if (a and max(r, g, b) - min(r, g, b) <= 3 and (r, g, b) not in VANILLA
                        and (r, g, b) not in VANILLA_EDGES and count[(r, g, b)] >= 30):
                    px[x, y] = tone(lum((r, g, b))) + (a,)
    return changed


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

def extract(im, word, box, rows):
    """Cuts the letters of a plate into glyphs of roles d (stroke), m, l (light)."""
    px = im.load()
    x0, x1 = box
    bg = Counter(px[x, rows[0] + 2] for x in range(x0, x1)).most_common(1)[0][0]
    base = lum(bg)

    def role(p):
        if p[3] == 0 or p == bg:
            return None
        v = lum(p)
        return "d" if v < base - 45 else "m" if v < base - 4 else "l"

    glyphs, cur = [], []
    for x in range(x0, x1):
        col = [role(px[x, y]) for y in range(*rows)]
        if any(col):
            cur.append(col)
        elif cur:
            glyphs.append(cur)
            cur = []
    if cur:
        glyphs.append(cur)
    letters = [c for c in word if c != " "]
    if len(glyphs) != len(letters):
        raise SystemExit(f"glyphs of {word}: {len(glyphs)} for {len(letters)} letters")
    return dict(zip(letters, glyphs))


def plate_palette(im, box, rows):
    px = im.load()
    x0, x1 = box
    bg = Counter(px[x, rows[0] + 2] for x in range(x0, x1)).most_common(1)[0][0]
    base = lum(bg)
    roles = {"d": Counter(), "m": Counter(), "l": Counter()}
    for y in range(*rows):
        for x in range(x0, x1):
            p = px[x, y]
            if p == bg or p[3] == 0:
                continue
            v = lum(p)
            roles["d" if v < base - 45 else "m" if v < base - 4 else "l"][p] += 1
    pal = {r: (c.most_common(1)[0][0] if c else None) for r, c in roles.items()}
    pal["d"] = pal["d"] or pal["m"]
    pal["m"] = pal["m"] or pal["d"]
    pal["l"] = pal["l"] or tuple(min(255, c + 25) for c in bg[:3]) + (255,)
    return pal, bg


def draw_word(im, glyphs, word, cx, y0, pal):
    px = im.load()
    width = sum(3 if c == " " else len(glyphs["E" if c == "É" else c]) + 1 for c in word) - 1
    x = cx - width // 2
    for c in word:
        if c == " ":
            x += 3
            continue
        g = glyphs["E" if c == "É" else c]
        for i, col in enumerate(g):
            for j, r in enumerate(col):
                if r:
                    px[x + i, y0 + j] = pal[r]
        if c == "É":
            px[x + len(g) - 2, y0 - 1] = pal["d"]
        x += len(g) + 1


def retitle(name, im, glyphs, plate):
    """Redraws the titles of a texture in French; returns the plates' boxes."""
    px = im.load()
    boxes = []
    if name in TITLES and name in GREYED:
        # coloured plates were greyed with their panel: lay GUI Revision's plain plate instead
        span = [x for x in range(im.width) if px[x, 0][3]]
        draw_plate(im, plate, span[0], 0, span[-1] - span[0] + 1)
    if name in TITLES:
        word, box, rows = TITLES[name]
        pal, bg = plate_palette(im, box, rows)
        if name in GREYED:
            rows = (4, 10)
            pal = {"d": LUT[(85, 89, 89)] + (255,), "m": LUT[(85, 89, 89)] + (255,),
                   "l": LUT[(218, 231, 230)] + (255,)}
            bg = FILL + (255,)
        top = rows[0] - 1 if "É" in word else rows[0]
        for y in range(top, rows[1]):
            for x in range(*box):
                px[x, y] = bg
        draw_word(im, glyphs, word, (box[0] + box[1]) // 2, rows[0], pal)
        boxes.append((box[0] - 8, 0, box[1] + 8, 15))
    if name in ("villager.png", "red_merchant.png", "orange_trader1.png"):
        for y in range(24):
            for x in range(100):
                if px[x, y][:3] in BANNER:
                    px[x, y] = BANNER[px[x, y][:3]] + (255,)
        bg = BANNER[(133, 15, 50)] + (255,)
        for y in range(5, 12):
            for x in range(24, 78):
                px[x, y] = bg
        pal = {"d": BANNER[(255, 225, 122)] + (255,), "m": BANNER[(227, 167, 43)] + (255,),
               "l": BANNER[(84, 10, 36)] + (255,)}
        draw_word(im, glyphs, "ÉCHANGES", 51, 6, pal)
    if name in ("red_merchant.png", "orange_trader1.png"):
        boxes.append(offer(im, glyphs, plate))
    return boxes


def plate_template(furnace):
    """GUI Revision's furnace title plate, recoloured, emptied of its letters."""
    im = recolour(furnace.crop((53, 0, 123, 14)))
    px = im.load()
    for y in range(14):
        for x in range(im.width):
            if px[x, y][3] == 0:
                px[x, y] = FILL + (255,)
    return im


def draw_plate(im, plate, x0, y0, w):
    """GUI Revision's title plate stretched to w pixels."""
    px = im.load()
    tp = plate.load()
    for x in range(w):
        sx = x if x < 4 else plate.width - (w - x) if x >= w - 4 else 7
        for y in range(14):
            px[x0 + x, y0 + y] = tp[sx, y]


def offer(im, glyphs, plate):
    """Supplementaries' traders: the red "TRADE OFFER" box becomes a title plate "OFFRE"."""
    px = im.load()
    for y in range(4, 24):
        for x in range(120, 249):
            px[x, y] = FILL + (255,)
    word = "OFFRE"
    text = sum(len(glyphs[c]) + 1 for c in word) - 1
    w = text + 28
    x0, y0 = 183 - w // 2, 6
    draw_plate(im, plate, x0, y0, w)
    pal = {"d": LUT[(85, 89, 89)] + (255,), "m": LUT[(85, 89, 89)] + (255,), "l": LUT[(218, 231, 230)] + (255,)}
    draw_word(im, glyphs, word, x0 + w // 2, y0 + 4, pal)
    return (x0 - 2, y0 - 2, x0 + w + 2, y0 + 16)


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


def add_frieze(im, mask):
    """Along the top of a 176-wide panel, behind slots and plates."""
    px = im.load()
    w = min(im.width, 180)
    top = next((y for y in range(min(im.height, 40))
                if 168 <= sum(px[x, y][3] > 0 for x in range(w)) <= 178), None)
    if top is None:
        return
    plate = set()
    for x in range(im.width):
        if any(px[x, y][3] > 0 for y in range(top)):
            plate.update(range(x - 2, x + 3))
    for (x, y), c in mask.items():
        if x not in plate and px[x, y + top][:3] == FILL:
            px[x, y + top] = c + (255,)


def add_pattern(im, skip=()):
    """Leaves on a staggered grid, only where a whole leaf fits on bare panel."""
    px = im.load()
    h, w = len(LEAF), len(LEAF[0])
    for gy in range(-LEAF_PITCH, im.height, LEAF_PITCH):
        off = LEAF_PITCH // 2 if (gy // LEAF_PITCH) % 2 else 0
        for gx in range(-LEAF_PITCH, im.width, LEAF_PITCH):
            x0, y0 = gx + off, gy
            if x0 < 1 or y0 < 1 or x0 + w + 1 > im.width or y0 + h + 1 > im.height:
                continue
            if any(a - 1 <= x0 + w and x0 - 1 <= c and b - 1 <= y0 + h and y0 - 1 <= d
                   for a, b, c, d in skip):
                continue
            if any(px[x, y][:3] != FILL for y in range(y0 - 1, y0 + h + 1) for x in range(x0 - 1, x0 + w + 1)):
                continue
            for j, row in enumerate(LEAF):
                for i, ch in enumerate(row):
                    if ch == "d":
                        px[x0 + i, y0 + j] = LEAF_TONE + (255,)


# ---------------------------------------------------------------- slots

def greyify(im, fixed=None, rare=0):
    """Maps a coloured screen onto GUI Revision's greys, its main colour to the panel grey.

    `fixed` maps a screen's own tones (its panel first) to given greys; otherwise the
    panel colour is read in the left margin. Colours seen fewer than `rare` times are
    icons and keep their colour."""
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


def round_slot(im, x, y, w, h, around=None):
    """Redraws a slot as a rounded, outlined well; its cut corners take the colour around it.

    A placeholder drawn inside the slot (a banner or dye outline) is kept."""
    px = im.load()
    if around is None:
        around = px[x - 1, y + h // 2] if x > 0 else FILL + (255,)
    ins = ROUND[2 if min(w, h) <= 18 else 3]
    before = {(i, j): px[x + i, y + j] for j in range(h) for i in range(w)}
    ground = Counter(before.values()).most_common(1)[0][0]

    def inside(i, j):
        if not (0 <= i < w and 0 <= j < h):
            return False
        k = 0
        if j < len(ins):
            k = ins[j]
        if h - 1 - j < len(ins):
            k = max(k, ins[h - 1 - j])
        return k <= i < w - k

    def edge(i, j):
        return inside(i, j) and any(not inside(i + di, j + dj) for di, dj in ((1, 0), (-1, 0), (0, 1), (0, -1)))

    for j in range(h):
        for i in range(w):
            if not inside(i, j):
                px[x + i, y + j] = tuple(around)
                continue
            if edge(i, j):
                c = SLOT_EDGE
            elif edge(i, j - 1):
                c = SLOT_SHADOW
            elif edge(i, j - 2) or edge(i - 1, j):
                c = SLOT_SHADOW_2 if edge(i, j - 2) else SLOT_SHADOW
            elif edge(i, j + 1):
                c = SLOT_LIGHT
            elif edge(i + 1, j):
                c = SLOT_RIGHT
            else:
                c = before[i, j][:3] if before[i, j] != ground else SLOT_FILL
            px[x + i, y + j] = c + (255,)


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
    glyphs = {}
    for name, word, box, rows in GLYPH_SOURCES:
        for k, g in extract(png(files[container + name]), word, box, rows).items():
            glyphs.setdefault(k, [[("d" if r == "m" and name.startswith("enchanting") else r) for r in col]
                                  for col in g])

    plate = plate_template(png(files[container + "furnace.png"]))

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
        spec = GREYED_COMPATS.get(path, {})
        cells = find_cells(im, *spec["cells"]) if "cells" in spec else []
        if (path.startswith(container) and name in GREYED) or spec:
            greyify(im, spec.get("fixed"), rare=30 if spec else 0)
        slots = find_slots(im) + cells
        if name == "anvil.png":  # GUI Revision's red mark under the name field
            px = im.load()
            for y in range(im.height):
                for x in range(im.width):
                    if px[x, y] == (255, 0, 0, 255):
                        px[x, y] = (193, 198, 198, 255)
        recolour(im)
        plates = []
        if path.startswith(container):
            plates = retitle(name, im, glyphs, plate)
            add_frieze(im, mask)
        elif path.startswith("assets/supplementaries/textures/gui/"):
            plates = retitle(name, im, glyphs, plate)
        for x, y, w, h in slots:
            round_slot(im, x, y, w, h)
        if path.startswith(container):
            for x, y in EXTRA_SLOTS.get(name, []):
                round_slot(im, x, y, 16, 16)
        if big_panel(path, im):
            add_pattern(im, plates + [(x, y, x + w, y + h) for x, y, w, h in slots])
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
                if recolour_vanilla(im):
                    px = im.load()
                    for x, y, w, h in slots:
                        clear = x == 0 or px[x - 1, y + h // 2][3] == 0
                        round_slot(im, x, y, w, h, (0, 0, 0, 0) if clear else FILL + (255,))
                    if big_panel(n, im):
                        add_pattern(im, [(x, y, x + w, y + h) for x, y, w, h in slots])
                    dest = OUT / n
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    im.save(dest)

    lang = OUT / "assets" / "minecraft" / "lang"
    lang.mkdir(parents=True, exist_ok=True)
    blank = {k: " " for k in HIDDEN_TITLES}
    for code in ("fr_fr", "en_us"):
        (lang / f"{code}.json").write_text(json.dumps(blank, indent=2, ensure_ascii=False) + "\n")

    hud = OUT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud"
    for name in ("hotbar_selection.png", "hotbar_offhand_left.png", "hotbar_offhand_right.png"):
        im = Image.open(hud / name).convert("RGBA")
        px = im.load()
        for y in range(im.height):
            for x in range(im.width):
                if px[x, y][3] and px[x, y][:3] in LANTERN:
                    px[x, y] = LANTERN[px[x, y][:3]] + (px[x, y][3],)
        im.save(hud / name)
    # rounded hotbar cells: the frame fills their corners
    im = Image.open(hud / "hotbar.png").convert("RGBA")
    px = im.load()
    frame = px[2, 10]
    for i in range(9):
        x0, y0 = 3 + 20 * i, 3
        for j, k in enumerate(ROUND[2]):
            for i2 in range(k):
                for x, y in ((x0 + i2, y0 + j), (x0 + 15 - i2, y0 + j),
                             (x0 + i2, y0 + 15 - j), (x0 + 15 - i2, y0 + 15 - j)):
                    px[x, y] = frame
    im.save(hud / "hotbar.png")

    questlog.draw(OUT)
    for f in (OUT / "assets" / "questlog" / "textures" / "gui").glob("*.png"):
        im = Image.open(f).convert("RGBA")
        add_pattern(im)
        im.save(f)


if __name__ == "__main__":
    main()
