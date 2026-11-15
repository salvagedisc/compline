package io.github.salvagedisc.compline.faune.moose;

import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/** Le rendu de l'élan de Compline, à la place de celui d'Alex's Mobs. Le petit est rendu à moitié de taille. */
public final class MooseRenderer extends GeoReplacedEntityRenderer<EntityMoose, MooseAnimatable> {
    public MooseRenderer(EntityRendererProvider.Context context) {
        super(context, new MooseModel(), new MooseAnimatable());
        this.shadowRadius = 0.8F;
    }

    @Override
    public void preRender(PoseStack poseStack, MooseAnimatable animatable, BakedGeoModel model, MultiBufferSource bufferSource,
            VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender && currentEntity != null && currentEntity.isBaby()) {
            poseStack.scale(0.5F, 0.5F, 0.5F);
        }
    }
}
