package io.github.salvagedisc.fallow.averse;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.ModIds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module averse (client) : la pluie, l'orage et le vent dans les feuilles, au niveau de finition des
 * meilleurs packs à shaders, sans shader.
 *
 * <ul>
 *   <li>{@link RainLook} : la pluie de Where Winds Blow prend la couleur de la brume et s'éclaire à
 *       contre-jour (mixin {@code averse.WwbRainMixin}, avec Where Winds Blow).</li>
 *   <li>{@link RainCurtains} : rideaux de pluie au loin.</li>
 *   <li>{@link Bolt} : l'éclair redessiné (mixin {@code averse.LightningBoltRendererMixin}) et l'éclat du ciel.</li>
 *   <li>Feuilles de Particular dans le vent et arrachées par l'orage (mixins {@code averse.LeafParticleMixin}
 *       et {@code averse.LeavesBlockMixin}, avec Particular).</li>
 *   <li>{@link GrassBits} : brins d'herbe projetés en courant dans les hautes herbes.</li>
 * </ul>
 */
public final class AverseModule {
    private AverseModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(AverseModule::onRenderStage);
        NeoForge.EVENT_BUS.addListener(AverseModule::onClientTick);
        NeoForge.EVENT_BUS.addListener(AverseModule::onRenderGui);
        Fallow.LOGGER.info("averse : pluie {}, feuilles {}",
                ModIds.isLoaded(ModIds.WHERE_WINDS_BLOW) ? "de Where Winds Blow teintée" : "du jeu",
                ModIds.isLoaded(ModIds.PARTICULAR) ? "de Particular dans le vent" : "absentes (Particular manque)");
    }

    private static void onRenderStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER || !FallowConfig.averseEnabled()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        RainCurtains.render(level, event.getCamera().getPosition(), partialTick);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            return;
        }
        if (FallowConfig.averseGrassBits()) {
            GrassBits.tick(minecraft.level, minecraft.player);
        }
    }

    /** L'éclat du ciel : un voile blanc-bleu très bref sur l'écran, seulement si l'on voit le ciel. */
    private static void onRenderGui(RenderGuiEvent.Pre event) {
        if (!FallowConfig.averseSkyFlash()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float flash = Bolt.flash(event.getPartialTick().getGameTimeDeltaPartialTick(false));
        if (flash <= 0.0F || minecraft.level == null || minecraft.player == null) {
            return;
        }
        BlockPos eye = BlockPos.containing(minecraft.gameRenderer.getMainCamera().getPosition());
        if (!minecraft.level.canSeeSky(eye)) {
            return;
        }
        int alpha = (int) (flash * 0.35F * 255.0F);
        RenderSystem.enableBlend();
        event.getGuiGraphics().fill(0, 0, event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(),
                (alpha << 24) | 0xE6ECFF);
    }
}
