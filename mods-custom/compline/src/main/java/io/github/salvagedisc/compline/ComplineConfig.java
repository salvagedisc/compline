package io.github.salvagedisc.compline;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
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
    private static final ModConfigSpec.BooleanValue BORD_ENABLED;
    private static final ModConfigSpec.BooleanValue ZOO_ENABLED;
    private static final ModConfigSpec.IntValue ZOO_SPOT_DISTANCE;
    private static final ModConfigSpec.IntValue ZOO_OBSERVE_SECONDS;
    private static final ModConfigSpec.IntValue ZOO_SPYGLASS_DISTANCE;
    private static final ModConfigSpec.BooleanValue APPROCHE_ENABLED;
    private static final ModConfigSpec.DoubleValue APPROCHE_SCENT_RANGE;
    private static final ModConfigSpec.DoubleValue APPROCHE_SIGHT_RANGE;
    private static final ModConfigSpec.DoubleValue APPROCHE_HEARING_RANGE;
    private static final ModConfigSpec.BooleanValue APPROCHE_HINTS;
    private static final ModConfigSpec.BooleanValue REPOS_ENABLED;
    private static final ModConfigSpec.IntValue REPOS_REST_SECONDS;
    private static final ModConfigSpec.DoubleValue REPOS_BASE_MINUTES;
    private static final ModConfigSpec.DoubleValue REPOS_MINUTES_PER_COMFORT;
    private static final ModConfigSpec.BooleanValue PATINE_ENABLED;
    private static final ModConfigSpec.IntValue PATINE_MINUTES;
    private static final ModConfigSpec.BooleanValue CLIMAT_ENABLED;
    private static final ModConfigSpec.DoubleValue CLIMAT_SEASON_STRENGTH;
    private static final ModConfigSpec.DoubleValue CLIMAT_SNOW_COLD;
    private static final ModConfigSpec.DoubleValue CLIMAT_SNOW_WET;
    private static final ModConfigSpec.DoubleValue CLIMAT_SAND_THIRST;
    private static final ModConfigSpec.BooleanValue CLIMAT_STORM_VISUALS;
    private static final ModConfigSpec.BooleanValue ECRAN_ENABLED;
    private static final ModConfigSpec.IntValue ECRAN_IDLE_SECONDS;
    private static final ModConfigSpec.BooleanValue ECRAN_SHOW_WHILE_MOVING;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> ECRAN_LAYERS;

    /** La barre d'action et ce qui l'entoure : couches du jeu, air du scaphandre de Create, température du corps de Cold Sweat. La soif de Thirst est dessinée dans la couche de la faim. */
    private static final List<String> ECRAN_DEFAULT_LAYERS = List.of(
            "minecraft:hotbar", "minecraft:jump_meter", "minecraft:experience_bar", "minecraft:player_health",
            "minecraft:armor_level", "minecraft:food_level", "minecraft:vehicle_health", "minecraft:air_level",
            "minecraft:selected_item_name", "minecraft:experience_level",
            "create:remaining_air", "cold_sweat:body_temp", "cold_sweat:vague_temp");

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

        client.comment("Module ecran (client) : l'interface s'efface quand on ne fait rien.").push("ecran");
        ECRAN_ENABLED = client
                .comment("Interrupteur du module. Vrai : après quelques secondes sans geste, la barre d'action, la vie, la faim,",
                        "l'armure et les jauges voisines s'effacent en fondu ; elles reviennent dès qu'on change d'objet, frappe,",
                        "utilise, prend un coup ou qu'une valeur bouge, et restent quand la vie ou la faim sont basses et sous l'eau.",
                        "Faux : l'interface du jeu, toujours affichée.")
                .define("enabled", true);
        ECRAN_IDLE_SECONDS = client
                .comment("Secondes sans geste avant que l'interface s'efface.")
                .defineInRange("idleSeconds", 6, 1, 120);
        ECRAN_SHOW_WHILE_MOVING = client
                .comment("Marcher, nager ou sauter garde aussi l'interface. Faux : elle s'efface même en marche, comme au repos.")
                .define("showWhileMoving", false);
        ECRAN_LAYERS = client
                .comment("Couches de l'interface qui s'effacent (noms NeoForge des couches). Le viseur, le chat, les titres,",
                        "les effets et les messages ne sont pas dans la liste et restent toujours visibles.")
                .defineListAllowEmpty("layers", ECRAN_DEFAULT_LAYERS, () -> "minecraft:hotbar",
                        o -> o instanceof String text && ResourceLocation.tryParse(text) != null);
        client.pop();

        client.comment("Module climat (client) : ce qu'on voit des tempêtes.").push("climat");
        CLIMAT_STORM_VISUALS = client
                .comment("Vrai : dans une tempête de sable, la poussière vole avec le vent du pack et la brume se ferme et prend la",
                        "couleur du sable ; dans une tempête de neige, la neige est soufflée et la brume blanchit. Faux : rien de plus",
                        "que la neige du jeu (le froid et la soif restent).")
                .define("stormVisuals", true);
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
        ZOO_SPYGLASS_DISTANCE = common
                .comment("Distance en blocs à laquelle une bête vue dans la longue-vue (au centre de l'image) vaut le tampon Apercevoir.")
                .defineInRange("spyglassDistance", 64, 4, 128);
        common.pop();

        common.comment("Module approche : les bêtes farouches (tag compline:farouches) sentent le joueur quand le vent porte son",
                "odeur vers elles, l'entendent quand il court et le voient quand il se tient debout à découvert ; elles lèvent",
                "la tête, puis fuient. Approcher contre le vent et accroupi permet de s'en approcher.").push("approche");
        APPROCHE_ENABLED = common
                .comment("Interrupteur du module (serveur). Faux : les bêtes ne se soucient plus du vent ni de la posture.")
                .define("enabled", true);
        APPROCHE_SCENT_RANGE = common
                .comment("Portée de l'odeur en blocs, sous le vent, par vent moyen. Le vent fort la porte jusqu'à 30 % plus loin,",
                        "la pluie l'abrège ; hors du cône de 70° autour du vent, aucune odeur.")
                .defineInRange("scentRange", 32.0, 0.0, 96.0);
        APPROCHE_SIGHT_RANGE = common
                .comment("Distance en blocs à laquelle une bête voit un joueur debout, sans obstacle. Accroupi : 45 % ;",
                        "en courant : 130 % ; dans l'ombre ou la nuit : 60 %.")
                .defineInRange("sightRange", 14.0, 0.0, 64.0);
        APPROCHE_HEARING_RANGE = common
                .comment("Distance en blocs à laquelle une bête entend un joueur qui marche. En courant : le double ; accroupi : 30 %.")
                .defineInRange("hearingRange", 6.0, 0.0, 32.0);
        APPROCHE_HINTS = common
                .comment("Vrai : quand une bête fuit, un mot dans la barre d'action dit ce qui a trahi le joueur (au plus une",
                        "fois toutes les 90 secondes).")
                .define("hints", true);
        common.pop();

        common.comment("Module repos : immobile ou assis près d'un feu allumé (5 blocs), sous un toit, le joueur se repose et",
                "gagne l'effet « Reposé » : un demi-cœur toutes les 6 secondes et un quart de faim en moins. L'effet dure",
                "d'autant plus que la maison est variée (familles de confort, tags compline:confort/*, à 8 blocs).").push("repos");
        REPOS_ENABLED = common
                .comment("Interrupteur du module (serveur). Faux : plus de repos ; un effet en cours va à son terme sans réduire la faim.")
                .define("enabled", true);
        REPOS_REST_SECONDS = common
                .comment("Secondes de repos au foyer avant de gagner l'effet.")
                .defineInRange("restSeconds", 20, 0, 300);
        REPOS_BASE_MINUTES = common
                .comment("Durée de l'effet sans aucun confort, en minutes.")
                .defineInRange("baseMinutes", 2.0, 0.5, 60.0);
        REPOS_MINUTES_PER_COMFORT = common
                .comment("Minutes ajoutées par famille de confort trouvée (lit, siège, table, tapis, rangement, livres, lumière,",
                        "décoration, cuisine). Avec les 9 : 2 + 9 × 1,5 = 15,5 minutes.")
                .defineInRange("minutesPerComfort", 1.5, 0.0, 10.0);
        common.pop();

        common.comment("Module patine : les armures portées se salissent (marche, terre, combat) et se lavent dans l'eau ; la pluie",
                "les rince lentement. La saleté reste sur l'objet.").push("patine");
        PATINE_ENABLED = common
                .comment("Interrupteur du module. Faux : les armures ne se salissent plus et s'affichent propres.")
                .define("enabled", true);
        PATINE_MINUTES = common
                .comment("Minutes d'activité (marche, sans boue ni combat) pour qu'une cuirasse soit couverte de boue.",
                        "Les bottes vont plus vite, le casque plus lentement.")
                .defineInRange("minutesToFull", 40, 5, 600);
        common.pop();

        common.comment("Module climat : le climat pèse sur le corps (Cold Sweat). La saison d'Ecliptic Seasons réchauffe ou refroidit",
                "chaque biome comme Ecliptic le dit pour ses cultures et sa neige : l'hiver mord, l'été écrase.").push("climat");
        CLIMAT_ENABLED = common
                .comment("Interrupteur du module (serveur). Faux : la saison ne change plus la température ressentie.")
                .define("enabled", true);
        CLIMAT_SEASON_STRENGTH = common
                .comment("Force de la saison : l'écart de température d'Ecliptic (en unités de biome, -0,45 au cœur de l'hiver,",
                        "+0,25 au cœur de l'été dans un biome tempéré) multiplié par ce nombre, puis compté en unités de Cold Sweat",
                        "(1 = 25 °C). 1,5 : l'hiver retire environ 15 à 17 °C, l'été ajoute environ 9 °C. 0 : aucune saison.")
                .defineInRange("seasonStrength", 1.5, 0.0, 4.0);
        CLIMAT_SNOW_COLD = common
                .comment("Froid de la neige qui tombe sur soi, à découvert, en unités de Cold Sweat (1 = 25 °C). 0,12 : environ 3 °C",
                        "sous une neige simple ; deux fois et demie plus (environ 7,5 °C) sous une tempête de neige (neige et orage).")
                .defineInRange("snowCold", 0.12, 0.0, 1.0);
        CLIMAT_SNOW_WET = common
                .comment("Froid de la neige posée sur les vêtements quand on en est couvert (elle se pose en une minute et demie",
                        "de neige, une demi-minute de tempête, et fond à l'abri). 0,2 : environ 5 °C.")
                .defineInRange("snowWet", 0.2, 0.0, 1.0);
        CLIMAT_SAND_THIRST = common
                .comment("Fatigue de soif (Thirst Was Reclaimed) ajoutée chaque seconde à découvert dans une tempête de sable sous",
                        "l'orage, 60 % sans orage. 4 de fatigue font une goutte : 0,15, une goutte toutes les 27 secondes en plus de la",
                        "soif ordinaire. 0 : le sable n'assoiffe pas.")
                .defineInRange("sandThirst", 0.15, 0.0, 4.0);
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

    public static boolean patineEnabled() {
        return read(COMMON_SPEC, PATINE_ENABLED);
    }

    public static int patineMinutesToFull() {
        return read(COMMON_SPEC, PATINE_MINUTES);
    }

    public static int zooObserveSeconds() {
        return read(COMMON_SPEC, ZOO_OBSERVE_SECONDS);
    }

    public static int zooSpyglassDistance() {
        return read(COMMON_SPEC, ZOO_SPYGLASS_DISTANCE);
    }

    public static boolean approcheEnabled() {
        return read(COMMON_SPEC, APPROCHE_ENABLED);
    }

    public static double approcheScentRange() {
        return read(COMMON_SPEC, APPROCHE_SCENT_RANGE);
    }

    public static double approcheSightRange() {
        return read(COMMON_SPEC, APPROCHE_SIGHT_RANGE);
    }

    public static double approcheHearingRange() {
        return read(COMMON_SPEC, APPROCHE_HEARING_RANGE);
    }

    public static boolean approcheHints() {
        return read(COMMON_SPEC, APPROCHE_HINTS);
    }

    public static boolean reposEnabled() {
        return read(COMMON_SPEC, REPOS_ENABLED);
    }

    public static int reposRestSeconds() {
        return read(COMMON_SPEC, REPOS_REST_SECONDS);
    }

    public static double reposBaseMinutes() {
        return read(COMMON_SPEC, REPOS_BASE_MINUTES);
    }

    public static double reposMinutesPerComfort() {
        return read(COMMON_SPEC, REPOS_MINUTES_PER_COMFORT);
    }

    public static boolean climatEnabled() {
        return read(COMMON_SPEC, CLIMAT_ENABLED);
    }

    public static double climatSeasonStrength() {
        return climatEnabled() ? read(COMMON_SPEC, CLIMAT_SEASON_STRENGTH) : 0.0;
    }

    public static double climatSnowCold() {
        return read(COMMON_SPEC, CLIMAT_SNOW_COLD);
    }

    public static double climatSnowWet() {
        return read(COMMON_SPEC, CLIMAT_SNOW_WET);
    }

    public static double climatSandThirst() {
        return read(COMMON_SPEC, CLIMAT_SAND_THIRST);
    }

    public static boolean climatStormVisuals() {
        return read(CLIENT_SPEC, CLIMAT_STORM_VISUALS);
    }

    public static boolean ecranEnabled() {
        return read(CLIENT_SPEC, ECRAN_ENABLED);
    }

    public static int ecranIdleSeconds() {
        return read(CLIENT_SPEC, ECRAN_IDLE_SECONDS);
    }

    public static boolean ecranShowWhileMoving() {
        return read(CLIENT_SPEC, ECRAN_SHOW_WHILE_MOVING);
    }

    public static List<? extends String> ecranLayers() {
        return CLIENT_SPEC.isLoaded() ? ECRAN_LAYERS.get() : ECRAN_DEFAULT_LAYERS;
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
