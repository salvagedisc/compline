package io.github.salvagedisc.tilth.rayons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module rayons (client) : {@link Glints}, des éclats de soleil qui scintillent sur l'eau, là où elle
 * renvoie le soleil vers l'œil.
 *
 * <p>Les rayons de soleil entre les feuillages venaient d'un pack de ressources maison, retiré
 * (décision 0024). Les éclats restent en Java : il faut savoir où est l'eau.
 */
public final class RayonsModule {
    private RayonsModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RayonsModule::onClientTick);
        NeoForge.EVENT_BUS.addListener(RayonsModule::onRenderStage);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        Glints.scan(minecraft.level, minecraft.player.blockPosition());
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Glints.render(level, event.getCamera().getPosition(), partialTick);
    }
}
