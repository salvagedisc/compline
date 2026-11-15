package io.github.salvagedisc.compline.zoo;

import io.github.mortuusars.exposure.neoforge.api.event.FrameAddedEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;

/** Le tampon Photographier : chaque bête qu'Exposure voit dans une photo (chargé seulement avec Exposure). */
final class ZooPhoto {
    private ZooPhoto() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(FrameAddedEvent.class, ZooPhoto::onFrame);
    }

    private static void onFrame(FrameAddedEvent event) {
        event.getCameraHolder().getPlayerExecutingExposure().ifPresent(player -> {
            if (player instanceof ServerPlayer serverPlayer) {
                for (LivingEntity entity : event.getEntitiesInFrame()) {
                    Zoo.grant(serverPlayer, entity, Stamp.PHOTOGRAPHIER);
                }
            }
        });
    }
}
