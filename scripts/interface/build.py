"""Builds the fallow-interface resource pack from free-licensed packs.

Base: GUI Revision by Vixel and GUI Revision Compats by Fyoncle (both
CC BY-NC-SA 4.0), with the top frieze of Hollow GUI by Melee (CC BY 4.0).
The script downloads the pinned releases, keeps the files that concern
vanilla and the mods of the pack, and adapts them to Fallow:

- recolour: GUI Revision's greys become Fallow's mist, sage and moss;
- titles: the titles drawn in the textures are redrawn in French with
  the letters of GUI Revision's own plates;
- details: Hollow GUI's frieze along the top of vanilla panels and a faint
  sown pattern (sprouting seeds) on every large panel;
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

# frieze and pattern colours, one step from the panel
SOFT = (188, 188, 167)
ACCENT = (140, 149, 130)
SEED = (184, 184, 162)
SEED_LIGHT = (218, 216, 198)
SEED_TILE = [
    "........................",
    "..l.l...................",
    "...s....................",
    "...s...........ss.......",
    "........................",
    "........................",
    "...................l.l..",
    "........s...........s...",
    ".......s............s...",
    "........................",
    "........................",
    ".l.l....................",
    "..s.............s.......",
    "..s..............s......",
    "........................",
    "..........l.l...........",
    "...........s............",
    "...........s............",
    "....ss..................",
    "........................",
    "....................ss..",
    "........................",
    "........................",
    "........................",
]

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
    return changed


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


def retitle(name, im, glyphs):
    px = im.load()
    if name in TITLES:
        word, box, rows = TITLES[name]
        pal, bg = plate_palette(im, box, rows)
        top = rows[0] - 1 if "É" in word else rows[0]
        for y in range(top, rows[1]):
            for x in range(*box):
                px[x, y] = bg
        draw_word(im, glyphs, word, (box[0] + box[1]) // 2, rows[0], pal)
    if name in ("villager.png", "red_merchant.png", "orange_trader1.png"):
        bg = (133, 15, 50, 255)
        for y in range(5, 12):
            for x in range(24, 78):
                px[x, y] = bg
        pal = {"d": (255, 225, 122, 255), "m": (227, 167, 43, 255), "l": (84, 10, 36, 255)}
        draw_word(im, glyphs, "ÉCHANGES", 51, 6, pal)
    if name == "orange_trader1.png":
        offer(im)


def offer(im):
    """Supplementaries' orange trader: "TRADE OFFER" in white bold -> "OFFRE"."""
    px = im.load()
    white = (255, 255, 255, 255)
    cols = [[px[x, y] == white for y in range(10, 17)] for x in range(140, 225)]
    glyphs, cur = [], []
    for col in cols:
        if any(col):
            cur.append(col)
        elif cur:
            glyphs.append(cur)
            cur = []
    letters = dict(zip("TRADEOFFER", glyphs))
    bg = px[141, 13]
    for y in range(10, 17):
        for x in range(140, 225):
            px[x, y] = bg
    word = "OFFRE"
    width = sum(len(letters[c]) + 1 for c in word) - 1
    x = 182 - width // 2
    for c in word:
        for i, col in enumerate(letters[c]):
            for j, on in enumerate(col):
                if on:
                    px[x + i, 10 + j] = white
        x += len(letters[c]) + 1


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


def add_pattern(im):
    px = im.load()
    ok = (FILL, SEED, SEED_LIGHT)
    for y in range(1, im.height - 1):
        for x in range(1, im.width - 1):
            c = SEED_TILE[y % 24][x % 24]
            if c == "." or px[x, y][:3] != FILL:
                continue
            if all(px[x + dx, y + dy][:3] in ok for dx in (-1, 0, 1) for dy in (-1, 0, 1)):
                px[x, y] = (SEED if c == "s" else SEED_LIGHT) + (255,)


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

    if OUT.exists():
        shutil.rmtree(OUT / "assets", ignore_errors=True)
    OUT.mkdir(parents=True, exist_ok=True)

    for path, data in sorted(files.items()):
        dest = OUT / path
        dest.parent.mkdir(parents=True, exist_ok=True)
        if not path.endswith(".png"):
            dest.write_bytes(data)
            continue
        im = recolour(png(data))
        if path.startswith(container):
            retitle(path[len(container):], im, glyphs)
            add_frieze(im, mask)
        elif path.startswith("assets/supplementaries/textures/gui/"):
            retitle(path.rsplit("/", 1)[1], im, glyphs)
        if big_panel(path, im):
            add_pattern(im)
        im.save(dest)

    # vanilla-grey screens of the mods without a compat
    for jar in sorted(JARS.glob("*.jar")):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                m = re.match(r"assets/([^/]+)/textures/gui/(.+\.png)$", n)
                if not m or m.group(1) not in RECOLOUR_MODS:
                    continue
                im = png(z.read(n))
                if recolour_vanilla(im):
                    if big_panel(n, im):
                        add_pattern(im)
                    dest = OUT / n
                    dest.parent.mkdir(parents=True, exist_ok=True)
                    im.save(dest)

    lang = OUT / "assets" / "minecraft" / "lang"
    lang.mkdir(parents=True, exist_ok=True)
    blank = {k: " " for k in HIDDEN_TITLES}
    for code in ("fr_fr", "en_us"):
        (lang / f"{code}.json").write_text(json.dumps(blank, indent=2, ensure_ascii=False) + "\n")

    questlog.draw(OUT)


if __name__ == "__main__":
    main()
