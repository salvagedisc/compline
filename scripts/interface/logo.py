"""The Fallow logo: the word in worn stone capitals (the Metamorphous typeface by
Sorkin Type, SIL Open Font License), bone fading to mist, with moss on their
tops and vines hanging from them, and a lit lantern hanging on a chain inside
the O, its light warming the stone around it.

Drawn at one pixel per GUI pixel, then scaled four times. Called by menus.py.
"""
import hashlib
import math
import random
import urllib.request
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter, ImageFont

FONT = ("https://raw.githubusercontent.com/google/fonts/main/ofl/metamorphous/Metamorphous-Regular.ttf",
        "1c55f044447667f8dd883b571dd01c07ebc33b13")
CACHE = Path(__file__).resolve().parents[2] / ".cache" / "interface"

OUTLINE = (26, 31, 28)
BONE = (240, 236, 214)
MIST = (203, 202, 182)
SHADE = (150, 156, 136)
DEEP = (112, 121, 106)
MOSS = [(52, 66, 50), (78, 98, 72), (106, 130, 90), (140, 160, 112)]
LANTERN_L = (248, 214, 140)
CHAIN = (90, 96, 86)
# the pack icon's lantern (build.py), hung inside the O
LANTERN = [
    "......OO......", ".....O..O.....", ".....O..O.....", "......OO......", "....OOOOOO....",
    "...OmMMMMdO...", "..OmMMMMMMdO..", ".OOOOOOOOOOOO.", "..OmDDDDDDdO..", "..OmgYYYYLdO..",
    "..OmgYFFYYdO..", "..OmYFHHFYdO..", "..OmYFHHFYdO..", "..OmYYFFYYdO..", "..OmLYYYYLdO..",
    "..OmDLLLLDdO..", ".OOOOOOOOOOOO.", "..OmMMMMMMdO..", "...OOOOOOOO...", "...OO....OO...",
]
LANTERN_COLOURS = {"O": OUTLINE, "M": (82, 92, 82), "m": (130, 140, 122), "d": (52, 60, 53),
                   "D": (168, 106, 44), "L": (226, 166, 78), "Y": LANTERN_L, "F": (255, 226, 160),
                   "H": (255, 250, 235), "g": (255, 246, 222)}

TEXT, SIZE, SPACING, SEED = "FALLOW", 54, 1, 4
PAD = (16, 16, 16, 22)
GLOW = 30          # radius of the lantern's light, in pixels
MOSS_TOP = 0.6     # share of the letters' top pixels under moss
VINES = 0.07       # share of the letters' bottom pixels a vine hangs from
DRIPS = 0.09       # share of their top pixels a vine drips from, over the stone
DANGLE = 8         # how far a vine hangs below the stone, in pixels
SCALE = 4


def font_file():
    url, sha1 = FONT
    path = CACHE / "Metamorphous-Regular.ttf"
    if not path.exists() or hashlib.sha1(path.read_bytes()).hexdigest() != sha1:
        path.parent.mkdir(parents=True, exist_ok=True)
        with urllib.request.urlopen(url, timeout=120) as response:
            data = response.read()
        if hashlib.sha1(data).hexdigest() != sha1:
            raise SystemExit("Metamorphous: checksum mismatch")
        path.write_bytes(data)
    return path


def mix(a, b, t):
    return tuple(round(x + (y - x) * t) for x, y in zip(a, b))


