"""The Fallow logo: the word in old carved stone uncials (the Uncial Antiqua
typeface by Astigmatic, SIL Open Font License), weathered, chipped and
cracked, with moss in cushions on their ledges, ivy hanging from them and
wound round the F, a seedling and a few flowers, and a lit lantern hanging on
a chain inside the O, its light warming the stone around it.

Everything is drawn at one pixel per GUI pixel, lit from the top left, then
scaled four times. The ruin and the plants are composed by hand: each chip,
crack, cushion of moss and vine below is placed on its letter, in pixels from
the top left of the letter's box, and snapped to its edge. Called by menus.py.
"""
import hashlib
import math
import urllib.request
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT = ("https://raw.githubusercontent.com/google/fonts/main/ofl/uncialantiqua/UncialAntiqua-Regular.ttf",
        "7499d3790224f6d568ac37b267f4b4e02bbcc05d")
CACHE = Path(__file__).resolve().parents[2] / ".cache" / "interface"

OUTLINE = (26, 31, 28)
BONE = (240, 236, 214)
MIST = (203, 202, 182)
SHADE = (150, 156, 136)
DEEP = (112, 121, 106)
# the stone, from its lit edges to the shade of its sides
STONE = [(255, 252, 236), BONE, (224, 221, 198), MIST, (178, 180, 158), SHADE, DEEP, (84, 94, 82), (62, 72, 62)]
MOSS = [(52, 66, 50), (78, 98, 72), (106, 130, 90), (140, 160, 112), (176, 190, 136)]
STEM = (58, 74, 54)
BLOOM = ((250, 246, 228), (226, 166, 78))           # a flower's petals and heart
LANTERN_L = (248, 214, 140)
# the lantern's light on the stone: on the sides of the O facing it, from
# the nearest to the furthest, and on the outline
WARM = [(226, 180, 116), (190, 142, 90), (146, 112, 78)]
WARM_EDGE = (66, 48, 34)
IRON = [(40, 46, 42), (70, 78, 72), (108, 116, 104), (146, 152, 136)]

# The lantern hung in the O, after the pack icon's (build.py), one pixel
# narrower so that it sits on the counter's middle column: a ring, a cap lit
# from the left, the glass in its frame around the flame, a rim and two feet.
LANTERN = [
    ".....KKK.....",
    "....K...K....",
    ".....KhK.....",
    "....KhmdK....",
    "...KhmmMdK...",
    "..KhmMMMMdK..",
    ".KKKKKKKKKKK.",
    ".KpDLLLLLDqK.",
    ".KpgYYYYYLqK.",
    ".KpYYYFYYYqK.",
    ".KpYYFHFYYqK.",
    ".KpYYFWFYYqK.",
    ".KpYFHWHFYqK.",
    ".KpLYFHFYLqK.",
    ".KpDLYKYLDqK.",
    "KKKKKKKKKKKKK",
    ".KhmmMMMMMdK.",
    "..KKKKKKKKK..",
    "..KK.....KK..",
]
LANTERN_COLOURS = {"K": OUTLINE, "d": IRON[0], "M": IRON[1], "m": IRON[2], "h": IRON[3],
                   "p": (138, 122, 98), "q": (66, 60, 50),
                   "D": (168, 106, 44), "L": (226, 166, 78), "Y": LANTERN_L, "F": (255, 226, 160),
                   "H": (255, 246, 222), "W": (255, 255, 248), "g": (255, 250, 235)}
FLAME = (6, 11)    # the flame's heart in the lantern
# the chain, from the top: a link seen flat, then one seen edge on
CHAIN = [".K.", "K.K", "K.K", ".K.", ".M.", ".M."]
GLOW = 20          # reach of the lantern's light, in pixels
HALO = [(5, 150), (8, 112), (11, 80), (14, 52), (17, 28), (20, 12)]   # its halo: radius, opacity

