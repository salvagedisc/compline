package io.github.salvagedisc.compline.zoo.client.guide;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Les images du zoo, composées au lancement à partir des planches du guide d'Archie's Dinosaurs, lues dans son mod
 * et jamais copiées dans le nôtre : ses tampons, son marque-page doré, les onglets de ses dinosaures, sa bannière et
 * les rubans de sa page des tampons. On efface ce qui y est écrit ou dessiné en reprenant le tissu voisin, on change
 * la teinte quand un milieu n'a pas de dinosaure de sa couleur, et on y pose nos signes : l'appareil d'Exposure pour
 * la photo, des silhouettes d'objets du jeu pour les milieux. Les losanges des portraits sont tracés selon la même
 * construction que ceux d'Archie, avec ses couleurs. Cette classe ne dépend pas du jeu : elle travaille sur des
 * pixels ARGB.
 */
final class ZooArt {
    /** Un tampon : losange de 34 x 36 (avec son ombre), comme sur la page des tampons d'Archie. */
    static final int STAMP_W = 34;
    static final int STAMP_H = 36;
    /** Un onglet de côté, comme ceux des dinosaures : 64 x 32, dont le bout passe sous la page. */
    static final int TAB_W = 64;
    static final int TAB_H = 32;
    /** Le marque-page du bas, 32 x 48. */
    static final int BOOKMARK_W = 32;
    static final int BOOKMARK_H = 48;
    /** Un ruban, de l'axe du losange au bout fourchu, avec sa lisière sombre et son ombre sur le papier. */
    static final int RIBBON_W = 147;
    static final int RIBBON_H = 20;
    /** Les colonnes du ruban qu'on répète pour l'allonger (du tissu clair et uni, une fois le nom effacé, avant le bout plus sombre). */
    static final int RIBBON_TILE_X = 40;
    static final int RIBBON_TILE_W = 60;
    /** Le losange d'un portrait : 58 de large, 57 de haut, son rang le plus large au milieu. */
    static final int FRAME_W = 58;
    static final int FRAME_H = 57;
    /** La bannière du titre, avec les taches de papier qui l'entourent. */
    static final int BANNER_W = 161;
    static final int BANNER_H = 23;
    /** L'écart transparent entre deux images d'une planche, pour qu'aucune ne déteigne sur sa voisine. */
    static final int GAP = 2;
    /** Variantes : 0 pour ce qui manque (sépia, comme les tampons éteints d'Archie), puis un milieu chacune. */
    static final int VARIANTS = 5;

    private static final String GUIDE = "textures/gui/guidebook/";
    /** L'encre brune des textes d'Archie, pour les rubans éteints. */
    private static final int OFF_INK = 0xFF573D19;
    private static final int[] LEFT_STAMPS = {117, 155, 193, 231};
    private static final int[] RIGHT_STAMPS = {369, 407, 445, 483};

    /** Ce qu'Archie a dessiné pour chaque variante : ligne de tampons, ruban, onglet. */
    private record Model(String dino, int[] stampXs, int stampY, int ribbonTop, int ribbonEnd) {
    }

    private static final Model THERIMOSS = new Model("therimoss", LEFT_STAMPS, 175, 153, 240);
    private static final Model CORALOSAUR = new Model("coralosaur", LEFT_STAMPS, 235, 213, 240);
    private static final Model OBSIDICERATOPS = new Model("obsidiceratops", RIGHT_STAMPS, 55, 33, 481);
    /** Le ciel n'a pas de dinosaure : on reprend celui des eaux, tourné vers un bleu plus clair. */
    private static final Tint SKY = new Tint(18.0F, 150.0F, 230.0F, 0.45F, 0.8F, 1.18F);
    /** Le marque-page du zoo : le doré d'Archie, tourné vers le vert de la mousse. */
    private static final Tint MOSS = new Tint(72.0F, 20.0F, 60.0F, 0.3F, 0.78F, 0.66F);

    /**
     * Un changement de teinte : tourne de {@code degrees} les couleurs dont la teinte est dans [{@code from},
     * {@code to}] et la saturation au moins {@code minSaturation} (le reste, papier, blanc, verre de la loupe, ne
     * bouge pas), puis multiplie saturation et clarté.
     */
    private record Tint(float degrees, float from, float to, float minSaturation, float saturation, float value) {
    }

    /** Où lire les images : le mod d'Archie, Exposure, le jeu. Rend {@code null} si l'image manque. */
    interface Source {
        Img load(String namespace, String path);
    }

    /** Une image en ARGB. */
    static final class Img {
        final int width;
        final int height;
        final int[] argb;

        Img(int width, int height) {
            this.width = width;
            this.height = height;
            this.argb = new int[width * height];
        }

        int get(int x, int y) {
            return x < 0 || y < 0 || x >= width || y >= height ? 0 : argb[y * width + x];
        }

        void set(int x, int y, int color) {
            if (x >= 0 && y >= 0 && x < width && y < height) {
                argb[y * width + x] = color;
            }
        }

