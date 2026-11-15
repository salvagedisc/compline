package io.github.salvagedisc.compline.faune.warden;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Les taches de sculk qui luisent ({@code warden_glowmask.png}). Comme dans le jeu, la lueur respire lentement
 * et s'avive à chaque battement du cœur.
 */
final class WardenGlowLayer extends AutoGlowingGeoLayer<WardenAnimatable> {
    private final WardenRenderer owner;

    WardenGlowLayer(WardenRenderer renderer) {
        super(renderer);
        this.owner = renderer;
    }

    @Override
    public void render(PoseStack poseStack, WardenAnimatable animatable, BakedGeoModel bakedModel, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType glow = getRenderType(animatable, bufferSource);
        if (glow == null || !(owner.getCurrentEntity() instanceof Warden warden)) {
            return;
        }
        float age = warden.tickCount + partialTick;
        float breath = 0.5F + 0.5F * Mth.cos(age * 0.045F);
        float strength = Mth.clamp(0.45F + 0.25F * breath + 0.4F * warden.getHeartAnimation(partialTick), 0, 1);
        int colour = FastColor.ARGB32.colorFromFloat(strength, strength, strength, strength);
        getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick,
                LightTexture.FULL_BRIGHT, packedOverlay, colour);
    }
}
