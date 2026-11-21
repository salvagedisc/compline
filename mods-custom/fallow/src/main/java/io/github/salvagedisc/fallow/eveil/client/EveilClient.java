package io.github.salvagedisc.fallow.eveil.client;

import io.github.salvagedisc.fallow.eveil.EtatEveil;
import io.github.salvagedisc.fallow.eveil.EveilModule;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * L'infobulle d'un objet qui a commencé à vivre : ses éveils (quoi, quel jour, par qui, de quelle bête), la place qui
 * lui reste, et s'il est prêt à s'éveiller.
 */
public final class EveilClient {
    private EveilClient() {
    }

    public static void start() {
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, EveilClient::onTooltip);
    }

    private static void onTooltip(ItemTooltipEvent event) {
        EtatEveil etat = event.getItemStack().get(EveilModule.ETAT.get());
        if (etat == null) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        for (EtatEveil.Trace trace : etat.traces()) {
            Component nom = nom(trace);
            Component ligne = trace.eclat().isPresent()
                    ? Component.translatable("eveil.fallow.trace.eclat", nom, trace.jour(), trace.par(),
                            Component.translatable(trace.eclat().get()))
                    : Component.translatable("eveil.fallow.trace", nom, trace.jour(), trace.par());
            tooltip.add(ligne.copy().withStyle(ChatFormatting.DARK_AQUA));
        }
        if (etat.pret()) {
            tooltip.add(Component.translatable("eveil.fallow.tooltip.pret").withStyle(ChatFormatting.GOLD));
        } else if (etat.eteint()) {
            tooltip.add(Component.translatable("eveil.fallow.tooltip.eteint").withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("eveil.fallow.tooltip.places", etat.occupees(), etat.places())
                .withStyle(ChatFormatting.GRAY));
    }

    private static Component nom(EtatEveil.Trace trace) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            var holder = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT)
                    .getHolder(ResourceKey.create(Registries.ENCHANTMENT, trace.enchantement()));
            if (holder.isPresent()) {
                return net.minecraft.world.item.enchantment.Enchantment.getFullname(holder.get(), trace.niveau());
            }
        }
        return Component.literal(trace.enchantement().toString());
    }
}
