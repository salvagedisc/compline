package io.github.salvagedisc.fallow.ciel;

import io.github.salvagedisc.fallow.mixin.ciel.BiomeBlendUtilInvoker;
import net.minecraft.client.Minecraft;

/**
 * Le cache de brume d'ATMOSPHERICS. {@code BiomeBlendUtil.collect} garde les biomes échantillonnés
 * autour du joueur tant qu'il reste dans la même cellule de 4 blocs, et ne les relit pas quand un
 * biome change : après un {@code /fillbiome}, la brume ne bouge que quand le joueur change de cellule.
 * Le vider force une nouvelle lecture à l'image suivante ; la transition reste lissée par ATMOSPHERICS.
 *
 * <p>Ne charger cette classe que si ATMOSPHERICS est présent.
 */
public final class FogCache {
    private FogCache() {
    }

    /** Vide le cache, sur le fil de rendu (repoussé à la prochaine tâche du client sinon). */
    public static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            BiomeBlendUtilInvoker.fallow$resetCache();
        } else {
            minecraft.execute(BiomeBlendUtilInvoker::fallow$resetCache);
        }
    }
}
