"""Tilth's widgets, drawn from scratch: buttons, sliders, checkboxes, tabs, text
fields and scrollers, the recipe book's toggles and the lists' arrows.

Raised parts (buttons, the slider's knob, the scroller's thumb, tabs) are sage
with a moss outline, rounded corners, a bone light on top and a shadow below;
the buttons carry an engraved inner frame with a rivet at each end. Recessed
parts (the slider's track, checkboxes, text fields) are moss wells with a
shadow under their top edge, like the slots. What the pointer is on lights up:
a button filled with the lantern's warm light, a well's outline lit.

Called by build.py once the rest of the pack is built.
"""
import json

from PIL import Image

OUTLINE = (26, 31, 28)
T = (0, 0, 0, 0)
BONE = (232, 229, 208)
LANTERN_EDGE = (110, 70, 34)
LANTERN_D = (168, 106, 44)
LANTERN = (226, 166, 78)
LANTERN_L = (248, 214, 140)

# a raised part: outline, light, body (top and bottom of its gradient), shade, deep shade, leaf, leaf light
RAISED = {
    "normal": (OUTLINE, (150, 160, 138), (104, 116, 100), (92, 104, 89), (72, 82, 70), (60, 69, 59),
               (80, 92, 78), (124, 134, 116)),
    "highlighted": (LANTERN_EDGE, LANTERN_L, (204, 142, 64), (176, 112, 48), (150, 92, 38), (128, 78, 32),
                    (150, 94, 40), (232, 180, 100)),
    "disabled": ((44, 50, 46), (82, 90, 80), (74, 82, 72), (70, 78, 68), (62, 70, 60), (56, 63, 55),
                 (66, 74, 64), (78, 86, 76)),
}
# a recessed well: outline, top shadow, second shadow, body, bottom light
WELL = {
    "normal": (OUTLINE, (36, 43, 38), (44, 52, 46), (52, 60, 53), (94, 104, 93)),
    "highlighted": (LANTERN_D, (36, 43, 38), (44, 52, 46), (52, 60, 53), (130, 104, 70)),
}
# the leaf engraved at each end of a button, 7 wide and 6 high, its stem to the outside
LEAF = [
    "....##.",
    "..####.",
    ".####..",
    "####...",
    ".##....",
    "#......",
]
TICK = [
    ".......##",
    "......##.",
    ".....##..",
    "##..##...",
    ".####....",
    "..##.....",
]
CROSS = [
    "##...##",
    ".##.##.",
    "..###..",
    ".##.##.",
    "##...##",
]


def shape(rows):
    return [(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c == "#"]


def nine_slice(path, w, h, border):
    """The .mcmeta that makes the game draw a sprite in nine slices."""
    path.with_name(path.name + ".mcmeta").write_text(json.dumps(
        {"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": border}}}, indent=4) + "\n")


def raised(w, h, state, leaves=False, bottom=True):
    """A raised part w x h. Corners are cut by one pixel, the outline running
    inside them; without `bottom` its lower edge stays open (a tab)."""
    edge, light, top, low, shade, deep, leaf, leaf_light = RAISED[state]
    im = Image.new("RGBA", (w, h), T)
    px = im.load()
    for y in range(h):
        t = y / max(1, h - 1)
        body = tuple(round(a + (b - a) * t) for a, b in zip(top, low))
        for x in range(w):
            px[x, y] = body + (255,)
    last = h - 1 if bottom else h
    for x in range(1, w - 1):
        px[x, 1] = light + (255,)
        if bottom:
            px[x, h - 3] = shade + (255,)
            px[x, h - 2] = deep + (255,)
    for y in range(1, last):
        px[1, y] = (light if y < h - 3 or not bottom else shade) + (255,)
        px[w - 2, y] = (shade if y > 1 else light) + (255,)
    for x in range(w):
        px[x, 0] = edge + (255,)
        if bottom:
            px[x, h - 1] = edge + (255,)
    for y in range(h):
        px[0, y] = edge + (255,)
        px[w - 1, y] = edge + (255,)
    # rounded corners: the corner pixel goes, the outline steps inside it
    for cx, cy, dx, dy in [(0, 0, 1, 1), (w - 1, 0, -1, 1)] + ([(0, h - 1, 1, -1), (w - 1, h - 1, -1, -1)] if bottom else []):
        px[cx, cy] = T
        px[cx + dx, cy + dy] = edge + (255,)
    if leaves and h >= 16:
        # an engraved inner frame, as on a carved plaque, its corners cut
        x0, y0, x1, y1 = 3, 3, w - 4, h - 4
        for x in range(x0 + 1, x1):
            px[x, y0] = leaf + (255,)
            px[x, y0 + 1] = leaf_light + (255,) if x < x1 - 1 else px[x, y0 + 1]
            px[x, y1] = leaf_light + (255,)
        for y in range(y0 + 1, y1):
            px[x0, y] = leaf + (255,)
            px[x0 + 1, y] = leaf_light + (255,) if y > y0 and y < y1 - 1 and y != y0 + 1 else px[x0 + 1, y]
            px[x1, y] = leaf_light + (255,)
        # a rivet at each end, inside the frame
        cy = h // 2
        for cx in (7, w - 8):
            for dx, dy in ((0, -1), (-1, 0), (1, 0), (0, 1)):
                px[cx + dx, cy + dy - 1] = leaf + (255,)
            px[cx, cy - 1] = leaf_light + (255,)
    return im


