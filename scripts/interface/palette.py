"""Fallow's colours for GUI Revision's screens.

GUI Revision draws every screen with one ramp of greys (outline, darks, slots,
panel, light). The player's inventory, the chests and most screens take
Fallow's mist ramp (`BRUME`); the other screens take a family of their own on
their top panel, so that each kind of work has its colour, as GUI Revision gives
the crafting table its wood and the enchanting table its purple: every grey
keeps its brightness, so the relief stays as GUI Revision drew it.
"""
import colorsys

# GUI Revision's greys, darkest first.
GREYS = [(28, 28, 31), (54, 56, 56), (85, 89, 89), (98, 102, 102), (100, 107, 107), (109, 115, 115),
         (125, 130, 130), (131, 139, 139), (162, 166, 166), (193, 198, 198), (218, 231, 230)]
# One step off the panel grey, left in a few compats.
NEAR = {(193, 197, 197): (193, 198, 198), (194, 198, 198): (193, 198, 198)}

# Fallow's mist: moss darks, sage slots, mist panel, bone light.
BRUME = {
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


def lum(p):
    return 0.299 * p[0] + 0.587 * p[1] + 0.114 * p[2]


def ramp(hue, sat, warm_light=0.0):
    """A family's ramp: each of GUI Revision's greys at its own brightness, in a
    hue (degrees) and saturation, the saturation easing off at both ends; the
    light leaning to bone by warm_light."""
    out = {}
    for g in GREYS:
        target = lum(BRUME[g])
        s = sat * (0.55 + 0.45 * (1 - abs(target / 255 - 0.55) / 0.55))
        best = None
        for i in range(101):  # the lightness that gives the target brightness
            r, gg, b = colorsys.hls_to_rgb(hue / 360, i / 100, s)
            c = (round(r * 255), round(gg * 255), round(b * 255))
            if best is None or abs(lum(c) - target) < abs(lum(best) - target):
                best = c
        if g == GREYS[-1] and warm_light:
            best = tuple(round(a + (b - a) * warm_light) for a, b in zip(best, BRUME[g]))
        out[g] = best
    return out


FAMILIES = {
    # the player's things, the chests, the hoppers, the mounts: mist
    "brume": BRUME,
    # wood: the crafting table, the loom, the cartography table, the traders
    "bois": ramp(32, 0.30, 0.3),
    # the furnaces: warm ash and brick
    "forge": ramp(24, 0.17, 0.3),
    # the enchanting table, the brewing stand, the beacon: dusk
    "arcane": ramp(262, 0.20, 0.2),
    # the shulker box: the End's purple, softened
    "ender": ramp(300, 0.16, 0.2),
    # moss: the anvil, the grindstone, the stonecutter, the smithing table, the
    # dispenser and the crafter
    "mousse": ramp(95, 0.22, 0.3),
}


def tones(family):
    """The plate's tones and the lettering's for a family: GUI Revision's outline
    of plates (its 85 grey), its light, panel and shade."""
    f = FAMILIES[family]
    return {
        "outline": f[(85, 89, 89)], "light": f[(218, 231, 230)], "fill": f[(193, 198, 198)],
        "shade": f[(131, 139, 139)], "ink": f[(85, 89, 89)], "shadow": f[(54, 56, 56)] + (90,),
    }


def hexa(c):
    return "#%02x%02x%02x" % c
