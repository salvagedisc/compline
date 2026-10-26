"""The title screen's panorama: the six faces of the cube the game turns slowly
behind the menu, in three variants. Each shows the idea of the pack rather
than a mere landscape: a world lost in a persistent mist that can be lifted
around a fire, a lantern or a machine, the clearing that opens there, green
and warm, and what the lifted mist lets one see beyond.

- vigil: dusk in a small clearing around a vigil fire; the fire warms the
  grass, the trunks and the mist beside it, sparks rise, fireflies wake; all
  around, a wall of cool mist where the land turns grey, ghosts of trees and
  a deer at its edge, and overhead the mist opening onto the evening sky;
- threshold: by day, at the edge of a lantern's clearing; behind, the lantern
  on its post and autumn birches along the far edge; ahead, the mist a few
  blocks away, a ring of standing stones fading into it and a monolith risen
  above it, a ring carved on it glowing faintly;
- lifted: high above the clearing a dissipateur keeps, in the evening light:
  its green floor with the machine and the crowns of its trees, the mist
  standing around like the wall of a crater, and from its rim a sea of mist
  to the horizon, holed by other clearings with a light in each; far ranges
  rise out of it, layer behind layer, a monolith on a ridge.

The scene is painted once on an equirectangular sphere (heading 0 to 360 left
to right, elevation 90 to -90 top to bottom), then each face is read from it,
so that the faces join without seams. The mist is a volume, not a gradient: a
layer of a given height that the clearing cuts like a cylinder, its colour
mixed in by the length a ray travels through it, so that the dome over the
clearing, the wall at its edge and the fading of the things beyond all come
from the same rule. Trees, stones and lights are flat cut-outs standing in
that world at their distance, drawn in texels like the game's blocks.

Python 3 and Pillow only, seeded: the same faces at every build, in about
seven seconds a variant.
    import panorama; panorama.faces(panorama.DEFAULT)
"""
import functools
import math
import random

from PIL import Image, ImageChops, ImageDraw, ImageFilter

SIZE = 1024        # a face as the game reads it
FACE = 512         # a face as drawn, then scaled twice: a mild pixel
W, H = 2048, 1024  # the sphere, about 5.7 pixels a degree
PAD = 320          # columns repeated on either side, so that blurs and faces wrap
CW = W + 2 * PAD
TPB = 8            # texels a block for the cut-outs
HALF = H // 2      # the horizon's row

# Fallow's tones: mist and bone, sage, moss, lantern light.
MIST = (206, 205, 188)
MIST_DEEP = (150, 160, 146)
SKY_HIGH = (122, 140, 136)
SKY_LOW = (188, 190, 174)
SUNLIGHT = (250, 230, 186)
AMBER = (246, 168, 74)
FLAME = (255, 214, 130)
GRASS = [(62, 86, 50), (70, 92, 54), (56, 78, 46), (76, 98, 56), (66, 88, 48)]
DRY = [(122, 124, 104), (114, 118, 100), (128, 128, 108), (118, 120, 98), (124, 126, 106)]
FLOWERS = [(236, 190, 84), (232, 228, 206), (176, 150, 186)]
BARK = (64, 52, 40)
BIRCH = (212, 208, 192)
OAK_LEAVES = [(50, 74, 46), (60, 86, 52), (72, 98, 58), (86, 112, 66)]
BIRCH_LEAVES = [(96, 120, 64), (108, 132, 70), (88, 110, 60), (124, 144, 80)]
SPRUCE = [(36, 52, 42), (44, 62, 48), (54, 74, 56)]
AUTUMN = [(128, 84, 44), (150, 104, 52), (168, 126, 64), (184, 146, 82)]
GOLD = [(132, 112, 58), (150, 128, 66), (166, 144, 80), (118, 100, 52)]
STONE = [(98, 102, 96), (112, 116, 108), (126, 128, 118), (86, 90, 86)]
MOSS = [(66, 88, 52), (82, 104, 62)]

VARIANTS = ("vigil", "threshold", "lifted")
DEFAULT = "vigil"


# --- small helpers -------------------------------------------------------------

def mix(a, b, t):
    return tuple(x + (y - x) * t for x, y in zip(a, b))


def rgb(c):
    return tuple(max(0, min(255, round(v))) for v in c)


def smooth(t):
    t = 0.0 if t < 0 else 1.0 if t > 1 else t
    return t * t * (3 - 2 * t)


def col_x(yaw):
    """The canvas column of a heading, margin included."""
    return PAD + (yaw % 360) / 360 * W


def row_y(pitch):
    return (90 - pitch) / 180 * H


YAWS = [(x + 0.5) / W * 360 for x in range(W)]
SIN = [math.sin(math.radians(a)) for a in YAWS]
COS = [math.cos(math.radians(a)) for a in YAWS]
PITCHES = [90 - (y + 0.5) / H * 180 for y in range(H)]


def wrapdiff(a, b):
    return (a - b + 180) % 360 - 180


def noise(rnd, w, h, cx, cy, both=False):
    """Smooth noise, w x h, from a grid of cx x cy random cells, periodic across
    its width (and its height too with `both`)."""
    grid = Image.new("L", (cx, cy))
    grid.putdata([rnd.randrange(256) for _ in range(cx * cy)])
    ty = 3 if both else 1
    big = Image.new("L", (cx * 3, cy * ty))
    for i in range(3):
        for j in range(ty):
            big.paste(grid, (i * cx, j * cy))
    big = big.resize((w * 3, h * ty), Image.BICUBIC)
    return big.crop((w, h if both else 0, 2 * w, 2 * h if both else h))


def fbm(rnd, w, h, octaves, both=False):
    """Octaves of noise summed, [(cells across, cells down, weight)...], their
    contrast stretched back to 0-255."""
    out, total = None, 0
    for cx, cy, wt in octaves:
        n = noise(rnd, w, h, cx, cy, both)
        out = n if out is None else Image.blend(out, n, wt / (total + wt))
        total += wt
    lo, hi = out.getextrema()
    return out.point(lambda v: round((v - lo) * 255 / max(1, hi - lo)))


def hashf(a, b, c=0):
    """A deterministic number in [0, 1) for three integers."""
    h = (a * 374761393 + b * 668265263 + c * 2147483647) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return (h ^ (h >> 16)) / 4294967296


def by_rows(fn, y0=0, y1=H):
    """An image W x H whose rows y0 to y1 are the colour fn(pitch)."""
    strip = Image.new("RGB", (1, H))
    for y in range(y0, y1):
        strip.putpixel((0, y), rgb(fn(PITCHES[y])))
    return strip.resize((W, H), Image.NEAREST)


def by_cols(fn):
    """An L image W x H whose columns are fn(yaw) in 0-1."""
    strip = Image.new("L", (W, 1))
    strip.putdata([max(0, min(255, round(255 * fn(a)))) for a in YAWS])
    return strip.resize((W, H), Image.NEAREST)


def mask(values):
    """An L image W x H from a flat list of 0-1 values."""
    return Image.frombytes("L", (W, H), bytes(max(0, min(255, int(v * 255 + 0.5))) for v in values))


def tinted(base, colour, amount):
    """base blended towards a flat colour by an L mask."""
    return Image.composite(Image.new("RGB", base.size, colour), base, amount)


def screen_add(base, colour, amount):
    """Light added: base screened with a colour weighed by an L mask."""
    lit = ImageChops.multiply(Image.new("RGB", base.size, colour), Image.merge("RGB", (amount,) * 3))
    return ImageChops.screen(base, lit)


def pad(im):
    """The sphere widened by its margins, each a copy of the other end."""
    out = Image.new(im.mode, (CW, im.height))
    out.paste(im, (PAD, 0))
    out.paste(im.crop((W - PAD, 0, W, im.height)), (0, 0))
    out.paste(im.crop((0, 0, PAD, im.height)), (W + PAD, 0))
    return out


def resync(im):
    """After drawing on the canvas: copies each end into the other's margin."""
    im.paste(im.crop((W, 0, W + PAD, im.height)), (0, 0))
    im.paste(im.crop((PAD, 0, 2 * PAD, im.height)), (W + PAD, 0))
    return im


