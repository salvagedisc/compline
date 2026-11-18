package io.github.salvagedisc.fallow.horizon;

import io.github.salvagedisc.fallow.Fallow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Module horizon : il relie Distant Horizons à la brume du pack.
 *
 * <ul>
 *   <li>Client : {@link HorizonFog} recopie à chaque image la brume du terrain (celle d'ATMOSPHERICS) dans
 *       le brouillard lointain de Distant Horizons. Dans la brume, les LOD restent cachés et ne sont même pas
 *       dessinés ; là où la brume se lève (clairières), le paysage lointain apparaît avec la même courbe que
 *       le terrain proche, puis s'efface dans le ciel au cercle de brume. Le mixin {@code horizon.FogRendererMixin}
 *       relève la brume du terrain ; {@code horizon.FarFadeMixin} place le cercle ; {@code horizon.VanillaFadeMixin}
 *       coupe le fondu terrain-LOD quand les LOD ne sont pas dessinés.</li>
 *   <li>Client : {@code horizon.GLStateMixin} corrige la lecture de {@code GL_POLYGON_MODE} de Distant Horizons
 *       3.3.3, qui déborde de sa mémoire sur les Mac Apple Silicon (plantages aléatoires).</li>
 *   <li>Client et serveur : {@code horizon.ChunkyAccessorMixin} empêche Distant Horizons de planter au
 *       chargement du monde quand Chunky n'a pas encore démarré.</li>
 * </ul>
 */
public final class HorizonModule {
    private HorizonModule() {
    }

    public static void start(IEventBus modBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            HorizonClient.start();
        } else {
            Fallow.LOGGER.info("horizon : serveur, seul le correctif Chunky est actif");
        }
    }
}