TEXT, SIZE, SPACING, SEED = "FALLOW", 54, 1, 4
WEATHER = 1.4      # how far the stone's tone wanders, in broad patches
PAD = (16, 16, 16, 22)
SCALE = 4

# The ruin. A chip is a chunk broken off a letter, given as the outline of
# the break (pixels from the letter's top left); a crack a line through the
# stone, from where it opens on an edge.
CHIPS = [
    (0, [(3.5, 11.5), (6, 13), (6.5, 15), (5, 16.5), (3.5, 17.5)]),            # F: a bite out of the stem
    (1, [(16.5, -0.5), (21.5, -0.5), (21.5, 1), (19, 1.5), (18, 1)]),           # A: the top of its stroke
    (2, [(9.5, -0.5), (13.5, -0.5), (13.5, 4.5), (12, 3.5), (11, 1.5), (10, 1)]),  # L: a corner gone
    (3, [(13.5, 24), (11, 26), (11.5, 27.5), (13.5, 29)]),                     # L: a nick in the stem
    (4, [(32.5, 3.5), (34, 3.5), (38, 7.5), (38, 9), (36.5, 8.5), (34, 6.5), (32.5, 5.5)]),  # O: its shoulder
    (5, [(57.5, -0.5), (62.5, -0.5), (62.5, 2.5), (60, 3), (59, 1.5)]),         # W: the end of its arm
]
CRACKS = [
    (2, [(11, 3), (9, 6), (10, 9), (8, 13)]),        # L: down from the broken corner
    (4, [(34, 6), (32, 9), (33, 12), (32, 14)]),     # O: in from its shoulder
    (1, [(27, 9), (25, 11), (25, 13), (23, 15)]),    # A: across the stroke
]
# The moss: cushions on ledges (letter, point on a ledge, half width, height),
# and small ones where a ledge meets a rising wall, where water sits.
CUSHIONS = [
    (0, (3, 0), 4, 2), (0, (21, 0), 4, 2), (0, (17, 21), 5, 2),
    (1, (13, 0), 4, 2), (1, (3, 36), 3, 1), (1, (39, 35), 5, 2),
    (2, (5, 0), 4, 2), (2, (17, 47), 6, 3), (2, (27, 47), 2, 1),
    (3, (6, 0), 5, 2), (3, (16, 47), 4, 2),
    (4, (20, 0), 6, 3), (4, (20, 37), 5, 2),
    (5, (9, 0), 6, 2), (5, (32, 0), 4, 2), (5, (54, 0), 4, 3),
]
# The plants. A vine hangs from a letter's underside, from the point nearest
# to (x, y), long so many pixels, swaying left (-1) or right (1), and ends in
# a curl or not. The ivy winds down round the F's stem, from a cushion of moss.
VINES = [
    (0, (24, 2), 7, 1, True),       # F: under its top arm
    (0, (22, 22), 6, -1, True),     # F: under its middle arm
    (1, (9, 2), 10, -1, True),      # A: from its flag
    (1, (41, 37), 7, 1, False),     # A: its right foot
    (2, (15, 48), 12, -1, True),    # L
    (3, (8, 48), 4, 1, False),      # L
    (3, (20, 48), 13, 1, True),     # L, the longest
    (4, (12, 36), 9, -1, True),     # O
    (4, (28, 36), 4, 1, False),
    (5, (18, 37), 11, 1, True),     # W
    (5, (43, 36), 8, -1, True),
]
# letter, the stem's span, and the height and drop of each turn in front
IVY = (0, (4, 12), [(23, 5), (32, 5), (40, 4)])
# vines clinging to a letter's face or edge: letter, the vine's path
CLINGS = [
    (5, [(57, 1), (59, 5), (58, 10), (56, 15), (55, 20), (56, 23)]),    # W: down its right arm
]
# the leaves on a vine, hanging to the right of its stem (a: lit, b, c: shade)
LEAVES = {3: ((1, 0, "a"), (2, 0, "b"), (1, 1, "b"), (2, 1, "b"), (3, 1, "c"), (2, 2, "c")),
          2: ((1, 0, "a"), (2, 0, "b"), (1, 1, "b"), (2, 1, "c")),
          1: ((1, 0, "b"), (1, 1, "c"))}
