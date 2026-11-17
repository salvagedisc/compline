package io.github.salvagedisc.compline.patine.client;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.patine.Coat;
import io.github.salvagedisc.compline.patine.Patine;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Le côté client de la patine : la texture salie de l'armure portée, et ses lignes dans l'infobulle. */
public final class PatineClient {
    private PatineClient() {
    }

    public static void start(IEventBus modBus) {
        modBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener((ResourceManagerReloadListener) manager -> PatineTextures.clear()));
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> {
            if (!ComplineConfig.patineEnabled()) {
                return;
            }
            int level = Patine.level(event.getItemStack());
            if (level > 0) {
                event.getToolTip().add(Component.translatable("tooltip.compline.patine." + level).withStyle(ChatFormatting.GRAY));
            }
            Coat coat = Patine.coat(event.getItemStack());
            if (coat.sandLevel() > 0) {
                event.getToolTip().add(Component.translatable("tooltip.compline.patine.sand").withStyle(ChatFormatting.GRAY));
            }
            if (coat.snowLevel() > 0) {
                event.getToolTip().add(Component.translatable("tooltip.compline.patine.snow").withStyle(ChatFormatting.GRAY));
            }
            if (coat.wetLevel() > 0) {
                event.getToolTip().add(Component.translatable("tooltip.compline.patine.wet").withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
