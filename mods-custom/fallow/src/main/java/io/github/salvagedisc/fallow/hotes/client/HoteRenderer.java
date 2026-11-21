package io.github.salvagedisc.fallow.hotes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.hotes.Hote;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Rendu d'un Hôte : la couche de modèle du joueur ({@link ModelLayers#PLAYER}) elle-même, pas une copie. Entity Model
 * Features remplace cette couche par le modèle de Fresh Animations et de son extension joueur : l'Hôte bouge donc
 * exactement comme un joueur, sans aucune animation à nous. Il porte l'armure de sa bête telle qu'Archie l'a peinte ;
 * ce qui le distingue d'un joueur, c'est sa capuche sombre, le masque de son peuple et sa parure.
 */
public class HoteRenderer extends HumanoidMobRenderer<Hote, PlayerModel<Hote>> {
    public HoteRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        addLayer(new ParureLayer(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(Hote hote) {
        return ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/entity/hote/" + hote.peuple().id() + ".png");
    }

    @Override
    protected void scale(Hote hote, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.9375F, 0.9375F, 0.9375F);
    }
}
