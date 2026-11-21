package io.github.salvagedisc.fallow.indices;

import io.github.salvagedisc.fallow.Fallow;
import net.neoforged.bus.api.IEventBus;

/**
 * Module indices : les messages de la barre d'action (ceux de Fallow et des autres mods) quittent le dessus de la barre
 * d'inventaire et s'affichent en marge à gauche, en notes discrètes qui glissent puis s'estompent. Client seulement
 * ({@link IndicesClient}).
 */
public final class IndicesModule {
    private IndicesModule() {
    }

    public static void start(IEventBus modBus) {
        IndicesClient.register(modBus);
        Fallow.LOGGER.info("indices : les messages de la barre d'action s'affichent en marge (selon config/fallow-client.toml)");
    }
}
