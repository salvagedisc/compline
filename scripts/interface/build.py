"""Builds the fallow-interface resource pack from free-licensed packs.

Base: GUI Revision by Vixel and GUI Revision Compats by Fyoncle (both
CC BY-NC-SA 4.0), with two curls from Hollow GUI by Melee (CC BY 4.0).
The script downloads the pinned releases, keeps the files that concern
vanilla and the mods of the pack, and adapts them to Fallow:

- recolour: GUI Revision's greys become Fallow's mist, sage and moss;
- titles: the names drawn in the textures (plates, banners) are removed,
  since they cannot follow the game's language;
- screens GUI Revision paints in wood or purple are greyed first, so that
  they take the same palette;
- slots: redrawn over their whole cell as wells with rounded corners and a
  soft shadow inside; deeper wells with a fine grain where creatures stand;
  the crafting table's frame around every 2x2 and 3x3 grid with room for it;
- arrows: one slim engraved arrow, the progress one in lantern light, mods'
  own arrows found by shape and redrawn alike;
- details: the crafting table's ornaments (GUI Revision's corners and two
  curls from Hollow GUI's frieze) on every large panel, and one tight pattern
  of small leaves on a grid shared by all screens, passing under the slots;
- HUD: the hotbar as a bar with rounded ends;
- mods without a GUI Revision compat: their vanilla-grey screens, read from
  the jars cached by `npm run check-fr`, take the same colours, their own
  drawings keep theirs;
- sprites: buttons, tabs, lists and toasts follow the same tones by
  brightness, highlights in lantern light; vanilla textures GUI Revision
  lacks (the crafter, text fields, checkboxes...) are added from the game's
  own jar and dressed alike;
- Questlog: its journal is drawn by questlog.py in the same style.

Only textures the game or a mod of the pack reads are kept: compat files
made for other versions of a mod, OptiFine's and those left as they were
are dropped.

Authoring tool, not run by CI: needs Python 3 and Pillow 12.1 or newer, and
the jars in .cache/jars (run `npm run check-fr` once first).
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
    # Minecraft 1.21.1's client: the reference for what vanilla reads, and the
    # source of the textures GUI Revision does not cover
    "vanilla": ("https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar",
                "30c73b1c5da787909b2f73340419fdf13b9def88"),
}

# Compats kept: the mods of the pack that GUI Revision Compats covers.
COMPAT_NAMESPACES = {
    "amendments", "chipped", "cold_sweat", "curios", "emi", "entity_features", "exposure",
    "farm_and_charm", "immersiveenchanting", "jei", "naturalist", "overgeared", "quark",
    "supplementaries",
}
# Mods whose own screens are recoloured when they use vanilla's greys, and
# whose licence lets a modified copy be shared (credited in CREDITS.md).
# Config libraries, books and mods with a style of their own (Create and its
# addons) are left alone, and so are the mods whose textures are all rights
# reserved (Adorable Hamster Pets, Critters and Companions, Mowzie's Mobs,
# No Man's Land).
RECOLOUR_MODS = {"alexsmobs", "companions", "envelope", "kaleidoscope_cookery", "treechop"}
# Compat files whose change does not suit the mod's version: its own texture stays.
KEEP_ORIGINAL = {
    "assets/supplementaries/textures/gui/sprites/present/overlay.png",
    "assets/supplementaries/textures/gui/sprites/hud/selectable_item_overlay.png",
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

# the curls' colours, one step from the panel
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

# Slots: a recessed well with rounded corners. Its outline is a step darker at
# the top and left than at the bottom and right, and the top casts a soft
# two-row shadow inside the well; no light line, no hard black edge.
EDGE_TOP = (100, 110, 96)
EDGE_FADE = (112, 121, 106)
EDGE_LOW = (160, 166, 148)
SHADE = [(113, 123, 107), (121, 131, 114)]
SLOT_FILL = (130, 140, 122)
# corner profile: how far each of the first rows is cut in, by slot size
CORNERS = {18: [3, 1, 1], 30: [4, 2, 1, 1]}
# deeper wells behind the player and the mounts, with a fine two-tone grain
WELL_EDGE = (84, 94, 81)
WELL_GRAIN = [(100, 110, 96), (106, 116, 101)]

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
# The same arrow at 17x8, where a mod's progress sprite is that small.
SMALL_ARROW = [
    "...........#.....",
    "...........##....",
    ".#..........###..",
    "#################",
    "#################",
    ".#..........###..",
    "...........##....",
    "...........#.....",
]
# Shapes mods draw their own arrows in, whatever the colour: Farm & Charm's two
# (inside an outline), and Exposure's plus. Vanilla's wide arrow is VANILLA_ARROW.
FC_ARROWS = [
    [".............#...", ".............##..", ".............###.", "#################",
     "#################", ".............###.", ".............##..", ".............#..."],
    [".............##...", ".............###..", ".............####.", "##################",
     "##################", "..............###.", ".............###..", ".............##..."],
]
PLUS = ["....##...."] * 4 + ["##########"] * 2 + ["....##...."] * 4
# Arrows a mod draws in a shape of its own: the box to clear, then Fallow's arrow,
# ("right", its top-left corner) or ("bent", from the shaft's left end (x, y) along
# to the column x1, then down to the tip's row).
MOD_ARROWS = {
    "assets/envelope/textures/gui/mailbox.png": [((247, 37, 267, 52), ("right", 246, 39))],
    "assets/kaleidoscope_cookery/textures/gui/jei/millstone.png": [((112, 49, 139, 62), ("right", 114, 50))],
    "assets/kaleidoscope_cookery/textures/gui/jei/chopping_board.png": [((90, 30, 111, 45), ("right", 90, 32))],
    "assets/kaleidoscope_cookery/textures/gui/jei/steamer.png": [((90, 30, 111, 45), ("right", 90, 32))],
    "assets/kaleidoscope_cookery/textures/gui/jei/stockpot.png": [((127, 39, 157, 54), ("bent", 127, 40, 149, 53))],
    "assets/kaleidoscope_cookery/textures/gui/jei/teapot.png": [((90, 19, 144, 35), ("bent", 90, 20, 135, 34))],
    "assets/kaleidoscope_cookery/textures/gui/jei/bamboo_tray.png": [((96, 28, 112, 43), ("right", 95, 30))],
}

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
# Large wells (cell x, y, w, h), by vanilla file name or full path: where the player and
# the mount stand, with a fine grain.
WELLS = {
    "inventory.png": [(27, 7, 48, 72, "creature")],
    "horse.png": [(26, 16, 53, 55, "creature"), (79, 16, 90, 55, "square")],
    "tab_inventory.png": [(72, 4, 34, 46, "creature")],
    "assets/quark/textures/misc/backpack_gui.png": [(27, 7, 48, 72, "creature")],
    "assets/naturalist/textures/gui/container/elephant.png": [(7, 17, 72, 71, "creature")],
}
# Grids a compat draws in a frame of its own with buttons for cells: the frame is
# cleared from its top-left pixel, the cells (x, y of each 18-pixel cell) are
# redrawn as slots, then framed like the crafting table's grid.
REFRAMED = {
    "assets/quark/textures/gui/container/crafter.png": ((21, 20), [(25 + 18 * c, 15 + 18 * r)
                                                                   for r in range(3) for c in range(3)]),
}
# Wells a compat draws in a style of its own (x, y, w, h of the cell): redrawn as slots.
MOD_SLOTS = {
    "assets/farm_and_charm/textures/gui/pot_gui.png": [(122, 26, 20, 20)],
    "assets/farm_and_charm/textures/gui/roaster_gui.png": [(122, 26, 20, 20)],
    "assets/cold_sweat/textures/gui/screen/sewing_gui.png": [(42, 52, 18, 18)],
    "assets/immersiveenchanting/textures/gui/container/enchanting_table.png": [(232, 168, 18, 20)],
}
# Marks GUI Revision embosses in light where the others are engraved (x0, y0, x1, y1):
# the light becomes ink and the shadow under it light, like the arrows.
ENGRAVED = {"cartography_table.png": (20, 38, 27, 46)}
# Leftovers of the vanilla frame a compat left outside its own (x0, y0, x1, y1): cleared.
LEFTOVERS = {
    "assets/farm_and_charm/textures/gui/silo.png": [(0, 85, 4, 138), (172, 85, 176, 138)],
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
    "container.enderchest", "container.barrel", "container.crafter",
]

# Sprites (under textures/gui/sprites) and screens whose greys follow Fallow's tones
# by brightness; the colours a mod or the game gives meaning to (red crosses,
# green ticks, pings, boss bars) are not greys and stay.
RETONED = tuple("assets/minecraft/textures/gui/" + d for d in (
    "container/", "sprites/widget/", "sprites/social_interactions/", "sprites/toast/",
    "sprites/advancements/", "sprites/gamemode_switcher/", "sprites/container/",
    "sprites/recipe_book/", "sprites/popup/", "sprites/hud/effect_background",
    "sprites/server_list/", "sprites/transferable_list/", "sprites/world_list/", "advancements/",
)) + (
    "assets/amendments/textures/gui/sprites/widget/", "assets/supplementaries/textures/gui/sprites/present/",
    "assets/supplementaries/textures/gui/sprites/trapped_present/", "assets/envelope/textures/gui/sprites/",
    "assets/emi/textures/gui/buttons.png", "assets/emi/textures/gui/widgets.png",
)
# Sprites GUI Revision leaves in colours of their own (purple, wood, blue, gold):
# "all" takes every pixel to Fallow's tones by brightness, "light" the same with
# the sprite lifted to the panel's brightness (a button), "dim" a step darker (a
# disabled state), "lantern" to the lantern's light, "glow" its pale light (a highlight).
SPRITE_MODES = {f"assets/minecraft/textures/gui/sprites/{k}.png": v for k, v in {
    "container/enchanting_table/enchantment_slot": "light",
    "container/loom/pattern": "all", "container/loom/pattern_selected": "all", "container/loom/scroller": "all",
    "container/beacon/button": "all", "container/beacon/button_disabled": "dim",
    "advancements/box_unobtained": "all", "toast/system": "all", "hud/effect_background_ambient": "all",
    "container/enchanting_table/enchantment_slot_highlighted": "glow",
    "container/beacon/button_highlighted": "glow", "container/stonecutter/recipe_highlighted": "glow",
    "container/loom/pattern_highlighted": "glow", "recipe_book/crafting_overlay_highlighted": "glow",
    "recipe_book/furnace_overlay_highlighted": "glow", "advancements/box_obtained": "lantern",
    "advancements/challenge_frame_obtained": "lantern", "advancements/goal_frame_obtained": "lantern",
    "advancements/task_frame_obtained": "lantern", "gamemode_switcher/selection": "lantern",
    "container/brewing_stand/fuel_length": "lantern",
}.items()}
# Last touches on single textures, once built: a box (x0, y0, x1, y1) and what it
# becomes: "fill" (bare panel), "clear" (transparent), "grey" (its stray greys
# cleared), a mode as above, a colour, or a map of colours.
TOUCH_UPS = {
    # pills of an older Cold Sweat under the hearth's and boiler's gauges
    "assets/cold_sweat/textures/gui/screen/hearth_gui.png": [
        ((60, 26, 75, 67), "fill"), ((101, 26, 117, 67), "fill")],
    "assets/cold_sweat/textures/gui/screen/boiler_gui.png": [((108, 60, 141, 79), "fill")],
    "assets/cold_sweat/textures/gui/screen/icebox_gui.png": [((108, 60, 141, 79), "fill")],
    "assets/exposure/textures/gui/lightroom.png": [((176, 2, 199, 17), "grey")],
    "assets/curios/textures/gui/curios/inventory.png": [((33, 38, 54, 49), {(255, 255, 255): (130, 140, 122)})],
    "assets/companions/textures/gui/cornelius_gui_bottom.png": [
        ((61, 171, 63, 253), {(0, 0, 0): (26, 31, 28), (255, 255, 255): (232, 229, 208)})],
    "assets/quark/textures/gui/general_icons.png": [((0, 159, 17, 165), "lantern")],
    # the crafter's lit arrow, laid over the panel's leaves
    "assets/quark/textures/gui/container/crafter.png": [((176, 17, 192, 29), {FILL: None, LEAF_TONE: None})],
    "assets/minecraft/textures/gui/sprites/social_interactions/background.png": [
        ((7, 25, 8, 26), (130, 140, 122)), ((228, 25, 229, 26), (130, 140, 122))],
    # GUI Revision's own corner marks, where the standard ornaments go instead
    "assets/minecraft/textures/gui/container/crafting_table.png": [
        ((4, 4, 14, 15), "fill"), ((163, 4, 173, 15), "fill"), ((3, 68, 14, 80), "fill"), ((162, 68, 173, 80), "fill")],
    # the 3-row inventory's side bevel, as on the other screens
    **{f"assets/minecraft/textures/gui/container/{n}.png": [
        ((3, y0, 4, y1), {(166, 170, 150): (130, 140, 122)}), ((172, y0, 173, y1), {(166, 170, 150): (130, 140, 122)})]
       for n, y0, y1 in (("inventory", 84, 136), ("horse", 84, 136), ("hopper", 51, 103))},
}
# Mod screens drawn without a title: their top-left corner keeps its ornament.
UNTITLED = ("/jei/", "/emi/", "/sprites/")


# ---------------------------------------------------------------- sources

def fetch(name):
    url, sha1 = SOURCES[name]
    path = CACHE / f"{name}.zip"
    if not path.exists() or hashlib.sha1(path.read_bytes()).hexdigest() != sha1:
        CACHE.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(url, timeout=300) as response:
            data = response.read()
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


def matches(im, tpl, alone=False):
    """Where the template's pixels all appear; alone, only where the rest of its
    box is another colour (a shape standing out, not a patch of a flat area)."""
    px = im.load()
    width, height = im.size
    k0 = min(tpl)
    tw = max(x for x, y in tpl) + 1
    th = max(y for x, y in tpl) + 1
    found = []
    for y in range(height):
        for x in range(width):
            if not px[x, y][3] or px[x, y][:3] != tpl[k0]:
                continue
            ox, oy = x - k0[0], y - k0[1]
            if all(0 <= ox + dx < width and 0 <= oy + dy < height and px[ox + dx, oy + dy][3]
                   and px[ox + dx, oy + dy][:3] == c for (dx, dy), c in tpl.items()) and not (alone and any(
                    px[ox + dx, oy + dy][:3] == tpl[k0] for dy in range(th) for dx in range(tw)
                    if (dx, dy) not in tpl)):
                found.append((ox, oy))
    return found


def shape(rows):
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"}


def draw_arrow(im, x0, y0, ink, light, top=None, rows=ARROW, pts=None):
    """Fallow's arrow, 22x10, its tip to the right (or another shape in the same ink),
    with a pixel of light under it."""
    px = im.load()
    pts = pts or shape(rows)
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


def bent_arrow(x0, y0, x1, tip):
    """Fallow's arrow turning down: its shaft from (x0, y0) along to the column x1,
    then down to the tip, with the same head and tail."""
    head = {(y, x - 14) for x, y in shape(ARROW) if x >= 14}
    hx, hy = x1 - 4, tip - 7
    pts = {(hx + a, hy + b) for a, b in head}
    pts |= {(x, y) for x in range(x0, x1 + 2) for y in (y0, y0 + 1)}
    pts |= {(x, y) for x in (x1, x1 + 1) for y in range(y0, hy)}
    return pts | {(x0 + 1, y0 - 1), (x0 + 1, y0 + 2)}


def reshape_arrows(im, path):
    """Arrows a compat or a mod drew in its own shape and colour (vanilla's wide arrow,
    Farm & Charm's, Exposure's plus, and those of MOD_ARROWS) become Fallow's: in ink
    on the panel, in lantern light where they are progress sprites, on transparency."""
    px = im.load()
    width, height = im.size
    shapes = [(VANILLA_ARROW, ARROW, 0, 2), (FC_ARROWS[0], SMALL_ARROW, 1, 0), (FC_ARROWS[1], SMALL_ARROW, 1, 0)]
    if "/exposure/" in path:
        shapes.append((PLUS, PLUS, 0, 0))
    for old, new, outline, dy in shapes:
        pts, w, h = shape(old), len(old[0]), len(old)
        k0 = min(pts)
        for y in range(height - h + 1):
            for x in range(width - w + 1):
                c = px[x + k0[0], y + k0[1]]
                if not c[3] or c[:3] in (FILL, LEAF_TONE) or not all(px[x + a, y + b] == c for a, b in pts):
                    continue
                if any(px[x + a, y + b] == c for a in range(w) for b in range(h) if (a, b) not in pts):
                    continue
                ring = [px[i, j] for i in range(x - 2, x + w + 2) for j in range(y - 2, y + h + 2)
                        if 0 <= i < width and 0 <= j < height and not (x <= i < x + w and y <= j < y + h)]
                progress = sum(1 for q in ring if not q[3]) * 2 > len(ring)
                ground = (0, 0, 0, 0) if progress else FILL + (255,)
                for i in range(max(x - outline, 0), min(x + w + outline, width)):
                    for j in range(max(y - outline, 0), min(y + h + outline, height)):
                        if (i - x, j - y) in pts or (outline and px[i, j][:3] not in (FILL, LEAF_TONE)):
                            px[i, j] = ground
                if progress:
                    draw_arrow(im, x, y + dy, LANTERN, LANTERN_D, LANTERN_L, rows=new)
                else:
                    draw_arrow(im, x, y + dy, ARROW_INK, ARROW_LIGHT, rows=new)
    for (a, b, c, d), (kind, *at) in MOD_ARROWS.get(path, []):
        ground = px[a - 1, b - 1] if px[a - 1, b - 1][:3] == FILL or not px[a - 1, b - 1][3] else FILL + (255,)
        for i in range(a, c):
            for j in range(b, d):
                px[i, j] = ground
        if kind == "right":
            draw_arrow(im, *at, ARROW_INK, ARROW_LIGHT)
        else:
            draw_arrow(im, 0, 0, ARROW_INK, ARROW_LIGHT, pts=bent_arrow(*at))


def replace_arrows(im, arrows):
    """GUI Revision's arrows (a wide one, a small triangle) and the progress arrow
    stored next to them become Fallow's arrow; returns where they were."""
    px = im.load()
    big, small, filled = arrows
    out = []
    for tpl in (big, small):
        for ox, oy in matches(im, tpl):
            tw, th = max(x for x, y in tpl) + 1, max(y for x, y in tpl) + 1
            ring = [px[i, j] for i in range(ox - 2, ox + tw + 2) for j in range(oy - 2, oy + th + 2)
                    if 0 <= i < im.width and 0 <= j < im.height and (i - ox, j - oy) not in tpl]
            bare = sum(1 for q in ring if not q[3]) * 2 > len(ring)
            for dx, dy in tpl:
                px[ox + dx, oy + dy] = (0, 0, 0, 0) if bare else (193, 198, 198, 255)
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


