package io.github.salvagedisc.compline.zoo.client.guide;

/**
 * Les signes blancs des onglets des milieux, dessinés pour le zoo dans le style de ceux d'Archie (aplats pleins, une
 * quinzaine de pixels de large, trous pour les détails). {@code #} est un pixel du signe.
 */
final class ZooGlyphs {
    /** Terre ferme : une empreinte de patte. */
    static final String[] PAW = {
            "....##...##....",
            "...####.####...",
            "...####.####...",
            "...####.####...",
            "....##...##....",
            ".##.........##.",
            "####.......####",
            "####.......####",
            ".##...###...##.",
            "....#######....",
            "...#########...",
            "..###########..",
            "..###########..",
            "..###########..",
            "...####.####...",
    };

    /** Grand bleu : un poisson. */
    static final String[] FISH = {
            ".....#####.......",
            "...#########...##",
            "..###########.###",
            ".##.#############",
            "#################",
            "#################",
            ".###############.",
            "..###########.###",
            "...#########...##",
            ".....#####.......",
    };

    /** Plein ciel : un oiseau. */
    static final String[] BIRD = {
            "..........###..",
            ".........#####.",
            ".........##.###",
            ".........#####.",
            "#.......######.",
            "##.....#######.",
            "###...########.",
            ".##############",
            ".####....#####.",
            "..#####...###..",
            "...##########..",
            ".....#######...",
            "......#...#....",
            ".....##..##....",
    };

    /** Bêtes noires : un crâne. */
    static final String[] SKULL = {
            "....#######....",
            "..###########..",
            ".#############.",
            "###############",
            "###############",
            "##....###....##",
            "##....###....##",
            "###..#####..###",
            "#######.#######",
            ".#####...#####.",
            "..###########..",
            "...#.#.#.#.#...",
            "...#########...",
    };

    private ZooGlyphs() {
    }

    /** Le signe {@code rows} à l'encre {@code ink}. */
    static ZooArt.Img draw(String[] rows, int ink) {
        ZooArt.Img out = new ZooArt.Img(rows[0].length(), rows.length);
        for (int y = 0; y < rows.length; y++) {
            for (int x = 0; x < rows[y].length(); x++) {
                if (rows[y].charAt(x) == '#') {
                    out.set(x, y, ink);
                }
            }
        }
        return out;
    }
}
