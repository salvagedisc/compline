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
    private static final ModConfigSpec.BooleanValue CIEL_FRIEND_MOON;
    private static final ModConfigSpec.BooleanValue HORIZON_ENABLED;
    private static final ModConfigSpec.BooleanValue HORIZON_SKIP_HIDDEN_LODS;
    private static final ModConfigSpec.IntValue HORIZON_RING_MAX_DISTANCE;
    private static final ModConfigSpec.BooleanValue TITRES_ENABLED;
    private static final ModConfigSpec.BooleanValue XP_ENABLED;
    private static final ModConfigSpec.BooleanValue XP_HIDE_BAR;
    private static final ModConfigSpec.BooleanValue XP_BLOCK_ENCHANT_MERGE;
    private static final ModConfigSpec.BooleanValue CLAIRIERE_ENABLED;
    private static final ModConfigSpec.IntValue CLAIRIERE_CHUNKS_PER_TICK;
    private static final ModConfigSpec.IntValue CLAIRIERE_RING_TICKS;
    private static final ModConfigSpec.DoubleValue CIEL_CLEARING_FADE;
    private static final ModConfigSpec.BooleanValue OUTILS_ENABLED;
    private static final ModConfigSpec.BooleanValue AVERSE_ENABLED;
    private static final ModConfigSpec.BooleanValue AVERSE_RAIN_TINT;
    private static final ModConfigSpec.BooleanValue AVERSE_CURTAINS;
    private static final ModConfigSpec.DoubleValue AVERSE_CURTAIN_OPACITY;
    private static final ModConfigSpec.BooleanValue AVERSE_LIGHTNING;
    private static final ModConfigSpec.BooleanValue AVERSE_SKY_FLASH;
    private static final ModConfigSpec.BooleanValue AVERSE_WINDBLOWN_LEAVES;
    private static final ModConfigSpec.IntValue AVERSE_LEAF_REST_SECONDS;
    private static final ModConfigSpec.BooleanValue AVERSE_GRASS_BITS;
    private static final ModConfigSpec.BooleanValue RAYONS_ENABLED;
    private static final ModConfigSpec.BooleanValue RAYONS_SHAFTS;
    private static final ModConfigSpec.DoubleValue RAYONS_SHAFT_STRENGTH;
    private static final ModConfigSpec.BooleanValue RAYONS_GLINTS;

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
        CIEL_CLEARING_FADE = client
                .comment("Quand une clairière s'ouvre ou se referme, la brume s'éclaircit ou s'épaissit en douceur :",
                        "secondes pour passer d'une brume pleine à un ciel dégagé, ou l'inverse (le fondu ralentit en fin",
                        "de course ; la brume visible part dans les 4 ou 5 premières). 0 : fondu normal d'ATMOSPHERICS, presque immédiat.")
                .defineInRange("clearingFadeSeconds", 10.0, 0.0, 30.0);
        CIEL_FRIEND_MOON = client
                .comment("Laisser No Man's Land dessiner la Lune Amie et son ombre sous le ciel d'ATMOSPHERICS.",
                        "Faux : la Lune Amie n'apparaît pas au Point de rencontre et l'ombre ne montre pas le chemin.")
                .define("friendMoon", true);
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
        HORIZON_RING_MAX_DISTANCE = client
                .comment("Rayon maximal du cercle de brume, en blocs : même là où la brume se lève, le paysage lointain",
                        "s'efface toujours dans la brume puis le ciel à cette distance (ou avant, si la brume du terrain",
                        "finit plus tôt : 500 blocs de jour en clairière, 260 la nuit).")
                .defineInRange("ringMaxDistance", 512, 64, 4096);
        client.pop();

        client.comment("Module titres (client) : le nom de chaque écran sur une plaque au-dessus du panneau.").push("titres");
        TITRES_ENABLED = client
                .comment("Interrupteur du module. Faux : le jeu écrit de nouveau les titres dans les panneaux, à ses places.")
                .define("enabled", true);
        client.pop();

        client.comment("Module averse (client) : la pluie, l'orage et le vent dans les feuilles, sans shader.").push("averse");
        AVERSE_ENABLED = client
                .comment("Interrupteur du module. Faux : la pluie et l'orage redeviennent ceux de Where Winds Blow et du jeu.")
                .define("enabled", true);
        AVERSE_RAIN_TINT = client
                .comment("Teinter la pluie de Where Winds Blow avec la couleur de la brume, et l'éclairer d'or à contre-jour",
                        "quand on regarde vers le soleil.")
                .define("rainTint", true);
        AVERSE_CURTAINS = client
                .comment("Rideaux de pluie : des pans de pluie de 14 à 84 blocs autour du joueur, penchés par le vent,",
                        "qui donnent les averses qu'on voit traverser la vallée. La brume les efface au loin.")
                .define("curtains", true);
        AVERSE_CURTAIN_OPACITY = client
                .comment("Opacité des rideaux de pluie (1 : réglage du pack).")
                .defineInRange("curtainOpacity", 1.0, 0.0, 2.0);
        AVERSE_LIGHTNING = client
                .comment("Dessiner l'éclair de Fallow (trait blanc fin en zigzag, halos, branches) à la place de celui du jeu.")
                .define("lightning", true);
        AVERSE_SKY_FLASH = client
                .comment("À chaque coup de foudre, un bref éclat blanc-bleu sur tout l'écran quand on voit le ciel.")
                .define("skyFlash", true);
        AVERSE_WINDBLOWN_LEAVES = client
                .comment("Les feuilles qui tombent (Particular) suivent le vent de Where Winds Blow ; dans l'orage, le vent",
                        "en arrache bien plus aux arbres et les emporte à l'horizontale.")
                .define("windblownLeaves", true);
        AVERSE_LEAF_REST_SECONDS = client
                .comment("Secondes qu'une feuille tombée reste posée au sol ou flotte sur l'eau avant de s'effacer",
                        "(Particular seul : 5).")
                .defineInRange("leafRestSeconds", 25, 5, 120);
        AVERSE_GRASS_BITS = client
                .comment("Brins d'herbe projetés quand on court dans les hautes herbes ; le vent les emporte.")
                .define("grassBits", true);
        client.pop();

        client.comment("Module rayons (client) : la lumière du soleil dans la brume, sans shader.").push("rayons");
        RAYONS_ENABLED = client
                .comment("Interrupteur du module.")
                .define("enabled", true);
        RAYONS_SHAFTS = client
                .comment("Rayons de soleil qui tombent par les trouées des arbres et au bord des clairières,",
                        "plus visibles quand la brume est épaisse, absents la nuit et sous la pluie.")
                .define("shafts", true);
        RAYONS_SHAFT_STRENGTH = client
                .comment("Intensité des rayons et des éclats sur l'eau (1 : réglage du pack).")
                .defineInRange("shaftStrength", 1.0, 0.0, 3.0);
        RAYONS_GLINTS = client
                .comment("Éclats du soleil sur l'eau : le chemin de lumière qui scintille entre le soleil et le joueur.")
                .define("glints", true);
        client.pop();
        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
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
                "ou faire tourner un dissipateur (64) change les biomes autour en clairière ; le retirer y rend la brume.").push("clairiere");
        CLAIRIERE_ENABLED = common
                .comment("Interrupteur de la conversion (serveur). Faux : les blocs restent, mais ne changent plus rien.")
                .define("enabled", true);
        CLAIRIERE_CHUNKS_PER_TICK = common
                .comment("Tronçons (16 × 16 blocs) convertis par tick. Un dissipateur en touche environ 80.")
                .defineInRange("chunksPerTick", 2, 1, 16);
        CLAIRIERE_RING_TICKS = common
                .comment("La clairière s'ouvre (et se referme) en vague, un anneau de 4 blocs tous les tant de ticks.",
                        "6 : une lanterne (32 blocs) en 2,7 secondes, un dissipateur (64) en 5. 1 : presque d'un coup.")
                .defineInRange("ringTicks", 6, 1, 40);
        common.pop();

        common.comment("Module outils : à mains nues, on ramasse mais on ne coupe pas. Le bois, brut ou travaillé (tag fallow:outil_requis),",
                "et les blocs qui ne lâcheraient rien sans le bon outil (roches, minerais) ne se cassent plus sans lui.").push("outils");
        OUTILS_ENABLED = common
                .comment("Interrupteur du module. Faux : on casse de nouveau tout à la main, comme en vanilla.",
                        "Client et serveur doivent avoir la même valeur.")
                .define("enabled", true);
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

    /** Constante de temps du fondu de brume après un changement de biomes, en secondes ; 0 si coupé. */
    public static float cielClearingFadeSeconds() {
        return cielEnabled() ? (float) read(CLIENT_SPEC, CIEL_CLEARING_FADE) : 0.0F;
    }

    public static boolean cielFriendMoon() {
        return cielEnabled() && read(CLIENT_SPEC, CIEL_FRIEND_MOON);
    }

    public static boolean horizonEnabled() {
        return read(CLIENT_SPEC, HORIZON_ENABLED);
    }

    public static boolean horizonSkipHiddenLods() {
        return horizonEnabled() && read(CLIENT_SPEC, HORIZON_SKIP_HIDDEN_LODS);
    }

    public static int horizonRingMaxDistance() {
        return read(CLIENT_SPEC, HORIZON_RING_MAX_DISTANCE);
    }

    public static boolean titresEnabled() {
        return read(CLIENT_SPEC, TITRES_ENABLED);
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

    public static int clairiereRingTicks() {
        return read(COMMON_SPEC, CLAIRIERE_RING_TICKS);
    }

    public static boolean outilsEnabled() {
        return read(COMMON_SPEC, OUTILS_ENABLED);
    }

    public static boolean averseEnabled() {
        return read(CLIENT_SPEC, AVERSE_ENABLED);
    }

    public static boolean averseRainTint() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_RAIN_TINT);
    }

    public static boolean averseCurtains() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_CURTAINS);
    }

    public static double averseCurtainOpacity() {
        return read(CLIENT_SPEC, AVERSE_CURTAIN_OPACITY);
    }

    public static boolean averseLightning() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_LIGHTNING);
    }

    public static boolean averseSkyFlash() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_SKY_FLASH);
    }

    public static boolean averseWindblownLeaves() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_WINDBLOWN_LEAVES);
    }

    public static int averseLeafRestTicks() {
        return read(CLIENT_SPEC, AVERSE_LEAF_REST_SECONDS) * 20;
    }

    public static boolean averseGrassBits() {
        return averseEnabled() && read(CLIENT_SPEC, AVERSE_GRASS_BITS);
    }

    public static boolean rayonsShafts() {
        return read(CLIENT_SPEC, RAYONS_ENABLED) && read(CLIENT_SPEC, RAYONS_SHAFTS);
    }

    public static double rayonsShaftStrength() {
        return read(CLIENT_SPEC, RAYONS_SHAFT_STRENGTH);
    }

    public static boolean rayonsGlints() {
        return read(CLIENT_SPEC, RAYONS_ENABLED) && read(CLIENT_SPEC, RAYONS_GLINTS);
    }

    /** Lit une valeur sans planter si la config n'est pas encore chargée. */
    static boolean read(ModConfigSpec spec, ModConfigSpec.BooleanValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }

    static int read(ModConfigSpec spec, ModConfigSpec.IntValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }

    static double read(ModConfigSpec spec, ModConfigSpec.DoubleValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }
}
