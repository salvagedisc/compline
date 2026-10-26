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
    private static final ModConfigSpec.BooleanValue HORIZON_ENABLED;
    private static final ModConfigSpec.BooleanValue HORIZON_SKIP_HIDDEN_LODS;
    private static final ModConfigSpec.BooleanValue TITRES_ENABLED;
    private static final ModConfigSpec.BooleanValue SAC_ENABLED;
    private static final ModConfigSpec.IntValue SAC_BOTTLES_PER_DRINK;
    private static final ModConfigSpec.BooleanValue XP_ENABLED;
    private static final ModConfigSpec.BooleanValue XP_HIDE_BAR;
    private static final ModConfigSpec.BooleanValue XP_BLOCK_ENCHANT_MERGE;
    private static final ModConfigSpec.BooleanValue CLAIRIERE_ENABLED;
    private static final ModConfigSpec.IntValue CLAIRIERE_CHUNKS_PER_TICK;

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

        client.comment("Module horizon (client) : le paysage lointain de Distant Horizons suit la brume du terrain.").push("horizon");
        HORIZON_ENABLED = client
                .comment("Interrupteur du module. Vrai : dans la brume, les LOD restent cachés ; là où elle se lève,",
                        "le paysage lointain apparaît. Faux : Distant Horizons garde le brouillard de sa config",
                        "(celle du pack cache tous les LOD).")
                .define("enabled", true);
        HORIZON_SKIP_HIDDEN_LODS = client
                .comment("Ne pas dessiner du tout les LOD quand la brume les cache entièrement (gain de performance).")
                .define("skipHiddenLods", true);
        client.pop();

        client.comment("Module titres (client) : le titre des écrans vanilla sur une plaque au-dessus du panneau.").push("titres");
        TITRES_ENABLED = client
                .comment("Interrupteur du module. Faux : le jeu écrit de nouveau les titres dans les panneaux, à ses places.")
                .define("enabled", true);
        client.pop();
        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.comment("Module sac : il relie Traveler's Backpack et Thirst Was Reclaimed.").push("sac");
        SAC_ENABLED = common
                .comment("Interrupteur du module. Vrai : boire de l'eau au tuyau du sac désaltère, selon la pureté",
                        "de l'eau du réservoir (effets de l'eau sale compris). Faux : le tuyau ne désaltère pas.")
                .define("enabled", true);
        SAC_BOTTLES_PER_DRINK = common
                .comment("Une gorgée au tuyau (le sac retire 1000 mB du réservoir) vaut ce nombre de bouteilles d'eau",
                        "au sens de Thirst Was Reclaimed (hydratation et désaltération de minecraft:potion).")
                .defineInRange("bottlesPerDrink", 1, 1, 4);
        common.pop();

        common.comment("Module xp : Fallow n'a plus d'expérience. Les orbes et les gains sont coupés par",
                "kubejs/server_scripts/sans_xp.js ; ce module règle l'enclume et cache la barre.").push("xp");
        XP_ENABLED = common
                .comment("Interrupteur du module. Vrai : l'enclume ne demande plus de niveaux ; son coût devient une usure",
                        "(à 40, « Trop usé ! »). Faux : l'enclume vanilla, qui demande des niveaux.",
                        "Client et serveur doivent avoir la même valeur.")
                .define("enabled", true);
        XP_HIDE_BAR = common
                .comment("Cacher la barre d'expérience et le chiffre du niveau (client). La jauge de saut à cheval reste.")
                .define("hideBar", true);
        XP_BLOCK_ENCHANT_MERGE = common
                .comment("L'enclume refuse un second objet qui porte des enchantements (livre enchanté, objet enchanté) :",
                        "l'enchantement ne passe plus que par la table. Faux : l'enclume fusionne comme en vanilla.")
                .define("blockEnchantMerge", false);
        common.pop();

        common.comment("Modules clairiere et dissipateur : poser un feu de veille (rayon 16), une lanterne de clairière (32)",
                "ou faire tourner un dissipateur (64) change les biomes autour en clairière, une fois pour toutes.").push("clairiere");
        CLAIRIERE_ENABLED = common
                .comment("Interrupteur de la conversion (serveur). Faux : les blocs restent, mais ne changent plus rien.")
                .define("enabled", true);
        CLAIRIERE_CHUNKS_PER_TICK = common
                .comment("Tronçons (16 × 16 blocs) convertis par tick. Un dissipateur en touche environ 80.")
                .defineInRange("chunksPerTick", 2, 1, 16);
        common.pop();
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

    public static boolean horizonEnabled() {
        return read(CLIENT_SPEC, HORIZON_ENABLED);
    }

    public static boolean horizonSkipHiddenLods() {
        return horizonEnabled() && read(CLIENT_SPEC, HORIZON_SKIP_HIDDEN_LODS);
    }

    public static boolean titresEnabled() {
        return read(CLIENT_SPEC, TITRES_ENABLED);
    }

    public static boolean sacEnabled() {
        return read(COMMON_SPEC, SAC_ENABLED);
    }

    public static int sacBottlesPerDrink() {
        return read(COMMON_SPEC, SAC_BOTTLES_PER_DRINK);
    }

    public static boolean xpEnabled() {
        return read(COMMON_SPEC, XP_ENABLED);
    }

    public static boolean xpHideBar() {
        return xpEnabled() && read(COMMON_SPEC, XP_HIDE_BAR);
    }

    public static boolean xpBlockEnchantMerge() {
        return xpEnabled() && read(COMMON_SPEC, XP_BLOCK_ENCHANT_MERGE);
    }

    public static boolean clairiereEnabled() {
        return read(COMMON_SPEC, CLAIRIERE_ENABLED);
    }

    public static int clairiereChunksPerTick() {
        return read(COMMON_SPEC, CLAIRIERE_CHUNKS_PER_TICK);
    }

    /** Lit une valeur sans planter si la config n'est pas encore chargée. */
    static boolean read(ModConfigSpec spec, ModConfigSpec.BooleanValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }

    static int read(ModConfigSpec spec, ModConfigSpec.IntValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }
}