        Img crop(int x, int y, int w, int h) {
            Img out = new Img(w, h);
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i++) {
                    out.set(i, j, get(x + i, y + j));
                }
            }
            return out;
        }

        Img copy() {
            return crop(0, 0, width, height);
        }

        /** Recopie {@code src} tel quel en ({@code x}, {@code y}), transparence comprise. */
        void paste(Img src, int x, int y) {
            for (int j = 0; j < src.height; j++) {
                for (int i = 0; i < src.width; i++) {
                    set(x + i, y + j, src.get(i, j));
                }
            }
        }

        /** Pose les pixels opaques de {@code src} en ({@code x}, {@code y}). */
        void stamp(Img src, int x, int y) {
            for (int j = 0; j < src.height; j++) {
                for (int i = 0; i < src.width; i++) {
                    int c = src.get(i, j);
                    if (alpha(c) >= 128) {
                        set(x + i, y + j, c | 0xFF000000);
                    }
                }
            }
        }
    }

    /** Ce qui a été composé, et les encres qui vont avec. */
    record Art(Img stamps, Img bookmark, Img tabs, Img banner, int bannerInk, Img ribbons, int[] ribbonInk, Img frames) {
    }

    private ZooArt() {
    }

    /** La place de la {@code index}-ième image de taille {@code size} sur une planche. */
    static int at(int index, int size) {
        return index * (size + GAP);
    }

    /** La taille d'une planche de {@code count} images de taille {@code size}. */
    static int sheet(int size, int count) {
        return count * (size + GAP) - GAP;
    }

    static Art compose(Source source) {
        Img info = need(source, "ec_ad", GUIDE + "badges/info.png");
        Img stampsOn = need(source, "ec_ad", GUIDE + "badges/stamps/on.png");
        Img stampsOff = need(source, "ec_ad", GUIDE + "badges/stamps/off.png");
        Img camera = source.load("exposure", "textures/item/camera/camera_gui.png");
        if (camera == null) {
            camera = source.load("minecraft", "textures/item/spyglass.png");
        }

        Stamps stamps = stamps(stampsOn, stampsOff, camera);

        Img[] ribbons = new Img[VARIANTS];
        int[] inks = new int[VARIANTS];
        ribbons[1] = ribbon(info, THERIMOSS, inks, 1);
        ribbons[2] = ribbon(info, CORALOSAUR, inks, 2);
        ribbons[3] = tint(ribbons[2], SKY);
        inks[3] = inks[2];
        ribbons[4] = ribbon(info, OBSIDICERATOPS, inks, 4);
        int[] fill = paperPalette(stamps.litBases(), stamps.offBases());
        Set<Integer> shadow = new HashSet<>();
        for (int x = 0; x < RIBBON_W; x++) {
            shadow.add(ribbons[1].get(x, RIBBON_H - 1));
        }
        ribbons[0] = fade(ribbons[1], fill, shadow);
        inks[0] = OFF_INK;
        Img ribbonSheet = new Img(RIBBON_W, sheet(RIBBON_H, VARIANTS));
        for (int v = 0; v < VARIANTS; v++) {
            ribbonSheet.paste(ribbons[v], 0, at(v, RIBBON_H));
        }

        int[] ring = ringColors(info);
        Img frames = new Img(FRAME_W, sheet(FRAME_H, VARIANTS));
        for (int v = 1; v < VARIANTS; v++) {
            frames.paste(frame(ring, ribbons[v]), 0, at(v, FRAME_H));
        }
        Set<Integer> kept = new HashSet<>();
        for (int c : ring) {
            kept.add(c);
        }
        frames.paste(fade(frames.crop(0, at(1, FRAME_H), FRAME_W, FRAME_H), fill, kept), 0, 0);

        Img banner = info.crop(87, 23, BANNER_W, BANNER_H);
        int bannerInk = eraseBanner(banner);

        Img footprint = glyph(subTab(need(source, "ec_ad", GUIDE + "categories/lushsaur/markers/off.png")));
        Img tabs = tabSheet(source, footprint);
        Img bookmark = bookmark(need(source, "ec_ad", GUIDE + "markers/off.png"),
                need(source, "ec_ad", GUIDE + "markers/on.png"), footprint);
        return new Art(stamps.sheet(), bookmark, tabs, banner, bannerInk, ribbonSheet, inks, frames);
    }

    private static Img need(Source source, String namespace, String path) {
        Img img = source.load(namespace, path);
        if (img == null) {
            throw new IllegalStateException("image manquante : " + namespace + ":" + path);
        }
        return img;
    }

    // ---------------------------------------------------------------- tampons

    private static Img[] row(Img sheet, Model model) {
        Img[] out = new Img[4];
        for (int i = 0; i < 4; i++) {
            out[i] = sheet.crop(model.stampXs()[i], model.stampY(), STAMP_W, STAMP_H);
        }
        return out;
    }

    /** La planche des tampons, et les fonds vides d'une ligne allumée et de la ligne éteinte. */
    private record Stamps(Img sheet, Img[] litBases, Img[] offBases) {
    }

    /** Le centre du losange d'un tampon (34 sur 34, l'ombre en dessous). */
    private static final float STAMP_CENTER = (STAMP_W - 1) / 2.0F;

    /**
     * Les tampons : une ligne éteinte puis une par milieu, dans l'ordre Apercevoir, Photographier, Héberger, Maîtriser.
     * Archie a la loupe, le crâne, les cœurs et la coupe ; le crâne laisse la place à l'appareil photo. Chaque dessin
     * est détaché de son fond et reposé au centre du losange (ceux d'Archie en sont décalés d'un pixel ou deux).
     */
    private static Stamps stamps(Img on, Img off, Img camera) {
        Img[][] rows = new Img[VARIANTS][];
        rows[0] = row(off, THERIMOSS);
        rows[1] = row(on, THERIMOSS);
        rows[2] = row(on, CORALOSAUR);
        rows[4] = row(on, OBSIDICERATOPS);
        boolean[][] masks = glyphMasks(rows[1], rows[2], rows[4]);
        Img[][] bases = new Img[VARIANTS][];
        for (int v : new int[] {0, 1, 2, 4}) {
            bases[v] = bases(rows[v], masks);
            for (int i = 0; i < 4; i++) {
                rows[v][i] = recentred(rows[v][i], bases[v][i], masks[i]);
            }
        }
        rows[3] = new Img[4];
        bases[3] = new Img[4];
        for (int i = 0; i < 4; i++) {
            rows[3][i] = tint(rows[2][i], SKY);
            bases[3][i] = tint(bases[2][i], SKY);
        }
        if (camera != null) {
            Img offCamera = engrave(camera, offInk(rows[0], masks));
            for (int v = 0; v < VARIANTS; v++) {
                rows[v][1] = centred(bases[v][1], v == 0 ? offCamera : camera);
            }
        }
        Img sheet = new Img(sheet(STAMP_W, 4), sheet(STAMP_H, VARIANTS));
        for (int v = 0; v < VARIANTS; v++) {
            for (int i = 0; i < 4; i++) {
                sheet.paste(rows[v][i], at(i, STAMP_W), at(v, STAMP_H));
            }
        }
        return new Stamps(sheet, bases[1], bases[0]);
    }

    /** La profondeur d'un pixel dans le losange d'un tampon : 0 sur son bord, 17 au centre. */
    private static float depth(int x, int y) {
        return STAMP_W / 2.0F - (Math.abs(x - STAMP_CENTER) + Math.abs(y - STAMP_CENTER));
    }

    /**
     * Les dessins des quatre tampons, relevés sur les lignes allumées : à l'intérieur du losange, tout ce qui n'est
     * pas la couleur vive du fond. Les dessins sont aux mêmes places d'une ligne à l'autre ; la réunion des lignes
     * rattrape un verre de loupe qui aurait la couleur du fond.
     */
    private static boolean[][] glyphMasks(Img[]... lit) {
        boolean[][] masks = new boolean[4][STAMP_W * STAMP_H];
        for (Img[] row : lit) {
            for (int i = 0; i < 4; i++) {
                float ground = groundHue(row[i]);
                for (int y = 0; y < STAMP_H; y++) {
                    for (int x = 0; x < STAMP_W; x++) {
                        int c = row[i].get(x, y);
                        float[] hsv = hsv(c);
                        float gap = Math.abs(hsv[0] - ground);
                        boolean background = Math.min(gap, 360.0F - gap) < 30.0F && hsv[1] > 0.35F;
                        if (depth(x, y) > 4.0F && !background) {
                            masks[i][y * STAMP_W + x] = true;
                        }
                    }
                }
            }
        }
        return masks;
    }

    /** La teinte du fond d'un tampon allumé : la couleur vive la plus courante près de son bord. */
    private static float groundHue(Img stamp) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 0; y < STAMP_H; y++) {
            for (int x = 0; x < STAMP_W; x++) {
                int c = stamp.get(x, y);
                float d = depth(x, y);
                if (d > 4.0F && d < 7.0F && hsv(c)[1] > 0.35F) {
                    counts.merge(c, 1, Integer::sum);
                }
            }
        }
        return hsv(mostCommon(counts, 0))[0];
    }

    /**
     * Le fond vide de chaque tampon d'une ligne : sous son dessin (élargi d'un pixel), le même pixel d'un autre
     * tampon de la ligne où il n'y a rien, sinon le plus proche pixel connu.
     */
    private static Img[] bases(Img[] stamps, boolean[][] masks) {
        int n = STAMP_W * STAMP_H;
        boolean[][] wide = new boolean[4][];
        for (int i = 0; i < 4; i++) {
            wide[i] = masks[i].clone();
            dilate(wide[i], STAMP_W, 0, STAMP_W - 1, 0, STAMP_H - 1);
        }
        Img[] out = new Img[4];
        for (int i = 0; i < 4; i++) {
            Img base = stamps[i].copy();
            boolean[] known = new boolean[n];
            for (int p = 0; p < n; p++) {
                known[p] = !wide[i][p];
                for (int k = 1; k < 4 && !known[p]; k++) {
                    int j = (i + k) % 4;
                    if (!wide[j][p]) {
                        base.argb[p] = stamps[j].argb[p];
                        known[p] = true;
                    }
                }
            }
            nearest(base, known);
            out[i] = base;
        }
        return out;
    }

    /** Le décalage qui met le rectangle {@code box} au centre du losange ; un demi-pixel se rattrape vers le haut. */
    private static int[] toCentre(int[] box) {
        int dx = Math.round(STAMP_CENTER - (box[0] + box[2]) / 2.0F);
        int dy = (int) Math.floor(STAMP_CENTER - (box[1] + box[3]) / 2.0F);
        return new int[] {dx, dy};
    }

    /** Le dessin d'un tampon, reposé au centre de son fond vide. */
    private static Img recentred(Img stamp, Img base, boolean[] mask) {
        int minX = STAMP_W, minY = STAMP_H, maxX = -1, maxY = -1;
        for (int p = 0; p < mask.length; p++) {
            if (mask[p]) {
                minX = Math.min(minX, p % STAMP_W);
                maxX = Math.max(maxX, p % STAMP_W);
                minY = Math.min(minY, p / STAMP_W);
                maxY = Math.max(maxY, p / STAMP_W);
            }
        }
        if (maxX < 0) {
            return stamp;
        }
        int[] shift = toCentre(new int[] {minX, minY, maxX, maxY});
        Img out = base.copy();
        for (int p = 0; p < mask.length; p++) {
            if (mask[p]) {
                out.set(p % STAMP_W + shift[0], p / STAMP_W + shift[1], stamp.argb[p]);
            }
        }
        return out;
    }

    /** Une icône posée au centre d'un fond vide. */
    private static Img centred(Img base, Img icon) {
        int[] box = bounds(icon);
        int[] shift = toCentre(box);
        Img out = base.copy();
        out.stamp(icon, shift[0], shift[1]);
        return out;
    }

    /** Les couleurs des dessins éteints d'Archie, gravés dans le papier, de la plus sombre à la plus claire. */
    private static int[] offInk(Img[] off, boolean[][] masks) {
        Set<Integer> colors = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            if (i == 1) {
                continue;
            }
            for (int p = 0; p < masks[i].length; p++) {
                int c = off[i].argb[p];
                if (masks[i][p] && alpha(c) > 0) {
                    colors.add(c | 0xFF000000);
                }
            }
        }
        return colors.stream().sorted((a, b) -> luma(a) - luma(b)).mapToInt(Integer::intValue).toArray();
    }

    /**
     * Grave une icône comme Archie éteint les siennes : chaque couleur prend, à son rang de clarté, une couleur de ses
     * dessins éteints.
     */
    private static Img engrave(Img icon, int[] ink) {
        return ink.length == 0 ? icon : fade(icon, ink, Set.of());
    }

    /** Remplit chaque pixel inconnu avec le plus proche pixel connu. */
    private static void nearest(Img img, boolean[] known) {
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int p = 0; p < known.length; p++) {
            if (known[p]) {
                queue.add(p);
            }
        }
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        while (!queue.isEmpty()) {
            int p = queue.poll();
            int x = p % img.width;
            int y = p / img.width;
            for (int d = 0; d < 4; d++) {
                int nx = x + dx[d];
                int ny = y + dy[d];
                if (nx < 0 || ny < 0 || nx >= img.width || ny >= img.height) {
                    continue;
                }
                int q = ny * img.width + nx;
                if (!known[q]) {
                    known[q] = true;
                    img.argb[q] = img.argb[p];
                    queue.add(q);
                }
            }
        }
    }

    // ---------------------------------------------------------------- rubans, bannière, losanges

    /**
     * Un ruban de la page des tampons, son nom effacé ; note l'encre du nom dans {@code inks}. Ses rangs : un damier
     * clair en haut (0) et en bas (17), le tissu entre les deux, la lisière sombre (18), l'ombre sur le papier (19).
     */
    private static Img ribbon(Img info, Model model, int[] inks, int variant) {
        int x0 = model.ribbonEnd() - RIBBON_W + 1;
        Img ribbon = info.crop(x0, model.ribbonTop(), RIBBON_W, RIBBON_H);
        int refFrom = RIBBON_W - 9;
        int refTo = RIBBON_W - 5;
        int last = RIBBON_W - 5;
        boolean[] mask = new boolean[ribbon.argb.length];
        Set<Integer> body = new HashSet<>();
        for (int y = 1; y <= 16; y++) {
            for (int x = refFrom; x <= refTo; x++) {
                body.add(ribbon.get(x, y));
            }
            for (int x = 36; x <= last && (y <= 4 || y >= 14); x++) {
                body.add(ribbon.get(x, y));
            }
        }
        Map<Integer, Integer> ink = new HashMap<>();
        for (int y = 0; y < RIBBON_H; y++) {
            Set<Integer> palette = body;
            if (y == 0 || y >= 17) {
                palette = new HashSet<>();
                for (int x = refFrom; x <= refTo; x++) {
                    palette.add(ribbon.get(x, y));
                }
            }
            for (int x = 0; x <= last; x++) {
                int c = ribbon.get(x, y);
                if (!palette.contains(c)) {
                    mask[y * RIBBON_W + x] = true;
                    if (x >= 40 && y >= 5 && y <= 13) {
                        ink.merge(c, 1, Integer::sum);
                    }
                }
            }
        }
        inks[variant] = mostCommon(ink, 0xFFFFFFFF);
        dilate(mask, RIBBON_W, 0, last, 1, 16);
        for (int y : new int[] {0, 17, 18, 19}) {
            fillRowParity(ribbon, mask, y, 0, last);
        }
        fillRowsNear(ribbon, mask, 0, last, 1, 16);
        return ribbon;
    }

    /** Efface le mot de la bannière ; rend son encre. */
    private static int eraseBanner(Img banner) {
        Set<Integer> palette = new HashSet<>();
        for (int y = 2; y <= 19; y++) {
            for (int x = 3; x <= 40; x++) {
                palette.add(banner.get(x, y));
            }
            for (int x = 125; x <= 150; x++) {
                palette.add(banner.get(x, y));
            }
        }
        boolean[] mask = new boolean[banner.argb.length];
        Map<Integer, Integer> ink = new HashMap<>();
        for (int y = 2; y <= 19; y++) {
            for (int x = 41; x < 125; x++) {
                int c = banner.get(x, y);
                if (!palette.contains(c)) {
                    mask[y * banner.width + x] = true;
                    ink.merge(c, 1, Integer::sum);
                }
            }
        }
        dilate(mask, banner.width, 41, 124, 2, 19);
        fillRowsNear(banner, mask, 3, 150, 2, 19);
        return mostCommon(ink, 0xFF2D241F);
    }

    /** Les couleurs du cadre d'un portrait d'Archie, relevées sur celui du Therimoss. */
    private static int[] ringColors(Img info) {
        int top = THERIMOSS.ribbonTop() - 10;
        return new int[] {
                info.get(93, top), // bord haut gauche
                info.get(94, top), // bord haut droit
                info.get(93, top + 1), // bande claire du haut
                info.get(93, top + 3), // filet sombre du haut
                info.get(93, top + FRAME_H - 2), // bande du bas
                info.get(93, top + FRAME_H - 1), // bord bas gauche
                info.get(94, top + FRAME_H - 1), // bord bas droit
        };
    }

    /**
     * Le losange d'un portrait, construit comme ceux d'Archie : bord d'un pixel, bande claire de deux, filet sombre
     * sur la moitié haute, puis le tissu du ruban de même couleur.
     */
    private static Img frame(int[] ring, Img ribbon) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 1; y <= 17; y++) {
            for (int x = 60; x < 120; x++) {
                counts.merge(ribbon.get(x, y), 1, Integer::sum);
            }
        }
        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());
        int main = sorted.get(0).getKey();
        int second = sorted.size() > 1 ? sorted.get(1).getKey() : main;
        int edge = ribbon.get(80, 18);
        Img frame = new Img(FRAME_W, FRAME_H);
        int middle = FRAME_H / 2;
        for (int y = 0; y < FRAME_H; y++) {
            int half = 1 + Math.min(y, FRAME_H - 1 - y);
            int left = FRAME_W / 2 - half;
            int right = FRAME_W / 2 + half - 1;
            boolean top = y < middle;
            for (int x = left; x <= right; x++) {
                int layer = Math.min(x - left, right - x);
                int c;
                if (layer == 0) {
                    boolean west = x - left <= right - x;
                    c = top ? (west ? ring[0] : ring[1]) : (west ? ring[5] : ring[6]);
                } else if (layer <= 2) {
                    c = top ? ring[2] : ring[4];
                } else if (layer == 3 && top) {
                    c = ring[3];
                } else if (layer == 3 || (top && y <= 3 + 1)) {
                    c = edge;
                } else {
                    c = hash(x, y) % 4 == 0 ? second : main;
                }
                frame.set(x, y, c);
            }
        }
        return frame;
    }

    // ---------------------------------------------------------------- onglets et marque-page

    private static Img subTab(Img markers) {
        return markers.crop(0, 32, TAB_W, TAB_H);
    }

    /** Le signe blanc d'un onglet d'Archie (l'empreinte du premier onglet de chaque dinosaure). */
    private static Img glyph(Img tab) {
        Img out = new Img(TAB_W, TAB_H);
        for (int y = 2; y <= 29; y++) {
            for (int x = 15; x <= 34; x++) {
                if (white(tab.get(x, y))) {
                    out.set(x, y, tab.get(x, y));
                }
            }
        }
        int[] box = bounds(out);
        return out.crop(box[0], box[1], box[2] - box[0] + 1, box[3] - box[1] + 1);
    }

    /** Efface le signe blanc d'un onglet en reprenant le tissu de la même colonne. */
    private static Img eraseTab(Img tab) {
        Img out = tab.copy();
        boolean[] mask = new boolean[out.argb.length];
        for (int y = 2; y <= 29; y++) {
            for (int x = 15; x <= 34; x++) {
                mask[y * TAB_W + x] = white(out.get(x, y));
            }
        }
        fillColumns(out, mask, 15, 34, 2, 29);
        return out;
    }

    /** Pose un signe au centre de celui d'Archie (colonnes 17 à 31, rangs 6 à 25). */
    private static void placeGlyph(Img tab, Img glyph) {
        tab.stamp(glyph, Math.round(24.0F - (glyph.width - 1) / 2.0F), Math.round(15.5F - (glyph.height - 1) / 2.0F));
    }

    /**
     * Les onglets de côté : l'accueil (brun du Lushsaur, avec l'empreinte d'Archie), puis un par milieu, chacun en
     * deux états (au repos, survolé).
     */
    private static Img tabSheet(Source source, Img footprint) {
        String[] dinos = {"lushsaur", "therimoss", "coralosaur", "coralosaur", "obsidiceratops"};
        String[][] signs = {null, {"block", "spruce_sapling"}, {"item", "cod"}, {"item", "feather"}, {"item", "bone"}};
        Img sheet = new Img(sheet(TAB_W, 2), sheet(TAB_H, VARIANTS));
        for (int v = 0; v < VARIANTS; v++) {
            Img sign = signs[v] == null ? null : silhouette(source.load("minecraft",
                    "textures/" + signs[v][0] + "/" + signs[v][1] + ".png"), footprint);
            for (int state = 0; state < 2; state++) {
                Img tab = subTab(need(source, "ec_ad", GUIDE + "categories/" + dinos[v] + "/markers/" + (state == 0 ? "off" : "on") + ".png"));
                if (sign != null) {
                    tab = eraseTab(tab);
                    placeGlyph(tab, sign);
                }
                if (v == 3) {
                    tab = tint(tab, SKY);
                }
                sheet.paste(tab, at(state, TAB_W), at(v, TAB_H));
            }
        }
        return sheet;
    }

    /** La silhouette blanche d'un objet du jeu, de la couleur des signes d'Archie. */
    private static Img silhouette(Img item, Img footprint) {
        if (item == null) {
            return null;
        }
        int ink = footprint.get(footprint.width / 2, footprint.height - 2);
        if (alpha(ink) == 0) {
            ink = 0xFFFFFFFF;
        }
        Img out = new Img(16, 16);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (alpha(item.get(x, y)) >= 128) {
                    out.set(x, y, ink);
                }
            }
        }
        int[] box = bounds(out);
        return out.crop(box[0], box[1], box[2] - box[0] + 1, box[3] - box[1] + 1);
    }

    /** Le marque-page doré des tampons d'Archie, tourné vers le vert, avec l'empreinte à la place de la coupe. */
    private static Img bookmark(Img off, Img on, Img footprint) {
        Img sheet = new Img(sheet(BOOKMARK_W, 2), BOOKMARK_H);
        Img[] states = {off.crop(128, 272, BOOKMARK_W, BOOKMARK_H), on.crop(128, 272, BOOKMARK_W, BOOKMARK_H)};
        for (int s = 0; s < 2; s++) {
            Img mark = states[s];
            boolean[] mask = new boolean[mark.argb.length];
            int minX = BOOKMARK_W, minY = BOOKMARK_H, maxX = -1, maxY = -1;
            for (int y = 21; y <= 40; y++) {
                Set<Integer> palette = new HashSet<>();
                for (int x = 2; x <= 5; x++) {
                    palette.add(mark.get(x, y));
                    palette.add(mark.get(BOOKMARK_W - 1 - x, y));
                }
                for (int x = 6; x <= BOOKMARK_W - 7; x++) {
                    if (!palette.contains(mark.get(x, y))) {
                        mask[y * BOOKMARK_W + x] = true;
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            fillRows(mark, mask, 2, BOOKMARK_W - 3, 21, 40);
            mark = tint(mark, MOSS);
            float cx = (minX + maxX) / 2.0F;
            float cy = (minY + maxY) / 2.0F;
            mark.stamp(footprint, Math.round(cx - (footprint.width - 1) / 2.0F), Math.round(cy - (footprint.height - 1) / 2.0F));
            sheet.paste(mark, at(s, BOOKMARK_W), 0);
        }
        return sheet;
    }

    // ---------------------------------------------------------------- remplissages

    /**
     * Remplit les pixels masqués d'un rectangle avec le tissu de leur colonne (les rangs non masqués de la même
     * colonne, ou de la plus proche qui en a), tiré au sort de façon fixe : le dégradé d'un côté à l'autre est gardé.
     */
    private static void fillColumns(Img img, boolean[] mask, int x0, int x1, int y0, int y1) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                if (!mask[y * img.width + x]) {
                    continue;
                }
                List<Integer> pool = new ArrayList<>();
                for (int d = 0; pool.size() < 3 && d < img.width; d++) {
                    for (int sx : d == 0 ? new int[] {x} : new int[] {x + d, x - d}) {
                        if (sx < x0 || sx > x1 || pool.size() >= 3) {
                            continue;
                        }
                        for (int sy = y0; sy <= y1; sy++) {
                            if (!mask[sy * img.width + sx]) {
                                pool.add(img.get(sx, sy));
                            }
                        }
                    }
                }
                if (!pool.isEmpty()) {
                    img.set(x, y, pool.get(hash(x, y) % pool.size()));
                }
            }
        }
        clear(mask, img.width, x0, x1, y0, y1);
    }

    /**
     * Remplit les pixels masqués avec le tissu proche du même rang (les pixels propres à moins de huit colonnes, ou
     * plus loin s'il n'y en a pas), tiré au sort de façon fixe : le grain et le dégradé du rang sont gardés.
     */
    private static void fillRowsNear(Img img, boolean[] mask, int x0, int x1, int y0, int y1) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                if (!mask[y * img.width + x]) {
                    continue;
                }
                List<Integer> pool = new ArrayList<>();
                for (int radius = 8; pool.size() < 4 && radius <= 2 * (x1 - x0); radius *= 2) {
                    pool.clear();
                    for (int sx = Math.max(x0, x - radius); sx <= Math.min(x1, x + radius); sx++) {
                        if (!mask[y * img.width + sx]) {
                            pool.add(img.get(sx, y));
                        }
                    }
                }
                if (!pool.isEmpty()) {
                    img.set(x, y, pool.get(hash(x, y) % pool.size()));
                }
            }
        }
        clear(mask, img.width, x0, x1, y0, y1);
    }

    /** Remplit les pixels masqués avec le tissu de leur rang, tiré au sort de façon fixe. */
    private static void fillRows(Img img, boolean[] mask, int x0, int x1, int y0, int y1) {
        for (int y = y0; y <= y1; y++) {
            List<Integer> pool = new ArrayList<>();
            for (int x = x0; x <= x1; x++) {
                if (!mask[y * img.width + x]) {
                    pool.add(img.get(x, y));
                }
            }
            for (int x = x0; x <= x1 && !pool.isEmpty(); x++) {
                if (mask[y * img.width + x]) {
                    img.set(x, y, pool.get(hash(x, y) % pool.size()));
                }
            }
        }
        clear(mask, img.width, x0, x1, y0, y1);
    }

    /** Remplit un rang en damier (lisière, ombre) en recopiant le pixel propre le plus proche, de même parité. */
    private static void fillRowParity(Img img, boolean[] mask, int y, int x0, int x1) {
        for (int x = x1; x >= x0; x--) {
            if (!mask[y * img.width + x]) {
                continue;
            }
            for (int d = 2; d < img.width; d += 2) {
                if (x + d <= x1 && !mask[y * img.width + x + d]) {
                    img.set(x, y, img.get(x + d, y));
                    break;
                }
                if (x - d >= x0 && !mask[y * img.width + x - d]) {
                    img.set(x, y, img.get(x - d, y));
                    break;
                }
            }
            mask[y * img.width + x] = false;
        }
    }

    /** Élargit le masque d'un pixel autour de ce qui est masqué (l'ombre et l'adoucissement des lettres). */
    private static void dilate(boolean[] mask, int width, int x0, int x1, int y0, int y1) {
        boolean[] grown = mask.clone();
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int dy = -1; dy <= 1 && !grown[y * width + x]; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= x0 && nx <= x1 && ny >= y0 && ny <= y1 && mask[ny * width + nx]) {
                            grown[y * width + x] = true;
                            break;
                        }
                    }
                }
            }
        }
        System.arraycopy(grown, 0, mask, 0, mask.length);
    }

    private static void clear(boolean[] mask, int width, int x0, int x1, int y0, int y1) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                mask[y * width + x] = false;
            }
        }
    }

    // ---------------------------------------------------------------- couleurs

    /**
     * Les couleurs du fond d'un tampon éteint d'Archie (là où l'allumé est coloré), de la plus sombre à la plus
     * claire.
     */
    private static int[] paperPalette(Img[] lit, Img[] dim) {
        Set<Integer> colors = new HashSet<>();
        for (int i = 0; i < lit.length; i++) {
            collectPaper(lit[i], dim[i], colors);
        }
        return colors.stream().sorted((a, b) -> luma(a) - luma(b)).mapToInt(Integer::intValue).toArray();
    }

    private static void collectPaper(Img lit, Img dim, Set<Integer> colors) {
        for (int p = 0; p < lit.argb.length; p++) {
            int c = lit.argb[p];
            int r = (c >> 16) & 0xFF;
            int g = (c >> 8) & 0xFF;
            int b = c & 0xFF;
            int hi = Math.max(r, Math.max(g, b));
            int lo = Math.min(r, Math.min(g, b));
            if (alpha(c) > 0 && hi > 0 && (hi - lo) * 2 > hi && alpha(dim.argb[p]) > 0) {
                colors.add(dim.argb[p]);
            }
        }
    }

    /**
     * Éteint un tissu comme Archie éteint ses tampons : chaque couleur prend, à son rang de clarté, une couleur du
     * fond d'un tampon éteint. Les couleurs de {@code keep} ne changent pas.
     */
    private static Img fade(Img img, int[] paper, Set<Integer> keep) {
        List<Integer> colors = new ArrayList<>();
        for (int c : img.argb) {
            if (alpha(c) > 0 && !keep.contains(c) && !colors.contains(c)) {
                colors.add(c);
            }
        }
        colors.sort((a, b) -> luma(a) - luma(b));
        Map<Integer, Integer> to = new HashMap<>();
        for (int i = 0; i < colors.size(); i++) {
            int rank = colors.size() == 1 ? paper.length / 2 : Math.round(i * (paper.length - 1) / (float) (colors.size() - 1));
            to.put(colors.get(i), paper[rank]);
        }
        Img out = img.copy();
        for (int p = 0; p < out.argb.length; p++) {
            Integer c = to.get(out.argb[p]);
            if (c != null) {
                out.argb[p] = c;
            }
        }
        return out;
    }

    private static Img tint(Img img, Tint tint) {
        Img out = img.copy();
        for (int p = 0; p < out.argb.length; p++) {
            out.argb[p] = tint(out.argb[p], tint);
        }
        return out;
    }

    private static int tint(int c, Tint tint) {
        if (alpha(c) == 0) {
            return c;
        }
        float[] hsv = hsv(c);
        float h = hsv[0];
        if (hsv[2] == 0.0F || hsv[1] < tint.minSaturation()) {
            return c;
        }
        if (h < tint.from() || h > tint.to()) {
            return c;
        }
        h = (h + tint.degrees()) % 360.0F;
        float s = Math.min(1.0F, hsv[1] * tint.saturation());
        float v = Math.min(1.0F, hsv[2] * tint.value());
        float k = h / 60.0F;
        int sector = (int) Math.floor(k) % 6;
        float f = k - (float) Math.floor(k);
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));
        float[] rgb = switch (sector) {
            case 0 -> new float[] {v, t, p};
            case 1 -> new float[] {q, v, p};
            case 2 -> new float[] {p, v, t};
            case 3 -> new float[] {p, q, v};
            case 4 -> new float[] {t, p, v};
            default -> new float[] {v, p, q};
        };
        return (c & 0xFF000000) | Math.round(rgb[0] * 255) << 16 | Math.round(rgb[1] * 255) << 8 | Math.round(rgb[2] * 255);
    }

    /** Teinte (0 à 360), saturation et clarté (0 à 1). */
    private static float[] hsv(int c) {
        float r = ((c >> 16) & 0xFF) / 255.0F;
        float g = ((c >> 8) & 0xFF) / 255.0F;
        float b = (c & 0xFF) / 255.0F;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;
        if (delta == 0.0F) {
            return new float[] {0.0F, 0.0F, max};
        }
        float h;
        if (max == r) {
            h = 60.0F * (((g - b) / delta) % 6.0F);
        } else if (max == g) {
            h = 60.0F * ((b - r) / delta + 2.0F);
        } else {
            h = 60.0F * ((r - g) / delta + 4.0F);
        }
        return new float[] {h < 0.0F ? h + 360.0F : h, delta / max, max};
    }

    private static int mostCommon(Map<Integer, Integer> counts, int fallback) {
        int best = fallback;
        int bestCount = 0;
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > bestCount) {
                bestCount = entry.getValue();
                best = entry.getKey();
            }
        }
        return best;
    }

    /** Le rectangle des pixels opaques : {minX, minY, maxX, maxY}. */
    static int[] bounds(Img img) {
        int minX = img.width, minY = img.height, maxX = -1, maxY = -1;
        for (int y = 0; y < img.height; y++) {
            for (int x = 0; x < img.width; x++) {
                if (alpha(img.get(x, y)) >= 128) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxX < 0 ? new int[] {0, 0, img.width - 1, img.height - 1} : new int[] {minX, minY, maxX, maxY};
    }

    private static boolean white(int c) {
        return alpha(c) > 0 && ((c >> 16) & 0xFF) >= 235 && ((c >> 8) & 0xFF) >= 235 && (c & 0xFF) >= 235;
    }

    static int alpha(int c) {
        return (c >>> 24) & 0xFF;
    }

    static int luma(int c) {
        return (((c >> 16) & 0xFF) * 299 + ((c >> 8) & 0xFF) * 587 + (c & 0xFF) * 114) / 1000;
    }

    private static int hash(int x, int y) {
        int h = x * 374761393 + y * 668265263;
        h = (h ^ (h >>> 13)) * 1274126177;
        return (h ^ (h >>> 16)) & 0x7FFFFFFF;
    }
}
