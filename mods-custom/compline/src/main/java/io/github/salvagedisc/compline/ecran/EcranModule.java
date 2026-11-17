package io.github.salvagedisc.compline.ecran;

import io.github.salvagedisc.compline.Compline;
import net.neoforged.bus.api.IEventBus;

/**
 * Module écran : l'interface s'efface quand on ne fait rien. La barre d'action, la vie, la faim, l'armure, l'air et
 * les jauges que les mods posent à côté disparaissent en fondu après quelques secondes sans geste, et reviennent dès
 * qu'on change d'objet, qu'on frappe ou qu'on utilise quelque chose, qu'on prend un coup ou qu'une valeur bouge. Elles
 * restent affichées quand la vie ou la faim sont basses et sous l'eau. Le viseur, le chat, les titres et les effets
 * restent toujours visibles. Client seulement ({@link EcranClient}).
 */
public final class EcranModule {
    private EcranModule() {
    }

    public static void start(IEventBus modBus) {
        EcranClient.register();
        Compline.LOGGER.info("ecran : l'interface s'efface au repos (selon config/compline-client.toml)");
    }
}
