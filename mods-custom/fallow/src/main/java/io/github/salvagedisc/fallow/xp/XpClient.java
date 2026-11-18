package io.github.salvagedisc.fallow.xp;

import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Cache la barre d'expérience et le chiffre du niveau. Ce sont deux couches de l'interface
 * NeoForge ; la jauge de saut à cheval ({@code JUMP_METER}) en est une autre et reste visible.
 * La place laissée libre est reprise : la vie, l'armure, la faim, l'air et tout ce que les mods
 * empilent au-dessus (la soif) descendent de 5 pixels, sauf quand la jauge de saut est affichée.
 * Chargée seulement chez le client.
 */
final class XpClient {
    /** Hauteur de départ des barres au-dessus de la barre d'action, dans le jeu de base. */
    private static final int VANILLA_HEIGHT = 39;
    /** Sans barre d'expérience : un pixel reste entre les cœurs et le cadre de la case choisie. */
    private static final int LOWERED_HEIGHT = 34;

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
            if (VanillaGuiLayers.EXPERIENCE_BAR.equals(name)) {
                lowerBars();
            }
        }
    }

    /**
     * Les hauteurs sont remises à 39 au début de chaque image, avant les couches ; celle de
     * l'expérience passe juste avant la vie et la faim, qui les lisent.
     */
    private static void lowerBars() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.player.jumpableVehicle() != null) {
            return;
        }
        Gui gui = minecraft.gui;
        if (gui.leftHeight == VANILLA_HEIGHT) {
            gui.leftHeight = LOWERED_HEIGHT;
        }
        if (gui.rightHeight == VANILLA_HEIGHT) {
            gui.rightHeight = LOWERED_HEIGHT;
        }
    }
}
