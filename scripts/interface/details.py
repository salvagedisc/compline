"""Fallow's own details over GUI Revision's panels: the leaf pattern, the rounded
slots, the tiled wells where creatures stand, the crafting grids' frames, and the
slim fletched arrow and plus.

Everything here is drawn in the mist (`palette.BRUME` and the tones below);
build.py then takes a screen's family region to its family by brightness
(`familify`), so a detail keeps its relief in every colour.
"""
from collections import Counter

from PIL import Image

from palette import BRUME, FAMILIES, lum

FILL = BRUME[(193, 198, 198)]
OUTLINE = BRUME[(28, 28, 31)]
BONE = BRUME[(218, 231, 230)]

# Background pattern: one small leaf, repeated tone on tone on a tight
# staggered grid anchored to the texture's corner, like the beans of a coffee
# sack; the slots and ornaments are drawn over it.
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

# Slots: a recessed well with rounded corners, its outline a step darker at the
# top and left than at the bottom and right, no black edge and no cast shadow.
EDGE_TOP = (100, 110, 96)
EDGE_FADE = (112, 121, 106)
EDGE_LOW = (160, 166, 148)
SLOT_FILL = (130, 140, 122)
# corner profile: how far each of the first rows is cut in, by slot size
CORNERS = {18: [3, 1, 1], 30: [4, 2, 1, 1]}
# deeper wells behind the player and the mounts, tiled
WELL_EDGE = (84, 94, 81)
WELL_FILL = (110, 120, 104)
WELL_TILE = (116, 126, 109)
WELL_JOINT = (98, 108, 93)
TILE = 6

ARROW_INK = BRUME[(109, 115, 115)]
ARROW_LIGHT = BONE
LANTERN_D = (168, 106, 44)
LANTERN = (226, 166, 78)
LANTERN_L = (248, 214, 140)
LANTERN_EDGE = (110, 70, 34)

# every tone a detail is drawn in, which a family takes by brightness
TONES = set(BRUME.values()) | {LEAF_TONE, EDGE_TOP, EDGE_FADE, EDGE_LOW, SLOT_FILL, WELL_EDGE, WELL_FILL,
                               WELL_TILE, WELL_JOINT}


def arrow_rows(width, height, fletched=True):
    """Fallow's arrow, width long and height tall (even): a two-pixel shaft, a
    barbed head of two slim diagonals meeting at the tip, and a notch of
    fletching at the tail."""
    k = height // 2 - 1  # rows above the shaft
    head = 2 * k
    rows = []
    for y in range(height):
        d = k - y if y < k else y - k - 1 if y > k + 1 else 0
        row = ["."] * width
        if d == 0:
            row = ["#"] * width
        else:
            a, b = max(0, k - d - 1), max(0, 2 * (k - d) - 1)
            for x in range(a, b + 1):
                row[width - head + x] = "#"
            if fletched and d == 1 and width >= 12:
                row[1] = "#"
        rows.append("".join(row))
    return rows


ARROW = arrow_rows(22, 10)
# between the inventory's crafting grid and its result
SHORT_ARROW = arrow_rows(13, 10)
# where a mod's progress sprite is that small
SMALL_ARROW = arrow_rows(17, 8)
# Fallow's plus, 8x8 with two-pixel bars
PLUS = ["...##..."] * 3 + ["########"] * 2 + ["...##..."] * 3

# Shapes mods draw their own arrows in, whatever the colour: vanilla's wide arrow,
# Farm & Charm's two (inside an outline), and Exposure's plus.
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
FC_ARROWS = [
    [".............#...", ".............##..", ".............###.", "#################",
     "#################", ".............###.", ".............##..", ".............#..."],
    [".............##...", ".............###..", ".............####.", "##################",
     "##################", "..............###.", ".............###..", ".............##..."],
]
EXPOSURE_PLUS = ["....##...."] * 4 + ["##########"] * 2 + ["....##...."] * 4
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


def shape(rows):
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"}


# Brewing's arrow, pointing down: the small arrow turned, its shaft drawn out to 28 rows.
BREW_ARROW = ({(c, r) for c in (3, 4) for r in range(22)}
              | {(y, x + 11) for x, y in shape(SMALL_ARROW) if x >= 11})
