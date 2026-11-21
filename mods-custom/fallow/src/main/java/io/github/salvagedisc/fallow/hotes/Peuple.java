package io.github.salvagedisc.fallow.hotes;

import java.util.Arrays;
import java.util.Optional;

/**
 * Les six peuples des Hôtes, chacun veillant sur un dinosaure d'Archie's Dinosaurs et portant son armure
 * (étude « Étude : les tribus masquées », variante du 20/11). Chacun a son émissaire attitré, son masque et sa parure.
 */
public enum Peuple {
    ECORCE("ecorce", "lushsaur", "masque_de_l_ecorce"),
    BRAISE("braise", "obsidiceratops", "masque_de_la_braise"),
    GIVRE("givre", "chorusoatl", "masque_du_givre"),
    FONDS("fonds", "coralosaur", "masque_des_fonds"),
    SABLES("sables", "therimoss", "masque_des_sables"),
    SILENCE("silence", "sculksaur", "masque_du_silence");

    private final String id;
    private final String bete;
    private final String masque;

    Peuple(String id, String bete, String masque) {
        this.id = id;
        this.bete = bete;
        this.masque = masque;
    }

    /** L'identifiant de l'objet masque du peuple ({@code fallow:<masque>}). */
    public String masque() {
        return masque;
    }

    public String id() {
        return id;
    }

    /** Le nom du dinosaure gardien, tel qu'il apparaît dans les identifiants d'armure d'Archie ({@code ec_ad:<bête>_helmet}). */
    public String bete() {
        return bete;
    }

    public static Optional<Peuple> parse(String id) {
        return Arrays.stream(values()).filter(p -> p.id.equals(id)).findFirst();
    }
}