def well(w, h, state):
    """A recessed well w x h, with rounded corners."""
    edge, shadow, shadow2, body, low = WELL[state]
    im = Image.new("RGBA", (w, h), body + (255,))
    px = im.load()
    for x in range(1, w - 1):
        px[x, 1] = shadow + (255,)
        px[x, 2] = shadow2 + (255,)
        px[x, h - 2] = low + (255,)
    for y in range(1, h - 1):
        px[1, y] = shadow + (255,) if y < h - 2 else low + (255,)
    for x in range(w):
        px[x, 0] = px[x, h - 1] = edge + (255,)
    for y in range(h):
        px[0, y] = px[w - 1, y] = edge + (255,)
    for cx, cy, dx, dy in [(0, 0, 1, 1), (w - 1, 0, -1, 1), (0, h - 1, 1, -1), (w - 1, h - 1, -1, -1)]:
        px[cx, cy] = T
        px[cx + dx, cy + dy] = edge + (255,)
    return im


def mark(im, rows, x0, y0, ink, shadow=None):
    px = im.load()
    pts = shape(rows)
    if shadow:
        for x, y in pts:
            if (x, y + 1) not in pts:
                px[x0 + x, y0 + y + 1] = shadow + (255,)
    for x, y in pts:
        px[x0 + x, y0 + y] = ink + (255,)


def knob(state):
    """The slider's knob. The game draws it 8 pixels wide from the outer 4 columns
    at each end of a 200-wide sprite; the middle repeats the knob's centre."""
    small = raised(8, 20, state)
    px = small.load()
    groove = (LANTERN_D if state == "highlighted" else (72, 82, 70))
    for y in range(7, 13):
        px[3, y] = groove + (255,)
        px[4, y] = (LANTERN_L if state == "highlighted" else (130, 140, 122)) + (255,)
    im = Image.new("RGBA", (200, 20), T)
    im.paste(small.crop((0, 0, 4, 20)), (0, 0))
    im.paste(small.crop((4, 0, 8, 20)), (196, 0))
    middle = small.crop((3, 0, 5, 20))
    for x in range(4, 196, 2):
        im.paste(middle, (x, 0))
    return im


