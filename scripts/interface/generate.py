"""Draws the textures of the fallow-interface resource pack.

Every pixel is drawn here from Fallow's own palette and shapes: nothing is
copied from Dark Coffee GUI (all rights reserved) nor from the mods' own
textures. Sizes, UV regions and drawable areas follow the originals so the
mods' code keeps placing text and items where it expects them.

Authoring tool, not run by CI: needs Python 3 and Pillow.
    python3 scripts/interface/generate.py
"""
import random
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "pack" / "resourcepacks" / "fallow-interface"

# Fallow palette: roasted leather, worn brass, parchment, lantern light, mist.
T = (0, 0, 0, 0)
EDGE = (30, 17, 16)
LEATHER_D = (50, 26, 26)
LEATHER = (72, 38, 36)
LEATHER_L = (98, 56, 50)
LEATHER_H = (126, 80, 66)
SOCKET = (40, 20, 21)
SOCKET_D = (27, 13, 14)
BRASS_D = (112, 82, 56)
BRASS = (160, 124, 88)
BRASS_L = (204, 170, 126)
PARCH_D = (186, 160, 124)
PARCH = (214, 194, 160)
PARCH_L = (230, 215, 186)
PARCH_H = (242, 232, 210)
INK = (58, 36, 24)
INK_FADED = (150, 118, 88)
LANTERN_D = (170, 108, 46)
LANTERN = (222, 162, 80)
LANTERN_L = (246, 212, 140)
WINE_D = (76, 26, 26)
WINE = (116, 40, 36)
WINE_L = (152, 62, 52)
MIST_D = (110, 124, 128)
MIST = (156, 170, 172)
MIST_L = (200, 210, 208)


def rgba(c, a=255):
    return c if len(c) == 4 else (*c, a)


class Canvas:
    def __init__(self, w, h, fill=T):
        self.img = Image.new("RGBA", (w, h), rgba(fill))
        self.px = self.img.load()
        self.w, self.h = w, h

    def set(self, x, y, c, a=255):
        if 0 <= x < self.w and 0 <= y < self.h:
            self.px[x, y] = rgba(c, a)

    def get(self, x, y):
        return self.px[x, y]

    def rect(self, x0, y0, x1, y1, c):
        """Fills the inclusive rectangle (x0, y0)-(x1, y1)."""
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.set(x, y, c)

    def hline(self, x0, x1, y, c):
        self.rect(x0, y, x1, y, c)

    def vline(self, x, y0, y1, c):
        self.rect(x, y0, x, y1, c)

    def clear_corners(self, x0, y0, x1, y1, cut, top=True, bottom=True):
        """Makes the corners of a box transparent along a staircase of `cut` pixels."""
        for i in range(cut):
            n = cut - i
            for k in range(n):
                if top:
                    self.set(x0 + k, y0 + i, T)
                    self.set(x1 - k, y0 + i, T)
                if bottom:
                    self.set(x0 + k, y1 - i, T)
                    self.set(x1 - k, y1 - i, T)

    def blit_map(self, x0, y0, rows, colors):
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch in colors:
                    self.set(x0 + dx, y0 + dy, colors[ch])

    def save(self, rel):
        path = OUT / rel
        path.parent.mkdir(parents=True, exist_ok=True)
        self.img.save(path)


def shade(c, d):
    return tuple(max(0, min(255, v + d)) for v in c[:3])