def glow(canvas, yaw, pitch, radius, colour, strength, squash=1.0):
    """A soft light screened onto the canvas, `radius` in degrees."""
    r = radius / 360 * W
    size = int(r * 5) + 4
    layer = Image.new("RGB", (size, size))
    c = size / 2
    ImageDraw.Draw(layer).ellipse((c - r, c - r * squash, c + r, c + r * squash),
                                  fill=rgb(tuple(v * strength for v in colour)))
    layer = layer.filter(ImageFilter.GaussianBlur(r * 0.8))
    for off in (-W, 0, W):
        x0, y0 = round(col_x(yaw) + off - c), round(row_y(pitch) - c)
        if x0 + size < 0 or x0 > CW:
            continue
        box = (x0, y0, x0 + size, y0 + size)
        canvas.paste(ImageChops.screen(canvas.crop(box), layer), box)


# --- the mist --------------------------------------------------------------------

SOFT = 2.5  # blocks over which the mist sets in at the clearing's edge


def onset(ln):
    """A length into the mist, its start softened over SOFT blocks either side."""
    if ln <= -SOFT:
        return 0.0
    if ln < SOFT:
        return (ln + SOFT) ** 2 / (4 * SOFT)
    return ln


class Mist:
    """The mist of a scene: a layer about `top` blocks high, cut by a clearing of
    radius `radius` centred at `centre` (blocks east and north of the viewer),
    `depth` blocks of it hiding two thirds of what is behind. Above it, a thin
    veil up to `veil_top`, `veil` times as thick."""

    def __init__(self, rnd, eye, radius, centre, top, depth, veil=0.0, veil_top=60.0, wobble=0.12, swell=0.25):
        self.eye, self.radius, self.centre, self.top, self.depth = eye, radius, centre, top, depth
        self.veil, self.veil_top = veil, veil_top
        # the edge of the clearing and the top of the mist breathe by heading
        n = fbm(rnd, 720, 1, [(10, 1, 1), (36, 1, 0.45)]).tobytes()
        m = fbm(rnd, 720, 1, [(8, 1, 1), (28, 1, 0.5), (90, 1, 0.3)]).tobytes()
        self.edge = [1 + wobble * (v / 127.5 - 1) for v in n]
        self.lift = [top * (1 - swell + 2 * swell * v / 255) for v in m]

    def reach(self, yaw):
        """The distance to the edge of the clearing at a heading."""
        k = int(yaw % 360 * 2) % 720
        dx, dz = math.sin(math.radians(yaw)), math.cos(math.radians(yaw))
        cx, cz = self.centre
        b = dx * cx + dz * cz
        r = self.radius * self.edge[k]
        return max(0.5, b + math.sqrt(max(0.0, b * b - (cx * cx + cz * cz) + r * r)))

    def ceiling(self, yaw):
        return self.lift[int(yaw % 360 * 2) % 720]

    def length(self, yaw, dist, height, reach=None):
        """The length of mist between the eyes and a point `dist` blocks away
        (horizontally) and `height` blocks above the ground."""
        r = self.reach(yaw) if reach is None else reach
        if dist <= r - SOFT:
            return 0.0
        top = self.ceiling(yaw)
        slope = (height - self.eye) / dist
        hi = dist
        if slope > 0:
            hi = min(dist, max(r, (top - self.eye) / slope))
        return onset(hi - r) * math.sqrt(1 + slope * slope)

    def clear(self, yaw, dist, height, reach=None):
        """How much of a thing is still seen through the mist, 0 to 1."""
        return math.exp(-self.length(yaw, dist, height, reach) / self.depth)

    def sky(self, billow, stretch=0.9):
        """The mask of the sky seen through the mist above the horizon (white: clear),
        the billows thickening and thinning it."""
        reach = [self.reach(a) for a in YAWS]
        tops = [self.ceiling(a) - self.eye for a in YAWS]
        out = []
        inv = 1 / self.depth
        for y in range(HALF):
            p = math.radians(PITCHES[y])
            s, c = math.sin(p), math.cos(p)
            is_, ic = 1 / s, 1 / c
            vt = (self.veil_top - self.eye) * is_
            row = y * W
            line = []
            for x in range(W):
                exit_ = reach[x] * ic
                top = tops[x] * is_
                dense = top - exit_
                if dense < 0:
                    dense = 0.0
                thin = vt - (top if top > exit_ else exit_)
                ln = dense + (thin * self.veil if thin > 0 else 0.0)
                line.append(math.exp(-ln * inv * (1 - stretch / 2 + stretch * billow[row + x] / 255)))
            out.extend(line)
        return out


# --- the ground ----------------------------------------------------------------------

def grass_palettes(fresh, dry, flowers, light=1.0):
    """Two palettes for the same texel indices: the clearing's grass and the grey
    grass under the mist. Each base colour in eight brightnesses, then the flowers."""
    def build(bases, flw):
        pal = []
        for b in bases:
            for k in range(8):
                f = (0.86 + 0.04 * k) * light
                pal += rgb(v * f for v in b)
        for c in flw:
            pal += c
        pal += [72, 60, 46, 64, 52, 40, 128, 124, 70, 114, 112, 62]  # tilled earth, wheat
        return pal + [0] * (768 - len(pal))
    return build(fresh, flowers), build(dry, [rgb(mix(c, dry[0], 0.75)) for c in flowers])


def ground(eye, lights, reach, y1=H, flowers=0.05, terrain=None, field=None):
    """The ground below the horizon, seen from `eye` blocks up: an index image
    of its texels (one palette for fresh grass, one for dry), the distance of
    each row, and masks of the lights' warmth and of the land under the mist.
    `terrain(tan)` may replace the flat ground: given the tangent of the angle
    below the horizon, it returns the distance where the ray meets it, or None.
    `field` (x0, z0, x1, z1) is a field sown there: rows of tilled earth and wheat."""
    nb = len(GRASS)
    idx = bytearray(W * H)
    warm = [0.0] * (W * H)
    grey = [0.0] * (W * H)
    dists = [None] * H
    for y in range(HALF, y1):
        p = -math.radians(PITCHES[y])
        t = math.tan(p)
        dist = eye / t if terrain is None else terrain(t)
        dists[y] = dist
        if dist is None:
            continue
        detail = 1 - smooth(dist / 16)
        blocky = 1 - smooth(dist / 45)
        row = y * W
        for x in range(W):
            gx, gz = dist * SIN[x], dist * COS[x]
            bx, bz = math.floor(gx), math.floor(gz)
            hb = hashf(bx, bz, 1)
            tx, tz = int((gx - bx) * 16), int((gz - bz) * 16)  # the game's 16 texels a block
            ht = hashf(bx * 16 + tx, bz * 16 + tz, 2)
            if field and field[0] <= gx < field[2] and field[1] <= gz < field[3]:
                idx[row + x] = nb * 8 + 3 + (bx % 3 > 0) * 2 + (ht < 0.5)
            elif flowers and hb < flowers and dist > 3.5 and detail > 0.4 and tx == 7 and tz == 8:
                idx[row + x] = nb * 8 + (int(hb / flowers * 3) % 3)
            else:
                k = 3.5 + (ht - 0.5) * 7 * detail + (hb - 0.5) * 3 * blocky
                idx[row + x] = int(hb * nb) % nb * 8 + max(0, min(7, int(k)))
            w = 0.0
            for lx, lz, power, spread in lights:
                dx, dz = gx - lx, gz - lz
                w += power / (1 + (dx * dx + dz * dz) / spread)
            warm[row + x] = w
            r = reach[x]
            grey[row + x] = smooth((dist - r * 0.82) / (r * 0.3))
    return idx, dists, warm, grey


# --- cut-outs ---------------------------------------------------------------------

def leaf_block(rnd, palette, holes=0.08, light=0.0):
    """One block of leaves, TPB x TPB texels, speckled like the game's."""
    im = Image.new("RGBA", (TPB, TPB))
    px = im.load()
    n = len(palette)
    for y in range(TPB):
        for x in range(TPB):
            if rnd.random() < holes:
                continue
            k = int(rnd.random() * (n - 1.2) + light * 1.5)
            px[x, y] = palette[max(0, min(n - 1, k))] + (255,)
    return im