def draw(out):
    gui = out / "assets" / "minecraft" / "textures" / "gui" / "sprites"
    w = gui / "widget"
    ends = {"left": 20, "top": 4, "right": 20, "bottom": 4}

    for suffix, state in (("", "normal"), ("_highlighted", "highlighted"), ("_disabled", "disabled")):
        raised(200, 20, state, leaves=True).save(w / f"button{suffix}.png")
        nine_slice(w / f"button{suffix}.png", 200, 20, ends)
    for suffix, state in (("", "normal"), ("_highlighted", "highlighted")):
        well(200, 20, state).save(w / f"slider{suffix}.png")
        nine_slice(w / f"slider{suffix}.png", 200, 20, ends)
        knob(state).save(w / f"slider_handle{suffix}.png")
        nine_slice(w / f"slider_handle{suffix}.png", 200, 20, ends)

    # checkboxes: drawn 17 wide in vanilla, so in nine slices with the tick inside
    # the part that is never cut
    for name, state, ticked in (("checkbox", "normal", False), ("checkbox_highlighted", "highlighted", False),
                                ("checkbox_selected", "normal", True),
                                ("checkbox_selected_highlighted", "highlighted", True)):
        im = well(20, 20, state)
        if ticked:
            mark(im, TICK, 4, 5, LANTERN_L if state == "highlighted" else BONE, OUTLINE)
        im.save(w / f"{name}.png")
        nine_slice(w / f"{name}.png", 20, 20, 4)

    # text fields: a moss well, its outline lit when focused
    for suffix, edge in (("", OUTLINE), ("_highlighted", LANTERN)):
        im = well(200, 20, "normal")
        px = im.load()
        for x in range(200):
            for y in (0, 19):
                px[x, y] = edge + (255,)
        for y in range(20):
            px[0, y] = px[199, y] = edge + (255,)
        im.save(w / f"text_field{suffix}.png")
        nine_slice(w / f"text_field{suffix}.png", 200, 20, 1)

    # scroller: a sage thumb in a moss groove
    thumb = Image.new("RGBA", (6, 32), OUTLINE + (255,))
    px = thumb.load()
    for y in range(1, 31):
        for x, c in zip(range(1, 5), [(166, 170, 150), (130, 140, 122), (130, 140, 122), (94, 104, 93)]):
            px[x, y] = c + (255,)
    for x in range(1, 5):
        px[x, 1] = (166, 170, 150, 255)
        px[x, 30] = (94, 104, 93, 255)
    thumb.save(w / "scroller.png")
    nine_slice(w / "scroller.png", 6, 32, 1)
    groove = Image.new("RGBA", (6, 32), (44, 52, 46, 255))
    px = groove.load()
    for y in range(32):
        px[0, y] = px[5, y] = OUTLINE + (255,)
    for x in range(6):
        px[x, 0] = px[x, 31] = OUTLINE + (255,)
    groove.save(w / "scroller_background.png")
    nine_slice(w / "scroller_background.png", 6, 32, 1)

    # tabs: open at the bottom, the selected one lighter
    for name, state, lift in (("tab", "normal", -14), ("tab_highlighted", "highlighted", -10),
                              ("tab_selected", "normal", 6), ("tab_selected_highlighted", "highlighted", 6)):
        im = raised(130, 24, state, bottom=False)
        if lift:
            px = im.load()
            for y in range(2, 24):
                for x in range(2, 128):
                    r, g, b, a = px[x, y]
                    px[x, y] = (r + lift, g + lift, b + lift, a)
        im.save(w / f"{name}.png")
        nine_slice(w / f"{name}.png", 130, 24, {"left": 2, "top": 2, "right": 2, "bottom": 0})

    # small square buttons: the padlocks keep their drawing on the new base
    for base in ("locked_button", "unlocked_button"):
        for suffix, state in (("", "normal"), ("_highlighted", "highlighted"), ("_disabled", "disabled")):
            old = Image.open(w / f"{base}{suffix}.png").convert("RGBA")
            im = raised(20, 20, state)
            src, dst = old.load(), im.load()
            ink = padlock_colours(old)
            for y in range(5, 15):
                for x in range(5, 15):
                    if src[x, y][:3] in ink:
                        dst[x, y] = src[x, y]
            im.save(w / f"{base}{suffix}.png")
    for suffix, state in (("", "normal"), ("_highlighted", "highlighted")):
        im = raised(14, 14, state)
        mark(im, CROSS, 4, 4, LANTERN_L if state == "highlighted" else BONE, OUTLINE)
        im.save(w / f"cross_button{suffix}.png")

    # the recipe book's "craftable only" toggles: a button with a tick or a cross
    rb = gui / "recipe_book"
    for kind in ("filter", "furnace_filter"):
        for on in ("enabled", "disabled"):
            for suffix, state in (("", "normal"), ("_highlighted", "highlighted")):
                im = Image.new("RGBA", (26, 16), T)
                im.alpha_composite(raised(22, 16, state), (2, 0))
                if on == "enabled":
                    mark(im, TICK, 9, 5, LANTERN_L if state == "highlighted" else BONE, OUTLINE)
                else:
                    mark(im, CROSS, 10, 5, LANTERN if state == "highlighted" else (166, 170, 150), OUTLINE)
                im.save(rb / f"{kind}_{on}{suffix}.png")

    # a recipe that cannot be made keeps its rust colour, lit at the edge when pointed at
    for kind in ("crafting", "furnace"):
        im = Image.open(rb / f"{kind}_overlay_disabled.png").convert("RGBA")
        px = im.load()
        ring = [(x, y) for y in range(im.height) for x in range(im.width) if px[x, y][3] and any(
            not (0 <= x + dx < im.width and 0 <= y + dy < im.height) or not px[x + dx, y + dy][3]
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))]
        for x, y in ring:
            px[x, y] = LANTERN + (255,)
        im.save(rb / f"{kind}_overlay_disabled_highlighted.png")


def padlock_colours(im):
    """The colours of a padlock: the lightest ones of the sprite."""
    from collections import Counter
    px = im.load()
    seen = Counter(px[x, y][:3] for y in range(5, 15) for x in range(5, 15) if px[x, y][3])
    ranked = sorted(seen, key=lambda c: -sum(c))
    return set(ranked[:2])
