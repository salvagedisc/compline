"""Builds the fallow-interface resource pack from free-licensed packs.

Base: GUI Revision by Vixel and GUI Revision Compats by Fyoncle (both
CC BY-NC-SA 4.0). The script downloads the pinned releases, keeps the files that concern
vanilla and the mods of the pack, and adapts them to Fallow while keeping GUI
Revision's drawing as it is: its panels with their relief, its slots, its
arrows, its trays and its corner ornaments.

- colours (palette.py): GUI Revision's greys become Fallow's mist; each kind of
  screen takes a family of its own on its top panel, every grey keeping its
  brightness so that the relief stays: wood for the crafting table, the loom,
  the cartography table and the traders, forge for the furnaces, stone for the
  anvil, the grindstone, the stonecutter, the smithing table, the dispenser and
  the crafter, dusk for the enchanting table, the brewing stand and the beacon,
  the End's purple for the shulker box; the player's inventory, the chests, the
  hoppers and the mounts stay mist;
- names: the names GUI Revision drew in English in a few textures are removed;
  every screen's name stands on GUI Revision's plate, centred over its top edge,
  in its engraved capitals (lettering.py), drawn in game by the fallow mod from
  what this script writes (`assets/fallow/titres.json`, the plates of each
  family, the letters): it follows the game's language and a container's name;
- details: the light lip GUI Revision draws above lone slots is left out; its
  small triangles (crafting table, anvil, smithing table, grindstone, trader,
  cartography table) become its furnace's arrow, which the inventory takes too,
  and the trader's blank arrow a small one of the same make; the plus of the
  anvil and the cartography table is drawn like that arrow; each screen without corner
  ornaments of its own takes those of a family of GUI Revision's (the crafting
  table's, the enchanting table's runes, the chest's, the ender chest's), in its
  colours, where no slot, widget or name plate is;
- mods without a GUI Revision compat: their vanilla-grey screens, read from the
  jars cached by `npm run check-fr`, take the mist, their slots and arrows
  become GUI Revision's, their own drawings keep their colours;
- sprites: lists, toasts and containers' sprites follow the same tones by
  brightness, highlights, selected states and progress in lantern light;
  vanilla textures GUI Revision lacks (the crafter, the statistics...) are added
  and dressed alike;
- HUD: the hotbar as a bar with rounded ends, GUI Revision's slots in it;
- widgets: buttons, sliders, checkboxes, tabs, text fields and scrollers are
  drawn by widgets.py;
- menus: their shading and separators in moss and bone, and the title screen's
  panorama, drawn by panorama.py; the Fallow logo (logo.py) and the FancyMenu
  and Drippy layouts of the title and loading screens are written by menus.py;
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

import lettering
import menus
import palette
import panorama
import questlog
import widgets
from palette import BRUME, FAMILIES, lum

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "pack" / "resourcepacks" / "fallow-interface"
CACHE = ROOT / ".cache" / "interface"
JARS = ROOT / ".cache" / "jars"

SOURCES = {
    "gui-revision": ("https://cdn.modrinth.com/data/orgtdk9U/versions/20jHoV4H/GUIRevision_2.2.0.zip",
                     "a25dbde8f0b834832ed532bd37e1caef759b81e9"),
    "gui-revision-compats": ("https://cdn.modrinth.com/data/STMnY6ly/versions/tZn6yME1/GUI%20Revision%20Compats.zip",
                             "3b271708462e750579eff55c86509bd76a8c1997"),
    # Minecraft 1.21.1's client: the reference for what vanilla reads, and the
    # source of the textures GUI Revision does not cover
    "vanilla": ("https://piston-data.mojang.com/v1/objects/30c73b1c5da787909b2f73340419fdf13b9def88/client.jar",
                "30c73b1c5da787909b2f73340419fdf13b9def88"),
    # its French texts, for the width of the names on the screens
    "fr_fr": ("https://resources.download.minecraft.net/0c/0c21164f5592587550d0db2ceb5e489d9fcf5bca",
              "0c21164f5592587550d0db2ceb5e489d9fcf5bca"),
}

# Menu textures drawn behind and between the menus' lists.
MENUS = ["menu_background", "inworld_menu_background", "menu_list_background", "inworld_menu_list_background",
         "header_separator", "footer_separator", "inworld_header_separator", "inworld_footer_separator"]

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

FILL = BRUME[(193, 198, 198)]
OUTLINE = BRUME[(28, 28, 31)]
BONE = BRUME[(218, 231, 230)]
# Vanilla container greys -> Fallow, for the mods without a compat.
VANILLA = {
    (198, 198, 198): FILL,
    (139, 139, 139): BRUME[(131, 139, 139)],
    (55, 55, 55): BRUME[(54, 56, 56)],
    (85, 85, 85): BRUME[(85, 89, 89)],
}
VANILLA_EDGES = {(255, 255, 255): BONE, (0, 0, 0): OUTLINE}
LANTERN_D = (168, 106, 44)
LANTERN = (226, 166, 78)
LANTERN_L = (248, 214, 140)
LANTERN_EDGE = (110, 70, 34)

# ---------------------------------------------------------------- screens

CONTAINER = "assets/minecraft/textures/gui/container/"
# Each vanilla screen: its texture, its family, the height of the screen (the
# player's inventory starts 83 rows above its bottom), the keys of the names it
# can show, and the column its name is centred on when not the panel's middle.
CHESTS = ("container.chest", "container.chestDouble", "container.enderchest", "container.barrel",
          "entity.minecraft.chest_minecart", "block.minecraft.trapped_chest")
MOUNTS = tuple(f"entity.minecraft.{k}" for k in (
    "horse", "donkey", "mule", "llama", "trader_llama", "camel", "skeleton_horse", "zombie_horse"))
PROFESSIONS = ("armorer", "butcher", "cartographer", "cleric", "farmer", "fisherman", "fletcher",
               "leatherworker", "librarian", "mason", "shepherd", "toolsmith", "weaponsmith")
SCREENS = {
    "FurnaceScreen": ("furnace.png", "forge", 166, ("container.furnace",)),
    "BlastFurnaceScreen": ("blast_furnace.png", "forge", 166, ("container.blast_furnace",)),
    "SmokerScreen": ("smoker.png", "forge", 166, ("container.smoker",)),
    "CraftingScreen": ("crafting_table.png", "bois", 166, ("container.crafting",)),
    "LoomScreen": ("loom.png", "bois", 166, ("container.loom",)),
    "CartographyTableScreen": ("cartography_table.png", "bois", 166, ("container.cartography_table",)),
    "MerchantScreen": ("villager.png", "bois", 166, "merchant", 187),
    "AnvilScreen": ("anvil.png", "pierre", 166, ("container.repair",)),
    "GrindstoneScreen": ("grindstone.png", "pierre", 166, ("container.grindstone_title",)),
    "StonecutterScreen": ("stonecutter.png", "pierre", 166, ("container.stonecutter",)),
    "SmithingScreen": ("smithing.png", "pierre", 166, ("container.upgrade",)),
    "CrafterScreen": ("crafter.png", "pierre", 166, ("container.crafter",)),
    "DispenserScreen": ("dispenser.png", "pierre", 166, ("container.dispenser", "container.dropper")),
    "EnchantmentScreen": ("enchanting_table.png", "arcane", 166, ("container.enchant",)),
    "BrewingStandScreen": ("brewing_stand.png", "arcane", 166, ("container.brewing",)),
    "BeaconScreen": ("beacon.png", "arcane", 219, ("container.beacon",)),
    "ShulkerBoxScreen": ("shulker_box.png", "ender", 166, ("container.shulkerBox",)),
    "ContainerScreen": ("generic_54.png", "brume", 222, CHESTS),
    "HopperScreen": ("hopper.png", "brume", 133, ("container.hopper", "entity.minecraft.hopper_minecart")),
    "HorseInventoryScreen": ("horse.png", "brume", 166, MOUNTS),
}
FAMILY_OF = {s[0]: s[1] for s in SCREENS.values()}
HEIGHT_OF = {s[0]: s[2] for s in SCREENS.values()}
# screens whose family covers the whole texture (the traders' offers run its full height)
WHOLE = {"villager.png"}
# the beacon's name over its two labels: the plate stands higher
RAISED = {"BeaconScreen": 6}

# Vanilla screens GUI Revision paints in colours (wood, purple): brought back to
# its greys first so that they take their family like the others.
GREYED = {"crafting_table.png", "loom.png", "cartography_table.png", "enchanting_table.png", "shulker_box.png"}
# Compat screens painted in colours: their own tones mapped to GUI Revision's greys,
# the rest greyed by brightness; `keep` boxes hold the mod's own drawings.
GREYED_COMPATS = {
    "assets/chipped/textures/gui/container/workbench.png": {
        "fixed": {
            (188, 152, 98): (193, 198, 198), (210, 183, 130): (218, 231, 230),
            (159, 127, 79): (162, 166, 166), (144, 113, 66): (131, 139, 139),
            (132, 103, 57): (109, 115, 115), (113, 88, 48): (85, 89, 89),
        },
        "keep": [(0, 147, 76, 256)],  # the tool rack
    },
    # the backpack's leather pocket becomes a darker mist pocket with bone stitches
    "assets/quark/textures/misc/backpack_gui.png": {
        "fixed": {(135, 63, 33): (162, 166, 166), (196, 92, 48): (218, 231, 230), (87, 41, 22): (85, 89, 89)},
    },
}
# Screens whose name GUI Revision draws on a plate over the top edge, and the
# traders' banner ("TRADES") and Supplementaries' "TRADE OFFER" box.
PLATED = {"furnace.png", "blast_furnace.png", "smoker.png", "crafting_table.png", "enchanting_table.png"}
BANNERED = {"villager.png", "red_merchant.png", "orange_trader1.png"}
OFFER = {"red_merchant.png", "orange_trader1.png"}
# Leftovers of the vanilla frame a compat left outside its own (x0, y0, x1, y1): cleared.
LEFTOVERS = {
    "assets/farm_and_charm/textures/gui/silo.png": [(0, 85, 4, 138), (172, 85, 176, 138)],
}
# Screens with corner ornaments of their own (GUI Revision's), and the family of
# ornaments each other screen takes: vanilla by texture, mods by namespace.
OWN_CORNERS = {"crafting_table.png", "enchanting_table.png"}
CORNER_FAMILY = {
    "brewing_stand.png": "runes", "beacon.png": "runes",
    "furnace.png": "key", "blast_furnace.png": "key", "smoker.png": "key", "stonecutter.png": "key",
    "dispenser.png": "key", "hopper.png": "key", "crafter.png": "key", "anvil.png": "key",
    "grindstone.png": "key", "smithing.png": "key",
    "generic_54.png": "rule", "shulker_box.png": "rule", "horse.png": "rule", "loom.png": "rule",
    "villager.png": "rule", "cartography_table.png": "rule",
    "farm_and_charm": "key", "kaleidoscope_cookery": "key", "cold_sweat": "key", "supplementaries": "rule",
    "quark": "rule", "reliable_backpacks": "rule", "immersiveenchanting": "runes",
    "curios": "runes", "exposure": "key", "envelope": "rule",
}
# Where the game draws widgets or items over a panel (x0, y0, x1, y1, inclusive):
# no ornament there.
WIDGETS = {
    "beacon.png": [(164, 107, 211, 128), (20, 109, 99, 124)],
    "anvil.png": [(58, 67, 167, 78)],
    "inventory.png": [(104, 61, 123, 78)],
    "furnace.png": [(20, 34, 39, 51)], "blast_furnace.png": [(20, 34, 39, 51)], "smoker.png": [(20, 34, 39, 51)],
}
# Texts blanked in every screen: the player's inventory label (the screens that
# carry a name plate no longer write it; the others keep their own title).
LANG = {"container.inventory": " "}
# The colour the game writes labels in (0x404040), and Fallow's: the moss ink,
# set by the text shader, for the titles mods write themselves.
TITLE_GREY = 0x40
TITLE_INK = (60, 76, 56)

# GUI Revision's plus of the anvil and the cartography table, cleared (x0, y0,
# x1, y1, inclusive) and drawn again like its arrow, centred on (cx, cy).
PLUSES = {"anvil.png": ((55, 50, 63, 60), (59, 55)), "cartography_table.png": ((18, 37, 27, 47), (23, 42))}
# The mods whose screens take the name plate, by the namespace of their menus:
# their panels are dressed here in the mist, their top edge on the texture's first
# row. The others (their own drawings, their own titles) keep their title.
PLATED_MODS = {m: "brume" for m in (
    "cold_sweat", "farm_and_charm", "naturalist", "overgeared", "quark", "supplementaries")}
# GUI Revision's small triangles between a screen's inputs and its result (x0,
# y0, x1, y1, inclusive), cleared and replaced by its furnace's arrow, centred on them.
TRIANGLES = {"crafting_table.png": (102, 39, 106, 47), "anvil.png": (110, 50, 114, 58),
             "smithing.png": (79, 52, 83, 60), "grindstone.png": (101, 37, 105, 45),
             "villager.png": (198, 40, 202, 48), "cartography_table.png": (48, 38, 51, 45)}
# Arrows a mod draws in vanilla's shape, found in vanilla's slot grey.
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

# Sprites (under textures/gui/sprites) and screens whose greys follow Fallow's tones
# by brightness; the colours a mod or the game gives meaning to (red crosses,
# green ticks, pings, boss bars) are not greys and stay.
RETONED = tuple("assets/minecraft/textures/gui/" + d for d in (
    "sprites/widget/", "sprites/social_interactions/", "sprites/toast/",
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
    "container/brewing_stand/fuel_length": "lantern", "container/brewing_stand/brew_progress": "lantern",
    **{f"container/{f}/{s}": "lantern" for f in ("furnace", "blast_furnace", "smoker")
       for s in ("burn_progress", "lit_progress")},
    # the lists' and the recipe book's arrows, green in GUI Revision when pointed at
    **{k: "lantern" for k in (
        "server_list/join_highlighted", "server_list/move_up_highlighted", "server_list/move_down_highlighted",
        "transferable_list/select_highlighted", "transferable_list/unselect_highlighted",
        "transferable_list/move_up_highlighted", "transferable_list/move_down_highlighted",
        "world_list/join_highlighted", "world_list/marked_join_highlighted",
        "recipe_book/page_forward_highlighted", "recipe_book/page_backward_highlighted")},
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
    "assets/curios/textures/gui/curios/inventory.png": [
        ((33, 38, 54, 49), {(255, 255, 255): BRUME[(131, 139, 139)]}), ((32, 24, 54, 51), "neutral"),
        ((76, 0, 81, 8), "neutral")],
    "assets/companions/textures/gui/cornelius_gui_bottom.png": [
        ((61, 171, 63, 253), {(0, 0, 0): OUTLINE, (255, 255, 255): BONE})],
    "assets/quark/textures/gui/general_icons.png": [((0, 159, 17, 165), "lantern")],
    # the crafter's lit arrow, stored on the panel's grey
    "assets/quark/textures/gui/container/crafter.png": [((176, 17, 192, 29), {FILL: None})],
    "assets/minecraft/textures/gui/sprites/social_interactions/background.png": [
        ((7, 25, 8, 26), BRUME[(131, 139, 139)]), ((228, 25, 229, 26), BRUME[(131, 139, 139)])],
    "assets/immersiveenchanting/textures/gui/container/enchanting_table.png": [((227, 126, 256, 222), "neutral")],
}


# ---------------------------------------------------------------- sources

def download(name, suffix):
    url, sha1 = SOURCES[name]
    path = CACHE / f"{name}.{suffix}"
    if not path.exists() or hashlib.sha1(path.read_bytes()).hexdigest() != sha1:
        CACHE.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(url, timeout=300) as response:
            data = response.read()
        if hashlib.sha1(data).hexdigest() != sha1:
            raise SystemExit(f"{name}: checksum mismatch")
        path.write_bytes(data)
    return path


def fetch(name):
    return zipfile.ZipFile(download(name, "zip"))


def png(data):
    return Image.open(io.BytesIO(data)).convert("RGBA")


def names(keys):
    """The names a screen can show, in French and in English."""
    texts = (json.loads(download("fr_fr", "json").read_text(encoding="utf-8")),
             json.loads(fetch("vanilla").read("assets/minecraft/lang/en_us.json")))
    out = []
    for t in texts:
        if keys == "merchant":
            out += [t["merchant.title"].replace("%s", t[f"entity.minecraft.villager.{p}"], 1)
                    .replace("%s", t[f"merchant.level.{lv}"], 1) for p in PROFESSIONS for lv in range(1, 6)]
            out.append(t["entity.minecraft.wandering_trader"])
        else:
            out += [t[k] for k in keys]
    return out


# ---------------------------------------------------------------- colours

def tone(v, family=BRUME):
    """A family's colour for a grey of luminance v, along GUI Revision's ramp."""
    ramp = sorted((lum(k), c) for k, c in family.items())
    if v <= ramp[0][0]:
        return ramp[0][1]
    for (l0, c0), (l1, c1) in zip(ramp, ramp[1:]):
        if v <= l1:
            t = (v - l0) / (l1 - l0)
            return tuple(round(a + (b - a) * t) for a, b in zip(c0, c1))
    return ramp[-1][1]


def top_panel(im, height, whole=False):
    """The pixels of a screen's top panel, outline included: from its middle, over
    everything but GUI Revision's outline, above the player's inventory (83 rows
    above the screen's bottom); every pixel for a screen coloured whole."""
    px = im.load()
    if whole:  # a screen in one colour, the trader's
        return {(x, y) for y in range(im.height) for x in range(im.width) if px[x, y][3]}
    width = min(im.width, 256)
    limit = height - 83
    cx = 88
    y0 = next((y for y in range(limit) if px[cx, y][3]), None)
    if y0 is None:
        return set()
    seed = (cx, y0 + 6)
    seen, todo = {seed}, [seed]
    outline = (28, 28, 31)
    while todo:
        x, y = todo.pop()
        for p in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if 0 <= p[0] < width and 0 <= p[1] < limit and p not in seen and px[p][3] and px[p][:3] != outline:
                seen.add(p)
                todo.append(p)
    rim = {(x + a, y + b) for x, y in seen for a in (-1, 0, 1) for b in (-1, 0, 1)}
    return seen | {p for p in rim if 0 <= p[0] < im.width and 0 <= p[1] < im.height
                   and px[p][3] and px[p][:3] == outline}


def recolour(im, family="brume", region=()):
    """GUI Revision's greys to Fallow's: the family's in the region, mist elsewhere."""
    px = im.load()
    fam = FAMILIES[family]
    for y in range(im.height):
        for x in range(im.width):
            r, g, b, a = px[x, y]
            k = palette.NEAR.get((r, g, b), (r, g, b))
            if a and k in BRUME:
                px[x, y] = (fam if (x, y) in region else BRUME)[k] + (a,)
    return im


def greyify(im, fixed=None, keep=()):
    """Maps a coloured screen onto GUI Revision's greys, its main colour to the panel
    grey. `fixed` maps a screen's own tones (its panel first) to given greys;
    otherwise the panel colour is read in the left margin. Everything in the
    `keep` boxes keeps its colours."""
    greys = [g for g in palette.GREYS if g not in ((100, 107, 107), (125, 130, 130))]
    levels = [lum(g) for g in greys]
    px = im.load()
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
            if not p[3] or p[:3] in greys:
                continue
            v = lum(p) * scale
            px[x, y] = greys[min(range(len(levels)), key=lambda k: abs(levels[k] - v))] + (p[3],)


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


FALLOW = {c for f in FAMILIES.values() for c in f.values()} | {LANTERN_D, LANTERN, LANTERN_L, LANTERN_EDGE}


def retone(im, mode, box=None, family=BRUME):
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
            new = lantern_tone(v) if mode == "lantern" else tone(v * 0.8, family) if mode == "dim" \
                else tone(v, family)
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


# ---------------------------------------------------------------- names

def clear_plate(im):
    """Removes the name plate GUI Revision draws over the top edge: each of its
    columns takes the column of the panel it hides (or the mirror of a frame
    running under it)."""
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


def panel_top(im, cx):
    px = im.load()
    return next(y for y in range(im.height) if px[cx, y][3])


def plate_box(name, im):
    """Where the name plate of a vanilla screen stands on its texture (x0, y0, x1,
    y1, inclusive), for its longest name."""
    for cls, (tex, _, _, keys, *centre) in SCREENS.items():
        if tex == name:
            cx = centre[0] if centre else 88
            w = max(lettering.plate_width(n) for n in names(keys))
            top = panel_top(im, cx)
            raise_ = RAISED.get(cls, 3)
            return cx - w // 2, top - raise_, cx + w - w // 2, top - raise_ + lettering.PLATE_HEIGHT
    return None


def write_titles(out, textures):
    """What the fallow mod reads to draw the names: each screen's top edge and
    family, the families' plates and inks, the letters."""
    fallow = out / "assets" / "fallow"
    sprites = fallow / "textures" / "gui" / "sprites" / "titre"
    sprites.mkdir(parents=True, exist_ok=True)
    families = {}
    for family in FAMILIES:
        tones = palette.tones(family)
        # four columns of plate between its ends, which the game tiles to the name's width
        w = 2 * lettering.PLATE_PAD + 4
        im = Image.new("RGBA", (w + 1, lettering.PLATE_HEIGHT + 1))
        for p, c in lettering.plate(w, tones).items():
            im.putpixel(p, c)
        im.save(sprites / f"{family}.png")
        side = lettering.PLATE_PAD
        (sprites / f"{family}.png.mcmeta").write_text(json.dumps({"gui": {"scaling": {
            "type": "nine_slice", "width": w + 1, "height": lettering.PLATE_HEIGHT + 1,
            "border": {"left": side, "top": 7, "right": side + 1, "bottom": 7}}}}, indent=2) + "\n")
        families[family] = {"encre": palette.hexa(tones["ink"]), "lumiere": palette.hexa(tones["light"])}
    atlas, table = lettering.atlas()
    (fallow / "textures" / "gui" / "titre").mkdir(parents=True, exist_ok=True)
    atlas.save(fallow / "textures" / "gui" / "titre" / "lettres.png")
    screens = {}
    for cls, (tex, family, _, _, *centre) in SCREENS.items():
        cx = centre[0] if centre else 88
        entry = {"famille": family, "haut": panel_top(textures[tex], cx)}
        if centre:
            entry["centre"] = cx
        if cls in RAISED:
            entry["dessus"] = RAISED[cls]
        screens[cls] = entry
    (fallow / "titres.json").write_text(json.dumps({
        "ecrans": screens, "mods": PLATED_MODS, "sans": [], "familles": families,
        "lettres": {"largeur": atlas.width, "hauteur": lettering.HEIGHT, "espace": lettering.SPACE,
                    "table": table},
    }, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


# ---------------------------------------------------------------- details

def template(im, box, bg):
    px = im.load()
    return {(x - box[0], y - box[1]): px[x, y][:3] for y in range(box[1], box[3])
            for x in range(box[0], box[2]) if px[x, y][3] and px[x, y][:3] != bg}


def stamp(im, tpl, x0, y0, colours=None):
    px = im.load()
    for (x, y), c in tpl.items():
        c = colours.get(c, c) if colours else c
        px[x0 + x, y0 + y] = c + (255,)


def engraved(pts):
    """A shape cut into the panel like GUI Revision's arrow: dark on its upper and
    left edges, light on its lower and right ones, the slot grey inside."""
    out = {}
    for x, y in pts:
        if (x, y - 1) not in pts or (x - 1, y) not in pts:
            out[(x, y)] = (85, 89, 89)
        elif (x, y + 1) not in pts or (x + 1, y) not in pts:
            out[(x, y)] = (218, 231, 230)
        else:
            out[(x, y)] = (131, 139, 139)
    return out


def plus_shape(size=11, arm=5):
    lo = (size - arm) // 2
    return engraved({(x, y) for y in range(size) for x in range(size)
                     if lo <= x < lo + arm or lo <= y < lo + arm})


def trade_arrow():
    """The trader's arrow, which GUI Revision leaves blank: a small one of its
    furnace's, ten by nine."""
    pts = {(x, y) for y in range(3, 6) for x in range(5)}
    pts |= {(x, y) for y in range(9) for x in range(5, 5 + 5 - abs(y - 4))}
    im = Image.new("RGBA", (10, 9))
    for p, c in engraved(pts).items():
        im.putpixel(p, c + (255,))
    return im


def clear_slot_lips(im, region):
    """The light lip GUI Revision draws above a lone slot (a 16-pixel run of light
    over the slot's dark top, bare panel above it) becomes panel."""
    px = im.load()
    light, dark, slot, panel = (218, 231, 230), (85, 89, 89), (131, 139, 139), (193, 198, 198)
    for y in range(1, im.height - 1):
        for x in range(im.width - 15):
            if (x, y) not in region:
                continue
            if all(px[x + i, y][:3] == light and px[x + i, y - 1][:3] == panel for i in range(16)) \
                    and px[x, y + 1][:3] == slot and px[x + 15, y + 1][:3] == slot \
                    and all(px[x + i, y + 1][:3] == dark for i in range(1, 15)):
                for i in range(16):
                    px[x + i, y] = panel + (255,)


def slot(im, x, y):
    """GUI Revision's slot, its item at (x, y): a dark top run between slot-grey
    corners, a second dark row, the sides shaded, light bottom corners."""
    px = im.load()
    d, s, side, low = (85, 89, 89), (131, 139, 139), (109, 115, 115), (162, 166, 166)
    for j in range(16):
        for i in range(16):
            c = s
            if j == 0:
                c = s if i in (0, 15) else d
            elif j == 1:
                c = side if i in (0, 15) else d
            elif j == 2 and i in (0, 1, 14, 15):
                c = side
            elif j == 15 and i in (0, 15):
                c = low
            px[x + i, y + j] = c + (255,)


CORNERS_ART = {
    "fleur": [".ll..lll", "lddl.ddd", "dmmd.mm.", "mmmm....", ".mm.....", "ll......", "dd......", "mm......",
              "m......."],
    "runes": ["ddd.dd.ddd", "m.m.m..m.m", "mdmdmd....", "..m.m.....", "ddmdm.....", "m.m......."],
    "rule": ["dddd.dd.ddd.", "mmm..m..m...", "mmm.dm.dmd..", "m...........", "..d.........",
             "ddm.........", "m...........", "............", "d..........."],
    "key": ["ddddd.dddd.d", "m...m....mdm", "m.....d.....", "m..dddm.....", "md.m........", "...m........",
            "d.dm........", "m...........", "m...........", "md..........", ".m.........."],
    "small": [".ll.", "lddl", "dmmd", "mmmm", ".mm."],
}


def corner_family(path):
    name = path.rsplit("/", 1)[1]
    if path.startswith("assets/minecraft/"):
        return CORNER_FAMILY.get(name, "fleur")
    return CORNER_FAMILY.get(path.split("/")[1], "fleur")


def panel_inside(im, fill):
    """Bounding box (x0, y0, x1, y1, inclusive) of the largest stretch of bare
    panel, or None when the texture has no panel large enough to dress."""
    px = im.load()
    width, height = im.size
    bare = fill + (255,)
    seen, best = set(), None
    for y in range(height):
        for x in range(width):
            if (x, y) in seen or px[x, y] != bare:
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
                    if 0 <= k < width and 0 <= l < height and (k, l) not in seen and px[k, l] == bare:
                        seen.add((k, l))
                        todo.append((k, l))
            if n >= 2000 and x1 - x0 >= 100 and y1 - y0 >= 20 and (best is None or n > best[0]):
                best = (n, (x0, y0, x1, y1))
    return best and best[1]


def add_corners(im, path, family="brume", plate=None):
    """GUI Revision's corner ornament of the screen's family in the four corners of
    its main panel, two pixels in from the side and one from the top or bottom,
    with a pixel of bare panel around, in the screen's colours; where any corner
    has no room (a slot, a widget the game draws there, the name plate), the
    small ornament in all four, or none."""
    fam = FAMILIES[family]
    inside = panel_inside(im, fam[(193, 198, 198)])
    if inside is None:
        return
    x0, y0, x1, y1 = inside
    name = path.rsplit("/", 1)[1]
    widgets_ = WIDGETS.get(name, []) if path.startswith("assets/minecraft/") else []
    if plate:
        widgets_ = widgets_ + [plate]
    tones = {"l": fam[(218, 231, 230)], "d": fam[(109, 115, 115)], "m": fam[(131, 139, 139)]}
    px = im.load()
    bare = fam[(193, 198, 198)]

    def free(p):
        return 0 <= p[0] < im.width and 0 <= p[1] < im.height and px[p][3] and px[p][:3] == bare

    def near(p, boxes, m):
        return any(a - m <= p[0] <= c + m and b - m <= p[1] <= d + m for a, b, c, d in boxes)

    for kind in (corner_family(path), "small"):
        art = CORNERS_ART[kind]
        motif = {(u, v): tones[c] for v, row in enumerate(art) for u, c in enumerate(row) if c != "."}
        w, h = len(art[0]), len(art)
        if x1 - x0 < 2 * w + 8 or y1 - y0 < 2 * h + 4:
            continue
        stamps = [{(x0 + 2 + u, y0 + 1 + v): c for (u, v), c in motif.items()},
                  {(x1 - 2 - u, y0 + 1 + v): c for (u, v), c in motif.items()},
                  {(x0 + 2 + u, y1 - 1 - v): c for (u, v), c in motif.items()},
                  {(x1 - 2 - u, y1 - 1 - v): c for (u, v), c in motif.items()}]
        placed = []
        for one in stamps:
            halo = {(x + a, y + b) for x, y in one for a in (-1, 0, 1) for b in (-1, 0, 1)} - set(one)
            if not all(free(p) for p in one) or not all(free(p) or not (x0 <= p[0] <= x1 and y0 <= p[1] <= y1)
                                                        for p in halo) \
                    or any(near(p, widgets_, 2) for p in set(one) | halo):
                break
            placed.append((one, halo))
        else:
            for one, halo in placed:
                for p in halo:
                    if free(p):
                        px[p] = bare + (255,)
                for p, c in one.items():
                    px[p] = c + (255,)
            return


def edge_rings(im):
    """Pure black and white left on a panel's outer rings (within two pixels of its
    edge) take the outline and the bone light."""
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
            px[x, y] = (OUTLINE if p[:3] == (0, 0, 0) else BONE) + (p[3],)


def big_panel(path, im):
    if "/sprites/" in path and "/sprites/container/" not in path:
        return False
    px = im.load()
    return sum(1 for y in range(0, im.height, 2) for x in range(0, im.width, 2)
               if px[x, y][:3] == FILL and px[x, y][3]) * 4 >= 1500


def find_vanilla_slots(im):
    """Vanilla slots: an 18x18 cell, dark top-left edge, white bottom-right edge."""
    px = im.load()
    width, height = im.size
    dark, fill, white = (55, 55, 55), (139, 139, 139), (255, 255, 255)
    found = []
    for y in range(height - 17):
        for x in range(width - 17):
            if (px[x, y][:3] == dark and px[x + 16, y][:3] == dark and px[x + 17, y][:3] == fill
                    and px[x, y + 16][:3] == dark and px[x, y + 17][:3] == fill
                    and px[x + 17, y + 17][:3] == white and px[x + 1, y + 1][:3] == fill
                    and all(px[x + i, y][:3] == dark for i in range(17))
                    and all(px[x + 1 + i, y + 1 + j][:3] == fill for i in range(16) for j in range(16))):
                found.append((x, y))
    return found


def dress_vanilla(im, n, arrow):
    """A screen drawn in vanilla's greys (a mod's, or the game's where GUI Revision
    has none) in GUI Revision's way: the mist, its slots, its arrow; returns
    whether it had vanilla's greys."""
    slots = find_vanilla_slots(im)
    tpl = {(x, y): (139, 139, 139) for y, row in enumerate(VANILLA_ARROW)
           for x, c in enumerate(row) if c == "#"}
    arrows = matches(im, tpl, alone=True)
    for ox, oy in arrows:
        for dx, dy in tpl:
            im.putpixel((ox + dx, oy + dy), (198, 198, 198, 255))
    for x, y in slots:
        for j in range(18):
            for i in range(18):
                im.putpixel((x + i, y + j), (198, 198, 198, 255))
    if not recolour_vanilla(im):
        return False
    for x, y in slots:
        slot(im, x + 1, y + 1)
    aw = max(a for a, b in arrow) + 1
    ah = max(b for a, b in arrow) + 1
    for ox, oy in arrows:
        stamp(im, arrow, ox + (22 - aw) // 2, oy + (15 - ah) // 2)
    recolour(im)
    return True


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


# ---------------------------------------------------------------- HUD

def rounded(w, h, r):
    """Mask of a rectangle with corners of radius r."""
    def inside(i, j):
        dx = max(r - i, i - (w - 1 - r), 0)
        dy = max(r - j, j - (h - 1 - r), 0)
        return 0 <= i < w and 0 <= j < h and dx * dx + dy * dy <= r * r + 0.5
    return inside


def draw_bar(im, x0, y0, w, h):
    """A mist bar with rounded ends, its relief GUI Revision's: an outline, a bone
    light along its top, a shade along its bottom, a soft shadow under it."""
    px = im.load()
    inside = rounded(w, h, 5)
    for j in range(h + 1):
        for i in range(w):
            if inside(i, j):
                edge = any(not inside(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                c = OUTLINE if edge else BONE if not inside(i, j - 2) else \
                    BRUME[(162, 166, 166)] if not inside(i, j + 2) else FILL
                px[x0 + i, y0 + j] = c + (255,)
            elif inside(i, j - 1):
                px[x0 + i, y0 + j] = OUTLINE + (110,)


def draw_hud(hud):
    """The hotbar as a bar with rounded ends and GUI Revision's slots; the
    selection and the offhand slot to match, in the lantern's light."""
    bar = Image.new("RGBA", (182, 22))
    draw_bar(bar, 0, 0, 182, 21)
    for i in range(9):
        slot(bar, 3 + 20 * i, 3)
    recolour(bar)
    bar.save(hud / "hotbar.png")

    sel = Image.new("RGBA", (24, 23))
    px = sel.load()
    outer = rounded(24, 23, 5)
    for j in range(23):
        for i in range(24):
            if not outer(i, j) or (4 <= i < 20 and 4 <= j < 20):
                continue
            edge = any(not outer(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            ring = 3 <= i < 21 and 3 <= j < 21
            c = LANTERN_EDGE if edge or ring else LANTERN_L if j < 3 else LANTERN_D if j > 19 else LANTERN
            px[i, j] = c + (255,)
    sel.save(hud / "hotbar_selection.png")

    for name, x0 in (("hotbar_offhand_left.png", 0), ("hotbar_offhand_right.png", 7)):
        im = Image.new("RGBA", (29, 24))
        draw_bar(im, x0, 1, 22, 21)
        slot(im, x0 + 3, 4)
        recolour(im)
        im.save(hud / name)


# ---------------------------------------------------------------- sprites

# Sprites whose selected state lights up in the lantern's light, from their
# plain state (GUI Revision draws them darker, as if sunk): the creative and
# advancement tabs, the beacon's effects, the stonecutter's and loom's choices.
SELECTED = {
    **{f"container/creative_inventory/tab_{side}_selected_{i}": f"container/creative_inventory/tab_{side}_unselected_{i}"
       for side in ("top", "bottom") for i in range(1, 8)},
    **{f"advancements/tab_{side}_{part}_selected": f"advancements/tab_{side}_{part}"
       for side, parts in (("above", ("left", "middle", "right")), ("below", ("left", "middle", "right")),
                           ("left", ("top", "middle", "bottom")), ("right", ("top", "middle", "bottom")))
       for part in parts},
    "container/beacon/button_selected": "container/beacon/button",
    "container/stonecutter/recipe_selected": "container/stonecutter/recipe",
    "container/loom/pattern_selected": "container/loom/pattern",
}
# Vanilla sprites GUI Revision lacks, taken from the game and toned.
TONED = {
    "container/crafter/powered_redstone": "glow",
    "hud/hotbar_attack_indicator_progress": "lantern",
    "hud/hotbar_attack_indicator_background": "all",
    "widget/page_forward": "all", "widget/page_backward": "all",
    "widget/page_forward_highlighted": "lantern", "widget/page_backward_highlighted": "lantern",
    **{f"icon/{k}": "all" for k in ("accessibility", "language", "link", "news", "invite", "draft_report")},
    "icon/link_highlighted": "lantern",
}
# the empty slots' placeholders (armour, smithing), drawn by the game in greys
EMPTY_SLOTS = ("empty_armor_slot_helmet", "empty_armor_slot_chestplate", "empty_armor_slot_leggings",
               "empty_armor_slot_boots", "empty_armor_slot_shield", "empty_slot_sword", "empty_slot_axe",
               "empty_slot_pickaxe", "empty_slot_shovel", "empty_slot_hoe", "empty_slot_diamond",
               "empty_slot_emerald", "empty_slot_ingot", "empty_slot_lapis_lazuli", "empty_slot_quartz",
               "empty_slot_redstone_dust", "empty_slot_amethyst_shard", "empty_slot_smithing_template_armor_trim",
               "empty_slot_smithing_template_netherite_upgrade")
# advancement map backgrounds whose colours clash: brought halfway to the moss
CLASHING = ("nether", "husbandry")


def sprite_family(path):
    """The family of a screen's own sprites (sprites/container/<screen>/)."""
    m = re.match(r"assets/minecraft/textures/gui/sprites/container/([^/]+)/", path)
    return m and FAMILY_OF.get(m.group(1) + ".png")


def dress_sprites(vanilla):
    """Last sprites: the advancements' three frame shapes, the selected states, the
    blinking heart containers, the disabled enchantment, the empty slots and the
    vanilla sprites GUI Revision lacks."""
    gui = OUT / "assets" / "minecraft" / "textures" / "gui"
    sprites = gui / "sprites"

    def ours(name):
        return Image.open(sprites / f"{name}.png").convert("RGBA")

    def game(path):
        return png(vanilla.read(f"assets/minecraft/textures/{path}.png"))

    # the goal's and challenge's frames keep their own outline (round, notched),
    # in the colours Fallow gives the task's square one, pixel role by pixel role
    for state in ("obtained", "unobtained"):
        task, ref = ours(f"advancements/task_frame_{state}"), game(f"gui/sprites/advancements/task_frame_{state}")
        roles = {}
        for y in range(ref.height):
            for x in range(ref.width):
                if ref.getpixel((x, y))[3]:
                    roles.setdefault(ref.getpixel((x, y)), Counter())[task.getpixel((x, y))] += 1
        for kind in ("goal", "challenge"):
            im = game(f"gui/sprites/advancements/{kind}_frame_{state}")
            px = im.load()
            for y in range(im.height):
                for x in range(im.width):
                    if px[x, y][3] and px[x, y] in roles:
                        px[x, y] = roles[px[x, y]].most_common(1)[0][0]
            im.save(sprites / "advancements" / f"{kind}_frame_{state}.png")

    for selected, plain in SELECTED.items():
        if (sprites / f"{plain}.png").exists():
            im = ours(plain)
            retone(im, "lantern")
            im.save(sprites / f"{selected}.png")

    # the heart containers flashing when hurt: their outline in bone instead of white
    for name in ("container", "container_hardcore"):
        im = ours(f"hud/heart/{name}")
        px = im.load()
        rim = [(x, y) for y in range(im.height) for x in range(im.width) if px[x, y][3] and any(
            not (0 <= x + a < im.width and 0 <= y + b < im.height) or not px[x + a, y + b][3]
            for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        for x, y in rim:
            px[x, y] = BONE + (255,)
        im.save(sprites / "hud" / "heart" / f"{name}_blinking.png")

    # a disabled enchantment: the plain bar, a step darker; the anvil's name field likewise
    for name in ("container/enchanting_table/enchantment_slot", "container/anvil/text_field"):
        im = ours(name)
        retone(im, "dim", family=FAMILIES[sprite_family(f"assets/minecraft/textures/gui/sprites/{name}.png")])
        im.save(sprites / f"{name}_disabled.png")

    for name, mode in TONED.items():
        im = game(f"gui/sprites/{name}")
        retone(im, mode)
        dest = sprites / f"{name}.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        im.save(dest)

    items = OUT / "assets" / "minecraft" / "textures" / "item"
    items.mkdir(parents=True, exist_ok=True)
    for name in EMPTY_SLOTS:
        im = game(f"item/{name}")
        px = im.load()
        for y in range(im.height):
            for x in range(im.width):
                if px[x, y][3]:  # the line a step darker than the slot, its light a step lighter
                    px[x, y] = (BRUME[(109, 115, 115)] if lum(px[x, y]) < 128 else BRUME[(162, 166, 166)]) \
                        + (px[x, y][3],)
        im.save(items / f"{name}.png")

    for name in CLASHING:
        im = game(f"gui/advancements/backgrounds/{name}")
        px = im.load()
        for y in range(im.height):
            for x in range(im.width):
                r, g, b, a = px[x, y]
                grey = tone(lum((r, g, b)) * 0.9)
                px[x, y] = tuple(round((c + 2 * t) / 3) for c, t in zip((r, g, b), grey)) + (a,)
        dest = gui / "advancements" / "backgrounds" / f"{name}.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        im.save(dest)


def menu_background(darker):
    """The shading laid over the panorama or the world behind the menus (the game
    maps 32 pixels of screen to the whole texture): a plain moss veil, darker
    behind the lists."""
    return Image.new("RGBA", (32, 32), (26, 31, 28, 140) if darker else (26, 31, 28, 96))


# The pack icon's lantern, 14x20 so that it sits in the middle of the 32-pixel
# panel: a ring, a two-tier moss cap lit from the left, the glass in lantern light
# around a flame, a rim and two feet.
LANTERN_ICON = [
    "......OO......",
    ".....O..O.....",
    ".....O..O.....",
    "......OO......",
    "....OOOOOO....",
    "...OmMMMMdO...",
    "..OmMMMMMMdO..",
    ".OOOOOOOOOOOO.",
    "..OmDDDDDDdO..",
    "..OmgYYYYLdO..",
    "..OmgYFFYYdO..",
    "..OmYFHHFYdO..",
    "..OmYFHHFYdO..",
    "..OmYYFFYYdO..",
    "..OmLYYYYLdO..",
    "..OmDLLLLDdO..",
    ".OOOOOOOOOOOO.",
    "..OmMMMMMMdO..",
    "...OOOOOOOO...",
    "...OO....OO...",
]
# the leaf sown on the icon's panel
LEAF = [".....##", "...####", "..###.#", ".##.##.", ".#.###.", ".####..", "#......"]


def draw_icon():
    """pack.png: a small mist panel sown with leaves, and a lit lantern in its
    middle casting a soft glow on it."""
    im = Image.new("RGBA", (32, 32))
    px = im.load()
    inside = rounded(32, 32, 4)
    leaf_tone = (195, 194, 173)
    for j in range(32):
        for i in range(32):
            if inside(i, j):
                edge = any(not inside(i + a, j + b) for a, b in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                c = (OUTLINE if edge else BONE if not inside(i, j - 2) else
                     BRUME[(131, 139, 139)] if not inside(i + 2, j) or not inside(i - 2, j) or not inside(i, j + 2)
                     else FILL)
                if c == FILL:
                    u, v = (i - (5 if (j // 8) % 2 else 0)) % 10, j % 8
                    if v < 7 and LEAF[v][u:u + 1] == "#":
                        c = leaf_tone
                px[i, j] = c + (255,)
    colours = {"O": OUTLINE, "M": BRUME[(85, 89, 89)], "m": BRUME[(131, 139, 139)], "d": BRUME[(54, 56, 56)],
               "D": LANTERN_D, "L": LANTERN, "Y": LANTERN_L, "F": (255, 226, 160), "H": (255, 250, 235),
               "g": (255, 246, 222)}
    w, h = len(LANTERN_ICON[0]), len(LANTERN_ICON)
    x0, y0 = (32 - w) // 2, (32 - h) // 2
    glass = {(x0 + x, y0 + y) for y, row in enumerate(LANTERN_ICON) for x, c in enumerate(row) if c in "DLYFHg"}
    for i in range(4, 28):  # the glow on the panel, fading over four pixels from the glass
        for j in range(4, 28):
            d = min(max(abs(i - x), abs(j - y)) for x, y in glass)
            if 0 < d <= 4 and px[i, j][:3] in (FILL, leaf_tone):
                t = (5 - d) / 9
                px[i, j] = tuple(round(a + (b - a) * t) for a, b in zip(px[i, j][:3], LANTERN_L)) + (255,)
    for y, row in enumerate(LANTERN_ICON):
        for x, c in enumerate(row):
            if c != ".":
                px[x0 + x, y0 + y] = colours[c] + (255,)
    return im.resize((128, 128), Image.NEAREST)


def crafter(dispenser):
    """The crafter's screen, which GUI Revision lacks: the dispenser's, its 3x3 grid
    moved to the crafter's place, a slot where the game puts the result (both in
    GUI Revision's greys)."""
    im = dispenser.copy()
    px = im.load()
    src = dispenser.load()
    panel = (193, 198, 198, 255)
    for y in range(12, 76):
        for x in range(56, 122):
            px[x, y] = panel
    for y in range(12, 76):
        for x in range(56, 122):
            if src[x, y] != panel:
                px[x - 36, y] = src[x, y]
    slot(im, 134, 35)
    return im


# ---------------------------------------------------------------- build

def main():
    rev = fetch("gui-revision")
    compats = fetch("gui-revision-compats")
    vanilla = fetch("vanilla")

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

    furnace = png(files[CONTAINER + "furnace.png"])
    arrow = template(furnace, (84, 36, 99, 49), (193, 198, 198))
    files[CONTAINER + "crafter.png"] = None  # drawn from the dispenser's

    if OUT.exists():
        shutil.rmtree(OUT / "assets")
    OUT.mkdir(parents=True, exist_ok=True)
    built = {}

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

    chipped = GREYED_COMPATS["assets/chipped/textures/gui/container/workbench.png"]["fixed"]
    for path, data in sorted(files.items()):
        dest = OUT / path
        if not path.endswith(".png"):
            dest.parent.mkdir(parents=True, exist_ok=True)
            dest.write_bytes(data)
            continue
        name = path.rsplit("/", 1)[1]
        im = crafter(png(files[CONTAINER + "dispenser.png"])) if data is None else png(data)
        if path.endswith("/container/villager/trade_arrow.png"):
            im = trade_arrow()
        vanilla_screen = path.startswith(CONTAINER)
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
            spec = {"fixed": chipped}
        if (vanilla_screen and name in GREYED) or spec:
            greyify(im, spec.get("fixed"), keep=spec.get("keep", ()))
        if name == "anvil.png":  # GUI Revision's red mark under the name field
            px = im.load()
            for y in range(im.height):
                for x in range(im.width):
                    if px[x, y] == (255, 0, 0, 255):
                        px[x, y] = (193, 198, 198, 255)
        family, region = "brume", set()
        if vanilla_screen and name in FAMILY_OF:
            family = FAMILY_OF[name]
            region = top_panel(im, HEIGHT_OF[name], whole=name in WHOLE)
            clear_slot_lips(im, region)
        elif sprite_family(path):
            family = sprite_family(path)
            region = {(x, y) for y in range(im.height) for x in range(im.width)}
        if vanilla_screen and name in PLUSES:
            (a, b, c, d), (cx, cy) = PLUSES[name]
            px = im.load()
            for y in range(b, d + 1):
                for x in range(a, c + 1):
                    px[x, y] = (193, 198, 198, 255)
            stamp(im, plus_shape(), cx - 5, cy - 5)
        if vanilla_screen and name in TRIANGLES:
            a, b, c, d = TRIANGLES[name]
            px = im.load()
            for y in range(b, d + 1):
                for x in range(a, c + 1):
                    px[x, y] = (193, 198, 198, 255)
            aw = max(x for x, _ in arrow) + 1
            ah = max(y for _, y in arrow) + 1
            stamp(im, arrow, (a + c + 1 - aw) // 2, (b + d + 1 - ah) // 2)
        if vanilla_screen and name == "inventory.png":  # the arrow GUI Revision leaves out
            stamp(im, arrow, 137, 30)
        recolour(im, family, region)
        built[path] = (im, family)

    # last touches, then the corner ornaments, around each name plate
    textures = {}
    for path, (im, family) in built.items():
        name = path.rsplit("/", 1)[1]
        if path in SPRITE_MODES:
            retone(im, SPRITE_MODES[path], family=FAMILIES[family])
        elif path.startswith(RETONED):
            retone(im, "neutral", family=FAMILIES[family])
        for box, action in TOUCH_UPS.get(path, []):
            touch_up(im, box, action)
        namespace = path.split("/")[1]
        if namespace in COMPAT_NAMESPACES:
            touch_up(im, (0, 0, im.width, im.height), dict(VANILLA))
        if big_panel(path, im) or (path.startswith(CONTAINER) and name in FAMILY_OF):
            edge_rings(im)
            if not (path.startswith(CONTAINER) and name in OWN_CORNERS):
                plate = plate_box(name, im) if path.startswith(CONTAINER) else None
                add_corners(im, path, family if path.startswith(CONTAINER) else "brume", plate)
        if path.startswith(CONTAINER):
            textures[name] = im
        save(path, im)

    # vanilla-grey screens of the mods without a compat
    for z in jars:
        for n in z.namelist():
            m = re.match(r"assets/([^/]+)/textures/gui/(.+\.png)$", n)
            if m and m.group(1) in RECOLOUR_MODS and origin[n] is z:
                im = png(z.read(n))
                if dress_vanilla(im, n, arrow):
                    if big_panel(n, im) and "/sprites/" not in n:
                        add_corners(im, n)
                    save(n, im)

    # vanilla textures GUI Revision does not cover: the sprites of widgets and
    # containers, retoned
    for n in sorted(origin):
        if origin[n] is not vanilla or not n.endswith(".png") or n in files:
            continue
        if n.startswith(tuple(f"assets/minecraft/textures/gui/sprites/{d}/" for d in (
                "widget", "container", "popup", "social_interactions", "statistics"))):
            im = png(vanilla.read(n))
            retone(im, "neutral", family=FAMILIES[sprite_family(n) or "brume"])
            save(n, im)

    # the menus: their shading and separators in moss and bone, the demo's panel
    # dressed, the title screen's panorama drawn by panorama.py
    gui = "assets/minecraft/textures/gui/"
    for name in MENUS:
        if name.endswith("background"):
            save(gui + name + ".png", menu_background("list" in name))
            continue
        im = png(vanilla.read(gui + name + ".png"))
        retone(im, "neutral")
        save(gui + name + ".png", im)
    im = png(vanilla.read(gui + "demo_background.png"))
    dress_vanilla(im, gui + "demo_background.png", arrow)
    save(gui + "demo_background.png", im)
    for face, im in enumerate(panorama.faces(panorama.DEFAULT)):
        dest = OUT / gui / "title" / "background" / f"panorama_{face}.png"
        dest.parent.mkdir(parents=True, exist_ok=True)
        im.save(dest, optimize=True)

    # the names on their plates, drawn by the fallow mod
    write_titles(OUT, textures)

    # the texts mods write on their screens in 0x404040, in Fallow's moss green:
    # the game's text shader, recolouring that one grey
    lang = OUT / "assets" / "minecraft" / "lang"
    lang.mkdir(parents=True, exist_ok=True)
    for code in ("fr_fr", "en_us"):
        (lang / f"{code}.json").write_text(json.dumps(LANG, indent=2) + "\n")
    shader = vanilla.read("assets/minecraft/shaders/core/rendertype_text.fsh").decode()
    grey, ink = TITLE_GREY / 255, ", ".join(f"{c / 255:.4f}" for c in TITLE_INK)
    shader = shader.replace("    fragColor = ", f"""    // Fallow: texts written in 0x404040 become moss green
    if (all(lessThan(abs(vertexColor.rgb - vec3({grey:.5f})), vec3(0.002)))) {{
        color.rgb = texture(Sampler0, texCoord0).rgb * vec3({ink}) * ColorModulator.rgb;
    }}
    fragColor = """, 1)
    dest = OUT / "assets" / "minecraft" / "shaders" / "core" / "rendertype_text.fsh"
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(shader)

    draw_hud(OUT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "hud")
    dress_sprites(vanilla)
    widgets.draw(OUT)
    draw_icon().save(OUT / "pack.png")
    questlog.draw(OUT)
    menus.draw(ROOT / "pack" / "config")

    # every coordinate table must still find its texture
    named = [*LEFTOVERS, *GREYED_COMPATS, *TOUCH_UPS, *SPRITE_MODES]
    named += [CONTAINER + k for k in (*GREYED, *PLATED, *PLUSES, *FAMILY_OF)]
    lost = sorted(k for k in named if not (OUT / k).exists())
    if lost:
        raise SystemExit("textures not found: " + ", ".join(lost))


if __name__ == "__main__":
    main()
