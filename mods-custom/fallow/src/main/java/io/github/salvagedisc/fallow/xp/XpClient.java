package io.github.salvagedisc.fallow.xp;

import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Cache la barre d'expérience et le chiffre du niveau. Ce sont deux couches de l'interface
 * NeoForge ; la jauge de saut à cheval ({@code JUMP_METER}) en est une autre et reste visible.
 * Chargée seulement chez le client.
 */
final class XpClient {
    private XpClient() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Pre.class, XpClient::onRenderLayer);
    }

    private static void onRenderLayer(RenderGuiLayerEvent.Pre event) {
        ResourceLocation name = event.getName();
        if ((VanillaGuiLayers.EXPERIENCE_BAR.equals(name) || VanillaGuiLayers.EXPERIENCE_LEVEL.equals(name))
                && FallowConfig.xpHideBar()) {
            event.setCanceled(true);
        }
    }
}
