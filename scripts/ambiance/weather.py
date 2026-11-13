"""Draws the weather textures of Tilth (Python 3 + Pillow, deterministic).

- pack/resourcepacks/tilth-ambiance/assets/minecraft/textures/environment/rain.png
  The rain drawn by Where Winds Blow (and vanilla): thin, long, pale streaks
  instead of short blue drops. 64 x 256, one block wide and four blocks tall.
  Texel (0, 0) is a marker for the particle core shader of tilth-ambiance.
- mods-custom/tilth/src/main/resources/assets/tilth/textures/environment/
  rain_curtain.png  the far rain curtains of the `averse` module (tiles both ways)
  glint.png         one glint of sunlight on water (`rayons` module)

Run from the repository root: python3 scripts/ambiance/weather.py
"""
import math
import os
import random

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
AMBIANCE = os.path.join(ROOT, "pack/resourcepacks/tilth-ambiance/assets/minecraft/textures/environment")
MOD = os.path.join(ROOT, "mods-custom/tilth/src/main/resources/assets/tilth/textures/environment")

STREAK = (232, 236, 238)
# "FAL" + alpha 1: keep in sync with RAIN_MARKER in tilth-ambiance/assets/minecraft/shaders/core/particle.vsh
RAIN_MARKER = (70, 65, 76, 1)


def streak(im, x, y0, length, peak, wrap=True):
    """A one-pixel streak, brightest a third of the way from its lower end (the falling head)."""
    w, h = im.size
    for i in range(length):
        t = i / max(1, length - 1)
        # 0 at the top, rising to the head at t = 0.7, then a short fade
        a = (t / 0.7) ** 1.6 if t < 0.7 else max(0.0, 1 - (t - 0.7) / 0.3) ** 0.8
        y = (y0 + i) % h if wrap else y0 + i
        if not 0 <= y < h:
            continue
        r, g, b, old = im.getpixel((x, y))
        im.putpixel((x, y), STREAK + (max(old, int(peak * a)),))


def rain():
    rnd = random.Random(7)
    im = Image.new("RGBA", (64, 256), STREAK + (0,))
    used = []
    # sparse on purpose: Where Winds Blow stacks this texture in several layers around the player
    for _ in range(7):
        for _try in range(40):
            x = rnd.randrange(1, 63)
            if all(abs(x - u) > 5 for u in used):
                break
        used.append(x)
        streak(im, x, rnd.randrange(256), rnd.randint(18, 40), rnd.randint(80, 125))
    # a few faint ones in between, for depth
    for _ in range(4):
        streak(im, rnd.randrange(64), rnd.randrange(256), rnd.randint(10, 22), rnd.randint(30, 50))
    # marker texel read by the particle core shader of tilth-ambiance, which then gives the drops
    # the colour of the mist; alpha 1 is never drawn (the shader discards alpha < 0.1)
    im.putpixel((0, 0), RAIN_MARKER)
    return im


def curtain():
    rnd = random.Random(11)
    im = Image.new("RGBA", (128, 256), STREAK + (0,))
    for _ in range(90):
        streak(im, rnd.randrange(128), rnd.randrange(256), rnd.randint(40, 120), rnd.randint(30, 80))
    return im


def glint():
    """A soft four-pointed star: a round core and two thin rays, 16 x 16."""
    n = 16
    im = Image.new("RGBA", (n, n), (255, 255, 255, 0))
    c = (n - 1) / 2
    for y in range(n):
        for x in range(n):
            dx, dy = x - c, y - c
            core = max(0.0, 1 - math.hypot(dx, dy) / 3.2) ** 1.5
            star = (max(0.0, 1 - abs(dx) / 1.1) * max(0.0, 1 - abs(dy) / 7.5)
                    + max(0.0, 1 - abs(dy) / 1.1) * max(0.0, 1 - abs(dx) / 7.5))
            a = min(1.0, core + 0.55 * star)
            im.putpixel((x, y), (255, 255, 255, int(round(255 * a))))
    return im


if __name__ == "__main__":
    os.makedirs(AMBIANCE, exist_ok=True)
    os.makedirs(MOD, exist_ok=True)
    rain().save(os.path.join(AMBIANCE, "rain.png"))
    curtain().save(os.path.join(MOD, "rain_curtain.png"))
    glint().save(os.path.join(MOD, "glint.png"))
    print("weather textures written")
