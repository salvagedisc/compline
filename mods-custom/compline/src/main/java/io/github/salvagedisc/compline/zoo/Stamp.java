package io.github.salvagedisc.compline.zoo;

import java.util.Locale;

/**
 * Les quatre tampons d'une espèce, dans l'ordre du guide d'Archie's Dinosaurs. Chaque tampon est un bit du masque
 * que {@link ZooRecord} garde par espèce.
 */
public enum Stamp {
    /** Avoir l'espèce en vue (champ de vision, sans obstacle) à moins de 25 blocs, la règle des dinosaures d'Archie. */
    APERCEVOIR,
    /** L'avoir dans une photo prise avec un appareil d'Exposure. Remplace « tuer » : le zoo étudie sans tuer. */
    PHOTOGRAPHIER,
    /** L'apprivoiser, faire naître un petit ou la prendre dans un seau. Seulement pour les espèces qu'on peut garder. */
    HEBERGER,
    /** Tous les autres tampons de l'espèce, puis l'observer longuement. */
    MAITRISER;

    public int bit() {
        return 1 << ordinal();
    }

    public boolean in(int mask) {
        return (mask & bit()) != 0;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Clé du nom du tampon ({@code zoo.compline.tampon.apercevoir}). */
    public String nameKey() {
        return "zoo.compline.tampon." + id();
    }

    public static Stamp byId(String id) {
        for (Stamp stamp : values()) {
            if (stamp.id().equals(id)) {
                return stamp;
            }
        }
        return null;
    }
}
