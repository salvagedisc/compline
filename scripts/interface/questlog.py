"""Draws the Questlog journal textures of the fallow-interface resource pack.

Called by build.py. The rest of the pack comes from GUI Revision; this file
draws the Questlog sheets in the same light, misty style so the journal
matches the containers: mist panels (build.py adds the leaf pattern), sage trims
with curled corners, raised sage tiles, linen pages, lantern light for
whatever is selected or hovered. Sizes and UV regions follow Questlog's own
textures (Apache-2.0) so the mod keeps placing text and buttons as expected.
"""
from pathlib import Path

from PIL import Image

OUT = None  # set by build.py

T = (0, 0, 0, 0)
# mist panels (the colours build.py gives GUI Revision's panels)
EDGE = (26, 31, 28)
SLATE_D = (166, 170, 150)
SLATE = (203, 202, 182)
SLATE_L = (232, 229, 208)
# sage trims
BONE_D = (106, 117, 104)
BONE = (130, 140, 122)
BONE_L = (166, 170, 150)
# sage tiles (slots, buttons)
TILE_D = (94, 104, 93)
TILE = (130, 140, 122)
TILE_L = (166, 170, 150)
TILE_H = (190, 196, 174)
# pages
PAGE_D = (196, 194, 172)
PAGE = (232, 229, 210)
PAGE_L = (242, 240, 226)
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
# moss binding of the journal button
MOSS_D = (52, 66, 50)
MOSS = (78, 98, 72)
MOSS_L = (112, 134, 100)

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
    paint(cv, rr(x0, y0, x1, y1, r), SLATE, EDGE, SLATE_L, SLATE_D)
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
    paint(cv, rr(x0, y0, x1, y1, 5), SLATE, EDGE, SLATE_L, SLATE_D)
    trim_frame(cv, x0, y0, x1, y1, 5)
    # the page, rounded and sunk into the cover
    px0, py0, px1, py1 = x0 + 7, y0 + 7, x1 - 7, y1 - 7
    paint(cv, rr(px0, py0, px1, py1, 3), page_tone, EDGE,
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
    paint(cv, rr(tx + 4, ty + 4, tx + 155, ty + 27, 2), PAGE_L, EDGE,
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
        paint(cv, rr(ox + 15, 2 + 19, ox + 42, 2 + 40, 4), SLATE_L if hot else SLATE,
              EDGE, BONE_D if hot else SLATE_L, SLATE_D)
        for y in range(2 + 37, 2 + 41):
            for x in range(ox + 15, ox + 43):
                cv.set(x, y, T)
        lens(cv, ox + 22, 2 + 22, hot)
    for hot, oy in ((False, 59), (True, 116)):
        bx = 2
        paint(cv, rr(bx + 30, oy + 19, bx + 222, oy + 40, 4), SLATE_L if hot else SLATE,
              EDGE, BONE_D if hot else SLATE_L, SLATE_D)
        for y in range(oy + 37, oy + 41):
            for x in range(bx + 30, bx + 223):
                cv.set(x, y, T)
        label(cv, bx + 44, oy + 25, "Rechercher", RUST if hot else (64, 64, 64), SLATE_L)
        paint(cv, rr(bx + 112, oy + 23, bx + 197, oy + 34, 2), PAGE_L, EDGE, PAGE_D, None)
        lens(cv, bx + 204, oy + 22, hot)
    left = [".....EE", "....ElE", "...ElLE", "..ElLLE", ".ElLLLE", "ElLLLLE",
            ".EdLLLE", "..EdLLE", "...EdLE", "....EdE", ".....EE"]
    for hot, ox in ((False, 2), (True, 32)):
        cols = {"E": EDGE, "l": LANTERN_L if hot else TILE_H, "L": LANTERN if hot else TILE, "d": LANTERN_D if hot else TILE_D}
        cv.blit_map(ox + 9, 173 + 8, left, cols)
        cv.blit_map(ox + 10, 202 + 8, [r[::-1] for r in left], cols)
    for (u, fill, light, dark) in ((62, TILE, TILE_L, TILE_D), (118, RUST, RUST_L, RUST_D)):
        chapter_tab(cv, u, 174, 22, fill, light, dark)
        chapter_tab(cv, u, 208, 28, fill, light, dark)
    for name, (u, v) in {"expand": (122, 2), "condense": (122, 30), "hidden": (180, 2), "visible": (180, 30)}.items():
        for hot, du in ((False, 0), (True, 28)):
            x0, y0 = u + du + 6, v + 6
            tile(cv, x0, y0, x0 + 13, y0 + 13, r=3, hot=hot)
            cv.blit_map(x0 + 3, y0 + 3, ICONS[name],
                        {"h": EDGE if not hot else RUST_D, "H": TILE_H if not hot else RUST, "d": EDGE})
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
    ], {"E": EDGE, "S": MOSS, "s": MOSS_L, "d": MOSS_D, "b": BONE_L, "n": SLATE_L,
        "p": PAGE, "h": LANTERN_D, "l": LANTERN, "L": LANTERN_L, "H": LANTERN_H})
    return cv


def draw(out):
    global OUT
    OUT = out
    q = "assets/questlog/textures/gui/"
    book_panel(PAGE).save(q + "questlog.png")
    book_panel(PAGE_L).save(q + "quest_page.png")
    peripherals().save(q + "quest_peripherals.png")
    tab_buttons().save(q + "questlog_search_tab_buttons.png")
    open_button().save(q + "questlog_button.png")
