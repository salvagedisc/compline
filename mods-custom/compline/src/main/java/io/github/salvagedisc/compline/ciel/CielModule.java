package io.github.salvagedisc.compline.ciel;

import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ModIds;
import net.neoforged.bus.api.IEventBus;

/**
 * Module ciel (client). ATMOSPHERICS annule le {@code renderSky} vanilla et dessine son propre ciel,
 * avec la lune vanilla : tout ce qu'Enhanced Celestials injecte dans {@code renderSky} est perdu.
 *
 * <ul>
 *   <li>La lune : {@link MoonOverlay} cache la lune vanilla d'ATMOSPHERICS et dessine celle
 *       d'Enhanced Celestials juste après le ciel (mixins {@code ciel.LevelRendererMixin} et
 *       {@code ciel.RenderSystemMixin}, appliqués seulement si les deux mods sont là).</li>
 *   <li>La brume : {@link FogCache} vide le cache de biomes d'ATMOSPHERICS ; appelé tout seul quand le
 *       serveur change des biomes, et ouvert aux scripts par {@link io.github.salvagedisc.compline.api.ComplineSky}.</li>
 * </ul>
 *
 * Tout passe par des mixins : le module n'a rien à enregistrer, il dit seulement ce qui est actif.
 */
public final class CielModule {
    private CielModule() {
    }

    public static void start(IEventBus modBus) {
        if (ModIds.isLoaded(ModIds.ENHANCED_CELESTIALS)) {
            Compline.LOGGER.info("ciel : lune d'Enhanced Celestials redessinée sous ATMOSPHERICS");
        } else {
            Compline.LOGGER.info("ciel : Enhanced Celestials absent, seule la remise à zéro de la brume est active");
        }
    }
}
