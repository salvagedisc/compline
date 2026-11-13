package io.github.salvagedisc.tilth.api;

import io.github.salvagedisc.tilth.TilthConfig;
import io.github.salvagedisc.tilth.ModIds;
import io.github.salvagedisc.tilth.ciel.FogCache;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * API publique du module ciel, faite pour être appelée depuis un script client KubeJS :
 * <pre>{@code
 * const TilthSky = Java.loadClass('io.github.salvagedisc.tilth.api.TilthSky')
 * TilthSky.clearFogCache()
 * }</pre>
 * Sans danger quand ATMOSPHERICS manque ou côté serveur : la méthode ne fait alors rien.
 */
public final class TilthSky {
    private TilthSky() {
    }

    /**
     * Vide le cache de brume d'ATMOSPHERICS, pour que la brume suive tout de suite un changement de
     * biome autour d'un joueur immobile. Tilth le fait déjà seul à chaque mise à jour de biomes
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
        return FMLEnvironment.dist.isClient() && ModIds.isLoaded(ModIds.ATMOSPHERICS) && TilthConfig.cielEnabled();
    }
}
