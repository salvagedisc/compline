package io.github.salvagedisc.compline.patine.client;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.patine.Patine;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Le côté client de la patine : la texture salie de l'armure portée, et une ligne dans l'infobulle. */
public final class PatineClient {
    private PatineClient() {
    }

    public static void start(IEventBus modBus) {
        modBus.addListener(RegisterClientReloadListenersEvent.class,
                event -> event.registerReloadListener((ResourceManagerReloadListener) manager -> PatineTextures.clear()));
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, event -> {
            int level = Patine.level(event.getItemStack());
            if (level > 0 && ComplineConfig.patineEnabled()) {
                event.getToolTip().add(Component.translatable("tooltip.compline.patine." + level).withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