def shade(im, top=1.0, bottom=0.7, side=0.0):
    """Darkens a cut-out towards its foot and on the side away from the light
    (side > 0: lit from the right)."""
    px = im.load()
    w, h = im.size
    for y in range(h):
        fy = top + (bottom - top) * y / max(1, h - 1)
        for x in range(w):
            c = px[x, y]
            if c[3]:
                f = fy * (1 + side * (x / max(1, w - 1) - 0.5))
                px[x, y] = rgb(v * f for v in c[:3]) + (c[3],)
    return im


def tree(rnd, kind, light_side=0.0, palette=None, small=False):
    """A tree of the game, flat: oak, birch or spruce (`small`: never a big one).
    Returns the cut-out and the column of its trunk."""
    if kind == "spruce":
        widths = [1, 1, 3, 1, 3, 5, 3, 5, 7, 5, 7] if rnd.random() < 0.5 else [1, 3, 1, 3, 5, 3, 5, 7]
        trunk = 2
        palette = palette or SPRUCE
    else:
        big = rnd.random() < 0.4 and not small
        widths = [3, 5, 7, 7, 7, 5] if big else rnd.choice([[1, 3, 5, 5], [3, 3, 5, 5], [3, 5, 5, 3]])
        trunk = rnd.randint(3, 4) + big
        palette = palette or (OAK_LEAVES if kind == "oak" else BIRCH_LEAVES)
    wmax = max(widths)
    im = Image.new("RGBA", (wmax * TPB, (len(widths) + trunk) * TPB))
    mid = wmax // 2
    for j in range(len(widths) - (2 if kind == "spruce" else 1), len(widths) + trunk):
        blk = Image.new("RGBA", (TPB, TPB))
        bp = blk.load()
        for y in range(TPB):
            for x in range(TPB):
                if kind == "birch":
                    c = BIRCH if hashf(x // 3, j * TPB + y, 7) > 0.16 else (52, 50, 44)
                else:
                    c = mix(BARK, (86, 72, 56), (x % 3 == 0) * 0.5 + 0.3 * hashf(x, y, j))
                c = mix(c, (0, 0, 0), max(0.0, 0.22 * (x >= TPB - 2) - 0.1 * (x < 2) * light_side))
                bp[x, y] = rgb(c) + (255,)
        im.paste(blk, (mid * TPB, j * TPB))
    for j, w in enumerate(widths):
        for i in range(w):
            col = mid - w // 2 + i
            corner = w > 1 and i in (0, w - 1)
            if corner and rnd.random() < 0.35:
                continue
            lit = (col - mid) / max(1, wmax) * light_side * 1.2 + 0.5 - j / len(widths) * 0.15
            blk = leaf_block(rnd, palette, 0.22 if corner else 0.015, lit)
            im.alpha_composite(blk, (col * TPB, j * TPB))
    shade(im, 1.05, 0.7, light_side * 0.3)
    return im, mid * TPB + TPB // 2


def from_art(rows, colours):
    """A cut-out from rows of characters, one a texel."""
    im = Image.new("RGBA", (max(len(r) for r in rows), len(rows)))
    px = im.load()
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colours:
                px[x, y] = colours[ch] + (255,)
    return im


DEER = [
    "...................a...a",
    "....................a.a.",
    ".................a..aa..",
    "..................a.a...",
    "...................aa...",
    "..................hhh...",
    ".................hhhhh..",
    ".................hhhhhhh",
    "................nhhh....",
    "...............nnn......",
    "..............nnnn......",
    ".tt..........nnnn.......",
    "..bbbbbbbbbbbbbbb.......",
    ".bbbbbbbbbbbbbbbb.......",
    ".bbbbbbbbbbbbbbb........",
    "..bbbbbbbbbbbbb.........",
    "..bb.b.......bb.........",
    "..b..b.......b.b........",
    "..b..b.......b.b........",
    "..b...b......b..b.......",
    "..b...b......b..b.......",
    "..b...b......b..b.......",
]


TUFT = [
    "..a....a..",
    ".a..a.a...",
    ".a.a..a.a.",
    "..aa.ab.a.",
    "a.ab.ab.a.",
    ".abb.bba..",
    "..bbbbb...",
]


def tuft(rnd, palette=((96, 122, 66), (70, 96, 52)), flower=None):
    """A tuft of short grass, 8 texels a block; with `flower`, a bloom on it."""
    rows = TUFT if rnd.random() < 0.5 else [r[::-1] for r in TUFT]
    im = from_art(rows, {"a": palette[0], "b": palette[1]})
    if flower:
        im = im.crop((0, -3, im.width, im.height))
        x = rnd.randint(3, 6)
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            im.putpixel((x + dx, dy), flower + (255,))
        for y in range(2, 5):
            im.putpixel((x, y), palette[1] + (255,))
    return im


def deer():
    c = (78, 66, 54)
    return from_art(DEER, {"a": (96, 86, 72), "h": c, "n": c, "b": c, "t": (180, 172, 156)})


def campfire():
    """The game's campfire, 16 texels a block: crossed logs, embers, a flame."""
    rows = [
        "......f.........",
        ".....ff....f....",
        ".....fff..ff....",
        "....ffYf..ff....",
        "....fYYff.fff...",
        "...ffYYYffYff...",
        "...fYYHYYfYYf...",
        "..ffYHHHYYYYff..",
        "..fYYHHHHYHYYf..",
        "..fYHHHHHHHHYf..",
        "LLLLLLLLLLLLLLLL",
        "lLeLLlLLLeLLlLLl",
        "dddddddddddddddd",
        ".LLLlLLLLLLlLLL.",
        ".lLLLLeLLLLLLel.",
        ".dddddddddddddd.",
    ]
    return from_art(rows, {"f": (240, 140, 50), "Y": (255, 196, 92), "H": (255, 238, 180),
                           "L": (92, 72, 50), "l": (70, 54, 38), "e": (255, 150, 60), "d": (46, 36, 28)})


def lantern_post():
    """A fence post with an arm and the game's lantern hanging from it, 16 texels a block."""
    rows = []
    post = "......ppPP......"
    for j in range(56):
        if j < 2:
            rows.append("......ppPP......")
        elif j == 2:
            rows.append("pppPPPppPPPPpppp")
        elif j == 3:
            rows.append("pp...........ppp")
        else:
            rows.append(post)
    lamp = [
        "..cc............",
        "..cc............",
        ".OOOOOO.........",
        ".OMMMMO.........",
        "OOOOOOOO........",
        ".OYYYYO.........",
        ".OYHHYO.........",
        ".OYHHYO.........",
        ".OYYYYO.........",
        "OOOOOOOO........",
    ]
    for j, line in enumerate(lamp):
        row = list(rows[4 + j])
        for i, ch in enumerate(line):
            if ch != ".":
                row[i] = ch
        rows[4 + j] = "".join(row)
    return from_art(rows, {"p": (70, 56, 42), "P": (54, 42, 32), "c": (60, 62, 58), "O": (40, 42, 40),
                           "M": (74, 82, 72), "Y": (255, 200, 110), "H": (255, 244, 210)})


def standing_stone(rnd, width, height, glyph=False, tpb=TPB):
    """A standing stone, `width` x `height` blocks at most: a rough slab tapering
    to a slanted top, cracked, lit from the left, moss at its foot and on its
    crown; `glyph` carves a ring and a stroke on it, in faint lantern light."""
    w, h = max(3, round(width * tpb)), max(4, round(height * tpb))
    im = Image.new("RGBA", (w, h))
    px = im.load()
    slant = rnd.choice((-1, 1)) * rnd.uniform(0.12, 0.3) * w
    lo, hi = 0.0, 0.0
    cracks = {rnd.randrange(w): (rnd.randrange(h // 3), rnd.randrange(h // 2, h)) for _ in range(max(1, w // 6))}
    for y in range(h):
        f = y / h
        half = w / 2 * (0.45 + 0.55 * smooth(f * 1.4) ** 0.7)
        if y % 3 == 0:
            lo = max(-1.5, min(1.5, lo + rnd.choice((-1, 0, 1)) * 0.5))
            hi = max(-1.5, min(1.5, hi + rnd.choice((-1, 0, 1)) * 0.5))
        x0, x1 = w / 2 - half + lo, w / 2 + half + hi
        for x in range(max(0, int(x0)), min(w, int(x1) + 1)):
            u = min(1.0, max(0.0, (x - x0) / max(1, x1 - x0)))
            top = abs(slant) + slant * (u - 0.5) * 2 + (u - 0.5) ** 2 * w * 0.8
            if y < top:
                continue
            c = mix(STONE[1], STONE[0], hashf(x // 3, y // 5, 3) * 0.6)
            c = mix(c, STONE[2], 0.3 * hashf(x, y, 5))
            c = mix(c, (0, 0, 0), 0.3 * u ** 1.5)  # lit from the left
            if x in cracks and cracks[x][0] < y < cracks[x][1]:
                c = mix(c, (0, 0, 0), 0.35)
            crown = y - top < max(2, h * 0.05) and hashf(x, y, 6) < 0.8
            foot = y > h * 0.86 and hashf(x, y, 8) < (y - h * 0.86) / (h * 0.14) * 1.2
            if crown or foot or hashf(x // 3, y // 3, 9) < 0.06:
                c = MOSS[int(hashf(x, y, 4) * 2)]
            elif hashf(x, y, 11) < 0.025:
                c = (164, 172, 148)  # lichen
            px[x, y] = rgb(c) + (255,)
    if glyph:
        ring = Image.new("RGBA", (w, h))
        d = ImageDraw.Draw(ring)
        cx, cy, r = w / 2, h * 0.36, w * 0.2
        line = max(1, round(tpb / 3))
        d.ellipse((cx - r, cy - r, cx + r, cy + r), outline=(240, 184, 96, 230), width=line)
        d.line((cx, cy - r * 1.7, cx, cy + r * 1.7), fill=(240, 184, 96, 230), width=line)
        ring = Image.composite(ring, Image.new("RGBA", (w, h)), im.split()[3])
        im.alpha_composite(ring)
    return im


def machine(levels=10):
    """The dissipateur, in the manner of Create: a tower of andesite casing
    `levels` blocks high bound in brass, warm windows, and great sails turning
    at its top. 8 texels a block."""
    t = TPB
    w, h = 11 * t, (7 + levels) * t
    im = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(im)
    x0, x1 = 3 * t, 8 * t
    for by in range(7, 7 + levels):
        for bx in range(3, 8):
            box = (bx * t, by * t, bx * t + t - 1, by * t + t - 1)
            c = (138, 136, 124) if (bx + by) % 2 else (128, 126, 116)
            d.rectangle(box, fill=c)
            d.rectangle(box, outline=(96, 92, 82))
            d.point((bx * t + 1, by * t + 1), fill=(170, 150, 96))
    for by in list(range(7, 6 + levels, 4)) + [6 + levels]:
        d.rectangle((x0 - 2, by * t, x1 + 1, by * t + 2), fill=(184, 140, 66))
    for by in range(9, 5 + levels, 4):
        d.rectangle((5 * t + 1, by * t + 1, 6 * t - 2, by * t + t - 2), fill=(250, 196, 104))
    d.rectangle((4 * t, 5 * t, 7 * t - 1, 7 * t - 1), fill=(110, 92, 70), outline=(70, 58, 44))
    # the sails: four arms, a diagonal cross, white canvas in wooden frames
    cx, cy = 5.5 * t, 4 * t
    for k in range(4):
        a = math.radians(30 + 90 * k)
        ux, uy = math.cos(a), math.sin(a)
        vx, vy = -uy, ux
        for s in range(int(1.0 * t), int(5.4 * t)):
            for q in range(-int(0.7 * t), int(0.7 * t) + 1):
                x, y = cx + ux * s + vx * q, cy + uy * s + vy * q
                frame = abs(q) >= 0.7 * t - 1 or s % t == 0
                c = (96, 76, 54) if frame else (226, 222, 204)
                if 0 <= x < w and 0 <= y < h:
                    im.putpixel((int(x), int(y)), c + (255,))
    d.ellipse((cx - 5, cy - 5, cx + 5, cy + 5), fill=(184, 140, 66), outline=(80, 64, 44))
    return im


def smoke(rnd, height=96, width=24, colour=(150, 150, 142), alpha=190):
    """A column of smoke, soft, drifting and widening as it rises."""
    im = Image.new("L", (width * 4, height))
    d = ImageDraw.Draw(im)
    x = width * 2.0
    for y in range(height, 0, -2):
        f = y / height
        x += rnd.uniform(-0.5, 0.6) + (1 - f) * 0.3
        r = 1.2 + (1 - f) ** 1.3 * width * 0.6
        d.ellipse((x - r, y - r * 0.7, x + r, y + r * 0.7), fill=round(alpha * f ** 0.8 * (0.7 + 0.3 * rnd.random())))
    out = Image.new("RGBA", im.size, colour + (0,))
    out.putalpha(im.filter(ImageFilter.GaussianBlur(2.5)))
    return out


def bank(rnd, colour, length=12, height=3):
    """A bank of mist lying on the ground, 4 texels a block: long, soft, its top
    rolling."""
    w, h = length * 4, height * 4
    im = Image.new("L", (w, h))
    d = ImageDraw.Draw(im)
    for _ in range(length):
        x = rnd.uniform(w * 0.1, w * 0.9)
        r = rnd.uniform(0.25, 0.5) * h
        top = h - r * rnd.uniform(0.8, 1.6)
        d.ellipse((x - r * 1.8, top, x + r * 1.8, h + r), fill=rnd.randrange(150, 230))
    edge = Image.linear_gradient("L").rotate(90, expand=True).resize((w, h))
    edge = ImageChops.multiply(edge, edge.transpose(Image.FLIP_LEFT_RIGHT)).point(lambda v: min(255, v * 4))
    foot = Image.linear_gradient("L").transpose(Image.FLIP_TOP_BOTTOM).resize((w, h)).point(lambda v: min(255, v * 3))
    a = ImageChops.multiply(ImageChops.multiply(im.filter(ImageFilter.GaussianBlur(h / 5)), edge), foot)
    out = Image.new("RGBA", (w, h), colour + (0,))
    out.putalpha(a)
    return out


class Painter:
    """The sphere being painted, and how to stand a cut-out in it."""

    def __init__(self, canvas, eye, mist, mist_image, lights=(), haze=None):
        self.canvas, self.eye, self.mist = canvas, eye, mist
        self.mist_px = mist_image.load()
        self.lights = lights
        self.haze = haze  # (colour, depth): aerial haze where there is no mist

    def mist_colour(self, yaw, pitch):
        return self.mist_px[int(yaw % 360 / 360 * W) % W, max(0, min(H - 1, int(row_y(pitch))))]

    def stand(self, im, yaw, dist, base=0.0, tpb=TPB, anchor=None, fog=1.0, lit=1.0):
        """Stands a cut-out `dist` blocks away at heading `yaw`, its foot `base`
        blocks above the eyes' ground, each texel row fogged by the mist between
        it and the eyes and warmed by the lights near it. `anchor` is the
        column that stands on the heading."""
        w, h = im.size
        anchor = w / 2 if anchor is None else anchor
        reach = self.mist.reach(yaw) if self.mist else None
        x0 = col_x(yaw) + math.degrees(math.atan2(-anchor / tpb, dist)) / 360 * W
        x1 = col_x(yaw) + math.degrees(math.atan2((w - anchor) / tpb, dist)) / 360 * W
        width = max(1, round(x1 - x0))
        wide = im.resize((width, h), Image.NEAREST)
        px_, pz_ = dist * math.sin(math.radians(yaw)), dist * math.cos(math.radians(yaw))
        for j in range(h):
            top = base + (h - j) / tpb
            bottom = base + (h - j - 1) / tpb
            y0 = round(row_y(math.degrees(math.atan2(top - self.eye, dist))))
            y1 = round(row_y(math.degrees(math.atan2(bottom - self.eye, dist))))
            if y1 <= y0:
                y1 = y0 + 1
            strip = wide.crop((0, j, width, j + 1)).resize((width, y1 - y0), Image.NEAREST)
            mid = (top + bottom) / 2
            r, g, b, a = strip.split()
            col = Image.merge("RGB", (r, g, b))
            warm = 0.0
            for lx, ly, lz, power, spread in self.lights:
                dd = (px_ - lx) ** 2 + (mid - ly) ** 2 + (pz_ - lz) ** 2
                warm += power / (1 + dd / spread)
            if warm > 0.01 and lit:
                col = ImageChops.screen(col, Image.new("RGB", col.size, rgb(tuple(v * min(0.8, warm) * lit for v in AMBER))))
            pitch = math.degrees(math.atan2(mid - self.eye, dist))
            if self.mist:
                t = self.mist.clear(yaw, dist, mid, reach) ** fog
                if t < 0.999:
                    col = Image.blend(Image.new("RGB", col.size, self.mist_colour(yaw, pitch)), col, t)
            if self.haze:
                hc, hd = self.haze
                t = math.exp(-dist / hd)
                col = Image.blend(Image.new("RGB", col.size, hc), col, t)
            strip = Image.merge("RGBA", col.split() + (a,))
            for off in (-W, 0, W):
                x = round(x0 + off)
                if -width <= x <= CW:
                    self.canvas.paste(strip, (x, y0), strip)


# --- the faces -----------------------------------------------------------------------

@functools.lru_cache(maxsize=None)
def _side_mesh(face, cells=32):
    """Pillow's mesh for a side face read from the canvas: each cell of the face
    and the quadrilateral of the canvas it comes from."""
    def point(u, v):
        x, y = u / FACE * 2 - 1, v / FACE * 2 - 1
        yaw = face * 90 + math.degrees(math.atan2(x, 1))
        pitch = math.degrees(math.atan2(-y, math.hypot(x, 1)))
        return PAD + yaw / 360 * W, row_y(pitch)
    step = FACE // cells
    mesh = []
    for j in range(cells):
        for i in range(cells):
            u0, v0, u1, v1 = i * step, j * step, (i + 1) * step, (j + 1) * step
            mesh.append(((u0, v0, u1, v1), point(u0, v0) + point(u0, v1) + point(u1, v1) + point(u1, v0)))
    return mesh


@functools.lru_cache(maxsize=None)
def _cap_map(face):
    """For the top and bottom faces: the canvas point each pixel comes from."""
    out = []
    for v in range(FACE):
        y = (v + 0.5) / FACE * 2 - 1
        for u in range(FACE):
            x = (u + 0.5) / FACE * 2 - 1
            yaw = math.degrees(math.atan2(x, -y if face == 4 else y)) % 360
            pitch = math.degrees(math.atan2(1, math.hypot(x, y))) * (1 if face == 4 else -1)
            out.append((PAD + yaw / 360 * W - 0.5, row_y(pitch) - 0.5))
    return out


def cut(canvas):
    """The six faces read from the painted sphere."""
    out = [canvas.transform((FACE, FACE), Image.MESH, _side_mesh(face), Image.BILINEAR) for face in range(4)]
    px = canvas.load()
    for face in (4, 5):
        data = []
        for fx, fy in _cap_map(face):
            x0, y0 = int(fx), min(max(int(fy), 0), H - 2)
            tx, ty = fx - x0, min(1.0, max(0.0, fy - y0))
            a, b, c, d = px[x0, y0], px[x0 + 1, y0], px[x0, y0 + 1], px[x0 + 1, y0 + 1]
            data.append(tuple(int((a[k] + (b[k] - a[k]) * tx) * (1 - ty) + (c[k] + (d[k] - c[k]) * tx) * ty + 0.5)
                              for k in range(3)))
        im = Image.new("RGB", (FACE, FACE))
        im.putdata(data)
        out.append(im)
    return [im.resize((SIZE, SIZE), Image.NEAREST) for im in out]


# --- shared scene parts -----------------------------------------------------------

def billows(rnd, plane_height=40.0):
    """The mist's billows: noise on the sphere near the horizon, and above, noise
    on a plane overhead seen in perspective, so that the zenith has no seam."""
    sphere = fbm(rnd, W, H, [(20, 10, 1), (56, 26, 0.55), (140, 64, 0.3)]).tobytes()
    tile = fbm(rnd, 512, 512, [(6, 6, 1), (16, 16, 0.55), (40, 40, 0.3)], both=True).tobytes()
    out = bytearray(sphere)
    for y in range(HALF):
        pitch = PITCHES[y]
        if pitch < 12:
            continue
        k = smooth((pitch - 12) / 16)
        d = plane_height / math.tan(math.radians(pitch)) * 2.2
        row = y * W
        for x in range(W):
            u = int(d * SIN[x]) % 512
            v = int(d * COS[x]) % 512
            out[row + x] = int(sphere[row + x] * (1 - k) + tile[v * 512 + u] * k)
    return bytes(out)


def sky_image(sun_yaw, sun_pitch, low=SKY_LOW, high=SKY_HIGH, sun=0.75):
    """The sky above the mist: a gradient from bone at the horizon to sage above,
    and a veiled sun."""
    im = by_rows(lambda p: mix(low, high, smooth(p / 75) ** 0.8), 0, HALF)
    pad_ = pad(im)
    glow(pad_, sun_yaw, sun_pitch, 22, SUNLIGHT, sun * 0.6)
    glow(pad_, sun_yaw, sun_pitch, 5, SUNLIGHT, sun)
    return resync(pad_).crop((PAD, 0, PAD + W, H))


def mist_image(billow, sun_yaw, sun_pitch, warmth=(), base=MIST, deep=MIST_DEEP, contrast=0.16, sun=SUNLIGHT,
               sun_power=0.55):
    """The colour of the mist on the whole sphere: deep near the ground, bright
    higher, lit towards the sun and near the fires, its billows light and dark."""
    im = by_rows(lambda p: mix(deep, base, 0.25 + 0.75 * smooth((p + 24) / 64)))
    cols = by_cols(lambda a: math.exp(-(wrapdiff(a, sun_yaw) / 55) ** 2))
    rows = by_rows(lambda p: (255 * math.exp(-((p - sun_pitch) / 34) ** 2),) * 3).split()[0]
    im = screen_add(im, rgb(mix((0, 0, 0), sun, sun_power)), ImageChops.multiply(cols, rows))
    for yaw, power, spread in warmth:
        cols = by_cols(lambda a: math.exp(-(wrapdiff(a, yaw) / spread) ** 2) * power)
        rows = by_rows(lambda p: (255 * math.exp(-abs(p) / 9),) * 3).split()[0]
        im = screen_add(im, AMBER, ImageChops.multiply(cols, rows))
    b = Image.frombytes("L", (W, H), billow)
    light = b.point(lambda v: round(255 * max(0, v / 255 - 0.5) * 2 * contrast))
    dark = b.point(lambda v: round(255 * max(0, 0.5 - v / 255) * 2 * contrast))
    im = screen_add(im, (255, 252, 236), light)
    return tinted(im, (110, 120, 108), dark)


def ground_image(idx, fresh, dry, warm, grey):
    """The ground's colours from its texel indices: fresh grass, grey grass under
    the mist, warmed by the lights."""
    p = Image.frombytes("P", (W, H), bytes(idx))
    p.putpalette(fresh)
    g = p.convert("RGB")
    p.putpalette(dry)
    d = p.convert("RGB")
    out = Image.composite(d, g, mask(grey))
    w = Image.merge("RGB", (mask([min(1.0, v) for v in warm]),) * 3)
    out = ImageChops.add(out, ImageChops.multiply(ImageChops.multiply(out, Image.new("RGB", out.size, (255, 150, 70))), w))
    return ImageChops.screen(out, ImageChops.multiply(Image.new("RGB", out.size, (96, 40, 6)), w))


def ground_clear(dists, reach, billow, depth):
    """How much of the ground is seen through the mist, below the horizon."""
    out = [1.0] * (W * (H - HALF))
    inv = 1 / depth
    for y in range(HALF, H):
        dist = dists[y]
        if dist is None:
            continue
        row = y * W
        o = (y - HALF) * W
        for x in range(W):
            ln = dist - reach[x]
            if ln > -SOFT:
                out[o + x] = math.exp(-onset(ln) * inv * (0.55 + 0.9 * billow[row + x] / 255))
    return out


def compose(sky, ground_im, mist_im, see):
    """The sky over the horizon and the ground under it, seen through the mist."""
    world = Image.new("RGB", (W, H))
    world.paste(sky.crop((0, 0, W, HALF)), (0, 0))
    world.paste(ground_im.crop((0, HALF, W, H)), (0, HALF))
    return pad(Image.composite(world, mist_im, mask(see)))


# --- variant 1: the vigil ---------------------------------------------------------

def vigil():
    """A small clearing in the mist around a vigil fire, at dusk: the mist cool
    and still luminous, an afterglow low on one side, the sky deepening in the
    opening overhead; the fire warms the grass, the trunks and the mist near
    it, sparks rise, fireflies wake at the edge."""
    rnd = random.Random(11)
    eye = 1.9
    fire_yaw, fire_d = 318.0, 6.0
    fx, fz = fire_d * math.sin(math.radians(fire_yaw)), fire_d * math.cos(math.radians(fire_yaw))
    mist = Mist(rnd, eye, 16.0, (fx * 0.8, fz * 0.8), top=10.0, depth=5.0, veil=0.12, veil_top=40.0, wobble=0.14)
    sun_yaw, sun_pitch = 150.0, 2.0  # just set: its afterglow low in the mist
    billow = billows(rnd)
    reach = [mist.reach(a) for a in YAWS]

    sky = sky_image(sun_yaw, sun_pitch, low=(176, 178, 164), high=(104, 124, 128), sun=0.3)
    mcol = mist_image(billow, sun_yaw, sun_pitch, warmth=[(fire_yaw, 0.85, 30)],
                      base=(160, 172, 168), deep=(116, 130, 128), contrast=0.2, sun=(236, 190, 140), sun_power=0.5)
    idx, dists, warm, grey = ground(eye, [(fx, fz, 1.5, 7.0)], reach, flowers=0.03)
    cool = [rgb(mix(c, (60, 80, 74), 0.25)) for c in GRASS]
    gim = ground_image(idx, *grass_palettes(cool, DRY, FLOWERS, 0.7), warm, grey)
    canvas = compose(sky, gim, mcol, mist.sky(billow) + ground_clear(dists, reach, billow, mist.depth))

    paint = Painter(canvas, eye, mist, mcol, [(fx, 0.6, fz, 1.6, 14.0)])
    things = []
    # ghosts of trees beyond the edge, sparse, and a deer at it
    for _ in range(44):
        yaw = rnd.uniform(0, 360)
        things.append((mist.reach(yaw) + rnd.uniform(1.5, 22), yaw, "tree",
                       rnd.choice(["oak", "spruce", "spruce", "birch", "oak"])))
    things.append((mist.reach(122) - 0.3, 122, "deer", None))
    # the clearing's own trees, at its edge, away from the fire and the logo's heading
    for yaw in (64, 104, 200, 248, 286):
        things.append((mist.reach(yaw) * rnd.uniform(0.9, 1.04), yaw, "small", rnd.choice(["oak", "birch", "oak"])))
    # short grass and a few flowers in the clearing
    for _ in range(110):
        yaw = rnd.uniform(0, 360)
        things.append((rnd.uniform(3, mist.reach(yaw) * 0.85), yaw, "tuft", None))
    things.append((fire_d, fire_yaw, "fire", None))
    # banks of mist rolling at the edge, some creeping in
    for _ in range(26):
        yaw = rnd.uniform(0, 360)
        things.append((mist.reach(yaw) * rnd.uniform(0.85, 1.15), yaw, "bank", None))
    dark = [(36, 50, 42), (42, 58, 46), (48, 64, 50), (54, 70, 52)]
    for d, yaw, what, kind in sorted(things, reverse=True):
        if what in ("tree", "small"):
            side = 1.0 if wrapdiff(fire_yaw, yaw) > 0 else -1.0
            im, anchor = tree(rnd, kind, side, dark if kind != "spruce" else None, small=what == "small")
            paint.stand(shade(im, 0.85, 0.85), yaw, d, anchor=anchor)
        elif what == "tuft":
            im = tuft(rnd, ((54, 76, 50), (44, 62, 44)), rnd.choice(FLOWERS) if rnd.random() < 0.15 else None)
            paint.stand(im, yaw, d, tpb=20, lit=0.5)
        elif what == "deer":
            paint.stand(deer(), yaw, d, tpb=13)
        elif what == "bank":
            c = rgb(mix(paint.mist_colour(yaw, 2), (240, 244, 236), 0.1))
            paint.stand(bank(rnd, c, rnd.randint(8, 22), rnd.randint(2, 4)), yaw, d, tpb=4, lit=0.3)
        elif what == "fire":
            paint.stand(smoke(rnd, 150, 20, colour=(132, 138, 134)), yaw + 0.8, d, base=0.5, tpb=12, lit=0.7)
            paint.stand(campfire(), yaw, d, tpb=16, lit=0.0)
    resync(canvas)
    fp = math.degrees(math.atan2(0.4 - eye, fire_d))
    glow(canvas, fire_yaw, fp + 2, 14, AMBER, 0.32)
    glow(canvas, fire_yaw, fp + 1, 4, AMBER, 0.6)
    glow(canvas, fire_yaw, fp + 1, 1.5, FLAME, 0.75)
    for _ in range(14):  # sparks
        glow(canvas, fire_yaw + rnd.uniform(-2.5, 3.5), fp + rnd.uniform(3, 20), 0.22, FLAME, 0.95)
    for _ in range(26):  # fireflies, low over the grass
        yaw = rnd.uniform(0, 360)
        d = mist.reach(yaw) * rnd.uniform(0.5, 0.95)
        p = math.degrees(math.atan2(rnd.uniform(0.3, 2.2) - eye, d))
        glow(canvas, yaw, p, 0.5, (210, 230, 140), 0.35)
        glow(canvas, yaw, p, 0.12, (250, 255, 200), 1.0)
    return canvas


# --- variant 2: the threshold -------------------------------------------------

def threshold():
    """At the edge of a lantern's clearing, by day: behind, the clearing in autumn
    and the lantern on its post; ahead, the mist a few blocks away, a ring of
    standing stones fading into it, and a monolith beyond, risen above the
    mist, a ring carved on it glowing faintly."""
    rnd = random.Random(23)
    eye = 1.7
    # the lantern stands at the clearing's centre, 22 blocks behind: the mist
    # begins 8 blocks ahead
    lan_yaw, lan_d = 168.0, 22.0
    lx, lz = lan_d * math.sin(math.radians(lan_yaw)), lan_d * math.cos(math.radians(lan_yaw))
    mist = Mist(rnd, eye, 30.0, (lx, lz), top=14.0, depth=7.0, veil=0.14, veil_top=50.0, wobble=0.1)
    sun_yaw, sun_pitch = 205.0, 26.0
    billow = billows(rnd)
    reach = [mist.reach(a) for a in YAWS]

    sky = sky_image(sun_yaw, sun_pitch, low=(214, 210, 190), high=(160, 178, 172), sun=0.8)
    mcol = mist_image(billow, sun_yaw, sun_pitch, base=(196, 198, 184), deep=(150, 160, 150), contrast=0.2)
    # a small field sown by the lantern: the land the light gave back
    idx, dists, warm, grey = ground(eye, [(lx, lz, 0.6, 10.0)], reach, flowers=0.02,
                                    field=(lx - 13, lz + 3, lx - 3, lz + 11))
    fresh = [(80, 98, 54), (88, 104, 56), (74, 92, 50), (94, 108, 60), (84, 100, 52)]
    gim = ground_image(idx, *grass_palettes(fresh, DRY, [(214, 160, 70), (230, 224, 200), (190, 120, 60)], 0.8),
                       warm, grey)
    canvas = compose(sky, gim, mcol, mist.sky(billow) + ground_clear(dists, reach, billow, mist.depth))
    paint = Painter(canvas, eye, mist, mcol, [(lx, 2.6, lz, 0.6, 6.0)])

    things = []
    # the stone circle, ahead in the mist, and the monolith beyond it
    cx, cz, cr = 1.0, 17.0, 8.5
    for k in range(9):
        a = math.radians(k * 40 + rnd.uniform(-8, 8))
        sx, sz = cx + cr * math.sin(a), cz + cr * math.cos(a)
        things.append((math.hypot(sx, sz), math.degrees(math.atan2(sx, sz)) % 360, "stone", rnd.randint(5, 8)))
    things.append((46.0, 28.0, "monolith", None))
    # the forest: ghosts in the mist ahead, autumn trees along the clearing's far
    # edge behind, and a grove by the lantern
    for _ in range(40):
        yaw = rnd.uniform(0, 360)
        d = mist.reach(yaw) + rnd.uniform(2, 26)
        if abs(wrapdiff(yaw, 8)) < 30 and d < 40:
            continue
        things.append((d, yaw, "tree", rnd.choice(["oak", "birch", "spruce", "spruce"])))
    for _ in range(34):
        yaw = 180 + rnd.uniform(-115, 115)
        r = mist.reach(yaw)
        if r < 22:
            continue
        things.append((r * rnd.uniform(0.82, 1.02), yaw, "tree", rnd.choice(["oak", "birch", "birch", "spruce"])))
    for k in range(5):  # a grove of birches by the lantern
        things.append((lan_d + rnd.uniform(2, 9), lan_yaw + rnd.uniform(-26, 26), "tree", "birch"))
    for _ in range(20):
        yaw = rnd.uniform(0, 360)
        things.append((mist.reach(yaw) * rnd.uniform(0.85, 1.1), yaw, "bank", None))
    for _ in range(90):
        yaw = rnd.uniform(0, 360)
        things.append((rnd.uniform(3, mist.reach(yaw) * 0.9), yaw, "tuft", None))
    things.append((mist.reach(312) + 1.5, 312, "deer", None))
    things.append((lan_d, lan_yaw, "lantern", None))
    for d, yaw, what, kind in sorted(things, reverse=True):
        side = 1.0 if wrapdiff(sun_yaw, yaw) > 0 else -1.0
        if what == "tree":
            pal = None
            if kind != "spruce":
                pal = rnd.choice([AUTUMN, GOLD, OAK_LEAVES, BIRCH_LEAVES if kind == "birch" else OAK_LEAVES])
            im, anchor = tree(rnd, kind, side, pal, small=d < 16)
            paint.stand(im, yaw, d, anchor=anchor)
        elif what == "stone":
            paint.stand(standing_stone(rnd, rnd.uniform(1.5, 2.6), kind), yaw, d)
        elif what == "monolith":
            paint.stand(shade(standing_stone(rnd, 6, 36, glyph=True, tpb=4), 0.5, 0.5), yaw, d, tpb=4, fog=0.22)
        elif what == "tuft":
            im = tuft(rnd, ((92, 108, 60), (70, 88, 50)), rnd.choice(AUTUMN) if rnd.random() < 0.2 else None)
            paint.stand(im, yaw, d, tpb=20)
        elif what == "bank":
            c = rgb(mix(paint.mist_colour(yaw, 2), (255, 255, 248), 0.12))
            paint.stand(bank(rnd, c, rnd.randint(8, 22), rnd.randint(2, 4)), yaw, d, tpb=4, lit=0.3)
        elif what == "deer":
            paint.stand(deer(), yaw, d, tpb=13)
        elif what == "lantern":
            paint.stand(lantern_post(), yaw, d, tpb=16, lit=0.0, anchor=8)
    resync(canvas)
    lp = math.degrees(math.atan2(3.1 - eye, lan_d))
    lyaw = lan_yaw - math.degrees(math.atan2(4 / 16, lan_d))
    glow(canvas, lyaw, lp, 6, AMBER, 0.45)
    glow(canvas, lyaw, lp, 0.8, FLAME, 0.9)
    return canvas


# --- variant 3: lifted -------------------------------------------------------------

def lifted():
    """High above the clearing a dissipateur keeps, in the evening light: below,
    the green floor of the clearing with its trees and the machine; around it
    the mist standing like the wall of a crater, and from its rim a sea of mist
    to the horizon, holed here and there by other clearings with a light in
    each; far ranges rising out of it, a monolith on a ridge."""
    rnd = random.Random(5)
    eye = 34.0      # above the clearing's floor
    sea = 21.0      # the mist's top above that floor
    lean = 0.45     # the crater's wall widens upwards, blocks out per block up
    sun_yaw, sun_pitch = 200.0, 7.0
    haze = (222, 210, 182)
    mach_yaw, mach_d = 52.0, 30.0
    mx, mz = mach_d * math.sin(math.radians(mach_yaw)), mach_d * math.cos(math.radians(mach_yaw))
    mist = Mist(rnd, eye, 64.0, (mx, mz), top=sea, depth=6.0, wobble=0.07, swell=0.08)
    billow = billows(rnd)

    sky = sky_image(sun_yaw, sun_pitch, low=(232, 214, 176), high=(130, 150, 146), sun=1.0)
    pad_ = pad(sky)
    glow(pad_, sun_yaw, sun_pitch, 40, (250, 210, 150), 0.35, squash=0.5)
    sky = resync(pad_).crop((PAD, 0, PAD + W, H))

    # the trees of the clearing, seen from above: their crowns of leaf blocks
    crowns = {}
    trees = []
    for _ in range(46):
        a = rnd.uniform(0, 2 * math.pi)
        r = 64 * math.sqrt(rnd.uniform(0.02, 0.85))
        tx, tz = mx + r * math.sin(a), mz + r * math.cos(a)
        if math.hypot(tx - mx, tz - mz) < 7:
            continue
        top = rnd.randint(5, 8)
        pal = rnd.choice((OAK_LEAVES, OAK_LEAVES, BIRCH_LEAVES, SPRUCE))
        trees.append((tx, tz, top))
        bx, bz = math.floor(tx), math.floor(tz)
        for i in range(-2, 3):
            for j in range(-2, 3):
                if abs(i) == 2 and abs(j) == 2 and rnd.random() < 0.7:
                    continue
                crowns[(bx + i, bz + j)] = (top, pal)
    shade_dx, shade_dz = -math.sin(math.radians(sun_yaw)) * 0.9, -math.cos(math.radians(sun_yaw)) * 0.9

    # the ranges, layer behind layer, each paler with distance and paler still
    # at its foot where the mist clings; the nearest barely break the mist
    tile = fbm(rnd, 512, 512, [(6, 6, 1), (16, 16, 0.6), (44, 44, 0.35)], both=True).tobytes()
    ranges = []
    for dist, peak, lo, cells, colour in ((4200, 19.0, 3.0, 6, (150, 160, 156)), (2600, 12.0, 0.5, 8, (122, 136, 130)),
                                          (1500, 7.5, -0.5, 11, (98, 114, 104)), (800, 3.0, -2.0, 14, (74, 92, 74))):
        n = fbm(rnd, W, 1, [(cells, 1, 1), (cells * 3, 1, 0.5), (cells * 9, 1, 0.2),
                            (cells * 27, 1, 0.08)]).tobytes()
        step = math.degrees(math.atan2(8, dist))  # the land in steps of 8 blocks
        prof = [math.floor((lo + (peak - lo) * (v / 255) ** 1.7) / step) * step for v in n]
        sm = list(Image.frombytes("L", (W, 1), bytes(n)).resize((W // 16, 1), Image.BOX)
                  .resize((W, 1), Image.BICUBIC).tobytes())
        slope = [(sm[(x + 24) % W] - sm[x - 24]) / 255 * (peak - lo) for x in range(W)]
        base = -math.degrees(math.atan2(eye - sea, dist))
        ranges.append((dist, prof, slope, base, colour))
    ranges.sort()
    # other clearings: holes in the sea of mist, a light in each
    holes = []
    for _ in range(14):
        a = rnd.uniform(0, 360)
        if abs(wrapdiff(a, sun_yaw)) < 15 or any(abs(wrapdiff(a, h[3])) < 14 for h in holes):
            continue
        d = rnd.uniform(130, 300)
        holes.append((d * math.sin(math.radians(a)), d * math.cos(math.radians(a)), rnd.uniform(18, 30), a, d))
        if len(holes) == 6:
            break

    reach = [mist.reach(a) for a in YAWS]
    hr, hg, hb = haze
    out = bytearray(W * H * 3)
    keep = bytearray(W * H)
    top_row = int(row_y(20))
    for y in range(top_row, H):
        p = PITCHES[y]
        t = math.tan(math.radians(-p)) if p < 0 else 0.0
        row = y * W
        for x in range(W):
            colour = None
            facing = wrapdiff(sun_yaw, YAWS[x])
            side = 1 if facing > 0 else -1
            for dist, prof, slope, base, (r_, g_, b_) in ranges:
                if base <= p <= prof[x]:
                    below = prof[x] - p
                    lit = max(-0.5, min(0.5, -slope[x] * side * 0.25))
                    gully = tile[(int(below * 9) % 512) * 512 + int(x * 0.7 + below * 30) % 512] / 255 - 0.5
                    k = 1 + 0.22 * lit + 0.06 * gully
                    r_, g_, b_ = r_ * k, g_ * k, b_ * k
                    rim = math.exp(-below / 0.2) * max(0.0, lit + 0.2) * math.exp(-(facing / 90) ** 2)
                    m = math.exp(-(p - base) / (0.35 + dist / 3000))
                    f = max(1 - math.exp(-dist / 6000), min(1.0, m))
                    r_, g_, b_ = r_ + (hr - r_) * f, g_ + (hg - g_) * f, b_ + (hb - b_) * f
                    colour = (r_ + (255 - r_) * rim, g_ + (224 - g_) * rim, b_ + (176 - b_) * rim)
                    break
            if colour is None:
                if p >= -0.03:
                    continue
                n = billow[row + x] / 255
                r0 = reach[x]
                s_w = (r0 + lean * eye) / (1 + lean * t)  # where the ray meets the crater's wall
                h_w = eye - t * s_w
                d_f = eye / t
                if d_f < s_w:  # the clearing's floor, or a crown on it
                    colour = None
                    for top in range(10, 4, -1):
                        s_c = (eye - top) / t
                        cx_, cz_ = s_c * SIN[x], s_c * COS[x]
                        hit = crowns.get((math.floor(cx_), math.floor(cz_)))
                        if hit and hit[0] == top:
                            pal = hit[1]
                            tx_ = int((cx_ % 1) * 8)
                            tz_ = int((cz_ % 1) * 8)
                            colour = pal[int(hashf(math.floor(cx_ * 8), math.floor(cz_ * 8), 4) * len(pal))]
                            colour = tuple(c * (1.08 if (tx_ == 0 or tz_ == 0) else 1.0) for c in colour)
                            break
                    gx, gz = d_f * SIN[x], d_f * COS[x]
                    if colour is None:
                        bx_, bz_ = math.floor(gx), math.floor(gz)
                        hb_ = hashf(bx_, bz_, 1)
                        ht = hashf(math.floor(gx * 8), math.floor(gz * 8), 2)
                        c = GRASS[int(hb_ * 5)]
                        k = 0.92 + 0.16 * ht
                        if crowns.get((math.floor(gx + shade_dx * 3), math.floor(gz + shade_dz * 3))):
                            k *= 0.6  # the shade of a crown
                        colour = (c[0] * k, c[1] * k, c[2] * k)
                    # the evening light warms the floor, and the mist skirts the wall
                    colour = (colour[0] * 1.08 + 10, colour[1] * 1.04 + 4, colour[2] * 0.96)
                    f = math.exp(-max(0.0, r0 - d_f) / 7) * 0.85
                    hc = (196, 200, 186)
                    colour = tuple(c + (m - c) * f for c, m in zip(colour, hc))
                elif h_w <= mist.ceiling(YAWS[x]):  # the wall: billows, lit across from the sun, deep at its foot
                    light = 0.5 - 0.5 * math.cos(math.radians(facing))
                    up = h_w / mist.ceiling(YAWS[x])
                    fold = tile[(int(h_w * 5) % 512) * 512 + x % 512] / 255 - 0.5
                    v = 150 + 70 * up ** 1.5 + 26 * light + fold * 60 + (n - 0.5) * 20
                    colour = (v + 10 * light, v + 3, v - 8 - 10 * light)
                    rim = smooth(1 - (mist.ceiling(YAWS[x]) - h_w) / 2.5)  # its sunlit brim
                    colour = tuple(c + (w - c) * rim * 0.8 for c, w in zip(colour, (244, 238, 222)))
                else:  # the sea of mist, from above
                    d_s = (eye - sea) / t
                    sx_, sz_ = d_s * SIN[x], d_s * COS[x]
                    u = int(sx_ / 0.8) % 512
                    v = int(sz_ / 0.8) % 512
                    amp = math.exp(-d_s / 1200)
                    nn = max(0.0, min(1.0, 0.5 + (tile[v * 512 + u] / 255 - 0.5) * 2.0 * amp))
                    toward = math.exp(-(facing / 60) ** 2)
                    lit = nn ** 1.2
                    r_, g_, b_ = 180 + 66 * lit + 12 * toward, 182 + 56 * lit + 6 * toward, 172 + 42 * lit
                    for hx, hz, hr_, _, hd in holes:
                        dx_, dz_ = sx_ - hx, sz_ - hz
                        q = math.hypot(dx_, dz_) / hr_
                        if q < 1.15:
                            far_side = (dx_ * hx + dz_ * hz) > 0
                            if q < 0.72 or (q < 1 and not far_side):  # down into another clearing
                                g = 0.85 * smooth((1 - q) / 0.3)
                                r_, g_, b_ = r_ + (70 - r_) * g, g_ + (90 - g_) * g, b_ + (60 - b_) * g
                            elif q < 1:  # the far inner wall of its hole, lit
                                r_, g_, b_ = 214, 210, 194
                            else:
                                r_, g_, b_ = r_ * 0.93, g_ * 0.94, b_ * 0.95  # its rim in shade
                            break
                    a = 1 - math.exp(-d_s / 1500)
                    colour = (r_ + (hr - r_) * a, g_ + (hg - g_) * a, b_ + (hb - b_) * a)
            i = (row + x) * 3
            out[i] = max(0, min(255, int(colour[0])))
            out[i + 1] = max(0, min(255, int(colour[1])))
            out[i + 2] = max(0, min(255, int(colour[2])))
            keep[row + x] = 255
    far = Image.frombytes("RGB", (W, H), bytes(out))
    world = Image.composite(far, sky, Image.frombytes("L", (W, H), bytes(keep)))
    canvas = pad(world)
    for hx, hz, hr_, a, d in holes:
        p = -math.degrees(math.atan2(eye - sea + 8, d))
        glow(canvas, a, p, 2.0, AMBER, 0.45)
        glow(canvas, a, p, 0.4, FLAME, 1.0)

    paint = Painter(canvas, eye, None, world, [], haze=(haze, 3000))
    # a monolith on a ridge of the middle range
    dist, prof, slope, base, _ = ranges[1]
    k = max(range(W), key=lambda x: prof[x] if abs(wrapdiff(YAWS[x], 330)) < 35 else -99)
    m_base = math.tan(math.radians(prof[k])) * dist + eye - 4
    paint.stand(shade(standing_stone(rnd, 3, 14, tpb=2), 0.5, 0.5), YAWS[k], dist, base=m_base, tpb=28 / 90, lit=0.0)
    # the dissipateur on the clearing's floor, its sails turning at the eyes' height
    side = 1.0 if wrapdiff(sun_yaw, mach_yaw) > 0 else -1.0
    paint.stand(shade(machine(30), 1.0, 0.8, side * 0.4), mach_yaw, mach_d)
    resync(canvas)
    return canvas


# Each scene, and the heading that faces 0 looks at: the one the game opens on.
SCENES = {"vigil": (vigil, 352.0), "threshold": (threshold, 74.0), "lifted": (lifted, 95.0)}


def turned(canvas, heading):
    """The sphere turned so that `heading` comes to the front."""
    if not heading:
        return canvas
    k = round(heading % 360 / 360 * W)
    body = canvas.crop((PAD, 0, PAD + W, H))
    out = Image.new("RGB", (W, H))
    out.paste(body.crop((k, 0, W, H)), (0, 0))
    out.paste(body.crop((0, 0, k, H)), (W - k, 0))
    return pad(out)


def faces(variant=DEFAULT):
    """The six faces of a variant, 0 to 3 the sides turning right, 4 the top and
    5 the bottom, each SIZE x SIZE pixels, RGB."""
    scene, heading = SCENES[variant]
    return cut(turned(scene(), heading))