def frieze_parts(mask):
    """The frieze's ornaments: 8-connected pieces of the mask, left to right."""
    seen, parts = set(), []
    for p in sorted(mask):
        if p in seen:
            continue
        todo, part = [p], []
        seen.add(p)
        while todo:
            x, y = todo.pop()
            part.append((x, y))
            for dx in (-1, 0, 1):
                for dy in (-1, 0, 1):
                    q = (x + dx, y + dy)
                    if q in mask and q not in seen:
                        seen.add(q)
                        todo.append(q)
        parts.append(part)
    parts.sort(key=min)
    return parts


def ornaments(crafting, mask):
    """The crafting table's decoration, laid on every panel: GUI Revision's corner
    ornament (its top-left one, mirrored for the other corners) and the two curls
    from the middle of Hollow GUI's frieze. Positions are relative to the panel's
    inside: corners 2 pixels in from the side and 1 from the top or bottom, the
    curls hanging from the top, centred."""
    px = crafting.load()
    corner = {(x - 4, y - 5): px[x, y][:3] for y in range(5, 14) for x in range(4, 13)
              if px[x, y][:3] != FILL}
    pieces = frieze_parts(mask)
    middle = pieces[len(pieces) // 2 - 1] + pieces[len(pieces) // 2]
    left = min(x for x, y in middle)
    width = max(x for x, y in middle) - left + 1
    top = min(y for x, y in middle)
    curls = {(x - left, y - top): mask[(x, y)] for x, y in middle}
    return corner, curls, width


def panel_inside(im):
    """Bounding box (x0, y0, x1, y1, inclusive) of the largest stretch of bare panel
    (leaves included), or None when the texture has no panel large enough to dress."""
    px = im.load()
    width, height = im.size
    bare = {FILL + (255,), LEAF_TONE + (255,)}
    seen, best = set(), None
    for y in range(height):
        for x in range(width):
            if (x, y) in seen or px[x, y] not in bare:
                continue
            todo, n = [(x, y)], 0
            seen.add((x, y))
            x0 = x1 = x
            y0 = y1 = y
            while todo:
                i, j = todo.pop()
                n += 1
                x0, x1, y0, y1 = min(x0, i), max(x1, i), min(y0, j), max(y1, j)
                for k, l in ((i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)):
                    if 0 <= k < width and 0 <= l < height and (k, l) not in seen and px[k, l] in bare:
                        seen.add((k, l))
                        todo.append((k, l))
            if n >= 2000 and x1 - x0 >= 100 and y1 - y0 >= 20 and (best is None or n > best[0]):
                best = (n, (x0, y0, x1, y1))
    return best and best[1]


def add_ornaments(im, decoration, titled=False):
    """Lays each ornament where it finds bare panel (the leaf pattern counts as bare),
    with one pixel of panel kept clear around it; an ornament that would touch a
    slot or an edge is left out whole. On a titled screen the top-left corner is
    the title's."""
    inside = panel_inside(im)
    if inside is None:
        return
    corner, curls, width = decoration
    x0, y0, x1, y1 = inside
    cw = max(u for u, v in corner) + 1
    ch = max(v for u, v in corner) + 1
    placed = [
        {(x0 + 2 + u, y0 + 1 + v): c for (u, v), c in corner.items()},
        {(x1 - 2 - u, y0 + 1 + v): c for (u, v), c in corner.items()},
        {(x0 + 2 + u, y1 - 1 - v): c for (u, v), c in corner.items()},
        {(x1 - 2 - u, y1 - 1 - v): c for (u, v), c in corner.items()},
        {((x0 + x1 + 1 - width) // 2 + u, y0 + v + (3 if y0 < 2 else 0)): c for (u, v), c in curls.items()},
    ]
    if titled:
        placed[0] = {}
    if y1 - y0 < 2 * ch + 6:  # a low strip of panel: the curls only
        placed = placed[4:]
    px = im.load()
    bare = (FILL, LEAF_TONE)

    def free(p):
        return 0 <= p[0] < im.width and 0 <= p[1] < im.height and px[p][:3] in bare and px[p][3]

    for stamp in filter(None, placed):
        halo = {(x + a, y + b) for x, y in stamp for a in (-1, 0, 1) for b in (-1, 0, 1)} - set(stamp)
        if x1 - x0 < 2 * cw + 8 or not all(free(p) for p in stamp) or not all(
                free(p) or p[1] < y0 for p in halo):
            continue
        for p in halo:
            if free(p):
                px[p] = FILL + (255,)
        for p, c in stamp.items():
            px[p] = c + (255,)


def add_pattern(im):
    """The leaf, repeated tone on tone over every large stretch of bare panel on one
    grid anchored to the texture's corner: the same pattern everywhere, passing
    under the slots and the ornaments drawn over it."""
    px = im.load()
    width, height = im.size
    fill = FILL + (255,)
    bare = set()
    seen = set()
    for y in range(height):
        for x in range(width):
            if (x, y) in seen or px[x, y] != fill:
                continue
            region, todo = [], [(x, y)]
            seen.add((x, y))
            while todo:
                i, j = todo.pop()
                region.append((i, j))
                for k, l in ((i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)):
                    if 0 <= k < width and 0 <= l < height and (k, l) not in seen and px[k, l] == fill:
                        seen.add((k, l))
                        todo.append((k, l))
            if len(region) >= 300:
                bare.update(region)
    pw, ph = LEAF_PITCH
    for i, j in bare:
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
    fixed = fixed or {}
    panel = next(iter(fixed), None) or Counter(
        px[x, y][:3] for x in range(3, 7) for y in range(20, 60) if px[x, y][3]).most_common(1)[0][0]
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
    """Vanilla slots: 18x18 up to 32x32 (26x26 for outputs), dark top-left edge, white bottom-right edge;
    also flat 16x16 slot-grey cells drawn in a grid without those edges."""
    px = im.load()
    width, height = im.size
    dark, slot, white = (55, 55, 55), (139, 139, 139), (255, 255, 255)
    found = []
    for n in range(18, 33):
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


def draw_slot(im, x, y, w, h, kind="square", around=FILL + (255,), cells=()):
    """Redraws the slot cell (x, y, w, h) as a well with rounded corners, or, for
    "creature", a deeper well with a fine two-tone grain. The outline is darker at
    the top and left, fading towards the ends, and the top casts a soft shadow
    into the well. A placeholder drawn inside the slot (a banner, a dye, a
    blueprint) is kept. With the other cells of the screen given, each cut corner
    takes the colour beside it (a frame, a border), and only where four cells meet
    does it stay panel, a small cross between them."""
    if x < 0 or y < 0 or x + w > im.width or y + h > im.height:
        return
    px = im.load()

    def covered(i, j):
        return any(a <= i < a + c and b <= j < b + d for a, b, c, d, *_ in cells)

    def beside(i, j):
        if not cells:
            return tuple(around)
        hx, vy = (x - 1 if i < w / 2 else x + w), (y - 1 if j < h / 2 else y + h)
        side, end = covered(hx, y + j), covered(x + i, vy)
        if side and end:
            return tuple(around)
        ox, oy = (x + i, vy) if side else (hx, y + j)
        if 0 <= ox < im.width and 0 <= oy < im.height:
            return px[ox, oy] if px[ox, oy][3] else (0, 0, 0, 0)
        return tuple(around)

    before = {(i, j): px[x + i, y + j] for j in range(h) for i in range(w)}
    ground = Counter(before.values()).most_common(1)[0][0]
    cut = CORNERS[18] if max(w, h) <= 18 else CORNERS[30]
    inside = corner_mask(w, h, cut)
    deep = kind == "creature"
    edge_top = WELL_EDGE if deep else EDGE_TOP

    for j in range(h):
        for i in range(w):
            if not inside(i, j):
                out = beside(i, j)
                near = sum(inside(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if near >= 2:  # soften the corner step
                    edge = edge_top if j < h / 2 else EDGE_LOW
                    c = tuple((e + g) // 2 for e, g in zip(edge, out[:3]))
                    px[x + i, y + j] = c + (max(out[3], 160),)
                else:
                    px[x + i, y + j] = out
                continue
            up, left = not inside(i, j - 1), not inside(i - 1, j)
            down, right = not inside(i, j + 1), not inside(i + 1, j)
            if up or left:
                c = EDGE_FADE if (up and i >= w - 5) or (left and j >= h - 5) else edge_top
            elif down or right:
                c = EDGE_LOW
            elif deep:
                c = WELL_GRAIN[(x + i + y + j) % 2]
            elif not inside(i, j - 2):
                c = SHADE[0]
            elif not inside(i, j - 3):
                c = SHADE[1]
            elif before[i, j] != ground and max(w, h) <= 18:
                c = before[i, j][:3]
            else:
                c = SLOT_FILL
            px[x + i, y + j] = c + (255,)


def clear_leftovers(im, rects):
    """Bits of the source's old slot art left just outside the redrawn cells (a light
    corner, a line along the player's well) become panel: marks in the pack's own
    tones lying wholly within two pixels of a cell."""
    px = im.load()
    width, height = im.size
    palette = set(LUT.values()) - {FILL}
    inside, near = set(), set()
    for a, b, c, d, *_ in rects:
        inside.update((i, j) for i in range(a, a + c) for j in range(b, b + d))
        near.update((i, j) for i in range(a - 2, a + c + 2) for j in range(b - 2, b + d + 2))

    def mark(i, j):
        return (0 <= i < width and 0 <= j < height and (i, j) not in inside
                and px[i, j][3] and px[i, j][:3] in palette)

    seen = set()
    for x, y in sorted(near - inside):
        if (x, y) in seen or not mark(x, y):
            continue
        comp, todo = [], [(x, y)]
        seen.add((x, y))
        while todo:
            i, j = todo.pop()
            comp.append((i, j))
            for k in (i - 1, i, i + 1):
                for l in (j - 1, j, j + 1):
                    if (k, l) not in seen and mark(k, l):
                        seen.add((k, l))
                        todo.append((k, l))
        if len(comp) <= 200 and all(p in near for p in comp):
            for p in comp:
                px[p] = FILL + (255,)


def frame_slices(crafting):
    """The frame of the crafting table's grid (cells 29-82 x 16-69): four pixels on
    each side and a two-row shadow under it, panel left out."""
    px = crafting.load()
    return {(u, v): px[sx, sy] for u, sx in ((0, 25), (1, 26), (2, 27), (3, 28), (4, 40), (5, 83), (6, 84), (7, 85), (8, 86))
            for v, sy in ((0, 12), (1, 13), (2, 14), (3, 15), (4, 40), (5, 70), (6, 71), (7, 72), (8, 73), (9, 74), (10, 75))
            if (u, v) != (4, 4) and px[sx, sy][3] and px[sx, sy][:3] != FILL}


def add_frames(im, cells, frame):
    """Frames each block of 2x2 to 3x3 slots like the crafting table's grid, where the
    panel around it is bare with two pixels to spare."""
    px = im.load()
    small = {(x, y) for x, y, w, h in cells if (w, h) == (18, 18)}
    seen = set()
    for start in sorted(small):
        if start in seen:
            continue
        block, todo = [], [start]
        seen.add(start)
        while todo:
            x, y = todo.pop()
            block.append((x, y))
            for q in ((x + 18, y), (x - 18, y), (x, y + 18), (x, y - 18)):
                if q in small and q not in seen:
                    seen.add(q)
                    todo.append(q)
        x0, y0 = min(x for x, y in block), min(y for x, y in block)
        cols, rows = (max(x for x, y in block) - x0) // 18 + 1, (max(y for x, y in block) - y0) // 18 + 1
        if len(block) != cols * rows or not (2 <= cols <= 3 and 2 <= rows <= 3):
            continue
        x1, y1 = x0 + 18 * cols, y0 + 18 * rows
        ring = [(i, j) for i in range(x0 - 6, x1 + 6) for j in range(y0 - 6, y1 + 8)
                if not (x0 <= i < x1 and y0 <= j < y1)]
        if not all(0 <= i < im.width and 0 <= j < im.height and px[i, j] == FILL + (255,) for i, j in ring):
            continue
        for i, j in ring:
            if not (x0 - 4 <= i < x1 + 4 and y0 - 4 <= j < y1 + 6):
                continue
            u = i - x0 + 4 if i < x0 else 4 if i < x1 else i - x1 + 5
            v = j - y0 + 4 if j < y0 else 4 if j < y1 else j - y1 + 5
            if (u, v) in frame:
                px[i, j] = frame[u, v]


def corner_mask(w, h, cut):
    """Mask of a w x h rectangle whose corners are cut in by cut[k] on row k."""
    def inside(i, j):
        if not (0 <= i < w and 0 <= j < h):
            return False
        k = min(j, h - 1 - j)
        return k >= len(cut) or cut[k] <= i <= w - 1 - cut[k]
    return inside


def fill_gaps(im, cells, wells=()):
    """Adds the cells a slot grid skipped, when a compat drew one slightly
    differently: within a row or a column of 18-pixel cells, and one step past
    its ends, a free place on the grid's pitch painted like the other slots is a
    slot too."""
    px = im.load()
    small = [c for c in cells if c[2:] == (18, 18)]
    if not small:
        return cells
    paint = Counter(px[x + 9, y + 9] for x, y, w, h in small).most_common(1)[0][0]

    def free(x, y):
        return all(x + 18 <= a or a + c <= x or y + 18 <= b or b + d <= y
                   for a, b, c, d in list(cells) + [w[:4] for w in wells])

    def painted(x, y):
        if x < 0 or y < 0 or x + 18 > im.width or y + 18 > im.height:
            return False
        inner = [px[x + i, y + j] for j in range(3, 15) for i in range(3, 15)]
        sides = [[(x - 1, y + k) for k in range(18)], [(x + 18, y + k) for k in range(18)],
                 [(x + k, y - 1) for k in range(18)], [(x + k, y + 18) for k in range(18)]]
        open_side = any(sum(0 <= i < im.width and 0 <= j < im.height and px[i, j] == paint for i, j in side) >= 14
                        for side in sides)  # the place is part of a larger well, not a cell
        return sum(p == paint for p in inner) >= 0.6 * len(inner) and not open_side

    for axis in (0, 1):
        lines = {}
        for c in small:
            lines.setdefault(c[1 - axis], []).append(c[axis])
        for fixed, run in lines.items():
            run.sort()
            places = [k for a, b in zip(run, run[1:]) if not (b - a) % 18 and b - a <= 144
                      for k in range(a + 18, b, 18)]
            if len(run) >= 2:
                places += [run[0] - 18, run[-1] + 18]
            for k in places:
                x, y = (k, fixed) if axis == 0 else (fixed, k)
                if free(x, y) and painted(x, y):
                    cells.append((x, y, 18, 18))
    return cells


def separate(cells):
    """Where a large well touches a smaller cell, pulls the well back by a pixel so
    that every slot keeps its own outline."""
    out = [list(c) for c in cells]
    for i, (x, y, w, h) in enumerate(cells):
        for a, b, c, d in cells:
            if min(w, h) < 30 or w * h <= c * d:  # only wells give way, not sprites of a sheet
                continue
            across = min(y + h, b + d) - max(y, b) > 0
            along = min(x + w, a + c) - max(x, a) > 0
            if across and x + w == a:
                out[i][2] -= 1
            elif across and a + c == x:
                out[i][0] += 1
                out[i][2] -= 1
            elif along and y + h == b:
                out[i][3] -= 1
            elif along and b + d == y:
                out[i][1] += 1
                out[i][3] -= 1
    return [tuple(c) for c in out]


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
                c = WELL_EDGE if edge else (232, 229, 208) if not inside(i, j - 2) else \
                    (166, 170, 150) if not inside(i, j + 2) else FILL
                px[x0 + i, y0 + j] = c + (255,)
            elif inside(i, j - 1):
                px[x0 + i, y0 + j] = WELL_EDGE + (110,)


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
    outer, inner = rounded(24, 23, 5), corner_mask(18, 18, CORNERS[18])
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


# ---------------------------------------------------------------- finish

FALLOW = set(LUT.values()) | {
    FILL, LEAF_TONE, SOFT, ACCENT, EDGE_TOP, EDGE_FADE, EDGE_LOW, *SHADE, SLOT_FILL, WELL_EDGE, *WELL_GRAIN,
    ARROW_INK, ARROW_LIGHT, LANTERN_D, LANTERN, LANTERN_L, LANTERN_EDGE}
OUTLINE = LUT[(28, 28, 31)]
BEVEL = LUT[(131, 139, 139)]
# Brewing's arrow, pointing down: the small arrow turned, its shaft drawn out to 28 rows.
BREW_ARROW = ({(c, r) for c in (3, 4) for r in range(22)} | {(2, 1), (5, 1)}
              | {(y, x + 11) for x, y in shape(SMALL_ARROW) if x >= 11})


def lantern_tone(v):
    """The lantern's light for a pixel of luminance v."""
    ramp = [(40, LANTERN_EDGE), (110, LANTERN_D), (170, LANTERN), (225, LANTERN_L)]
    if v <= ramp[0][0]:
        return ramp[0][1]
    for (l0, c0), (l1, c1) in zip(ramp, ramp[1:]):
        if v <= l1:
            t = (v - l0) / (l1 - l0)
            return tuple(round(a + (b - a) * t) for a, b in zip(c0, c1))
    return ramp[-1][1]


def retone(im, mode, box=None):
    """Takes greys ("neutral"), every colour ("all"), every colour a step darker
    ("dim") or to the lantern's light ("lantern") to Fallow's tones by brightness;
    Fallow's own colours stay. "light" and "glow" first lift the sprite's main
    colour to the panel's brightness or the lantern's pale light (a button, its
    highlight). Returns whether a pixel changed."""
    px = im.load()
    x0, y0, x1, y1 = box or (0, 0, im.width, im.height)
    shift = 0
    if mode in ("light", "glow"):
        main = Counter(px[x, y][:3] for y in range(y0, y1) for x in range(x0, x1) if px[x, y][3])
        shift = (lum(FILL) - 10 if mode == "light" else 215) - lum(main.most_common(1)[0][0])
        mode = "all" if mode == "light" else "lantern"
    changed = False
    for y in range(y0, y1):
        for x in range(x0, x1):
            r, g, b, a = px[x, y]
            if not a or (mode in ("neutral", "all") and (r, g, b) in FALLOW):
                continue
            if mode == "neutral" and max(r, g, b) - min(r, g, b) > 16:
                continue
            v = lum((r, g, b)) + shift
            new = lantern_tone(v) if mode == "lantern" else tone(v * 0.8) if mode == "dim" else tone(v)
            if new != (r, g, b):
                px[x, y] = new + (a,)
                changed = True
    return changed


def touch_up(im, box, action):
    px = im.load()
    if isinstance(action, str) and action in ("neutral", "all", "dim", "lantern", "light", "glow"):
        retone(im, action, box)
        return
    x0, y0, x1, y1 = box
    for y in range(y0, y1):
        for x in range(x0, x1):
            p = px[x, y]
            if action == "fill":
                px[x, y] = FILL + (255,)
            elif action == "clear" or (action == "grey" and p[3] and max(p[:3]) - min(p[:3]) <= 3):
                px[x, y] = (0, 0, 0, 0)
            elif isinstance(action, dict):
                if p[3] and p[:3] in action:
                    px[x, y] = action[p[:3]] + (p[3],) if action[p[:3]] else (0, 0, 0, 0)
            elif isinstance(action, tuple):
                px[x, y] = action + (255,)


def edge_rings(im, corners):
    """Pure black and white left on a panel's outer rings (within two pixels of its
    edge) take the outline and the bone light; with corners, black left on a
    rounded corner is cleared."""
    src = im.copy().load()
    px = im.load()
    width, height = im.size

    def out(i, j):
        return not (0 <= i < width and 0 <= j < height) or not src[i, j][3]

    for y in range(height):
        for x in range(width):
            p = src[x, y]
            if not p[3] or p[:3] not in ((0, 0, 0), (255, 255, 255)):
                continue
            if not any(out(x + a, y + b) for a in range(-2, 3) for b in range(-2, 3)):
                continue
            if corners and p[:3] == (0, 0, 0) and sum(out(x + a, y + b) for a, b in (
                    (1, 0), (-1, 0), (0, 1), (0, -1))) >= 2:
                px[x, y] = (0, 0, 0, 0)
            else:
                px[x, y] = (OUTLINE if p[:3] == (0, 0, 0) else ARROW_LIGHT) + (p[3],)


def finish_vanilla(c):
    """Single vanilla screens: the enchanting table's double frame and marks, the
    hopper's open right edge, the anvil's plus and the brewing stand's arrow."""
    furnace = Image.open(c / "furnace.png").convert("RGBA").load()
    im = Image.open(c / "enchanting_table.png").convert("RGBA")
    px = im.load()
    for y in range(3, 83):
        for x in range(176):
            if y in (3, 4, 5, 79, 80, 81, 82) or x in (0, 1, 174, 175):
                px[x, y] = furnace[x, y]
            elif x in (2, 3, 172, 173) or y in (6, 7) or (y in (76, 77, 78) and px[x, y][:3] not in (FILL, LEAF_TONE)):
                px[x, y] = FILL + (255,)
    for box in ((4, 8, 17, 21), (4, 62, 11, 79), (11, 70, 17, 79), (156, 8, 169, 13), (169, 8, 172, 23),
                (169, 62, 172, 79), (156, 72, 169, 79), (59, 10, 62, 13)):
        touch_up(im, box, "fill")
    im.save(c / "enchanting_table.png")

    im = Image.open(c / "hopper.png").convert("RGBA")
    px = im.load()
    for y in range(9, 50):
        if px[175, y][:3] == BEVEL:
            px[175, y] = OUTLINE + (255,)
            if px[174, y][:3] in (FILL, LEAF_TONE):
                px[174, y] = BEVEL + (255,)
    im.save(c / "hopper.png")

    im = Image.open(c / "anvil.png").convert("RGBA")
    px = im.load()
    plus = {(x, y) for y in range(51, 59) for x in range(56, 63) if px[x, y][:3] == LUT[(85, 89, 89)]}
    touch_up(im, (55, 50, 64, 60), {ARROW_LIGHT: FILL})
    draw_arrow(im, 0, 0, ARROW_INK, ARROW_LIGHT, pts=plus)
    im.save(c / "anvil.png")

    im = Image.open(c / "brewing_stand.png").convert("RGBA")
    touch_up(im, (96, 15, 108, 46), "fill")
    draw_arrow(im, 97, 16, ARROW_INK, ARROW_LIGHT, pts=BREW_ARROW)
    im.save(c / "brewing_stand.png")
    sprite = Image.new("RGBA", (9, 28))
    sp = sprite.load()
    for x, y in BREW_ARROW:
        sp[x, y] = (LANTERN_L if (x - 1, y) not in BREW_ARROW else
                    LANTERN_D if (x + 1, y) not in BREW_ARROW and y >= 22 else LANTERN) + (255,)
    sprite.save(c.parent / "sprites" / "container" / "brewing_stand" / "brew_progress.png")


def finish(decoration):
    """The passes run on the built textures: sprites retoned, single touch-ups,
    the outer rings of panels, vanilla greys a compat left, then the leaves and
    ornaments again where the panel changed."""
    finish_vanilla(OUT / "assets" / "minecraft" / "textures" / "gui" / "container")
    for f in sorted((OUT / "assets").rglob("*.png")):
        path = f.relative_to(OUT).as_posix()
        if "/questlog/" in path or "/sprites/hud/hotbar" in path:
            continue
        im = Image.open(f).convert("RGBA")
        before = im.tobytes()
        if path in SPRITE_MODES:
            retone(im, SPRITE_MODES[path])
        if path.startswith(RETONED):
            retone(im, "neutral")
        for box, action in TOUCH_UPS.get(path, []):
            touch_up(im, box, action)
        namespace = path.split("/")[1]
        if namespace in COMPAT_NAMESPACES:
            touch_up(im, (0, 0, im.width, im.height), {k: v for k, v in VANILLA.items()})
        if big_panel(path, im):
            edge_rings(im, corners=namespace == "minecraft")
            add_pattern(im)
            add_ornaments(im, decoration, titled(path))
        if path == "assets/emi/textures/gui/widgets.png":
            # the filling arrow: the empty one, lit
            px = im.load()
            pts = {(x - 44, y) for y in range(17) for x in range(44, 68) if px[x, y][:3] == ARROW_INK}
            for y in range(17):
                for x in range(44, 68):
                    px[x, y + 17] = px[x, y]
            draw_arrow(im, 44, 17, LANTERN, LANTERN_D, LANTERN_L, pts=pts)
        if im.tobytes() != before:
            im.save(f)


def titled(path):
    return not path.startswith("assets/minecraft/") and not any(k in path for k in UNTITLED)


# The pack icon's lantern: moss metal, lit glass.
LANTERN_ICON = [
    "....OOOOO....",
    "...O.....O...",
    "...O.....O...",
    "..OOOOOOOOO..",
    ".OMmmmmmmmMO.",
    ".OOOOOOOOOOO.",
    "..OMDLLLDMO..",
    "..OMLYYYLMO..",
    "..OMLYHYLMO..",
    "..OMLYHYLMO..",
    "..OMLYYYLMO..",
    "..OMDLLLDMO..",
    ".OOOOOOOOOOO.",
    ".OMmmmmmmmMO.",
    "..OOOOOOOOO..",
]


def draw_icon():
    """pack.png: a small panel with its leaves, and a lit lantern in its middle."""
    im = Image.new("RGBA", (32, 32))
    px = im.load()
    inside = rounded(32, 32, 4)
    for j in range(32):
        for i in range(32):
            if inside(i, j):
                edge = any(not inside(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                px[i, j] = (OUTLINE if edge else ARROW_LIGHT if not inside(i, j - 2) else
                            BEVEL if not inside(i + 2, j) or not inside(i - 2, j) or not inside(i, j + 2) else
                            FILL) + (255,)
    add_pattern(im)
    colours = {"O": OUTLINE, "M": LUT[(85, 89, 89)], "m": BEVEL, "D": LANTERN_D, "L": LANTERN, "Y": LANTERN_L,
               "H": (255, 244, 214)}
    x0, y0 = 10, 9
    glass = {(x0 + x, y0 + y) for y, row in enumerate(LANTERN_ICON) for x, c in enumerate(row) if c in "DLYH"}
    for i in range(4, 28):  # the glow on the panel around the glass
        for j in range(4, 28):
            d = min(abs(i - x) + abs(j - y) for x, y in glass)
            if 0 < d <= 3 and px[i, j][:3] in (FILL, LEAF_TONE):
                t = (4 - d) / 6
                px[i, j] = tuple(round(a + (b - a) * t) for a, b in zip(px[i, j][:3], LANTERN_L)) + (255,)
    for y, row in enumerate(LANTERN_ICON):
        for x, c in enumerate(row):
            if c != ".":
                px[x0 + x, y0 + y] = colours[c] + (255,)
    return im.resize((128, 128), Image.NEAREST)


def crafter(frame, decoration):
    """The crafter's screen, on the crafting table's: its 3x3 grid framed at the
    crafter's place, its result in a slot where the game draws it."""
    im = Image.open(OUT / "assets" / "minecraft" / "textures" / "gui" / "container" / "crafting_table.png")
    im = im.convert("RGBA")
    touch_up(im, (3, 4, 173, 79), "fill")
    cells = [(25 + 18 * c, 16 + 18 * r, 18, 18) for r in range(3) for c in range(3)] + [(133, 34, 18, 18)]
    add_frames(im, cells, frame)
    for x, y, w, h in cells:
        draw_slot(im, x, y, w, h, cells=cells)
    add_pattern(im)
    add_ornaments(im, decoration)
    return im


def dress_vanilla(im, n, frame, decoration):
    """A screen drawn in vanilla's greys (a mod's, or the game's where GUI Revision
    has none) dressed like the others; returns whether it had vanilla's greys."""
    slots = find_vanilla_slots(im)
    wells = find_black_wells(im)
    tpl = {(x, y): (139, 139, 139) for y, row in enumerate(VANILLA_ARROW)
           for x, c in enumerate(row) if c == "#"}
    drawn = []
    for ox, oy in matches(im, tpl, alone=True):
        for dx, dy in tpl:
            im.putpixel((ox + dx, oy + dy), (198, 198, 198, 255))
        drawn.append((ox, oy + 3))
    if not recolour_vanilla(im):
        return False
    for x0, y0 in drawn:
        draw_arrow(im, x0, y0, ARROW_INK, ARROW_LIGHT)
    if "/sprites/" not in n:
        reshape_arrows(im, n)
    px = im.load()
    slots = fill_gaps(im, list(dict.fromkeys(slots)), wells)
    spaced = separate(slots + wells)
    slots, wells = spaced[:len(spaced) - len(wells)], spaced[len(spaced) - len(wells):]
    clear_leftovers(im, slots + wells)
    add_frames(im, slots, frame)
    for x, y, w, h in slots:
        clear = x == 0 or px[x - 1, y + h // 2][3] == 0
        draw_slot(im, x, y, w, h, around=(0, 0, 0, 0) if clear else FILL + (255,), cells=slots + wells)
    for x, y, w, h in wells:
        draw_slot(im, x, y, w, h, "creature", cells=slots + wells)
    if big_panel(n, im):
        add_pattern(im)
        add_ornaments(im, decoration, titled(n))
    return True


# ---------------------------------------------------------------- build

def main():
    rev = fetch("gui-revision")
    compats = fetch("gui-revision-compats")
    hollow = fetch("hollow-gui")
    vanilla = fetch("vanilla")
    mask = frieze_mask(hollow)

    # where each texture the game and the mods read comes from
    origin = {n: vanilla for n in vanilla.namelist() if n.startswith("assets/minecraft/textures/")}
    jars = [zipfile.ZipFile(j) for j in sorted(JARS.glob("*.jar"))]
    for z in jars:
        for n in z.namelist():
            if n.startswith("assets/") and "/textures/" in n:
                origin.setdefault(n, z)

    files = {}
    for n in rev.namelist():
        if (n.startswith("assets/minecraft/") and not n.endswith("/") and "/lang/" not in n
                and n.removesuffix(".mcmeta") in origin):
            files[n] = rev.read(n)
    for prefix in ("", "overlay_34/"):
        for n in compats.namelist():
            m = re.match(prefix + r"assets/([^/]+)/(.+)$", n)
            path = m and f"assets/{m.group(1)}/{m.group(2)}"
            if m and not n.endswith("/") and m.group(1) in COMPAT_NAMESPACES and path in origin \
                    and path not in KEEP_ORIGINAL:
                files[path] = compats.read(n)

    container = "assets/minecraft/textures/gui/container/"
    crafting = png(files[container + "crafting_table.png"])
    clear_plate(crafting)
    greyify(crafting)
    recolour(crafting)
    decoration = ornaments(crafting, mask)
    frame = frame_slices(crafting)
    furnace = png(files[container + "furnace.png"])
    anvil = png(files[container + "anvil.png"])
    progress = png(files["assets/minecraft/textures/gui/sprites/container/furnace/burn_progress.png"])
    grey = (193, 198, 198)
    arrows = (template(furnace, (84, 36, 99, 49), grey), template(anvil, (110, 50, 115, 59), grey),
              template(progress, (0, 0, 24, 16), None))

    if OUT.exists():
        shutil.rmtree(OUT / "assets")
    OUT.mkdir(parents=True, exist_ok=True)
    written = {}

    def save(path, im):
        """Writes a texture unless it is the original's twin, with the original's
        metadata (a nine-slice, an animation) when the source had none."""
        source = origin.get(path)
        if source is not None:
            first = png(source.read(path))
            if first.size == im.size and first.tobytes() == im.tobytes():
                return
            meta = path + ".mcmeta"
            if meta not in files and meta in source.namelist():
                (OUT / meta).parent.mkdir(parents=True, exist_ok=True)
                (OUT / meta).write_bytes(source.read(meta))
        dest = OUT / path
        dest.parent.mkdir(parents=True, exist_ok=True)
        im.save(dest)
        written[path] = True

    chipped = GREYED_COMPATS["assets/chipped/textures/gui/container/workbench.png"]["fixed"]
    for path, data in sorted(files.items()):
        dest = OUT / path
        if not path.endswith(".png"):
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            continue
        im = png(data)
        name = path.rsplit("/", 1)[1]
        vanilla_screen = path.startswith(container)
        if vanilla_screen and name in PLATED:
            clear_plate(im)
        if (vanilla_screen or path.startswith("assets/supplementaries/textures/gui/")) and name in BANNERED:
            clear_banner(im, name)
        for x0, y0, x1, y1 in LEFTOVERS.get(path, []):
            for y in range(y0, y1):
                for x in range(x0, x1):
                    im.putpixel((x, y), (0, 0, 0, 0))
        spec = GREYED_COMPATS.get(path, {})
        if path.startswith("assets/chipped/textures/gui/sprites/"):  # buttons in the workbench's wood
            spec = {"fixed": chipped, "rare": 0}
        cells = find_cells(im, *spec["cells"]) if "cells" in spec else []
        if (vanilla_screen and name in GREYED) or spec:
            greyify(im, spec.get("fixed"), rare=spec.get("rare", 30) if spec else 0, keep=spec.get("keep", ()))
        if name == "anvil.png":  # GUI Revision's red mark under the name field
            px = im.load()
            for y in range(im.height):
                for x in range(im.width):
                    if px[x, y] == (255, 0, 0, 255):
                        px[x, y] = grey + (255,)
        slots = [(x - 1, y - 1, w + 2, h + 2) for x, y, w, h in find_slots(im) + cells]
        if vanilla_screen:
            slots += [(x - 1, y - 1, 18, 18) for x, y in EXTRA_SLOTS.get(name, [])]
        slots += MOD_SLOTS.get(path, [])
        drawn = replace_arrows(im, arrows) if "/gui/" in path or "/misc/" in path else []
        if vanilla_screen and name in WOOD_ARROWS:
            (a, b, c, d), (cx, cy) = WOOD_ARROWS[name]
            px = im.load()
            for y in range(b, d):
                for x in range(a, c):
                    px[x, y] = px[a - 2, y]
            drawn.append((cx - 11, cy - 4))
        recolour(im)
        for x0, y0 in drawn:
            draw_arrow(im, x0, y0, ARROW_INK, ARROW_LIGHT)
        if "/sprites/" not in path:
            reshape_arrows(im, path)
        if vanilla_screen and name in ENGRAVED:
            a, b, c, d = ENGRAVED[name]
            px = im.load()
            for j in range(b, d):
                for i in range(a, c):
                    if px[i, j][:3] == ARROW_LIGHT:
                        px[i, j] = ARROW_INK + (255,)
                    elif px[i, j][:3] == (166, 170, 150):
                        px[i, j] = ARROW_LIGHT + (255,)
        if path in REFRAMED:
            seed, extra = REFRAMED[path]
            px = im.load()
            todo = [seed]
            while todo:
                i, j = todo.pop()
                if 0 <= i < im.width and 0 <= j < im.height and px[i, j][3] and px[i, j][:3] != FILL:
                    px[i, j] = FILL + (255,)
                    todo += [(i + 1, j), (i - 1, j), (i, j + 1), (i, j - 1)]
            slots += [(x, y, 18, 18) for x, y in extra]
        wells = WELLS.get(name, []) if vanilla_screen else WELLS.get(path, [])
        slots = [s for s in slots if all(s[0] + s[2] <= a or a + c <= s[0] or s[1] + s[3] <= b or b + d <= s[1]
                                          for a, b, c, d, _ in wells)]
        slots = fill_gaps(im, list(dict.fromkeys(slots)), wells)
        spaced = separate(slots + [w[:4] for w in wells])
        slots, wells = spaced[:len(spaced) - len(wells)], [c + w[4:] for c, w in zip(spaced[len(spaced) - len(wells):], wells)]
        clear_leftovers(im, slots + wells)
        add_frames(im, slots, frame)
        for x, y, w, h in slots:
            draw_slot(im, x, y, w, h, cells=slots + wells)
        for x, y, w, h, kind in wells:
            draw_slot(im, x, y, w, h, kind, cells=slots + wells)
        if big_panel(path, im):
            add_pattern(im)
            add_ornaments(im, decoration, titled(path))
        save(path, im)

    # vanilla-grey screens of the mods without a compat
    for z in jars:
        for n in z.namelist():
            m = re.match(r"assets/([^/]+)/textures/gui/(.+\.png)$", n)
            if m and m.group(1) in RECOLOUR_MODS and origin[n] is z:
                im = png(z.read(n))
                if dress_vanilla(im, n, frame, decoration):
                    save(n, im)

    # vanilla textures GUI Revision does not cover: the crafter's screen, dressed
    # like the others, and the sprites of widgets and containers, retoned
    for n in sorted(origin):
        if origin[n] is not vanilla or not n.endswith(".png") or n in files:
            continue
        if n == container + "crafter.png":
            save(n, crafter(frame, decoration))
        elif n.startswith(tuple(f"assets/minecraft/textures/gui/sprites/{d}/" for d in (
                "widget", "container", "popup", "social_interactions"))):
            im = png(vanilla.read(n))
            retone(im, "neutral")
            save(n, im)

    lang = OUT / "assets" / "minecraft" / "lang"
    lang.mkdir(parents=True, exist_ok=True)
    blank = {k: " " for k in HIDDEN_TITLES}
    for code in ("fr_fr", "en_us"):
        (lang / f"{code}.json").write_text(json.dumps(blank, indent=2, ensure_ascii=False) + "\n")

    draw_hud(OUT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud")
    finish(decoration)
    draw_icon().save(OUT / "pack.png")

    questlog.draw(OUT)
    for f in (OUT / "assets" / "questlog" / "textures" / "gui").glob("*.png"):
        im = Image.open(f).convert("RGBA")
        add_pattern(im)
        im.save(f)

    # every coordinate table must still find its texture
    named = [*MOD_ARROWS, *REFRAMED, *MOD_SLOTS, *LEFTOVERS, *GREYED_COMPATS, *TOUCH_UPS, *SPRITE_MODES,
             *(k for k in WELLS if "/" in k)]
    named += [container + k for k in (*GREYED, *PLATED, *EXTRA_SLOTS, *ENGRAVED, *WOOD_ARROWS,
                                      *(k for k in WELLS if "/" not in k and k != "tab_inventory.png"))]
    lost = sorted(k for k in named if not (OUT / k).exists())
    if lost:
        raise SystemExit("textures not found: " + ", ".join(lost))


if __name__ == "__main__":
    main()
