package io.github.salvagedisc.compline.api;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.ciel.FogCache;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * API publique du module ciel, faite pour être appelée depuis un script client KubeJS :
 * <pre>{@code
 * const ComplineSky = Java.loadClass('io.github.salvagedisc.compline.api.ComplineSky')
 * ComplineSky.clearFogCache()
 * }</pre>
 * Sans danger quand ATMOSPHERICS manque ou côté serveur : la méthode ne fait alors rien.
 */
public final class ComplineSky {
    private ComplineSky() {
    }

    /**
     * Vide le cache de brume d'ATMOSPHERICS, pour que la brume suive tout de suite un changement de
     * biome autour d'un joueur immobile. Compline le fait déjà seul à chaque mise à jour de biomes
     * envoyée par le serveur (option {@code ciel.clearFogOnBiomeUpdate}).
     *
     * @return vrai si le cache a été vidé (ou le sera au prochain tick client), faux si c'est impossible ici
     */
    public static boolean clearFogCache() {
        if (!isFogCacheAvailable()) {
            return false;
        }
        FogCache.clear();
        return true;
    }

    /** Vrai côté client, avec ATMOSPHERICS présent et le module ciel actif. */
    public static boolean isFogCacheAvailable() {
        return FMLEnvironment.dist.isClient() && ModIds.isLoaded(ModIds.ATMOSPHERICS) && ComplineConfig.cielEnabled();
    }
}
