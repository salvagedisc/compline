"""Draws the textures of the fallow-interface resource pack.

Every pixel is drawn here from Fallow's own palette and shapes: nothing is
copied from Dark Coffee GUI (all rights reserved) nor from the mods' own
textures. Sizes, UV regions and drawable areas follow the originals so the
mods' code keeps placing text and items where it expects them.

Style: moss-slate panels sown with a faint pattern of sprouting seeds (the
fallow field), bone trims with curled corners, raised rounded tiles for
slots and buttons, lantern light for whatever is selected or hovered.

Authoring tool, not run by CI: needs Python 3 and Pillow.
    python3 scripts/interface/generate.py
"""
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "pack" / "resourcepacks" / "fallow-interface"

T = (0, 0, 0, 0)
# moss-slate panels
EDGE = (18, 22, 21)
SLATE_D = (31, 38, 35)
SLATE = (41, 50, 46)
SLATE_SEED = (47, 57, 52)
SLATE_L = (58, 70, 64)
# bone trims
BONE_D = (122, 128, 112)
BONE = (176, 176, 154)
BONE_L = (214, 210, 186)
# lichen tiles (slots, buttons)
TILE_D = (96, 108, 96)
TILE = (128, 140, 124)
TILE_L = (160, 170, 150)
TILE_H = (190, 196, 174)
# pages
PAGE_D = (196, 188, 162)
PAGE = (224, 218, 198)
PAGE_L = (236, 232, 216)
PAGE_SEED = (218, 211, 189)
INK = (40, 46, 40)
INK_FADED = (132, 136, 116)
# lantern light
LANTERN_D = (168, 106, 44)
LANTERN = (226, 166, 78)
LANTERN_L = (248, 214, 140)
LANTERN_H = (255, 240, 200)
# accents
RUST_D = (92, 40, 30)
RUST = (138, 62, 42)
RUST_L = (178, 96, 66)
MIST_D = (104, 120, 126)
MIST = (160, 178, 182)
MIST_L = (214, 226, 226)

# Corner insets per row for rounded shapes, by radius.
ROUND = {0: [], 1: [1], 2: [2, 1], 3: [3, 1, 1], 4: [4, 2, 1, 1], 5: [5, 3, 2, 1, 1]}


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3])


class Canvas:
    def __init__(self, w, h):
        self.img = Image.new("RGBA", (w, h), T)
        self.px = self.img.load()
        self.w, self.h = w, h

    def set(self, x, y, c, a=255):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = c if len(c) == 4 else (*c, a)

    def blit_map(self, x0, y0, rows, colors):
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch in colors and colors[ch] is not None:
                    self.set(x0 + dx, y0 + dy, colors[ch])

    def save(self, rel):
        path = OUT / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        self.img.save(path)


def rr(x0, y0, x1, y1, r=2, open_bottom=False):
    """Pixel mask of a rounded rectangle, inclusive bounds."""
    ins = ROUND[r]
    m = set()
    for y in range(y0, y1 + 1):
        i = y - y0
        j = y1 - y
        k = 0
        if i < len(ins):
            k = ins[i]
        if not open_bottom and j < len(ins):
            k = max(k, ins[j])
        for x in range(x0 + k, x1 - k + 1):
            m.add((x, y))
    return m


def paint(cv, mask, fill, edge=EDGE, light=None, dark=None, light2=None, open_bottom_y=None):
    """Fills a mask: edge on its border, light on the inner top/left rim,
    dark on the inner bottom/right rim. `fill` may be a colour or f(x, y)."""
    def inside(x, y):
        return (x, y) in mask

    border = set()
    for (x, y) in mask:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            if not inside(x + dx, y + dy):
                if open_bottom_y is not None and dy == 1 and y == open_bottom_y:
                    continue
                border.add((x, y))
                break
    for (x, y) in mask:
        if (x, y) in border:
            c = edge
        else:
            c = fill(x, y) if callable(fill) else fill
            if dark and ((x + 1, y) in border or (x, y + 1) in border):
                c = dark
            if light and ((x - 1, y) in border or (x, y - 1) in border):
                c = light
            if light2 and ((x, y - 2) in border and (x, y - 1) not in border) and (x - 1, y) not in border:
                c = light2
        if c is not None:
            cv.set(x, y, c)
    return border


