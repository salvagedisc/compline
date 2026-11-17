package io.github.salvagedisc.compline.patine.client;

/**
 * Ce que l'armure porte sur elle, peint au pixel sur une copie de sa planche (pixels ABGR, comme {@code NativeImage}) :
 * la terre, le sable, la neige, l'eau. Rien ici ne dépend du jeu. Le motif ne dépend que de la graine (le nom de la
 * texture) : tout le monde voit la même armure, et chaque palier s'étend sur le motif du précédent.
 *
 * <p>Les emplacements sont ceux d'une planche d'armure du jeu (64 × 32, ou un multiple) : le casque en haut à gauche,
 * le torse et les bras au milieu, les jambes et les bottes en bas à gauche. Une planche d'un mod rangée autrement
 * reçoit les mêmes taches, simplement moins bien placées.
 */
public final class ArmorWear {
    /** Seuil du bruit au-dessus duquel la terre tient, par palier : plus bas, plus de surface couverte. */
    private static final float[] EARTH = {0.74F, 0.60F, 0.46F};
    /** Même chose pour le sable, plus fin et plus bas. */
    private static final float[] SAND = {0.84F, 0.72F, 0.60F};

    private ArmorWear() {
    }

    /**
     * Peint une planche en place. Paliers de 0 (rien) à 3 pour la terre, le sable et la neige, de 0 à 2 pour l'eau.
     *
     * @param pixels planche ligne par ligne, ABGR
     * @param tinted vrai pour une couche que le jeu teint (le cuir) : la teinte rendrait la neige et le sable bruns,
     *        ils vont sur la couche par-dessus ({@link #paintOver}) ; ici seulement la terre et l'armure foncée par l'eau
     * @param leather vrai pour un cuir : il boit l'eau et ne rouille pas
     */
    public static void paint(int[] pixels, int width, int height, int seed, boolean tinted, boolean leather,
            int earth, int sand, int snow, int wet) {
        float scale = Math.max(1.0F, width / 64.0F);
        if (earth > 0) {
            earth(pixels, width, height, scale, seed, leather, earth);
        }
        if (!tinted && sand > 0) {
            sand(pixels, null, width, height, scale, seed, sand);
        }
        if (wet > 0) {
            wet(pixels, null, width, height, scale, seed, leather, wet, !tinted);
        }
        if (!tinted && snow > 0) {
            snow(pixels, null, width, height, scale, seed, snow);
        }
    }

    /**
     * Peint le sable, les gouttes et la neige d'une armure teinte sur sa couche non teinte (pour le cuir, celle des
     * coutures), dessinée par-dessus : là où elle est transparente mais où l'armure a de la matière ({@code shape},
     * la planche teinte de même taille), le dépôt est posé tel quel.
     */
    public static void paintOver(int[] pixels, int[] shape, int width, int height, int seed, int sand, int snow, int wet) {
        float scale = Math.max(1.0F, width / 64.0F);
        if (sand > 0) {
            sand(pixels, shape, width, height, scale, seed, sand);
        }
        if (wet > 0) {
            wet(pixels, shape, width, height, scale, seed, true, wet, true);
        }
        if (snow > 0) {
            snow(pixels, shape, width, height, scale, seed, snow);
        }
    }

