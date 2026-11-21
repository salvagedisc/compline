package io.github.salvagedisc.fallow.etabli;

/**
 * La grille de 3 × 3 cases telle que Visual Workbench la dessine sur la table : chaque case est à 3/16 de bloc de sa
 * voisine, la première à 5/16 du coin, et toute la grille tourne d'un quart de tour vers le joueur le plus proche
 * (son « secteur »). Les calculs sont ceux de son {@code CraftingTableBlockEntityRenderer} et de son
 * {@code CraftingTableAnimationController}, refaits ici sans le jeu pour savoir quelle case vise un clic.
 */
public final class Grille {
    static final double PAS = 3.0 / 16.0;
    static final double DEPART = 5.0 / 16.0;
    /** Au-delà d'une demi-case et de cette marge hors de la grille, le clic ne vise aucune case. */
    static final double MARGE = 0.35;

    private Grille() {
    }

    /** Le secteur (0 à 3) d'un joueur placé à {@code dx}, {@code dz} du centre de la table. */
    public static int secteur(double dx, double dz) {
        double angle = (Math.atan2(-dx, -dz) + 5.0 * Math.PI / 4.0) % (2.0 * Math.PI);
        return ((int) (angle * 2.0 / Math.PI)) & 3;
    }

    /** L'angle de la grille, en degrés, pour un secteur. */
    public static float angle(int secteur) {
        return secteur * 90.0F;
    }

    /**
     * La case (0 à 8) sous un point du dessus de la table, {@code x} et {@code z} entre 0 et 1 dans le bloc ; -1 si le
     * point est hors de la grille.
     */
    public static int caseVisee(double x, double z, int secteur) {
        double theta = Math.toRadians(angle(secteur));
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        double ox = x - 0.5;
        double oz = z - 0.5;
        // La grille est tournée de theta autour de +Y : on ramène le point dans son repère.
        double lx = ox * cos - oz * sin;
        double lz = ox * sin + oz * cos;
        double colonne = (lx + 0.5 - DEPART) / PAS;
        double rangee = (lz + 0.5 - DEPART) / PAS;
        if (colonne < -0.5 - MARGE || colonne > 2.5 + MARGE || rangee < -0.5 - MARGE || rangee > 2.5 + MARGE) {
            return -1;
        }
        int c = Math.max(0, Math.min(2, (int) Math.round(colonne)));
        int r = Math.max(0, Math.min(2, (int) Math.round(rangee)));
        return r * 3 + c;
    }

    /** Le centre d'une case dans le repère de la grille (avant sa rotation), décalé du centre du bloc : {x, z}. */
    public static double[] centre(int index) {
        return new double[] {(index % 3) * PAS + DEPART - 0.5, (index / 3) * PAS + DEPART - 0.5};
    }

    /** Le centre d'une case dans le bloc, entre 0 et 1, une fois la grille tournée : {x, z}. */
    public static double[] centreDansLeBloc(int index, int secteur) {
        double[] local = centre(index);
        double theta = Math.toRadians(angle(secteur));
        double cos = Math.cos(theta);
        double sin = Math.sin(theta);
        return new double[] {0.5 + local[0] * cos + local[1] * sin, 0.5 - local[0] * sin + local[1] * cos};
    }
}