# --- the sown pattern: sprouting seeds, staggered, a few levels above the ground
SEED_TILE = [
    "................",
    "..s.s...........",
    "...s............",
    "..ooo...........",
    "...o............",
    "................",
    "................",
    "................",
    "..........s.s...",
    "...........s....",
    "..........ooo...",
    "...........o....",
    "................",
    "................",
    "................",
    "................",
]


def sown(ground, seed, dx=0, dy=0):
    def f(x, y):
        ch = SEED_TILE[(y + dy) % 16][(x + dx) % 16]
        return seed if ch != "." else ground
    return f


def trim_frame(cv, x0, y0, x1, y1, r=4):
    """Bone trim running 3 px inside a panel, with curled corners."""
    m = rr(x0 + 3, y0 + 3, x1 - 3, y1 - 3, max(1, r - 2))
    inner = rr(x0 + 4, y0 + 4, x1 - 4, y1 - 4, max(1, r - 3))
    for p in m - inner:
        cv.set(*p, BONE_D)
    curl = ["bBB.", "B..b", "B.L.", ".b.."]
    for (cx, cy, fx, fy) in ((x0 + 3, y0 + 3, 1, 1), (x1 - 3, y0 + 3, -1, 1),
                             (x0 + 3, y1 - 3, 1, -1), (x1 - 3, y1 - 3, -1, -1)):
        for j, row in enumerate(curl):
            for i, ch in enumerate(row):
                if ch != ".":
                    cv.set(cx + fx * i, cy + fy * j, {"B": BONE, "b": BONE_D, "L": BONE_L}[ch])


def panel(cv, x0, y0, x1, y1, r=4, trim=True):
    paint(cv, rr(x0, y0, x1, y1, r), sown(SLATE, SLATE_SEED), EDGE, SLATE_L, SLATE_D)
    if trim:
        trim_frame(cv, x0, y0, x1, y1, r)


def tile(cv, x0, y0, x1, y1, r=2, hot=False, sunk=False):
    """Raised rounded tile (slot, button). hot: lit by the lantern."""
    if hot:
        fill, light, dark, hi = LANTERN, LANTERN_L, LANTERN_D, LANTERN_H
    elif sunk:
        fill, light, dark, hi = SLATE_D, EDGE, SLATE_L, None
    else:
        fill, light, dark, hi = TILE, TILE_L, TILE_D, TILE_H
    paint(cv, rr(x0, y0, x1, y1, r), fill, EDGE, light, dark)
    if hi:
        cv.set(x0 + r + 1, y0 + 1, hi)
        cv.set(x0 + 1, y0 + r + 1, hi)


def slot(cv, x, y, big=False):
    """Inventory slot: the 18x18 (or 26x26) box whose item sits 1 (or 5) px in."""
    if big:
        paint(cv, rr(x, y, x + 25, y + 25, 5), TILE, EDGE, TILE_L, TILE_D)
        cv.set(x + 6, y + 2, TILE_H)
        cv.set(x + 2, y + 6, TILE_H)
    else:
        tile(cv, x, y, x + 17, y + 17, r=2)


# --------------------------------------------------------------------------
# Questlog (Apache-2.0): textures/gui/*.png of the `questlog` namespace.
# --------------------------------------------------------------------------

def ink_corner(cv, x, y, fx, fy):
    rows = ["iii..", "i....", "i.d..", ".....", "....."]
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch != ".":
                cv.set(x + fx * i, y + fy * j, INK_FADED if ch == "i" else shade(INK_FADED, -24))
    for k in (4, 6):
        cv.set(x + fx * k, y, INK_FADED)
        cv.set(x, y + fy * k, INK_FADED)


