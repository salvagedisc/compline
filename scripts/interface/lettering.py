"""The lettering of the screens' names, after GUI Revision's: capitals five
pixels tall, engraved (the ink a step darker than the plate, a pixel of light
under each stroke where the plate shows), on a plate that stands over the top
edge of the panel.

The letters GUI Revision drew (in FURNACE, BLAST FURNACE, SMOKER, CRAFTING
TABLE and ENCHANTING TABLE) are taken as they are; the rest of the alphabet,
the accents French needs, the digits and some signs are drawn in the same
hand. The fallow mod draws the names with this atlas (`lettres.png` and
`lettres.json`), in each screen's colours.
"""
# Five rows each, '#' ink. A glyph is followed by a pixel of space.
LETTERS = {
    "A": [".###.", "#...#", "#####", "#...#", "#...#"],
    "B": ["####.", "#...#", "####.", "#...#", "####."],
    "C": [".###.", "#...#", "#....", "#...#", ".###."],
    "D": ["####.", "#...#", "#...#", "#...#", "####."],
    "E": ["####", "#...", "###.", "#...", "####"],
    "F": ["####", "#...", "####", "#...", "#..."],
    "G": [".###.", "#....", "#.###", "#...#", ".###."],
    "H": ["#...#", "#...#", "#####", "#...#", "#...#"],
    "I": ["#####", "..#..", "..#..", "..#..", "#####"],
    "J": ["..###", "...#.", "...#.", "#..#.", ".##.."],
    "K": ["#..#", "#.#.", "##..", "#.#.", "#..#"],
    "L": ["#...", "#...", "#...", "#...", "####"],
    "M": ["#...#", "##.##", "#.#.#", "#...#", "#...#"],
    "N": ["#...#", "##..#", "#.#.#", "#..##", "#...#"],
    "O": [".###.", "#...#", "#...#", "#...#", ".###."],
    "P": ["####.", "#...#", "####.", "#....", "#...."],
    "Q": [".###.", "#...#", "#...#", "#..#.", ".##.#"],
    "R": ["####.", "#...#", "####.", "#...#", "#...#"],
    "S": [".###.", "#....", ".###.", "....#", "####."],
    "T": ["#####", "..#..", "..#..", "..#..", "..#.."],
    "U": ["#...#", "#...#", "#...#", "#...#", ".###."],
    "V": ["#...#", "#...#", "#...#", ".#.#.", "..#.."],
    "W": ["#...#", "#...#", "#.#.#", "##.##", "#...#"],
    "X": ["#...#", ".#.#.", "..#..", ".#.#.", "#...#"],
    "Y": ["#...#", ".#.#.", "..#..", "..#..", "..#.."],
    "Z": ["#####", "...#.", "..#..", ".#...", "#####"],
    "Œ": [".####", "#.#..", "#.##.", "#.#..", ".####"],
    "Æ": [".####", "#.#..", "####.", "#.#..", "#.###"],
    "0": [".###.", "#..##", "#.#.#", "##..#", ".###."],
    "1": [".#.", "##.", ".#.", ".#.", "###"],
    "2": [".###.", "#...#", "..##.", ".#...", "#####"],
    "3": ["####.", "....#", ".###.", "....#", "####."],
    "4": ["#..#.", "#..#.", "#####", "...#.", "...#."],
    "5": ["#####", "#....", "####.", "....#", "####."],
    "6": [".###.", "#....", "####.", "#...#", ".###."],
    "7": ["#####", "...#.", "..#..", ".#...", ".#..."],
    "8": [".###.", "#...#", ".###.", "#...#", ".###."],
    "9": [".###.", "#...#", ".####", "....#", ".###."],
    "-": ["...", "...", "###", "...", "..."],
    "'": ["#", "#", ".", ".", "."],
    "’": ["#", "#", ".", ".", "."],
    ".": [".", ".", ".", ".", "#"],
    ",": [".", ".", ".", "#", "#"],
    ":": [".", "#", ".", "#", "."],
    "!": ["#", "#", "#", ".", "#"],
    "?": ["###.", "...#", ".##.", "....", ".#.."],
    "(": [".#", "#.", "#.", "#.", ".#"],
    ")": ["#.", ".#", ".#", ".#", "#."],
    "/": ["...#", "..#.", ".#..", ".#..", "#..."],
    "&": [".#..", "#.#.", ".#.#", "#.#.", ".#.#"],
    "+": ["...", ".#.", "###", ".#.", "..."],
    "#": [".#.#.", "#####", ".#.#.", "#####", ".#.#."],
}
SPACE = 3
# Accents on the row above the letter (columns), and the cedilla below it.
ACCENTS = {
    "acute": lambda w: [w // 2 + 1], "grave": lambda w: [w // 2 - 1],
    "circ": lambda w: list(range(w // 2 - 1, w // 2 + 2)) if w >= 5 else [1, 2],
    "trema": lambda w: [1, w - 2] if w >= 5 else [0, w - 1],
}
ACCENTED = {
    "É": ("E", "acute"), "È": ("E", "grave"), "Ê": ("E", "circ"), "Ë": ("E", "trema"),
    "À": ("A", "grave"), "Â": ("A", "circ"), "Ä": ("A", "trema"), "Á": ("A", "acute"),
    "Î": ("I", "circ"), "Ï": ("I", "trema"), "Í": ("I", "acute"),
    "Ô": ("O", "circ"), "Ö": ("O", "trema"), "Ó": ("O", "acute"),
    "Ù": ("U", "grave"), "Û": ("U", "circ"), "Ü": ("U", "trema"), "Ú": ("U", "acute"),
    "Ÿ": ("Y", "trema"), "Ñ": ("N", "circ"),
}
HEIGHT = 7  # accent row, five rows of letter, cedilla row


def glyph(char):
    """A character's pixels on seven rows (row 0 its accent, rows 1-5 the letter,
    row 6 the cedilla), and its width; None for a character the lettering lacks."""
    if char == "Ç":
        pts, w = glyph("C")
        return pts | {(2, 6)}, w
    if char in ACCENTED:
        base, accent = ACCENTED[char]
        pts, w = glyph(base)
        return pts | {(x, 0) for x in ACCENTS[accent](w)}, w
    rows = LETTERS.get(char)
    if rows is None:
        return None
    return {(x, y + 1) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"}, len(rows[0])


def text(word):
    """The pixels of a word on seven rows, and its width; characters the lettering
    lacks are left out."""
    pts, x = set(), 0
    for char in word.upper():
        if char == " ":
            x += SPACE + 1
            continue
        g = glyph(char)
        if g is None:
            continue
        pts |= {(x + a, b) for a, b in g[0]}
        x += g[1] + 1
    return pts, max(x - 1, 0)


def charset():
    return sorted(set(LETTERS) | set(ACCENTED) | {"Ç"})


def atlas():
    """The glyphs in one row of 8-pixel cells, white on transparency, and their
    table: each character's cell and width."""
    from PIL import Image
    chars = charset()
    im = Image.new("RGBA", (8 * len(chars), HEIGHT))
    table = {}
    for i, char in enumerate(chars):
        pts, w = glyph(char)
        for x, y in pts:
            im.putpixel((8 * i + x, y), (255, 255, 255, 255))
        table[char] = [8 * i, w]
    return im, table


# The plate, GUI Revision's: fourteen rows, an outline in the screen's dark, two
# rows of light under its top and a column of light on each side, a shade over
# its last three rows, its corners cut; the name engraved on rows 4 to 8.
PLATE_HEIGHT = 14
PLATE_PAD = 13
TEXT_TOP = 3  # the lettering's row 0 (accents) on the plate's row 3


def plate(width, tones):
    """The plate's pixels, {(x, y): colour}, for a width; tones: outline, light,
    fill, shade (rgb tuples), and shadow (rgba), cast on the panel at its foot."""
    out = {}
    o, light, fill, shade, shadow = (tones[k] for k in ("outline", "light", "fill", "shade", "shadow"))
    w = width
    for y in range(PLATE_HEIGHT):
        inset = {0: 2, 1: 1, PLATE_HEIGHT - 2: 1, PLATE_HEIGHT - 1: 2}.get(y, 0)
        for x in range(inset, w - inset):
            edge = x in (inset, w - 1 - inset) or y in (0, PLATE_HEIGHT - 1)
            if edge:
                c = o
            elif y in (1, 2):
                c = light
            elif y >= PLATE_HEIGHT - 3 or (y == PLATE_HEIGHT - 4 and x in (inset + 1, w - 2 - inset)):
                c = shade
            elif x in (inset + 1, w - 2 - inset) or (y == 3 and x in (inset + 2, w - 3 - inset)):
                c = light
            else:
                c = fill
            out[(x, y)] = c + (255,)
    # the shadow it casts at its foot, a pixel down and to the right
    for (x, y) in list(out):
        for p in ((x + 1, y + 1),):
            if p not in out and p[1] >= 3:
                out[p] = shadow
    return out


def plate_width(word):
    return text(word)[1] + 2 * PLATE_PAD


def draw_name(im, word, cx, top, tones):
    """The plate and its engraved name on an image, centred on column cx, its top
    row at top (for previews; the mod draws the same in game)."""
    px = im.load()
    pts, tw = text(word)
    w = tw + 2 * PLATE_PAD
    x0 = cx - w // 2
    for (x, y), c in plate(w, tones).items():
        if 0 <= x0 + x < im.width and 0 <= top + y < im.height:
            if len(c) == 4 and c[3] < 255:
                base = px[x0 + x, top + y]
                if not base[3]:
                    continue
                a = c[3] / 255
                c = tuple(round(b * (1 - a) + k * a) for b, k in zip(base[:3], c[:3])) + (255,)
            px[x0 + x, top + y] = c
    tx = x0 + (w - tw) // 2
    ty = top + TEXT_TOP
    for x, y in pts:
        if (x, y + 1) not in pts:
            px[tx + x, ty + y + 1] = tones["light"] + (255,)
    for x, y in pts:
        px[tx + x, ty + y] = tones["ink"] + (255,)
