package io.github.salvagedisc.fallow.mixin.averse;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.averse.Bolt;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Remplace l'éclair du jeu par celui de {@link Bolt} quand le réglage {@code averse.lightning} est actif. */
@Mixin(LightningBoltRenderer.class)
abstract class LightningBoltRendererMixin {
    @Inject(method = "render(Lnet/minecraft/world/entity/LightningBolt;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void fallow$bolt(LightningBolt bolt, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light,
            CallbackInfo ci) {
        if (FallowConfig.averseLightning()) {
            Bolt.render(bolt, partialTick, poseStack, buffers);
            ci.cancel();
        }
    }
}
