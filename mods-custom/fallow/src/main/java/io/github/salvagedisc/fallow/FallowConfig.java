package io.github.salvagedisc.fallow;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration de Fallow : un booléen par module, plus quelques réglages.
 * {@code config/fallow-client.toml} pour les modules client, {@code config/fallow-common.toml} pour les autres.
 * Les valeurs sont relues à chaque usage : couper un module en jeu prend effet tout de suite.
 */
public final class FallowConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;

    private static final ModConfigSpec.BooleanValue CIEL_ENABLED;
    private static final ModConfigSpec.BooleanValue CIEL_REDRAW_MOON;
    private static final ModConfigSpec.BooleanValue CIEL_CLEAR_FOG_ON_BIOME_UPDATE;

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        client.comment("Module ciel (client) : il relie ATMOSPHERICS et Enhanced Celestials.").push("ciel");
        CIEL_ENABLED = client
                .comment("Interrupteur du module. Faux : Fallow ne touche plus au ciel.")
                .define("enabled", true);
        CIEL_REDRAW_MOON = client
                .comment("Redessiner la lune d'Enhanced Celestials (texture, couleur, taille) à la place de la lune",
                        "vanilla que dessine ATMOSPHERICS. Demande Enhanced Celestials.")
                .define("redrawMoon", true);
        CIEL_CLEAR_FOG_ON_BIOME_UPDATE = client
                .comment("Vider le cache de brume d'ATMOSPHERICS dès que le serveur change des biomes",
                        "(/fillbiome, conversion en clairière), pour que la brume réagisse sans que le joueur bouge.")
                .define("clearFogOnBiomeUpdate", true);
        client.pop();
        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        COMMON_SPEC = common.build();
    }

    private FallowConfig() {
    }

    public static boolean cielEnabled() {
        return read(CLIENT_SPEC, CIEL_ENABLED);
    }

    public static boolean cielRedrawMoon() {
        return cielEnabled() && read(CLIENT_SPEC, CIEL_REDRAW_MOON);
    }

    public static boolean cielClearFogOnBiomeUpdate() {
        return cielEnabled() && read(CLIENT_SPEC, CIEL_CLEAR_FOG_ON_BIOME_UPDATE);
    }

    /** Lit une valeur sans planter si la config n'est pas encore chargée. */
    static boolean read(ModConfigSpec spec, ModConfigSpec.BooleanValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }

    static int read(ModConfigSpec spec, ModConfigSpec.IntValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }
}