def framed_box(cv, x0, y0, x1, y1, fill, light, dark, edge=EDGE, cut=2, open_bottom=False):
    """Raised box: dark edge, chamfered corners, light top/left, dark bottom/right."""
    cv.rect(x0, y0, x1, y1, edge)
    cv.rect(x0 + 1, y0 + 1, x1 - 1, y1 - 1 if not open_bottom else y1, fill)
    cv.hline(x0 + 1, x1 - 1, y0 + 1, light)
    cv.vline(x0 + 1, y0 + 1, y1 - 1 if not open_bottom else y1, light)
    if not open_bottom:
        cv.hline(x0 + 1, x1 - 1, y1 - 1, dark)
    cv.vline(x1 - 1, y0 + 2, y1 - 1 if not open_bottom else y1, dark)
    # chamfer: redraw the cut corners as transparent then re-edge them
    for i in range(cut):
        for k in range(cut - i):
            for (x, y) in ((x0 + k, y0 + i), (x1 - k, y0 + i)) + (
                () if open_bottom else ((x0 + k, y1 - i), (x1 - k, y1 - i))
            ):
                cv.set(x, y, T)
    for i in range(cut):
        k = cut - i
        cv.set(x0 + k, y0 + i, edge)
        cv.set(x1 - k, y0 + i, edge)
        if not open_bottom:
            cv.set(x0 + k, y1 - i, edge)
            cv.set(x1 - k, y1 - i, edge)


