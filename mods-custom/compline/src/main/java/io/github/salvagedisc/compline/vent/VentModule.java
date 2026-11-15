package io.github.salvagedisc.compline.vent;

import io.github.salvagedisc.compline.Compline;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module vent (client) : le vent de Compline ({@link Wind}), que suivent la fumée des feux de camp
 * ({@link Smoke}) et, sous Iris, le pack de shaders Brumaire (plantes, pluie).
 */
public final class VentModule {
    private VentModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && !minecraft.isPaused()) {
                Wind.tick(minecraft.level, minecraft.gameRenderer.getMainCamera().getPosition());
            }
        });
        Compline.LOGGER.info("vent : vent du pack et fumée des feux de camp");
    }
}
