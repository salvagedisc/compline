"""Draws the weather textures of the `tilth` mod (Python 3 + Pillow, deterministic).

- mods-custom/tilth/src/main/resources/assets/tilth/textures/environment/
  rain_curtain.png  the far rain curtains of the `averse` module (tiles both ways)
  glint.png         one glint of sunlight on water (`rayons` module)

Run from the repository root: python3 scripts/mod/weather.py
"""
import math
import os
import random

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
MOD = os.path.join(ROOT, "mods-custom/tilth/src/main/resources/assets/tilth/textures/environment")

STREAK = (232, 236, 238)


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
    os.makedirs(MOD, exist_ok=True)
    curtain().save(os.path.join(MOD, "rain_curtain.png"))
    glint().save(os.path.join(MOD, "glint.png"))
    print("weather textures written")
