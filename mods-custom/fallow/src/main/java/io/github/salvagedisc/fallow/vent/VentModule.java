package io.github.salvagedisc.fallow.vent;

import io.github.salvagedisc.fallow.Fallow;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module vent (client) : le vent de Fallow ({@link Wind}), que suivent la fumée des feux de camp
 * ({@link Smoke}), les brins qui filent avec lui sans pack de shaders ({@link WindMotes}) et, sous Iris, le pack de
 * shaders Brumaire (plantes, pluie).
 */
public final class VentModule {
    private VentModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && !minecraft.isPaused()) {
                Wind.tick(minecraft.level, minecraft.gameRenderer.getMainCamera().getPosition());
                WindMotes.tick(minecraft.level, minecraft.gameRenderer.getMainCamera().getPosition());
            }
        });
        Fallow.LOGGER.info("vent : vent du pack et fumée des feux de camp");
    }
}