# The same, six pixels wide and 27 rows long, for JEI's brewing stand.
JEI_BREW_ARROW = ({(c, r) for c in (2, 3) for r in range(23)}
                  | {(y, x + 17) for x, y in shape(arrow_rows(10, 6, fletched=False)) if x >= 6})


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


def draw_arrow(im, x0, y0, ink=ARROW_INK, light=ARROW_LIGHT, top=None, rows=ARROW, pts=None):
    """Fallow's arrow (or another shape in the same ink) engraved: a pixel of
    light under it; with top, its upper edge lit (a progress arrow)."""
    px = im.load()
    pts = pts or shape(rows)
    for x, y in pts:
        if (x, y + 1) not in pts and light and y0 + y + 1 < im.height:
            px[x0 + x, y0 + y + 1] = light + (255,)
    for x, y in pts:
        px[x0 + x, y0 + y] = (top if top and (x, y - 1) not in pts else ink) + (255,)


def lit_arrow(im, x0, y0, rows=ARROW, pts=None):
    draw_arrow(im, x0, y0, LANTERN, LANTERN_D, LANTERN_L, rows=rows, pts=pts)


def progress_arrow():
    """The 24x16 progress sprite of furnaces: the arrow in lantern light."""
    im = Image.new("RGBA", (24, 16))
    lit_arrow(im, 1, 3)
    return im


def brew_progress():
    """The brewing stand's 9x28 progress sprite: its arrow in lantern light, lit
    on its left side, shaded on the right of its head."""
    im = Image.new("RGBA", (9, 28))
    px = im.load()
    for x, y in BREW_ARROW:
        px[x, y] = (LANTERN_L if (x - 1, y) not in BREW_ARROW else
                    LANTERN_D if (x + 1, y) not in BREW_ARROW and y >= 22 else LANTERN) + (255,)
    return im


def trade_arrow():
    """The trader's 10x9 arrow, which GUI Revision leaves blank."""
    im = Image.new("RGBA", (10, 9))
    draw_arrow(im, 0, 0, rows=arrow_rows(10, 8, fletched=False))
    return im


def bent_arrow(x0, y0, x1, tip):
    """Fallow's arrow turning down: its shaft from (x0, y0) along to the column x1,
    then down to the tip, with the same head turned."""
    head = {(y, x - 14) for x, y in shape(ARROW) if x >= 14}
    hx, hy = x1 - 4, tip - 7
    pts = {(hx + a, hy + b) for a, b in head}
    pts |= {(x, y) for x in range(x0, x1 + 2) for y in (y0, y0 + 1)}
    pts |= {(x, y) for x in (x1, x1 + 1) for y in range(y0, hy)}
    return pts


def reshape_arrows(im, path):
    """Arrows a compat or a mod drew in its own shape and colour (vanilla's wide arrow,
    Farm & Charm's, Exposure's plus, and those of MOD_ARROWS) become Fallow's: in ink
    on the panel, in lantern light where they are progress sprites, on transparency."""
    px = im.load()
    width, height = im.size
    shapes = [(VANILLA_ARROW, ARROW, 0, 2), (FC_ARROWS[0], SMALL_ARROW, 1, 0), (FC_ARROWS[1], SMALL_ARROW, 1, 0)]
    if "/exposure/" in path:
        shapes.append((EXPOSURE_PLUS, PLUS, 0, 1))
    for old, new, outline, dy in shapes:
        pts, w, h = shape(old), len(old[0]), len(old)
        k0 = min(pts)
        for y in range(height - h + 1):
            for x in range(width - w + 1):
                c = px[x + k0[0], y + k0[1]]
                if not c[3] or c[:3] == FILL or not all(px[x + a, y + b] == c for a, b in pts):
                    continue
                if any(px[x + a, y + b] == c for a in range(w) for b in range(h) if (a, b) not in pts):
                    continue
                ring = [px[i, j] for i in range(x - 2, x + w + 2) for j in range(y - 2, y + h + 2)
                        if 0 <= i < width and 0 <= j < height and not (x <= i < x + w and y <= j < y + h)]
                progress = sum(1 for q in ring if not q[3]) * 2 > len(ring)
                ground = (0, 0, 0, 0) if progress else FILL + (255,)
                for i in range(max(x - outline, 0), min(x + w + outline, width)):
                    for j in range(max(y - outline, 0), min(y + h + outline, height)):
                        if (i - x, j - y) in pts or (outline and px[i, j][:3] != FILL):
                            px[i, j] = ground
                if progress:
                    lit_arrow(im, x, y + dy, rows=new)
                else:
                    draw_arrow(im, x, y + dy, rows=new)
    for (a, b, c, d), (kind, *at) in MOD_ARROWS.get(path, []):
        ground = px[a - 1, b - 1] if px[a - 1, b - 1][:3] == FILL or not px[a - 1, b - 1][3] else FILL + (255,)
        for i in range(a, c):
            for j in range(b, d):
                px[i, j] = ground
        if kind == "right":
            draw_arrow(im, *at)
        else:
            draw_arrow(im, 0, 0, pts=bent_arrow(*at))