    // La terre : des taches de trois pixels environ, frangées au pixel, en deux tons ; plus sur les jambes et les bottes,
    // moins sur le casque. Sur le métal nu, quelques points de rouille au dernier palier.
    private static void earth(int[] pixels, int width, int height, float scale, int seed, boolean leather, int level) {
        float threshold = EARTH[level - 1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = pixels[y * width + x];
                if (alpha(c) == 0) {
                    continue;
                }
                int px = (int) (x / scale);
                int py = (int) (y / scale);
                float n = 0.62F * blob(px / 3.0F, py / 3.0F, seed) + 0.38F * hash(px, py, seed + 17) + lowness(px, py);
                if (n < threshold) {
                    continue;
                }
                float r = red(c);
                float g = green(c);
                float b = blue(c);
                float max = Math.max(r, Math.max(g, b));
                float min = Math.min(r, Math.min(g, b));
                boolean bareMetal = !leather && max > 110 && (max - min) < 0.18F * max;
                if (bareMetal && level == 3 && blob(px / 2.0F, py / 2.0F, seed + 91) > 0.74F && hash(px, py, seed + 5) > 0.35F) {
                    r = mix(r, 124, 0.7F);
                    g = mix(g, 70, 0.7F);
                    b = mix(b, 40, 0.7F);
                } else if (n > threshold + 0.09F) {
                    r = mix(r, 98 + 0.15F * r, 0.75F);
                    g = mix(g, 80 + 0.15F * g, 0.75F);
                    b = mix(b, 58 + 0.15F * b, 0.75F);
                } else {
                    r = mix(r, r * 0.72F + 10, 0.55F);
                    g = mix(g, g * 0.64F + 8, 0.55F);
                    b = mix(b, b * 0.52F + 5, 0.55F);
                }
                pixels[y * width + x] = color(alpha(c), r, g, b);
            }
        }
    }

    // Le sable : un grain clair au pixel, qui monte des bottes vers le haut à chaque palier ; au dernier, une fine
    // poussière ternit toute l'armure.
    private static void sand(int[] pixels, int[] shape, int width, int height, float scale, int seed, int level) {
        float threshold = SAND[level - 1];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = pixels[y * width + x];
                boolean bare = alpha(c) == 0;
                if (bare && (shape == null || alpha(shape[y * width + x]) == 0)) {
                    continue;
                }
                int px = (int) (x / scale);
                int py = (int) (y / scale);
                float r = red(c);
                float g = green(c);
                float b = blue(c);
                if (level == 3 && !bare) {
                    r = mix(r, 206, 0.16F);
                    g = mix(g, 186, 0.16F);
                    b = mix(b, 140, 0.16F);
                }
                float n = 0.45F * blob(px / 2.0F, py / 2.0F, seed + 41) + 0.55F * hash(px, py, seed + 43) + 1.6F * lowness(px, py);
                if (n >= threshold) {
                    boolean light = hash(px, py, seed + 47) > 0.45F;
                    float t = bare ? 1.0F : n > threshold + 0.08F ? 0.8F : 0.5F;
                    r = mix(r, light ? 226 : 186, t);
                    g = mix(g, light ? 198 : 152, t);
                    b = mix(b, light ? 136 : 94, t);
                    pixels[y * width + x] = color(255, r, g, b);
                } else if (!bare) {
                    pixels[y * width + x] = color(alpha(c), r, g, b);
                }
            }
        }
    }

    // L'eau : l'armure foncée, les gouttes en points clairs avec une courte coulure sombre en dessous.
    private static void wet(int[] pixels, int[] shape, int width, int height, float scale, int seed, boolean leather,
            int level, boolean drawDrops) {
        float dark = shape != null ? 1.0F : level == 1 ? 0.86F : leather ? 0.64F : 0.72F;
        float drops = level == 1 ? 0.988F : 0.968F;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = pixels[y * width + x];
                boolean bare = alpha(c) == 0;
                if (bare && (shape == null || alpha(shape[y * width + x]) == 0)) {
                    continue;
                }
                int px = (int) (x / scale);
                int py = (int) (y / scale);
                float r = red(c) * dark;
                float g = green(c) * dark;
                float b = blue(c) * dark;
                if (drawDrops && side(px, py) >= 0) {
                    if (hash(px, py, seed + 300) > drops) {
                        float t = bare ? 1.0F : 0.72F;
                        r = mix(r, 236, t);
                        g = mix(g, 242, t);
                        b = mix(b, 250, t);
                        pixels[y * width + x] = color(255, r, g, b);
                        continue;
                    } else if (!bare && (hash(px, py - 1, seed + 300) > drops && side(px, py - 1) >= 0
                            || level == 2 && hash(px, py - 2, seed + 300) > drops && side(px, py - 2) >= 0)) {
                        r *= 0.8F;
                        g *= 0.82F;
                        b *= 0.86F;
                    }
                }
                if (!bare) {
                    pixels[y * width + x] = color(alpha(c), r, g, b);
                }
            }
        }
    }

    // La neige : posée sur le dessus du casque, des épaules et du torse, débordant sur le haut des faces ; tassée autour
    // du bas des bottes. Le pixel sous un amas est ombré de bleu, ce qui lui donne du relief.
    private static void snow(int[] pixels, int[] shape, int width, int height, float scale, int seed, int level) {
        int w = (int) (width / scale);
        int h = (int) (height / scale);
        boolean[] mask = new boolean[w * h];
        float[] top = {0.6F, 0.32F, -1.0F};
        float[] spill = {0.6F, 1.5F, 2.6F};
        float[] boots = {1.0F, 1.6F, 2.4F};
        for (int py = 0; py < h; py++) {
            for (int px = 0; px < w; px++) {
                float jitter = hash(px, py, seed + 211) - 0.5F;
                boolean on;
                if (top(px, py)) {
                    on = blob(px / 2.0F, py / 2.0F, seed + 207) > top[level - 1];
                } else {
                    int row = side(px, py);
                    on = row >= 0 && px >= 16 && row < spill[level - 1] + 1.6F * jitter + (blob(px / 2.0F, 0.0F, seed + 209) - 0.5F) * 2.0F;
                    if (!on && px < 16 && py >= 20) {
                        on = 31 - py < boots[level - 1] + 1.4F * jitter;
                    }
                    if (!on && level >= 2) {
                        on = hash(px, py, seed + 213) > (level == 2 ? 0.985F : 0.965F);
                    }
                }
                mask[py * w + px] = on;
            }
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = pixels[y * width + x];
                if (alpha(c) == 0 && (shape == null || alpha(shape[y * width + x]) == 0)) {
                    continue;
                }
                int px = Math.min(w - 1, (int) (x / scale));
                int py = Math.min(h - 1, (int) (y / scale));
                if (!mask[py * w + px]) {
                    // Sous un amas posé sur une face de côté, l'armure est à l'ombre de la neige : elle se détache même
                    // sur le fer clair.
                    if (alpha(c) > 0 && py > 0 && side(px, py) > 0 && mask[(py - 1) * w + px] && !top(px, py - 1)) {
                        pixels[y * width + x] = color(alpha(c), red(c) * 0.62F, green(c) * 0.66F, blue(c) * 0.76F);
                    }
                    continue;
                }
                boolean edge = !top(px, py) && py + 1 < h && !mask[(py + 1) * w + px];
                boolean light = hash(px, py, seed + 215) > 0.3F;
                float r = edge ? 196 : light ? 242 : 226;
                float g = edge ? 208 : light ? 246 : 234;
                float b = edge ? 228 : light ? 252 : 246;
                pixels[y * width + x] = color(255, r, g, b);
            }
        }
    }

    /** Vrai sur une face du dessus : casque, chapeau, torse, épaules, cuisses. */
    private static boolean top(int x, int y) {
        return y < 8 && (x >= 8 && x < 16 || x >= 40 && x < 48)
                || y >= 16 && y < 20 && (x >= 20 && x < 28 || x >= 44 && x < 48 || x >= 4 && x < 8);
    }

    /** Rang depuis le haut sur une face de côté (0 la plus haute), ou -1 hors des côtés. */
    private static int side(int x, int y) {
        if (y >= 8 && y < 16) {
            return y - 8;
        }
        if (y >= 20 && y < 32 && x < 56) {
            return y - 20;
        }
        return -1;
    }

    /** Ce qui pousse vers le bas : les jambes et les bottes en bas à gauche, le casque en haut. */
    private static float lowness(int x, int y) {
        if (y < 16) {
            return -0.06F;
        }
        if (x < 16) {
            return 0.08F + 0.10F * Math.min(1.0F, Math.max(0.0F, (y - 20) / 12.0F));
        }
        return 0.03F;
    }

    private static int alpha(int c) {
        return c >>> 24;
    }

    private static int blue(int c) {
        return c >> 16 & 255;
    }

    private static int green(int c) {
        return c >> 8 & 255;
    }

    private static int red(int c) {
        return c & 255;
    }

    private static int color(int a, float r, float g, float b) {
        return a << 24 | clamp(b) << 16 | clamp(g) << 8 | clamp(r);
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, (int) v));
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Bruit de valeur lissé sur une grille. */
    private static float blob(float x, float y, int seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fx = x - ix;
        float fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        float top = mix(hash(ix, iy, seed), hash(ix + 1, iy, seed), fx);
        float bottom = mix(hash(ix, iy + 1, seed), hash(ix + 1, iy + 1, seed), fx);
        return mix(top, bottom, fy);
    }

    private static float hash(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }
}
