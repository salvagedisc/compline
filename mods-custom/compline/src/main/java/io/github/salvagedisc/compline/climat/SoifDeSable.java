package io.github.salvagedisc.compline.climat;

import cn.mlus.thirst.foundation.common.capability.IThirst;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ComplineConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Le vent de sable assoiffe : à découvert dans une tempête de sable, Thirst Was Reclaimed reçoit de la fatigue en
 * plus, chaque seconde ({@link ComplineConfig#climatSandThirst()} sous l'orage, 60 % sans) : 4 de fatigue font une
 * goutte de soif. La soif ordinaire de Thirst continue à côté, plus forte quand le corps a chaud.
 *
 * <p>Ne charger cette classe que si Thirst Was Reclaimed est présent.
 */
final class SoifDeSable {
    private static final ResourceLocation THIRST = ResourceLocation.fromNamespaceAndPath("thirst", "player_thirst");
    private static AttachmentType<?> type;
    private static boolean warned;

    private SoifDeSable() {
    }

    static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0
                || player.getAbilities().invulnerable || !ComplineConfig.climatEnabled()) {
            return;
        }
        float amount = (float) ComplineConfig.climatSandThirst();
        if (amount <= 0.0F) {
            return;
        }
        BlockPos head = BlockPos.containing(player.getEyePosition());
        Storm storm = Storm.at(player.level(), head);
        if (!storm.isSand() || !Storm.exposed(player.level(), head)) {
            return;
        }
        if (type == null) {
            type = NeoForgeRegistries.ATTACHMENT_TYPES.get(THIRST);
            if (type == null) {
                if (!warned) {
                    warned = true;
                    Compline.LOGGER.warn("climat : soif de Thirst Was Reclaimed introuvable ({}), le sable n'assoiffe pas", THIRST);
                }
                return;
            }
        }
        if (player.getData(type) instanceof IThirst thirst) {
            // Directement dans la fatigue : addExhaustion la multiplierait par le facteur de lieu de Thirst, qui varie
            // beaucoup avec la température du corps (et s'emballe quand elle est négative).
            thirst.setExhaustion(thirst.getExhaustion() + amount * storm.strength());
        }
    }
}