LEAF = {"a": MOSS[3], "b": MOSS[2], "c": MOSS[1]}
SEEDLING = (3, (6, 0), [        # letter, point on a ledge: a seedling on the L
    ".a...a.",
    "abb.bbc",
    ".bbSbc.",
    "...S...",
    "...S.ab",
    "...Sbc.",
    "...S...",
])
FLOWERS = [(5, (56, 0)), (2, (18, 47)), (1, (41, 35))]   # letter, point on a ledge

N4 = ((1, 0), (-1, 0), (0, 1), (0, -1))


def font_file():
    url, sha1 = FONT
    path = CACHE / url.rsplit("/", 1)[1]
    if not path.exists() or hashlib.sha1(path.read_bytes()).hexdigest() != sha1:
        path.parent.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(url, timeout=120) as response:
            data = response.read()
        if hashlib.sha1(data).hexdigest() != sha1:
            raise SystemExit(f"{path.name}: checksum mismatch")
        path.write_bytes(data)
    return path


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def rand(*key):
    """A number in [0, 1) that depends only on the key and the seed."""
    h = SEED * 0x9E3779B1 & 0xFFFFFFFF
    for k in key:
        h = (h ^ (k & 0xFFFFFFFF)) * 0x85EBCA6B & 0xFFFFFFFF
        h ^= h >> 13
        h = h * 0xC2B2AE35 & 0xFFFFFFFF
        h ^= h >> 16
    return h / 2 ** 32


def noise(x, y, scale, salt=0):
    """Smooth value noise in [0, 1], with features about scale pixels across."""
    gx, gy = x / scale, y / scale
    ix, iy = math.floor(gx), math.floor(gy)
    fx, fy = gx - ix, gy - iy
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a, b = rand(salt, ix, iy), rand(salt, ix + 1, iy)
    c, d = rand(salt, ix, iy + 1), rand(salt, ix + 1, iy + 1)
    return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy


def components(pixels):
    seen, parts = set(), []
    for p in sorted(pixels):
        if p in seen:
            continue
        part, todo = set(), [p]
        seen.add(p)
        while todo:
            x, y = todo.pop()
            part.add((x, y))
            for dx, dy in N4:
                q = (x + dx, y + dy)
                if q in pixels and q not in seen:
                    seen.add(q)
                    todo.append(q)
        parts.append(part)
    return parts


