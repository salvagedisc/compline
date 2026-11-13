package io.github.salvagedisc.tilth.mixin.averse;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.salvagedisc.tilth.TilthConfig;
import io.github.salvagedisc.tilth.averse.Bolt;
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
    private void tilth$bolt(LightningBolt bolt, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light,
            CallbackInfo ci) {
        if (TilthConfig.averseLightning()) {
            Bolt.render(bolt, partialTick, poseStack, buffers);
            ci.cancel();
        }
    }
}