def book_panel(page_tone):
    """275x166 nine-slice panel (border 16) at (375, 174) of a 1024x512 sheet."""
    cv = Canvas(1024, 512)
    x0, y0, x1, y1 = 375, 174, 375 + 274, 174 + 165
    paint(cv, rr(x0, y0, x1, y1, 5), sown(SLATE, SLATE_SEED), EDGE, SLATE_L, SLATE_D)
    trim_frame(cv, x0, y0, x1, y1, 5)
    # the page, rounded and sunk into the cover
    px0, py0, px1, py1 = x0 + 7, y0 + 7, x1 - 7, y1 - 7
    seed = shade(page_tone, -6)
    paint(cv, rr(px0, py0, px1, py1, 3), sown(page_tone, seed, 5, 3), EDGE,
          shade(page_tone, -22), shade(page_tone, 10))
    for (cx, cy, fx, fy) in ((px0 + 3, py0 + 3, 1, 1), (px1 - 3, py0 + 3, -1, 1),
                             (px0 + 3, py1 - 3, 1, -1), (px1 - 3, py1 - 3, -1, -1)):
        ink_corner(cv, cx, cy, fx, fy)
    return cv


def pen_rule(cv, x0, x1, y):
    """Ink rule: thin at the ends, a leaf in the middle."""
    mid = (x0 + x1) // 2
    for x in range(x0, x1 + 1):
        cv.set(x, y, INK_FADED)
    for x in range(x0 + (x1 - x0) // 5, x1 - (x1 - x0) // 5 + 1):
        cv.set(x, y, shade(INK_FADED, -26))
    cv.blit_map(mid - 3, y - 2, [".ll.ll.", "l.lll.l", ".l.l.l.", "..l.l.."],
                {"l": shade(INK_FADED, -26)})
    for x in (x0 - 2, x1 + 2):
        cv.set(x, y, INK_FADED)


def peripherals():
    cv = Canvas(256, 256)
    tile(cv, 46, 65, 46 + 53, 65 + 17, r=3)
    tile(cv, 122, 65, 122 + 53, 65 + 17, r=3, hot=True)
    tile(cv, 12, 105, 12 + 87, 105 + 17, r=3)
    tile(cv, 122, 105, 122 + 87, 105 + 17, r=3, hot=True)
    # toast: area 160x32 at (94, 11)
    tx, ty = 81 + 13, 2 + 9
    panel(cv, tx, ty, tx + 159, ty + 31, r=4, trim=False)
    paint(cv, rr(tx + 4, ty + 4, tx + 155, ty + 27, 2), sown(PAGE_L, PAGE_SEED, 3, 1), EDGE,
          PAGE_D, shade(PAGE_L, 8))
    cv.set(tx + 2, ty + 2, BONE)
    cv.set(tx + 157, ty + 2, BONE)
    cv.set(tx + 2, ty + 29, BONE)
    cv.set(tx + 157, ty + 29, BONE)
    # "important": a lit lantern, 6x16 at (13, 12)
    cv.blit_map(2 + 11, 2 + 10, [
        "..EE..",
        ".E..E.",
        "EEEEEE",
        "EbBBbE",
        "EhLHlE",
        "EhLHlE",
        "EhHHlE",
        "EhLLlE",
        "EbBBbE",
        "EEEEEE",
        ".EBBE.",
        "..EE..",
        "......",
        "......",
        "......",
        "......",
    ], {"E": EDGE, "B": BONE, "b": BONE_D, "h": LANTERN_D, "l": LANTERN, "L": LANTERN_L, "H": LANTERN_H})
    # scrollbar handle, 8x12 at (42, 14)
    hx, hy = 32 + 10, 2 + 12
    tile(cv, hx, hy, hx + 7, hy + 11, r=2)
    for k in (4, 6, 8):
        cv.set(hx + 3, hy + k - 1, TILE_D)
        cv.set(hx + 4, hy + k - 1, TILE_D)
        cv.set(hx + 3, hy + k, TILE_H)
        cv.set(hx + 4, hy + k, TILE_H)
    for y in (19, 20, 21):
        cv.set(62 + 6, y, EDGE)
        cv.set(62 + 7, y, SLATE_D)
        cv.set(62 + 8, y, SLATE_D)
        cv.set(62 + 9, y, EDGE)
    pen_rule(cv, 2 + 16, 2 + 123, 157 + 4)
    pen_rule(cv, 2 + 62, 2 + 189, 135 + 4)
    pen_rule(cv, 2 + 12, 2 + 239, 146 + 4)
    return cv


LABEL = {  # "Rechercher", Minecraft-font sized glyphs
    "R": ["####.", "#...#", "####.", "#...#", "#...#", "#...#", "#...#"],
    "e": [".....", ".....", ".###.", "#...#", "#####", "#....", ".####"],
    "c": [".....", ".....", ".###.", "#...#", "#....", "#...#", ".###."],
    "h": ["#....", "#....", "#.##.", "##..#", "#...#", "#...#", "#...#"],
    "r": [".....", ".....", "#.##.", "##..#", "#....", "#....", "#...."],
}


def label(cv, x, y, text, c, shadow):
    for ch in text:
        cv.blit_map(x + 1, y + 1, LABEL[ch], {"#": shadow})
        cv.blit_map(x, y, LABEL[ch], {"#": c})
        x += 6


def lens(cv, x, y, hot):
    """14x14 magnifying glass with misty glass and a glint."""
    ring, ring_d = (LANTERN_L, LANTERN) if hot else (BONE_L, BONE_D)
    cv.blit_map(x, y, [
        "...EEEE.......",
        "..ErrrrE......",
        ".ErWgggdE.....",
        "ErWggggmdE....",
        "ErgggggmdE....",
        "ErggggmmdE....",
        ".EdmmmmdE.....",
        "..EdddddEE....",
        "...EEEEEhhE...",
        "........EhHE..",
        ".........EhHE.",
        "..........EhHE",
        "...........EE.",
        "..............",
    ], {"E": EDGE, "r": ring, "d": ring_d, "g": MIST, "m": MIST_D, "W": MIST_L,
        "h": RUST, "H": RUST_L})


def chapter_tab(cv, u, v, h, fill, light, dark):
    """Bookmark hanging from the top of the book, rounded tip, stitched hem."""
    x0, y0 = u + 14, v + 6
    x1, y1 = x0 + 25, y0 + h - 1
    paint(cv, rr(x0, y0 - 4, x1, y1, 4), fill, EDGE, light, dark)
    for y in range(y0 - 4, y0):
        for x in range(x0, x1 + 1):
            cv.set(x, y, T)
    for x in range(x0 + 1, x1):
        cv.set(x, y0, fill)
    cv.set(x0, y0, EDGE)
    cv.set(x1, y0, EDGE)
    for x in range(x0 + 4, x1 - 3, 3):
        cv.set(x, y1 - 4, light)
        cv.set(x + 1, y1 - 4, light)


ICONS = {
    "expand": ["...hh...", "..hHHh..", ".hH..Hh.", "........", "........", ".hH..Hh.", "..hHHh..", "...hh..."],
    "condense": [".hH..Hh.", "..hHHh..", "...hh...", "........", "........", "...hh...", "..hHHh..", ".hH..Hh."],
    "hidden": ["........", "........", "........", "H......H", ".hHHHHh.", "..h..h..", ".h....h.", "........"],
    "visible": ["........", "..hHHh..", ".h.HH.h.", "h.HddH.h", "h.HddH.h", ".h.HH.h.", "..hHHh..", "........"],
}


def tab_buttons():
    cv = Canvas(256, 256)
    for hot, ox in ((False, 2), (True, 62)):
        paint(cv, rr(ox + 15, 2 + 19, ox + 42, 2 + 40, 4), sown(SLATE_L if hot else SLATE, SLATE_SEED),
              EDGE, BONE_D if hot else SLATE_L, SLATE_D)
        for y in range(2 + 37, 2 + 41):
            for x in range(ox + 15, ox + 43):
                cv.set(x, y, T)
        lens(cv, ox + 22, 2 + 22, hot)
    for hot, oy in ((False, 59), (True, 116)):
        bx = 2
        paint(cv, rr(bx + 30, oy + 19, bx + 222, oy + 40, 4), sown(SLATE_L if hot else SLATE, SLATE_SEED, 7, 0),
              EDGE, BONE_D if hot else SLATE_L, SLATE_D)
        for y in range(oy + 37, oy + 41):
            for x in range(bx + 30, bx + 223):
                cv.set(x, y, T)
        label(cv, bx + 44, oy + 25, "Rechercher", LANTERN_L if hot else BONE_L, EDGE)
        paint(cv, rr(bx + 112, oy + 23, bx + 197, oy + 34, 2), PAGE_L, EDGE, PAGE_D, None)
        lens(cv, bx + 204, oy + 22, hot)
    left = ["......EE", "....EElE", "..EElLLE", "EElLLLLE", "..EEdLLE", "....EEdE", "......EE"]
    for hot, ox in ((False, 2), (True, 32)):
        cols = {"E": EDGE, "l": LANTERN_L if hot else TILE_H, "L": LANTERN if hot else TILE, "d": LANTERN_D if hot else TILE_D}
        cv.blit_map(ox + 9, 173 + 10, left, cols)
        cv.blit_map(ox + 10, 202 + 10, [r[::-1] for r in left], cols)
    for (u, fill, light, dark) in ((62, TILE, TILE_L, TILE_D), (118, RUST, RUST_L, RUST_D)):
        chapter_tab(cv, u, 174, 22, fill, light, dark)
        chapter_tab(cv, u, 208, 28, fill, light, dark)
    for name, (u, v) in {"expand": (122, 2), "condense": (122, 30), "hidden": (180, 2), "visible": (180, 30)}.items():
        for hot, du in ((False, 0), (True, 28)):
            x0, y0 = u + du + 6, v + 6
            tile(cv, x0, y0, x0 + 13, y0 + 13, r=3, hot=hot)
            cv.blit_map(x0 + 3, y0 + 3, ICONS[name],
                        {"h": EDGE if not hot else RUST_D, "H": SLATE_D if not hot else RUST, "d": EDGE})
    return cv


def open_button():
    """Inventory button opening the questlog: a moss-bound journal with a lantern clasp."""
    cv = Canvas(256, 256)
    cv.blit_map(114, 115, [
        "...EEEEEEEEEEEEEEEEEEEEE....",
        "..EsSSSSSSSSSSSSSSSSSSSSE...",
        ".EsSSSSSSSSSSSSSSSSSSSSSSE..",
        ".EsSSbbbbbbbbbbbbbbbbbSSSE..",
        ".EsSSbSSSSSSSSSSSSSSSbSSSEp.",
        ".EsSSbSSSSSSnSSnSSSSSbSSSEp.",
        ".EsSSbSSSSSSSnnSSSSSSbSSSEp.",
        ".EsSSbSSSSSSnnnnSSSSSbSSSEp.",
        ".EsSSbSSSSSSSnnSSSSSSbSSSEEEE",
        ".EsSSbSSSSSSSSnSSSSSSbSSEhLLE",
        ".EsSSbSSSSSSSSSSSSSSSbSSEhHlE",
        ".EsSSbSSSSSSSSSSSSSSSbSSEhllE",
        ".EsSSbSSSSSSSSSSSSSSSbSSSEEEE",
        ".EsSSbSSSSSSSSSSSSSSSbSSSEp.",
        ".EsSSbSSSSSSSSSSSSSSSbSSSEp.",
        ".EsSSbbbbbbbbbbbbbbbbbSSSEp.",
        ".EsSSSSSSSSSSSSSSSSSSSSSSEp.",
        ".EsddddddddddddddddddddddEp.",
        "..EEEEEEEEEEEEEEEEEEEEEEEEp.",
        "...ppppppppppppppppppppppp..",
        "....EEEEEEEEEEEEEEEEEEEEE...",
    ], {"E": EDGE, "S": SLATE_L, "s": shade(SLATE_L, 22), "d": SLATE_D, "b": BONE_D, "n": BONE,
        "p": PAGE, "h": LANTERN_D, "l": LANTERN, "L": LANTERN_L, "H": LANTERN_H})
    return cv


# --------------------------------------------------------------------------
# HUD: minecraft:textures/gui/sprites/hud/
# --------------------------------------------------------------------------

def hotbar():
    cv = Canvas(182, 22)
    paint(cv, rr(0, 0, 181, 21, 4), sown(SLATE, SLATE_SEED, 3, 5), EDGE, SLATE_L, SLATE_D)
    for i in range(9):
        slot(cv, 2 + 20 * i, 2)
        if i < 8:  # a lantern bead between slots
            x = 2 + 20 * i + 18
            cv.set(x, 9, BONE_D)
            cv.set(x, 10, LANTERN)
            cv.set(x, 11, LANTERN_D)
            cv.set(x, 12, BONE_D)
    return cv


def selection():
    cv = Canvas(24, 23)
    m = rr(0, 0, 23, 22, 4)
    hole = rr(3, 3, 20, 19, 2)
    paint(cv, m - hole, LANTERN, EDGE, LANTERN_L, LANTERN_D)
    for p in rr(4, 4, 19, 18, 1):
        cv.set(*p, (248, 214, 140, 38))  # lantern glow over the slot
    for (x, y) in ((11, 0), (12, 0), (11, 22), (12, 22)):
        cv.set(x, y, LANTERN_H)
    cv.set(3, 1, LANTERN_H)
    cv.set(1, 3, LANTERN_H)
    return cv


def offhand(side):
    cv = Canvas(29, 24)
    x0 = 0 if side == "left" else 7
    paint(cv, rr(x0, 0, x0 + 21, 21, 4), sown(SLATE, SLATE_SEED), EDGE, SLATE_L, SLATE_D)
    slot(cv, x0 + 2, 2)
    return cv


HEART_OUT = [
    "..OO.OO..",
    ".OxxOxxO.",
    "OxxxOxxxO",
    "OxxxxxxxO",
    "OxxxxxxxO",
    ".OxxxxxO.",
    "..OxxxO..",
    "...OxO...",
    "....O....",
]
HEART_IN = [
    ".........",
    "..Gh.rr..",
    ".hhr.rrm.",
    ".hrrrrrm.",
    ".rrrrrmd.",
    "..rrrmd..",
    "...rmd...",
    "....d....",
    ".........",
]


def heart(cols, half=False, hardcore=False):
    glint, hi, base, mid, dark = cols
    cv = Canvas(9, 9)
    for y, row in enumerate(HEART_IN):
        for x, ch in enumerate(row):
            if ch == "." or (half and x > 4):
                continue
            cv.set(x, y, {"G": glint, "h": hi, "r": base, "m": mid, "d": dark}[ch])
    if half:
        for y in range(2, 7):
            if (4, y) in {(4, y) for y in range(2, 7)}:
                cv.set(4, y, mid)
    if hardcore:  # stitches across the heart: one life, mended
        for (x, y) in ((2, 4), (3, 3), (4, 4), (5, 3), (6, 4)):
            if not (half and x > 4):
                cv.set(x, y, shade(dark, -34))
    return cv


def heart_container(edge, inside, glint=None):
    cv = Canvas(9, 9)
    cv.blit_map(0, 0, HEART_OUT, {"O": edge, "x": inside})
    if glint:
        cv.set(2, 2, glint)
    return cv


HEART_KINDS = {  # glint, highlight, base, mid, dark
    "": ((255, 226, 214), (236, 120, 104), (196, 48, 52), (150, 30, 40), (104, 18, 30)),
    "poisoned": ((236, 240, 196), (178, 192, 104), (122, 140, 54), (92, 108, 38), (62, 76, 26)),
    "withered": ((150, 146, 140), (100, 94, 90), (64, 58, 56), (46, 40, 40), (30, 26, 26)),
    "frozen": ((250, 255, 255), (196, 230, 240), (124, 182, 206), (92, 146, 172), (64, 106, 132)),
    "absorbing": ((255, 248, 214), (252, 214, 120), (226, 166, 60), (188, 124, 40), (140, 86, 28)),
}


def hearts():
    out = {
        "container": heart_container(EDGE, (52, 34, 38), (82, 58, 62)),
        "container_blinking": heart_container(BONE_L, (96, 72, 74), (140, 112, 112)),
        "container_hardcore": heart_container(EDGE, (52, 34, 38), (82, 58, 62)),
        "container_hardcore_blinking": heart_container(BONE_L, (96, 72, 74), (140, 112, 112)),
        "vehicle_container": heart_container(EDGE, (52, 40, 32), (84, 66, 52)),
    }
    for kind, cols in HEART_KINDS.items():
        blink = tuple(shade(c, 36) for c in cols)
        prefix = f"{kind}_" if kind else ""
        for hc in (False, True):
            hpre = "hardcore_" if hc else ""
            for half in (False, True):
                name = f"{prefix}{hpre}{'half' if half else 'full'}"
                out[name] = heart(cols, half, hc)
                out[name + "_blinking"] = heart(blink, half, hc)
    mount = ((255, 238, 210), (232, 196, 146), (196, 142, 86), (160, 108, 62), (120, 78, 44))
    out["vehicle_full"] = heart(mount)
    out["vehicle_half"] = heart(mount, half=True)
    return out


LOAF_OUT = [
    ".........",
    "..OOOOO..",
    ".OxxxxxO.",
    "OxxxxxxxO",
    "OxxxxxxxO",
    "OxxxxxxxO",
    ".OxxxxxO.",
    "..OOOOO..",
    ".........",
]
LOAF_IN = [
    ".........",
    ".........",
    "..hGhcb..",
    ".hhsccsb.",
    ".hscsscb.",
    ".bcscsbd.",
    "..bbbdd..",
    ".........",
    ".........",
]


def food(state, hunger=False):
    if hunger:  # gone stale, grey-green
        cols = {"G": (214, 220, 170), "h": (170, 176, 110), "c": (140, 148, 86), "s": (190, 196, 140),
                "b": (108, 116, 64), "d": (78, 86, 46)}
        socket, socket_hi = (40, 44, 30), (66, 72, 48)
    else:
        cols = {"G": (255, 236, 196), "h": (232, 176, 104), "c": (202, 138, 70), "s": (244, 214, 160),
                "b": (156, 96, 48), "d": (112, 64, 34)}
        socket, socket_hi = (46, 36, 30), (72, 58, 48)
    cv = Canvas(9, 9)
    if state == "empty":
        cv.blit_map(0, 0, LOAF_OUT, {"O": EDGE, "x": socket})
        cv.set(2, 3, socket_hi)
        cv.set(3, 2, socket_hi)
        return cv
    for y, row in enumerate(LOAF_IN):
        for x, ch in enumerate(row):
            if ch == "." or (state == "half" and x < 4):
                continue
            cv.set(x, y, cols[ch])
    if state == "half":  # the cut shows the crumb
        for y in range(2, 7):
            cv.set(4, y, cols["s"])
    return cv


ARMOR = [
    ".OO...OO.",
    "OhmO.OmdO",
    "OGhmOmmdO",
    "OhmmmmmdO",
    ".OhmbmdO.",
    ".OhmmmdO.",
    ".OhmbmdO.",
    ".OmmmddO.",
    "..OOOOO..",
]


def armor(state):
    cv = Canvas(9, 9)
    metal = {"G": (250, 250, 240), "h": (214, 216, 206), "m": (164, 168, 160), "d": (108, 112, 108),
             "b": LANTERN}
    for y, row in enumerate(ARMOR):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "O":
                cv.set(x, y, EDGE)
            elif state == "empty" or (state == "half" and x > 4):
                cv.set(x, y, (54, 60, 58) if ch in "Gh" else (42, 48, 46))
            else:
                cv.set(x, y, metal[ch])
    if state == "half":
        for y in range(1, 8):
            if ARMOR[y][4] != ".":
                cv.set(4, y, EDGE if y in (1, 2) else metal["d"])
    return cv


def air(bursting=False):
    cv = Canvas(9, 9)
    if bursting:
        rows = [
            "....O....",
            ".O..m..O.",
            "..m...m..",
            "O.......O",
            ".m.....m.",
            "O.......O",
            "..m...m..",
            ".O..m..O.",
            "....O....",
        ]
    else:
        rows = [
            "..OOOOO..",
            ".OmmmmmO.",
            "OmWwmmmdO",
            "OmwmmmmdO",
            "OmmmmmmdO",
            "OmmmmmddO",
            "OmmmmddDO",
            ".OddddDO.",
            "..OOOOO..",
        ]
    cv.blit_map(0, 0, rows, {"O": MIST_D, "m": (182, 204, 210, 210), "W": (255, 255, 255),
                             "w": MIST_L, "d": (132, 156, 164, 230), "D": (104, 126, 134)})
    return cv


# --------------------------------------------------------------------------
# Containers (preview of the style; not shipped yet)
# --------------------------------------------------------------------------

def inventory():
    cv = Canvas(256, 256)
    panel(cv, 0, 0, 175, 165, r=5)
    for y in (7, 25, 43, 61):
        slot(cv, 7, y)
    paint(cv, rr(25, 7, 75, 77, 3), sown(SLATE_D, SLATE, 2, 2), EDGE, EDGE, SLATE_L)
    for (x, y) in ((97, 17), (115, 17), (97, 35), (115, 35)):
        slot(cv, x, y)
    cv.blit_map(132, 29, [  # arrow fletched with two leaves
        "..........EE....",
        ".s..s.....ELE...",
        "..ss.EEEEEELLE..",
        ".sssELLLLLLLLLE.",
        "..ss.EddddddddE.",
        ".s..s.....EdE...",
        "..........EE....",
    ], {"E": EDGE, "L": BONE_L, "d": BONE_D, "s": BONE})
    slot(cv, 149, 23, big=True)
    slot(cv, 76, 61)
    for row in range(3):
        for col in range(9):
            slot(cv, 7 + 18 * col, 83 + 18 * row)
    for col in range(9):
        slot(cv, 7 + 18 * col, 141)
    for x in range(8, 168):  # seam between the backpack and the hotbar
        cv.set(x, 138, SLATE_D)
        cv.set(x, 139, SLATE_L)
    cv.blit_map(84, 136, [".ll.ll.", "l.lll.l", ".l.l.l.", "..l.l.."], {"l": BONE})
    return cv


def main():
    q = "assets/questlog/textures/gui/"
    book_panel(PAGE).save(q + "questlog.png")
    book_panel(PAGE_L).save(q + "quest_page.png")
    peripherals().save(q + "quest_peripherals.png")
    tab_buttons().save(q + "questlog_search_tab_buttons.png")
    open_button().save(q + "questlog_button.png")

    h = "assets/minecraft/textures/gui/sprites/hud/"
    hotbar().save(h + "hotbar.png")
    selection().save(h + "hotbar_selection.png")
    offhand("left").save(h + "hotbar_offhand_left.png")
    offhand("right").save(h + "hotbar_offhand_right.png")
    for name, cv in hearts().items():
        cv.save(h + f"heart/{name}.png")
    for state in ("empty", "half", "full"):
        food(state).save(h + f"food_{state}.png")
        food(state, hunger=True).save(h + f"food_{state}_hunger.png")
        armor(state).save(h + f"armor_{state}.png")
    air().save(h + "air.png")
    air(True).save(h + "air_bursting.png")


if __name__ == "__main__":
    main()