def letters():
    """The text as one set of pixels per letter, without antialiasing, and the
    canvas size."""
    font = ImageFont.truetype(str(font_file()), SIZE)
    mask = Image.new("L", (SIZE * len(TEXT) * 2, SIZE * 2), 0)
    d = ImageDraw.Draw(mask)
    d.fontmode = "1"
    x, middles = 8, []
    for ch in TEXT:
        d.text((x, SIZE // 2), ch, font=font, fill=255)
        box = d.textbbox((x, SIZE // 2), ch, font=font)
        middles.append((box[0] + box[2]) / 2)
        x += font.getlength(ch) + SPACING
    x0, y0, _, _ = mask.getbbox()
    mask = mask.crop(mask.getbbox())
    mp = mask.load()
    pixels = {(x + PAD[0], y + PAD[1]) for y in range(mask.height) for x in range(mask.width) if mp[x, y]}
    middles = [m - x0 + PAD[0] for m in middles]
    glyphs = [set() for _ in TEXT]
    for part in components(pixels):
        mx = sum(x for x, _ in part) / len(part)
        glyphs[min(range(len(TEXT)), key=lambda i: abs(middles[i] - mx))] |= part
    return glyphs, (mask.width + PAD[0] + PAD[2], mask.height + PAD[1] + PAD[3])


def corner(glyph):
    return min(x for x, _ in glyph), min(y for _, y in glyph)


def nearest(glyph, point, test):
    """The pixel of a letter passing a test nearest to a point given from the
    letter's top left."""
    x0, y0 = corner(glyph)
    px, py = x0 + point[0], y0 + point[1]
    return min((p for p in glyph if test(p)), key=lambda p: ((p[0] - px) ** 2 + (p[1] - py) ** 2, p))


def on_ledge(glyph, point):
    return nearest(glyph, point, lambda p: (p[0], p[1] - 1) not in glyph)


def underside(glyph, point):
    return nearest(glyph, point, lambda p: (p[0], p[1] + 1) not in glyph)


def inside(polygon, x, y):
    """Whether the point is inside the polygon (even-odd rule)."""
    hit = False
    for (x1, y1), (x2, y2) in zip(polygon, polygon[1:] + polygon[:1]):
        if (y1 > y) != (y2 > y) and x < x1 + (y - y1) * (x2 - x1) / (y2 - y1):
            hit = not hit
    return hit


def tidy(stone):
    """Take off the pixels a chip left hanging by a thread."""
    while True:
        loose = {p for p in stone if sum((p[0] + dx, p[1] + dy) in stone for dx, dy in N4) < 2}
        if not loose:
            return stone
        stone -= loose


def counter_of(glyph):
    """The enclosed hole of a letter."""
    x0, y0 = corner(glyph)
    x1, y1 = max(x for x, _ in glyph), max(y for _, y in glyph)
    air, todo = set(), [(x0 - 1, y0 - 1)]
    while todo:
        q = todo.pop()
        if q in air or q in glyph or not (x0 - 1 <= q[0] <= x1 + 1 and y0 - 1 <= q[1] <= y1 + 1):
            continue
        air.add(q)
        todo += [(q[0] + dx, q[1] + dy) for dx, dy in N4]
    return {(x, y) for x in range(x0, x1 + 1) for y in range(y0, y1 + 1) if (x, y) not in glyph | air}


def line(a, b):
    """The pixels of a line from a to b."""
    n = max(round(max(abs(b[0] - a[0]), abs(b[1] - a[1]))), 1)
    return [(round(a[0] + (b[0] - a[0]) * k / n), round(a[1] + (b[1] - a[1]) * k / n)) for k in range(n + 1)]


def grow_moss(glyphs, stone, counter):
    """The moss, as pixel: colour. Each cushion is a few lumps side by side on
    a run of ledge, grown a pixel or two down the face, lit from the top left,
    a shadow in the creases between the lumps."""
    cushions = list(CUSHIONS)
    for i, g in enumerate(glyphs):   # crevices: ledges between rising walls
        x0, y0 = corner(g)
        for x, y in g:
            if (x, y - 1) in stone or (x, y - 1) in counter:
                continue
            walls = sum(any((x + s * dx, y - dy) in g for dx in (1, 2) for dy in (1, 2, 3)) for s in (-1, 1))
            if walls == 2:
                cushions.append((i, (x - x0, y - y0), 2, 1))
    moss, crease = set(), set()
    for n, (i, point, half, height) in enumerate(cushions):
        g = glyphs[i]
        cx, cy = on_ledge(g, point)
        run, todo = {}, [(cx, cy)]
        while todo:   # the ledge on either side, as far as the cushion reaches
            p = todo.pop()
            if p[0] in run or p not in g or (p[0], p[1] - 1) in g or abs(p[0] - cx) > half:
                continue
            run[p[0]] = p[1]
            todo += [(p[0] + dx, p[1] + dy) for dx in (-1, 1) for dy in (-1, 0, 1)]
        lumps, x = [], cx - half + 1 + round(rand(n, 1))
        while x <= cx + half:
            r = 1.5 + rand(n, x)
            t = (x - cx) / (half + 1)
            lumps.append((x, r, max(1.0, height * math.sqrt(max(0.0, 1 - t * t)) * (0.8 + 0.5 * rand(n, x, 2)))))
            x += 2 + round(r)
        rise = {}
        for x, y in run.items():
            rise[x] = round(max((lh * math.sqrt(max(0.0, 1 - ((x - lx) / (r + 0.5)) ** 2)) for lx, r, lh in lumps),
                                default=0))
        for x, y in run.items():
            up = rise[x]
            if 0 < up < rise.get(x - 1, 0) and up < rise.get(x + 1, 0):
                crease.add((x, y - up))
            down = 1 + (up >= 2 and rand(x, y, 12) < 0.6) + (up >= 2 and rand(x, y, 13) < 0.2)
            for k in range(-up, down):   # leaving the stone's last row bare
                q = (x, y + k)
                if k < 0 or q in stone and (x, y + k + 1) in stone:
                    moss.add(q)
    # no lone threads of moss in the air
    moss = {p for p in moss if p in stone or (p[0] - 1, p[1]) in moss or (p[0] + 1, p[1]) in moss}
    colours = {}
    for x, y in moss:
        def bare(dx, dy):
            return (x + dx, y + dy) not in moss and (x + dx, y + dy) not in stone
        if (x, y) in crease:
            c = MOSS[1]
        elif bare(0, -1):
            c = MOSS[4] if bare(-1, 0) or bare(-1, -1) and not bare(1, -1) else MOSS[3]
        elif bare(-1, 0):
            c = MOSS[3]
        elif (x, y + 1) not in moss or bare(1, 0):
            c = MOSS[1]
        else:
            c = MOSS[2]
        colours[x, y] = c
    return colours


def draw():
    glyphs, (w, h) = letters()

    # the ruin: chips broken off, then cracks
    for i, polygon in CHIPS:
        x0, y0 = corner(glyphs[i])
        glyphs[i] -= {(x, y) for x, y in glyphs[i] if inside(polygon, x - x0 + 0.5, y - y0 + 0.5)}
    stone = tidy(set().union(*glyphs))
    glyphs = [g & stone for g in glyphs]
    top, bottom = min(y for _, y in stone), max(y for _, y in stone)
    cracks = set()
    for i, points in CRACKS:
        x0, y0 = corner(glyphs[i])
        path = [q for a, b in zip(points, points[1:]) for q in line(a, b)]
        cracks |= {(x0 + x, y0 + y) for x, y in path if (x0 + x, y0 + y) in stone}
    o = glyphs[TEXT.index("O")]
    counter = counter_of(o)

    im = Image.new("RGBA", (w, h))
    px = im.load()

    # the stone's thickness: a side face below and to the right of each letter
    side = {}
    for x, y in stone:
        for k, (dx, dy) in enumerate(((1, 1), (1, 2), (2, 3))):
            q = (x + dx, y + dy)
            if q not in stone:
                side[q] = min(side.get(q, 9), k)
    for (x, y), k in side.items():
        under = (x, y - 1) in stone or (x - 1, y - 1) in stone
        px[x, y] = STONE[8 if k == 2 or not under else 7] + (255,)

    # the stone's face: bone at the top fading to mist, weathered in broad
    # patches, bevelled towards the light; the cracks dark, their far lip lit
    for x, y in stone:
        t = (y - top) / (bottom - top)
        tone = 1.1 + 2.7 * t + (noise(x, y, 7) - 0.5) * WEATHER + (noise(x, y, 3, 1) - 0.5) * 0.4

        def out(dx, dy):
            return (x + dx, y + dy) not in stone
        if out(0, -1):
            tone = 0
        elif out(-1, 0) or out(-1, -1):
            tone -= 1.5
        elif out(0, -2) or out(-2, 0):
            tone -= 0.8
        if out(0, 1) or out(1, 0) or out(1, 1):
            tone = max(tone + 2, 5)
        elif out(0, 2) or out(2, 0):
            tone += 1
        px[x, y] = STONE[max(0, min(6, round(tone)))] + (255,)
    for x, y in cracks:
        px[x, y] = STONE[7] + (255,)
    for x, y in cracks:
        for q in ((x + 1, y), (x, y + 1)):
            if q in stone and q not in cracks:
                px[q] = STONE[1] + (255,)

    # the moss, darkening the stone just under it
    moss = grow_moss(glyphs, stone, counter)
    for (x, y), c in moss.items():
        px[x, y] = c + (255,)
    for x, y in moss:
        q = (x, y + 1)
        if q in stone and q not in moss:
            px[q] = mix(px[q][:3], STONE[6], 0.45) + (255,)

    # the outline, all round
    solid = stone | set(side) | set(moss)
    for x, y in list(solid):
        for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if q not in solid:
                px[q] = OUTLINE + (255,)

    def put(p, c):
        px[p] = c + (255,)

    def leaf(x, y, s, size):
        for dx, dy, c in LEAVES[size]:
            put((x + s * dx, y + dy), LEAF[c])

    # the ivy wound round the F's stem: in front for half a turn, then behind,
    # and on down the stem's left edge to dangle from its point
    i, (sl, sr), turns = IVY
    g = glyphs[i]
    x0, y0 = corner(g)

    def left_edge(y):
        return min(x for x, yy in g if yy == y) - 1
    for n, (ys, drop) in enumerate(turns):
        ys += y0
        rows = [x for x, y in g if y == ys and x0 + sl - 2 <= x <= x0 + sr + 2]
        a, b = min(rows) - 1, max(rows) + 1
        mid = ((a + b) / 2, ys + drop / 2 + 0.8)
        front = line((a, ys), mid)[:-1] + line(mid, (b, ys + drop))
        for p in front:
            put(p, STEM)
        leaf(a, ys, -1, 3)
        leaf(*front[len(front) // 3], 1 if n % 2 else -1, 2)
        leaf(*front[2 * len(front) // 3], -1 if n % 2 else 1, 3 if n % 2 else 2)
        leaf(b, ys + drop, 1, 2 if n % 2 else 3)
        end = y0 + turns[n + 1][0] if n + 1 < len(turns) else max(y for _, y in g) + 4
        for y in range(ys + drop + 2, end):   # where it comes back round on the left
            x = left_edge(y) if (x0 + sl, y) in g or (x0 + sl, y - 1) in g else x0 + sl - 1
            put((x, y), STEM)
            if n + 1 == len(turns) and (y - ys) % 3 == 0:
                leaf(x, y, -1 if (y - ys) % 6 else 1, 2 if y < end - 3 else 1)

    # the vines clinging to a face
    for i, points in CLINGS:
        x0, y0 = corner(glyphs[i])
        path = [(x0 + x, y0 + y) for a, b in zip(points, points[1:]) for x, y in line(a, b)[:-1]]
        path.append((x0 + points[-1][0], y0 + points[-1][1]))
        for k, p in enumerate(path):
            put(p, STEM)
            if k % 3 == 2 and k < len(path) - 1:
                leaf(*p, 1 if k // 3 % 2 else -1, 3 if k < len(path) * 0.6 else 2)

    # the vines hanging from the undersides
    for i, point, length, sway, curl in VINES:
        x, y = underside(glyphs[i], point)
        path = [(x + round(sway * 1.8 * (max(0, k - 4) / length) ** 1.6), y + k) for k in range(length + 4)]
        for p in path:
            put(p, STEM)
        k, n = 4, 0   # leaves every two or three pixels, on alternate sides, smaller to the tip
        while k < length + 3:
            size = 3 if k < length * 0.6 + 4 else 2 if k < length + 1 else 1
            leaf(*path[k], -sway if n % 2 == 0 else sway, size)
            k, n = k + (2 if n % 2 == 0 else 3), n + 1
        if curl:   # the tip turning up
            ex, ey = path[-1]
            put((ex + sway, ey + 1), STEM)
            put((ex + 2 * sway, ey), STEM)
            put((ex + 2 * sway, ey - 1), MOSS[2])

    # a seedling, and flowers in the moss
    i, point, rows = SEEDLING
    x, y = on_ledge(glyphs[i], point)
    while (x, y - 1) in moss:
        y -= 1
    for dy, row in enumerate(rows):
        for dx, c in enumerate(row):
            if c != ".":
                put((x - 3 + dx, y - len(rows) + dy), STEM if c == "S" else LEAF[c])
    for i, point in FLOWERS:
        x, y = on_ledge(glyphs[i], point)
        while (x, y - 1) in moss:
            y -= 1
        for dx, dy in ((0, -2), (-1, -1), (1, -1), (0, 0)):
            put((x + dx, y + dy), BLOOM[0])
        put((x, y - 1), BLOOM[1])

    # the lantern in the O, on the counter's middle column, on its chain
    cx = round(sum(x for x, _ in counter) / len(counter))
    ctop = min(y for x, y in counter if x == cx)
    cbottom = max(y for x, y in counter if x == cx)
    lw, lh = len(LANTERN[0]), len(LANTERN)
    lx, ly = cx - lw // 2, round((ctop + cbottom) / 2 - lh / 2) + 2
    fx, fy = lx + FLAME[0], ly + FLAME[1]
    lamp = {}
    for y in range(ctop, ly):
        for dx, c in enumerate(CHAIN[(y - ctop) % len(CHAIN)]):
            if c != ".":
                lamp[cx - 1 + dx, y] = LANTERN_COLOURS[c]
    for y, row in enumerate(LANTERN):
        for x, c in enumerate(row):
            if c != ".":
                lamp[lx + x, ly + y] = LANTERN_COLOURS[c]

    # its light: on the sides of the O facing it, on the face of the O near
    # its inner edge, and a halo in the counter
    for x in range(fx - GLOW, fx + GLOW + 1):
        for y in range(fy - GLOW, fy + GLOW + 1):
            d = math.hypot(x - fx, y - fy)
            p = (x, y)
            if d > GLOW or p in lamp or not px[p][3]:
                continue
            near = 1 - d / GLOW
            if p in side and p in counter:
                put(p, WARM[0 if near > 0.45 else 1 if near > 0.3 else 2] if near > 0.15 else px[p][:3])
            elif p in counter and px[p][:3] == OUTLINE:
                put(p, WARM_EDGE if near > 0.25 else OUTLINE)
            elif p in o:
                rim = min(abs(x - a) + abs(y - b) for a, b in counter) if counter else 9
                k = near * (1 if rim <= 1 else 0.6 if rim == 2 else 0.3 if rim == 3 else 0)
                k = round(k * 4) / 4
                if k:
                    put(p, mix(px[p][:3], LANTERN_L, 0.6 * k))
            elif p in moss and p in counter:
                put(p, mix(px[p][:3], LANTERN_L, 0.6 * near))
    for p in counter:
        if px[p][3] == 0:
            d = math.hypot(p[0] - fx, p[1] - fy)
            a = next((a for r, a in HALO if d <= r), 0)
            if a:
                px[p] = LANTERN_L + (a,)
    for p, c in lamp.items():
        put(p, c)

    # a soft moss halo, so that the logo reads on a light sky
    halo = Image.new("L", (w, h), 0)
    hp = halo.load()
    for x in range(w):
        for y in range(h):
            if px[x, y][3] == 255 and (x, y) not in counter:
                hp[x, y] = 255
    halo = halo.filter(ImageFilter.GaussianBlur(3)).point(lambda v: round(v * 0.45) if v > 8 else 0)
    hp = halo.load()
    for p in counter:   # but none in the O: there the lantern's light
        hp[p] = 0
    back = Image.new("RGBA", (w, h), MOSS[0] + (0,))
    back.putalpha(halo)
    back.alpha_composite(im)
    x0, y0, x1, y1 = back.getbbox()
    im = back.crop((x0 - 1, y0 - 1, x1 + 1, y1 + 1))
    return im.resize((im.width * SCALE, im.height * SCALE), Image.NEAREST)
