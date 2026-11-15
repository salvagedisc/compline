package io.github.salvagedisc.compline;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration de Compline : un booléen par module, plus quelques réglages.
 * {@code config/compline-client.toml} pour les modules client, {@code config/compline-common.toml} pour les autres.
 * Les valeurs sont relues à chaque usage : couper un module en jeu prend effet tout de suite.
 */
public final class ComplineConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;

    private static final ModConfigSpec.BooleanValue CIEL_ENABLED;
    private static final ModConfigSpec.BooleanValue CIEL_REDRAW_MOON;
    private static final ModConfigSpec.BooleanValue CIEL_CLEAR_FOG_ON_BIOME_UPDATE;
    private static final ModConfigSpec.BooleanValue HORIZON_ENABLED;
    private static final ModConfigSpec.BooleanValue HORIZON_SKIP_HIDDEN_LODS;
    private static final ModConfigSpec.IntValue HORIZON_RING_MAX_DISTANCE;
    private static final ModConfigSpec.BooleanValue XP_ENABLED;
    private static final ModConfigSpec.BooleanValue XP_HIDE_BAR;
    private static final ModConfigSpec.BooleanValue XP_BLOCK_ENCHANT_MERGE;
    private static final ModConfigSpec.BooleanValue CLAIRIERE_ENABLED;
    private static final ModConfigSpec.IntValue CLAIRIERE_RING_TICKS;
    private static final ModConfigSpec.DoubleValue CIEL_CLEARING_FADE;
    private static final ModConfigSpec.BooleanValue OUTILS_ENABLED;
    private static final ModConfigSpec.BooleanValue RENDU_ENABLED;
    private static final ModConfigSpec.BooleanValue VENT_ENABLED;
    private static final ModConfigSpec.BooleanValue FAUNE_ENABLED;
    private static final ModConfigSpec.BooleanValue FAUNE_WARDEN;
    private static final ModConfigSpec.BooleanValue FAUNE_MOOSE;
    private static final ModConfigSpec.BooleanValue BORD_ENABLED;
    private static final ModConfigSpec.BooleanValue ZOO_ENABLED;
    private static final ModConfigSpec.IntValue ZOO_SPOT_DISTANCE;
    private static final ModConfigSpec.IntValue ZOO_OBSERVE_SECONDS;

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        client.comment("Module ciel (client) : il relie ATMOSPHERICS et Enhanced Celestials.").push("ciel");
        CIEL_ENABLED = client
                .comment("Interrupteur du module. Faux : Compline ne touche plus au ciel.")
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

        client.comment("Module rendu (client) : sous Iris, il donne au pack de shaders Brumaire les clairières, le sol et le vent du pack.").push("rendu");
        RENDU_ENABLED = client
                .comment("Interrupteur du module. Vrai : la brume du pack de shaders se lève dans les clairières et ses plantes",
                        "ondulent au vent du pack. Faux : le pack de shaders garde une brume uniforme et son vent de repli.",
                        "Sans effet sans shader, ou avec un autre pack de shaders.")
                .define("enabled", true);
        client.pop();

        client.comment("Module vent (client) : le vent du pack, que suivent la fumée des feux de camp et les plantes du pack de shaders.").push("vent");
        VENT_ENABLED = client
                .comment("Interrupteur de la fumée des feux de camp. Vrai : elle suit le vent, les feux voisins font un seul",
                        "panache, le feu des âmes bleuit sa fumée. Faux : la fumée du jeu.")
                .define("enabled", true);
        client.pop();

        client.comment("Module faune (client) : des créatures redessinées et réanimées par Compline (GeckoLib), à la place",
                "de leur modèle d'origine. La créature garde son IA, ses sons et sa boîte de collision ; seul son rendu change.").push("faune");
        FAUNE_ENABLED = client
                .comment("Interrupteur du module. Faux : toutes les créatures reprennent leur modèle d'origine, tout de suite.")
                .define("enabled", true);
        FAUNE_WARDEN = client
                .comment("Le Warden de Compline (vrai) ou celui du jeu (faux).")
                .define("warden", true);
        FAUNE_MOOSE = client
                .comment("L'élan de Compline (vrai) ou celui d'Alex's Mobs (faux). Demande Alex's Mobs.")
                .define("moose", true);
        client.pop();


        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        common.comment("Module xp : Compline n'a plus d'expérience. Les orbes et les gains sont coupés par",
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
                "ou faire tourner un dissipateur (64) lève la brume autour ; le retirer la rend. Le biome ne change pas :",
                "climat, saisons, cultures et faune restent ceux du lieu ; seuls les monstres n'y apparaissent plus.").push("clairiere");
        CLAIRIERE_ENABLED = common
                .comment("Interrupteur des clairières (serveur). Faux : les blocs restent, mais ne lèvent plus la brume.")
                .define("enabled", true);
        CLAIRIERE_RING_TICKS = common
                .comment("La clairière s'ouvre (et se referme) en vague, un anneau de 4 blocs tous les tant de ticks.",
                        "6 : une lanterne (32 blocs) en 2,7 secondes, un dissipateur (64) en 5. 1 : presque d'un coup.")
                .defineInRange("ringTicks", 6, 1, 40);
        common.pop();

        common.comment("Module outils : à mains nues, on ramasse mais on ne coupe pas. Le bois, brut ou travaillé (tag compline:outil_requis),",
                "et les blocs qui ne lâcheraient rien sans le bon outil (roches, minerais) ne se cassent plus sans lui.").push("outils");
        OUTILS_ENABLED = common
                .comment("Interrupteur du module. Faux : on casse de nouveau tout à la main, comme en vanilla.",
                        "Client et serveur doivent avoir la même valeur.")
                .define("enabled", true);
        common.pop();

        common.comment("Module bord : le monde est un disque. Le cercle de data/compline/monde/bord.json (centre et rayon, écrit par",
                "scripts/terrain/disc.py) ne se franchit pas : le joueur, sa monture ou son bateau sont ramenés en deçà.").push("bord");
        BORD_ENABLED = common
                .comment("Interrupteur du module (serveur). Faux : plus de bord, on peut naviguer sans fin sur l'océan.")
                .define("enabled", true);
        common.pop();

        common.comment("Module zoo : les quatre tampons du guide d'Archie's Dinosaurs pour toutes les bêtes du monde (Apercevoir,",
                "Photographier, Apprivoiser ou héberger, Maîtriser), sans jamais tuer. Les tampons restent enregistrés quand le module est coupé.").push("zoo");
        ZOO_ENABLED = common
                .comment("Interrupteur du module (serveur). Faux : plus aucun tampon n'est donné.")
                .define("enabled", true);
        ZOO_SPOT_DISTANCE = common
                .comment("Distance en blocs à laquelle une bête en vue (champ de vision, sans obstacle) vaut le tampon Apercevoir.",
                        "25 : la règle des dinosaures d'Archie.")
                .defineInRange("spotDistance", 25, 4, 64);
        ZOO_OBSERVE_SECONDS = common
                .comment("Secondes d'observation (une bête de l'espèce en vue à moins de 16 blocs) demandées par le tampon Maîtriser,",
                        "en plus des autres tampons de l'espèce.")
                .defineInRange("observeSeconds", 60, 0, 600);
        common.pop();
        COMMON_SPEC = common.build();
    }

    private ComplineConfig() {
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

    public static boolean horizonEnabled() {
        return read(CLIENT_SPEC, HORIZON_ENABLED);
    }

    public static boolean horizonSkipHiddenLods() {
        return horizonEnabled() && read(CLIENT_SPEC, HORIZON_SKIP_HIDDEN_LODS);
    }

    public static int horizonRingMaxDistance() {
        return read(CLIENT_SPEC, HORIZON_RING_MAX_DISTANCE);
    }


    public static boolean fauneWarden() {
        return read(CLIENT_SPEC, FAUNE_ENABLED) && read(CLIENT_SPEC, FAUNE_WARDEN);
    }

    public static boolean fauneMoose() {
        return read(CLIENT_SPEC, FAUNE_ENABLED) && read(CLIENT_SPEC, FAUNE_MOOSE);
    }

    public static boolean ventEnabled() {
        return read(CLIENT_SPEC, VENT_ENABLED);
    }

    public static boolean renduEnabled() {
        return read(CLIENT_SPEC, RENDU_ENABLED);
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

    public static int clairiereRingTicks() {
        return read(COMMON_SPEC, CLAIRIERE_RING_TICKS);
    }

    public static boolean outilsEnabled() {
        return read(COMMON_SPEC, OUTILS_ENABLED);
    }

    public static boolean bordEnabled() {
        return read(COMMON_SPEC, BORD_ENABLED);
    }

    public static boolean zooEnabled() {
        return read(COMMON_SPEC, ZOO_ENABLED);
    }

    public static int zooSpotDistance() {
        return read(COMMON_SPEC, ZOO_SPOT_DISTANCE);
    }

    public static int zooObserveSeconds() {
        return read(COMMON_SPEC, ZOO_OBSERVE_SECONDS);
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