def replace_arrows(im, arrows):
    """GUI Revision's arrows (its furnace's wide one, its small triangle) and the
    progress arrow stored next to them are cleared, in its greys; returns where
    Fallow's arrow goes, and puts the lit one in the progress arrow's place."""
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
                out.append((ox - 4, oy + 1))  # where the progress sprite's arrow lies
            else:
                out.append((ox + 2 - 11, oy))
    for ox, oy in matches(im, filled):
        for dx, dy in filled:
            px[ox + dx, oy + dy] = (0, 0, 0, 0)
        im.alpha_composite(progress_arrow(), (ox, oy))
    return out


# ---------------------------------------------------------------- pattern

def add_pattern(im, family="brume", avoid=()):
    """The leaf, repeated tone on tone over every large stretch of bare panel of
    the family, on one grid anchored to the texture's corner: the same pattern
    everywhere, passing under the slots and ornaments drawn over it."""
    fam = FAMILIES[family]
    fill = fam[(193, 198, 198)] + (255,)
    leaf = tone(lum(LEAF_TONE), fam) + (255,)
    px = im.load()
    width, height = im.size
    bare, seen = set(), set()
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
    for i, j in bare - set(avoid):
        row = j // ph
        u = (i - (pw // 2 if row % 2 else 0)) % pw
        v = j % ph
        if v < len(LEAF) and u < len(LEAF[0]) and LEAF[v][u] == "#":
            px[i, j] = leaf


def tone(v, family=BRUME):
    """A family's colour for a grey of luminance v, along its ramp."""
    ramp = sorted((lum(k), c) for k, c in family.items())
    if v <= ramp[0][0]:
        return ramp[0][1]
    for (l0, c0), (l1, c1) in zip(ramp, ramp[1:]):
        if v <= l1:
            t = (v - l0) / (l1 - l0)
            return tuple(round(a + (b - a) * t) for a, b in zip(c0, c1))
    return ramp[-1][1]


def familify(im, family, region):
    """The mist's tones in the region to the family's: GUI Revision's greys to
    the family's own, the details' tones by brightness; the lantern's light and
    a mod's colours stay."""
    if family == "brume" or not region:
        return
    fam = FAMILIES[family]
    back = {v: k for k, v in BRUME.items()}
    px = im.load()
    for x, y in region:
        r, g, b, a = px[x, y]
        c = (r, g, b)
        if a and c in TONES:
            px[x, y] = (fam[back[c]] if c in back else tone(lum(c), fam)) + (a,)


# ---------------------------------------------------------------- slots

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
    """Vanilla slots: 18x18 up to 32x32, dark top-left edge, white bottom-right
    edge; also flat 16x16 slot-grey cells drawn in a grid without those edges."""
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


def corner_mask(w, h, cut):
    """Mask of a w x h rectangle whose corners are cut in by cut[k] on row k."""
    def inside(i, j):
        if not (0 <= i < w and 0 <= j < h):
            return False
        k = min(j, h - 1 - j)
        return k >= len(cut) or cut[k] <= i <= w - 1 - cut[k]
    return inside


def draw_slot(im, x, y, w, h, kind="square", around=FILL + (255,), cells=()):
    """Redraws the slot cell (x, y, w, h) as a well with rounded corners, or, for
    "creature", a deeper well laid with small tiles. The outline is darker at the
    top and left, fading towards the ends, lighter at the bottom and right. A
    placeholder drawn inside the slot (a banner, a dye, a blueprint) is kept.
    With the other cells of the screen given, each cut corner takes the colour
    beside it (a frame, a border), and only where four cells meet does it stay
    panel, a small cross between them."""
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
                c = well_tile(i - 1, j - 1)
            elif before[i, j] != ground and max(w, h) <= 18:
                c = before[i, j][:3]
            else:
                c = SLOT_FILL
            px[x + i, y + j] = c + (255,)


def well_tile(i, j):
    """The tiles of a well, TILE pixels apart from its inner corner: a dark joint
    along their top and left, the tiles in two tones, set like a checkerboard."""
    if i % TILE == TILE - 1 or j % TILE == TILE - 1:
        return WELL_JOINT
    return WELL_TILE if (i // TILE + j // TILE) % 2 else WELL_FILL


def clear_leftovers(im, rects):
    """Bits of the source's old slot art left just outside the redrawn cells (a light
    corner, a line along the player's well) become panel: marks in the mist's
    tones lying wholly within two pixels of a cell."""
    px = im.load()
    width, height = im.size
    tones = set(BRUME.values()) - {FILL}
    inside, near = set(), set()
    for a, b, c, d, *_ in rects:
        inside.update((i, j) for i in range(a, a + c) for j in range(b, b + d))
        near.update((i, j) for i in range(a - 2, a + c + 2) for j in range(b - 2, b + d + 2))

    def mark(i, j):
        return (0 <= i < width and 0 <= j < height and (i, j) not in inside
                and px[i, j][3] and px[i, j][:3] in tones)

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


# ---------------------------------------------------------------- frames

def frame_slices(crafting):
    """The frame of the crafting table's grid (cells 29-82 x 16-69), in the mist:
    four pixels on each side and a two-row shadow under it, panel left out."""
    px = crafting.load()
    return {(u, v): px[sx, sy] for u, sx in ((0, 25), (1, 26), (2, 27), (3, 28), (4, 40), (5, 83), (6, 84),
                                            (7, 85), (8, 86))
            for v, sy in ((0, 12), (1, 13), (2, 14), (3, 15), (4, 40), (5, 70), (6, 71), (7, 72), (8, 73),
                          (9, 74), (10, 75))
            if (u, v) != (4, 4) and px[sx, sy][3] and px[sx, sy][:3] != FILL}


def add_frames(im, cells, frame, texts=()):
    """Frames each block of 2x2 to 3x3 slots like the crafting table's grid, where the
    panel around it is bare with two pixels to spare. Where a text or the name
    plate (boxes x0, y0, x1, y1) would touch it, the frame is slim: its outline
    and inner row above and below, no shadow."""
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
        slim = any(a - 1 <= x1 + 3 and x0 - 4 <= c + 1 and b - 1 <= y1 + 5 and y0 - 4 <= d + 1
                   for a, b, c, d in texts)
        rows = {y0 - 2: 0, y0 - 1: 3, y1: 5, y1 + 1: 8} if slim else {}
        for i, j in ring:
            if not (x0 - 4 <= i < x1 + 4 and y0 - 4 <= j < y1 + 6):
                continue
            u = i - x0 + 4 if i < x0 else 4 if i < x1 else i - x1 + 5
            v = j - y0 + 4 if j < y0 else 4 if j < y1 else j - y1 + 5
            if slim and v != 4:
                v = rows.get(j)
            if (u, v) in frame:
                px[i, j] = frame[u, v]


def dress(im, slots, wells, frame, texts=()):
    """A screen's slots and wells, Fallow's way: the cells a grid skipped added,
    wells pulled back from the slots, the old slot art around them cleared, the
    crafting grids framed, then each slot and well redrawn."""
    slots = [s for s in slots if all(s[0] + s[2] <= a or a + c <= s[0] or s[1] + s[3] <= b or b + d <= s[1]
                                     for a, b, c, d, *_ in wells)]
    slots = fill_gaps(im, list(dict.fromkeys(slots)), wells)
    spaced = separate(slots + [w[:4] for w in wells])
    n = len(spaced) - len(wells)
    slots, wells = spaced[:n], [c + tuple(w[4:]) for c, w in zip(spaced[n:], wells)]
    clear_leftovers(im, slots + wells)
    add_frames(im, slots, frame, texts)
    px = im.load()
    for x, y, w, h in slots:
        clear = x == 0 or px[x - 1, y + h // 2][3] == 0
        draw_slot(im, x, y, w, h, around=(0, 0, 0, 0) if clear else FILL + (255,), cells=slots + wells)
    for x, y, w, h, *kind in wells:
        draw_slot(im, x, y, w, h, kind[0] if kind else "creature", cells=slots + wells)