def parchment(cv, x0, y0, x1, y1, base, seed, grain=5):
    rnd = random.Random(seed)
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            d = rnd.choice((0, 0, 0, 0, 0, 0, -grain, grain // 2))
            cv.set(x, y, shade(base, d))
    # a few horizontal fibres, as in laid paper
    for _ in range(max(1, (x1 - x0) * (y1 - y0) // 900)):
        y = rnd.randint(y0 + 2, y1 - 2)
        x = rnd.randint(x0 + 2, max(x0 + 2, x1 - 14))
        for k in range(rnd.randint(4, 10)):
            if x + k <= x1:
                cv.set(x + k, y, shade(base, -grain - 2))


def ink_corner(cv, x, y, sx, sy, c=INK_FADED):
    """Small ink flourish in a page corner; sx, sy give the direction (+1/-1)."""
    for k in range(5):
        cv.set(x + sx * k, y, c)
        cv.set(x, y + sy * k, c)
    cv.set(x + sx * 2, y + sy * 2, c)
    cv.set(x + sx * 6, y, c)
    cv.set(x, y + sy * 6, c)


# --------------------------------------------------------------------------
# Questlog (Apache-2.0): textures/gui/*.png of the `questlog` namespace.
# --------------------------------------------------------------------------

def book_panel(page, seed):
    """275x166 nine-slice panel (border 16) at (375, 174) of a 1024x512 sheet."""
    cv = Canvas(1024, 512)
    x0, y0, x1, y1 = 375, 174, 375 + 274, 174 + 165
    # leather cover
    cv.rect(x0, y0, x1, y1, EDGE)
    cv.rect(x0 + 1, y0 + 1, x1 - 1, y1 - 1, LEATHER)
    cv.hline(x0 + 2, x1 - 2, y0 + 1, LEATHER_H)
    cv.vline(x0 + 1, y0 + 2, y1 - 2, LEATHER_L)
    cv.hline(x0 + 2, x1 - 1, y1 - 1, LEATHER_D)
    cv.vline(x1 - 1, y0 + 2, y1 - 1, LEATHER_D)
    cv.hline(x0 + 2, x1 - 2, y1 - 2, LEATHER_D)
    # brass trim, continuous so the nine-slice can stretch it
    cv.hline(x0 + 3, x1 - 3, y0 + 3, BRASS_D)
    cv.hline(x0 + 3, x1 - 3, y1 - 4, BRASS_D)
    cv.vline(x0 + 3, y0 + 3, y1 - 4, BRASS_D)
    cv.vline(x1 - 3, y0 + 3, y1 - 4, BRASS_D)
    for (cx, cy) in ((x0 + 3, y0 + 3), (x1 - 3, y0 + 3), (x0 + 3, y1 - 4), (x1 - 3, y1 - 4)):
        cv.set(cx, cy, BRASS_L)
    # rounded outer corners
    for (cx, cy, sx, sy) in ((x0, y0, 1, 1), (x1, y0, -1, 1), (x0, y1, 1, -1), (x1, y1, -1, -1)):
        cv.set(cx, cy, T)
        cv.set(cx + sx, cy, T)
        cv.set(cx, cy + sy, T)
        cv.set(cx + sx, cy + sy, EDGE)
    # page
    px0, py0, px1, py1 = x0 + 6, y0 + 6, x1 - 6, y1 - 7
    cv.rect(px0 - 1, py0 - 1, px1 + 1, py1 + 1, EDGE)
    parchment(cv, px0, py0, px1, py1, page, seed)
    cv.hline(px0, px1, py0, shade(page, -26))
    cv.vline(px0, py0, py1, shade(page, -18))
    cv.hline(px0 + 1, px1, py0 + 1, shade(page, -10))
    cv.hline(px0, px1, py1, shade(page, 10))
    cv.vline(px1, py0 + 1, py1, shade(page, 6))
    for (cx, cy, sx, sy) in ((px0 + 3, py0 + 3, 1, 1), (px1 - 3, py0 + 3, -1, 1),
                             (px0 + 3, py1 - 3, 1, -1), (px1 - 3, py1 - 3, -1, -1)):
        ink_corner(cv, cx, cy, sx, sy)
    return cv


def page_button(cv, x, y, w, h, hovered):
    if hovered:
        framed_box(cv, x, y, x + w - 1, y + h - 1, LANTERN_L, PARCH_H, LANTERN, edge=INK, cut=1)
        cv.hline(x + 2, x + w - 3, y + h - 2, LANTERN_D)
    else:
        framed_box(cv, x, y, x + w - 1, y + h - 1, PARCH, PARCH_H, PARCH_D, edge=INK, cut=1)
        cv.hline(x + 2, x + w - 3, y + h - 2, BRASS)


def pen_rule(cv, x0, x1, y):
    """Ink rule with tapered ends, drawn on rows y-1..y+1."""
    n = x1 - x0
    cv.hline(x0, x1, y, INK_FADED)
    cv.hline(x0 + n // 4, x1 - n // 4, y, shade(INK_FADED, -30))
    cv.hline(x0 + n // 3, x1 - n // 3, y + 1, shade(INK_FADED, 30))
    for x in (x0 - 2, x1 + 2):
        cv.set(x, y, INK_FADED)


def peripherals():
    cv = Canvas(256, 256)
    page_button(cv, 46, 65, 54, 18, False)
    page_button(cv, 122, 65, 54, 18, True)
    page_button(cv, 12, 105, 88, 18, False)
    page_button(cv, 122, 105, 88, 18, True)
    # toast (173x51 at 81,2), drawable area 160x32 at +13,+9
    tx, ty = 81 + 13, 2 + 9
    framed_box(cv, tx, ty, tx + 159, ty + 31, LEATHER, LEATHER_H, LEATHER_D, cut=2)
    cv.rect(tx + 4, ty + 4, tx + 155, ty + 27, EDGE)
    parchment(cv, tx + 5, ty + 5, tx + 154, ty + 26, PARCH_L, 11, grain=4)
    cv.hline(tx + 5, tx + 154, ty + 5, PARCH_D)
    # "important" mark (28x36 at 2,2), area 6x16 at +11,+10: a wax drop with a stroke
    cv.blit_map(2 + 11, 2 + 10, [
        ".EEEE.",
        "EwwwWE",
        "EwWwWE",
        "EwWwWE",
        "EwWwWE",
        "EwWwWE",
        "EwWwWE",
        ".EwWE.",
        ".EwWE.",
        ".EwWE.",
        "..EE..",
        "......",
        ".EEEE.",
        ".EwWE.",
        ".EWWE.",
        ".EEEE.",
    ], {"E": EDGE, "w": WINE_L, "W": WINE})
    # scrollbar handle (28x36 at 32,2), area 8x12 at +10,+12
    hx, hy = 32 + 10, 2 + 12
    framed_box(cv, hx, hy, hx + 7, hy + 11, LEATHER_L, LEATHER_H, LEATHER_D, cut=1)
    for k in (4, 6, 8):
        cv.hline(hx + 2, hx + 5, hy + k - 1, LEATHER)
    # scrollbar track pieces (16x1 at 62,19..21), 4 px wide at +6
    for y in (19, 20, 21):
        cv.set(62 + 6, y, EDGE)
        cv.set(62 + 7, y, LEATHER_D)
        cv.set(62 + 8, y, LEATHER_D)
        cv.set(62 + 9, y, EDGE)
    # rules: panelHR (140x9 at 2,157) x14..125, smallHR (252x9 at 2,135) x60..191,
    # bigHR (252x9 at 2,146) x10..241, all on row 4
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


def label(cv, x, y, text, c):
    for ch in text:
        cv.blit_map(x, y, LABEL[ch], {"#": c})
        x += 6


def lens(cv, x, y, hovered):
    """14x14 magnifying glass: brass ring, misty glass, leather handle."""
    ring, glass = (BRASS_L, MIST_L) if hovered else (BRASS, MIST)
    cv.blit_map(x, y, [
        "...EEEE.......",
        "..ErrrrE......",
        ".ErgggllE.....",
        "ErggggglrE....",
        "ErgggggglrE...",
        "ErggggggrE....",
        ".ErggggrrE....",
        "..ErrrrrEE....",
        "...EEEEEhhE...",
        "........EhhE..",
        ".........EhhE.",
        "..........EhhE",
        "...........EE.",
        "..............",
    ], {"E": EDGE, "r": ring, "g": glass, "l": PARCH_H, "h": LEATHER_L})


def tab_buttons():
    cv = Canvas(256, 256)
    for hovered, ox in ((False, 2), (True, 62)):
        fill, light = (LEATHER_L, LEATHER_H) if hovered else (LEATHER, LEATHER_L)
        framed_box(cv, ox + 15, 2 + 19, ox + 42, 2 + 36, fill, light, LEATHER_D, open_bottom=True)
        lens(cv, ox + 22, 2 + 21, hovered)
    for hovered, oy in ((False, 59), (True, 116)):
        fill, light = (LEATHER_L, LEATHER_H) if hovered else (LEATHER, LEATHER_L)
        bx = 2
        framed_box(cv, bx + 30, oy + 19, bx + 222, oy + 36, fill, light, LEATHER_D, open_bottom=True)
        label(cv, bx + 42, oy + 26, "Rechercher", PARCH_L if hovered else PARCH)
        cv.rect(bx + 112, oy + 23, bx + 197, oy + 34, EDGE)
        cv.rect(bx + 113, oy + 24, bx + 196, oy + 33, PARCH_L)
        cv.hline(bx + 113, bx + 196, oy + 24, PARCH_D)
        lens(cv, bx + 204, oy + 22, hovered)
    # chapter arrows (28x27): area 8x13 at +9/+10,+7
    left = ["......EE", "....EEaE", "..EEaaaE", "EEaaaaaE", "..EEaaaE", "....EEaE", "......EE"]
    for hovered, ox in ((False, 2), (True, 32)):
        a = PARCH_L if hovered else LEATHER_L
        big_l = [r.replace("a", "a") for r in left]
        cv.blit_map(ox + 9, 173 + 10, big_l, {"E": EDGE, "a": a})
        cv.blit_map(ox + 10, 202 + 10, [r[::-1] for r in big_l], {"E": EDGE, "a": a})
    # chapter bookmarks: secondary in linen, main in wine leather; active ones hang lower
    for (u, fill, light, dark) in ((62, PARCH_D, PARCH_L, BRASS_D), (118, WINE, WINE_L, WINE_D)):
        for (v, h) in ((174, 22), (208, 28)):
            x0, y0 = u + 14, v + 6
            x1, y1 = x0 + 25, y0 + h - 1
            cv.rect(x0, y0, x1, y1, EDGE)
            cv.rect(x0 + 1, y0, x1 - 1, y1 - 1, fill)
            cv.vline(x0 + 1, y0, y1 - 2, light)
            cv.vline(x1 - 1, y0, y1 - 1, dark)
            cv.hline(x0 + 1, x1 - 1, y1 - 1, dark)
            for k in range(2):
                cv.set(x0 + k, y1, T)
                cv.set(x1 - k, y1, T)
            cv.set(x0, y1 - 1, T)
            cv.set(x1, y1 - 1, T)
            cv.set(x0 + 1, y1 - 1, EDGE)
            cv.set(x1 - 1, y1 - 1, EDGE)
            cv.set(x0 + 2, y1, EDGE)
            cv.set(x1 - 2, y1, EDGE)
            cv.hline(x0 + 3, x1 - 3, y1, EDGE)
            # stitched hem near the bottom
            for x in range(x0 + 3, x1 - 2, 3):
                cv.hline(x, x + 1, y1 - 4, light)
    icons = {
        "expand": ["...##...", "..####..", ".##..##.", "........", "........", ".##..##.", "..####..", "...##..."],
        "condense": [".##..##.", "..####..", "...##...", "........", "........", "...##...", "..####..", ".##..##."],
        "hidden": ["........", "........", "........", "#......#", ".######.", "..#..#..", "........", "........"],
        "visible": ["........", "..####..", ".#....#.", "#..##..#", "#..##..#", ".#....#.", "..####..", "........"],
    }
    for name, (u, v) in {"expand": (122, 2), "condense": (122, 30), "hidden": (180, 2), "visible": (180, 30)}.items():
        for hovered, du in ((False, 0), (True, 28)):
            x0, y0 = u + du + 6, v + 6
            if hovered:
                framed_box(cv, x0, y0, x0 + 13, y0 + 13, PARCH_L, PARCH_H, PARCH_D, cut=1)
                ic = LEATHER
            else:
                framed_box(cv, x0, y0, x0 + 13, y0 + 13, LEATHER, LEATHER_L, LEATHER_D, cut=1)
                ic = PARCH
            cv.blit_map(x0 + 3, y0 + 3, icons[name], {"#": ic})
    return cv


def open_button():
    """Inventory button opening the questlog: a small leather journal."""
    cv = Canvas(256, 256)
    x0, y0 = 114, 115
    cv.blit_map(x0, y0, [
        "..EEEEEEEEEEEEEEEEEEEEEEE...",
        ".ELLLLLLLLLLLLLLLLLLLLLLLE..",
        "EHLLLLLLLLLLLLLLLLLLLLLLLLE.",
        "EHLLLLLLLLLLLLLLLLLLLLLLLLE.",
        "EHLLbbbbbbbbbbbbbbbbbbbLLLEp",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbLLLLLLWWWWWLLLLLLbLLLEp",
        "EHLLbLLLLLLWwwwWLLLLLLbLLLEp",
        "EHLLbLLLLLLWwwwWLLLLLLbLLLEp",
        "EHLLbLLLLLLWwwwWLLLLLLbLLBBB",
        "EHLLbLLLLLLWWWWWLLLLLLbLLBbB",
        "EHLLbLLLLLLLLLLLLLLLLLbLLBBB",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbLLLLLLLLLLLLLLLLLbLLLEp",
        "EHLLbbbbbbbbbbbbbbbbbbbLLLEp",
        "EHLLLLLLLLLLLLLLLLLLLLLLLLEp",
        "EHLLLLLLLLLLLLLLLLLLLLLLLLEp",
        "EHDDDDDDDDDDDDDDDDDDDDDDDDEp",
        ".EEEEEEEEEEEEEEEEEEEEEEEEEEp",
        "..pppppppppppppppppppppppppp",
        "...EEEEEEEEEEEEEEEEEEEEEEEE.",
    ], {"E": EDGE, "L": LEATHER, "H": LEATHER_H, "D": LEATHER_D, "b": BRASS_D,
        "W": WINE_D, "w": WINE_L, "B": BRASS, "p": PARCH})
    return cv


# --------------------------------------------------------------------------
# HUD: minecraft:textures/gui/sprites/hud/
# --------------------------------------------------------------------------

def hotbar():
    cv = Canvas(182, 22)
    framed_box(cv, 0, 0, 181, 21, LEATHER, LEATHER_L, LEATHER_D, cut=2)
    for i in range(9):
        x = 2 + 20 * i
        cv.rect(x, 2, x + 17, 19, SOCKET)
        cv.hline(x, x + 17, 2, SOCKET_D)
        cv.vline(x, 2, 19, SOCKET_D)
        cv.hline(x + 1, x + 17, 19, LEATHER_L)
        cv.vline(x + 17, 3, 19, shade(LEATHER, 6))
        for (cx, cy) in ((x, 2), (x + 17, 2), (x, 19), (x + 17, 19)):
            cv.set(cx, cy, LEATHER)
        if i < 8:  # brass rivet between slots
            cv.set(x + 18, 10, BRASS_D)
            cv.set(x + 18, 11, BRASS)
    return cv


def selection():
    cv = Canvas(24, 23)
    cv.rect(0, 0, 23, 23, EDGE)
    cv.rect(1, 1, 22, 21, LANTERN)
    cv.hline(1, 22, 1, LANTERN_L)
    cv.vline(1, 1, 21, LANTERN_L)
    cv.hline(2, 22, 21, LANTERN_D)
    cv.vline(22, 2, 21, LANTERN_D)
    cv.rect(3, 3, 20, 19, EDGE)
    cv.rect(4, 4, 19, 18, (246, 212, 140, 34))  # faint lantern glow over the slot
    for (cx, cy) in ((0, 0), (23, 0), (0, 22), (23, 22)):
        cv.set(cx, cy, T)
    cv.hline(0, 23, 22, T)
    cv.hline(1, 22, 22, EDGE)
    cv.set(0, 21, T)
    cv.set(23, 21, T)
    return cv


def offhand(side):
    cv = Canvas(29, 24)
    framed_box(cv, 0, 0, 21, 21, LEATHER, LEATHER_L, LEATHER_D, cut=2) if side == "left" else \
        framed_box(cv, 7, 0, 28, 21, LEATHER, LEATHER_L, LEATHER_D, cut=2)
    x = 2 if side == "left" else 9
    cv.rect(x, 2, x + 17, 19, SOCKET)
    cv.hline(x, x + 17, 2, SOCKET_D)
    cv.vline(x, 2, 19, SOCKET_D)
    cv.hline(x + 1, x + 17, 19, LEATHER_L)
    return cv


HEART_SHAPE = [
    "..OO.OO..",
    ".OHROrrO.",
    "OHRRORRDO",
    "ORRRRRRDO",
    "ORRRRRRDO",
    ".ORRRRDO.",
    "..ORRDO..",
    "...ODO...",
    "....O....",
]


def heart_sprite(colors, half=False, hardcore=False, inner_only=True):
    """9x9 heart. Full/half sprites are drawn over the container, so they
    carry only the inside; the container carries the outline."""
    hi, base, dark = colors
    cv = Canvas(9, 9)
    rows = [r.replace("r", "R") for r in HEART_SHAPE]
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if half and x > 4:
                continue
            if ch == "O":
                if not inner_only:
                    cv.set(x, y, EDGE)
            elif ch == "H":
                cv.set(x, y, hi)
            elif ch == "R":
                cv.set(x, y, base)
            elif ch == "D":
                cv.set(x, y, dark)
    if half:
        cv.vline(4, 2, 7, dark)
    if hardcore:  # a seam of stitches across the heart: one life, mended
        for (x, y) in ((2, 3), (4, 4), (6, 3)):
            if not (half and x > 4):
                cv.set(x, y, shade(dark, -30))
    return cv


def heart_container(edge, inside):
    cv = Canvas(9, 9)
    for y, row in enumerate(HEART_SHAPE):
        for x, ch in enumerate(row):
            if ch == "O":
                cv.set(x, y, edge)
            elif ch != ".":
                cv.set(x, y, inside)
    return cv


HEART_KINDS = {
    "": ((232, 128, 112), (178, 46, 46), (118, 26, 30)),
    "poisoned": ((176, 186, 104), (118, 132, 52), (70, 82, 30)),
    "withered": ((106, 96, 92), (58, 50, 48), (34, 28, 28)),
    "frozen": ((214, 234, 240), (128, 176, 196), (78, 118, 140)),
    "absorbing": ((250, 222, 140), (222, 166, 64), (160, 104, 34)),
}


def hearts():
    out = {}
    out["container"] = heart_container(EDGE, SOCKET)
    out["container_blinking"] = heart_container(PARCH_H, (90, 60, 54))
    out["container_hardcore"] = heart_container(EDGE, SOCKET)
    out["container_hardcore_blinking"] = heart_container(PARCH_H, (90, 60, 54))
    out["vehicle_container"] = heart_container(EDGE, SOCKET)
    for kind, cols in HEART_KINDS.items():
        blink = tuple(shade(c, 40) for c in cols)
        prefix = f"{kind}_" if kind else ""
        for hc in (False, True):
            hpre = "hardcore_" if hc else ""
            for half in (False, True):
                name = f"{prefix}{hpre}{'half' if half else 'full'}"
                out[name] = heart_sprite(cols, half, hc)
                out[name + "_blinking"] = heart_sprite(blink, half, hc)
    vehicle = ((236, 200, 150), (196, 140, 86), (132, 86, 50))  # mount: tan hide
    out["vehicle_full"] = heart_sprite(vehicle)
    out["vehicle_half"] = heart_sprite(vehicle, half=True)
    return out


LOAF = [  # a round loaf of bread, scored twice
    ".........",
    "..OOOOO..",
    ".OhhccbO.",
    "OhsccscbO",
    "OcscscsbO",
    "ObcscsbbO",
    ".ObbbbbO.",
    "..OOOOO..",
    ".........",
]


def food(state, hunger=False):
    if hunger:  # gone stale: grey-green crust
        crust, score, base = (150, 156, 96), (112, 122, 70), (96, 104, 58)
    else:
        crust, score, base = (214, 160, 92), (240, 210, 150), (160, 104, 56)
    cv = Canvas(9, 9)
    for y, row in enumerate(LOAF):
        for x, ch in enumerate(row):
            if state == "empty":
                if ch == "O":
                    cv.set(x, y, EDGE)
                elif ch != ".":
                    cv.set(x, y, SOCKET if not hunger else (40, 40, 26))
                continue
            if state == "half" and x < 4:
                continue
            c = {"O": None, "h": shade(crust, 26), "c": crust, "s": score, "b": base}.get(ch)
            if c:
                cv.set(x, y, c)
    if state == "half":
        cv.vline(4, 2, 6, base)
    return cv


ARMOR = [
    ".OO...OO.",
    "OhaO.OaaO",
    "OhaaOaadO",
    "OhaaaaadO",
    ".OhaaadO.",
    ".OhastdO.",
    ".OhaaadO.",
    ".OaaaddO.",
    "..OOOOO..",
]


def armor(state):
    cv = Canvas(9, 9)
    for y, row in enumerate(ARMOR):
        for x, ch in enumerate(row):
            if ch == ".":
                continue
            if ch == "O":
                cv.set(x, y, EDGE)
                continue
            if state == "empty" or (state == "half" and x > 4):
                cv.set(x, y, SOCKET)
                continue
            cv.set(x, y, {"h": (214, 214, 206), "a": (168, 170, 164), "d": (112, 112, 110),
                          "s": LEATHER_L, "t": BRASS}[ch])
    if state == "half":
        cv.vline(4, 1, 7, EDGE)
    return cv


def air(bursting=False):
    cv = Canvas(9, 9)
    rows = [
        "..OOOOO..",
        ".OmmmmmO.",
        "OmllmmmdO",
        "Omlmmmmd0",
        "OmmmmmmdO",
        "OmmmmmmdO",
        "OmmmmmddO",
        ".OddddOO.",
        "..OOOOO..",
    ]
    if bursting:
        rows = [
            "..O...O..",
            ".O.m.m.O.",
            "O.l...d.O",
            ".m.....d.",
            "O.......O",
            ".m.....d.",
            "O.m...d.O",
            ".O.d.d.O.",
            "..O...O..",
        ]
    cv.blit_map(0, 0, rows, {"O": MIST_D, "0": MIST_D, "m": (180, 200, 206, 200),
                             "l": (240, 248, 248), "d": (130, 150, 158, 220)})
    return cv


def main():
    q = "assets/questlog/textures/gui/"
    book_panel(PARCH, 1).save(q + "questlog.png")
    book_panel(PARCH_L, 2).save(q + "quest_page.png")
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
