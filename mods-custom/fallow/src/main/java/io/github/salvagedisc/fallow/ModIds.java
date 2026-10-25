package io.github.salvagedisc.fallow;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

/** Identifiants des mods que Fallow relie, et leur détection. */
public final class ModIds {
    public static final String ATMOSPHERICS = "atmospherics";
    public static final String ENHANCED_CELESTIALS = "enhancedcelestials2core";
    public static final String TRAVELERS_BACKPACK = "travelersbackpack";
    public static final String THIRST = "thirst";
    public static final String DISTANT_HORIZONS = "distanthorizons";
    public static final String CHUNKY = "chunky";
    public static final String CREATE = "create";

    private ModIds() {
    }

    /**
     * Vrai si le mod est présent. Marche aussi avant la construction des mods (plugin de mixins),
     * où {@link ModList} n'existe pas encore : on lit alors la liste de chargement de FML.
     */
    public static boolean isLoaded(String modId) {
        ModList modList = ModList.get();
        if (modList != null) {
            return modList.isLoaded(modId);
        }
        LoadingModList loading = LoadingModList.get();
        return loading != null && loading.getModFileById(modId) != null;
    }
}