def letters():
    """The text as a set of pixels, without antialiasing, and each letter's box."""
    font = ImageFont.truetype(str(font_file()), SIZE)
    mask = Image.new("L", (SIZE * len(TEXT) * 2, SIZE * 2), 0)
    d = ImageDraw.Draw(mask)
    d.fontmode = "1"
    x, boxes = 8, []
    for ch in TEXT:
        d.text((x, SIZE // 2), ch, font=font, fill=255)
        boxes.append(d.textbbox((x, SIZE // 2), ch, font=font))
        x += font.getlength(ch) + SPACING
    x0, y0, _, _ = mask.getbbox()
    mask = mask.crop(mask.getbbox())
    mp = mask.load()
    pixels = {(x + PAD[0], y + PAD[1]) for y in range(mask.height) for x in range(mask.width) if mp[x, y]}
    boxes = [(b[0] - x0 + PAD[0], b[1] - y0 + PAD[1], b[2] - x0 + PAD[0], b[3] - y0 + PAD[1]) for b in boxes]
    return pixels, boxes, (mask.width + PAD[0] + PAD[2], mask.height + PAD[1] + PAD[3])


def draw():
    rnd = random.Random(SEED)
    stone, boxes, (w, h) = letters()
    im = Image.new("RGBA", (w, h))
    px = im.load()
    top, bottom = min(y for _, y in stone), max(y for _, y in stone)

    # a moss shadow cast down and right
    for x, y in stone:
        for k in (1, 2, 3):
            q = (x + min(k, 2), y + k)
            if q not in stone:
                px[q] = (mix(MOSS[0], MOSS[1], 0.3) if k == 1 else MOSS[0]) + (255,)
    # the stone: bone fading to mist then shade, speckled, lit at the top left
    for x, y in stone:
        t = (y - top) / max(1, bottom - top)
        c = mix(BONE, MIST, min(1, t * 1.4)) if t < 0.7 else mix(MIST, SHADE, (t - 0.7) / 0.3)
        if rnd.random() < 0.10:
            c = mix(c, SHADE, 0.5)
        if (x, y - 1) not in stone:
            c = mix(c, (255, 252, 236), 0.6)
        elif (x - 1, y) not in stone:
            c = mix(c, BONE, 0.5)
        if (x, y + 1) not in stone or (x + 1, y) not in stone:
            c = mix(c, DEEP, 0.6)
        px[x, y] = c + (255,)
    solid = {(x, y) for y in range(h) for x in range(w) if px[x, y][3]}
    for x, y in list(solid):
        for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
            if q not in solid and 0 <= q[0] < w and 0 <= q[1] < h:
                px[q] = OUTLINE + (255,)
    solid = {(x, y) for y in range(h) for x in range(w) if px[x, y][3]}

    # moss on the tops
    for x, y in sorted(stone):
        if (x, y - 1) not in stone and rnd.random() < MOSS_TOP:
            px[x, y] = MOSS[2 if rnd.random() < 0.6 else 3] + (255,)
            if rnd.random() < 0.5 and (x, y - 1) not in solid:
                px[x, y - 1] = MOSS[2] + (255,)
            if rnd.random() < 0.25 and (x, y - 2) not in solid:
                px[x, y - 2] = MOSS[3] + (255,)
    # vines as on the Friche concept: they hang from the letters' undersides and
    # drip from their tops, cling to the stone as they fall, and only dangle a
    # few pixels once they leave it; leaves on alternate sides
    starts = [(x, y + 1) for x, y in sorted(stone) if (x, y + 1) not in stone and rnd.random() < VINES]
    starts += [(x, y + 1) for x, y in sorted(stone)
               if (x, y - 1) not in stone and (x, y + 1) in stone and rnd.random() < DRIPS]
    for vx, vy in starts:
        free = 0
        for k in range(rnd.randint(4, 14)):
            if not (0 <= vx < w and 0 <= vy < h - 1):
                break
            free = 0 if (vx, vy) in stone else free + 1
            if free > DANGLE:
                break
            px[vx, vy] = MOSS[1] + (255,)
            if k % 3 == 1:
                side = 1 if (k // 3) % 2 else -1
                for lx, ly, c in ((side, 0, MOSS[2]), (side * 2, 0, MOSS[2]), (side * 2, -1, MOSS[3]),
                                  (side, -1, MOSS[3]), (side * 3, -1, MOSS[2])):
                    if 0 <= vx + lx < w and 0 <= vy + ly < h:
                        px[vx + lx, vy + ly] = c + (255,)
            vy += 1
            # a vine leaving the stone sways back towards it
            steps = (0, 0, 0, 1, -1)
            if (vx, vy) not in stone:
                steps += tuple(d for d in (1, -1) if (vx + d, vy) in stone) * 3
            vx += rnd.choice(steps)

    # the lantern in the O, on its chain from the top of the counter
    o = boxes[TEXT.index("O")]
    lw, lh = len(LANTERN[0]), len(LANTERN)
    lx, ly = round((o[0] + o[2]) / 2 - lw / 2), round((top + bottom) / 2 - lh / 2) + 2
    ky = ly - 1
    while ky > 0 and (lx + 6, ky) not in stone:
        px[lx + 6, ky] = OUTLINE + (255,)
        px[lx + 7, ky] = (CHAIN if ky % 2 else OUTLINE) + (255,)
        ky -= 1
    for y, row in enumerate(LANTERN):
        for x, c in enumerate(row):
            if c != ".":
                px[lx + x, ly + y] = LANTERN_COLOURS[c] + (255,)
    # its light on the stone, and a halo behind
    gx, gy = (o[0] + o[2]) / 2, (top + bottom) / 2
    lantern = set(LANTERN_COLOURS.values())
    for y in range(h):
        for x in range(w):
            d = math.hypot(x - gx, (y - gy) * 1.1)
            if d < GLOW:
                k = (1 - d / GLOW) ** 1.6
                p = px[x, y]
                if p[3] == 255 and p[:3] not in lantern:
                    px[x, y] = mix(p[:3], LANTERN_L, k * 0.55) + (255,)
                elif p[3] == 0:
                    px[x, y] = LANTERN_L + (round(110 * k * k),)

    # a soft moss halo, so that the logo reads on a light sky
    halo = Image.new("L", (w, h), 0)
    hp = halo.load()
    for q in solid:
        hp[q] = 255
    back = Image.new("RGBA", (w, h), MOSS[0] + (0,))
    back.putalpha(halo.filter(ImageFilter.GaussianBlur(3)).point(lambda v: round(v * 0.45)))
    back.alpha_composite(im)
    im = back.crop(back.getbbox())
    return im.resize((im.width * SCALE, im.height * SCALE), Image.NEAREST)
