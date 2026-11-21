package io.github.salvagedisc.fallow.mixin.postes;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.pockyl.diegesis.api.client.DiegesisClient;

import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;

/**
 * Overgeared dessine les pièces posées sur son enclume de forge, empilées au milieu. Au poste, la scène les montre déjà dans
 * leurs cases : le dessin d'Overgeared est coupé tant que la scène est là, comme Hands-On Vanilla le fait pour l'enclume vanilla.
 */
@Mixin(BlockEntityRenderDispatcher.class)
abstract class ForgeRendererMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void fallow$dessineeParLaScene(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
            CallbackInfo callback) {
        if (blockEntity instanceof AbstractSmithingAnvilBlockEntity && DiegesisClient.isSceneDrawn(blockEntity.getBlockPos())) {
            callback.cancel();
        }
    }
}
