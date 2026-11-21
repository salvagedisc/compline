package io.github.salvagedisc.fallow.hotes.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.hotes.Hote;
import io.github.salvagedisc.fallow.hotes.Peuple;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * La parure d'un peuple, ce qu'aucun joueur ne porte, posée par-dessus l'armure de sa bête : un collier qui porte une
 * amulette, une ceinture d'où pendent des franges, et une épaulière sur l'épaule droite (pour l'Écorce : lianes
 * tressées, pierre gravée, racines et pierre moussue). Mêmes matières, mêmes couleurs et même échelle que l'armure de
 * la bête. Chaque pièce suit le corps ou le bras tel que Fresh Animations l'a posé.
 *
 * <p>La forme vient de {@code assets/fallow/hotes/parures/<peuple>.json} ({@code torse} et {@code bras_droit}), la
 * texture de {@code textures/entity/hote/<peuple>_parure.png}.
 */
public class ParureLayer extends RenderLayer<Hote, PlayerModel<Hote>> {
    private static final Map<Peuple, ModelLayerLocation> COUCHES = new EnumMap<>(Peuple.class);

    static {
        for (Peuple peuple : Peuple.values()) {
            COUCHES.put(peuple, new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "hote_" + peuple.id()), "parure"));
        }
    }

    private final Map<Peuple, ModelPart> torses = new EnumMap<>(Peuple.class);
    private final Map<Peuple, ModelPart> epaules = new EnumMap<>(Peuple.class);
    private final Map<Peuple, ResourceLocation> textures = new EnumMap<>(Peuple.class);

    public ParureLayer(RenderLayerParent<Hote, PlayerModel<Hote>> parent, EntityModelSet models) {
        super(parent);
        for (Peuple peuple : Peuple.values()) {
            ModelPart racine = models.bakeLayer(COUCHES.get(peuple));
            torses.put(peuple, racine.getChild("torse"));
            epaules.put(peuple, racine.getChild("epaule"));
            textures.put(peuple, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/entity/hote/" + peuple.id() + "_parure.png"));
        }
    }

    public static ModelLayerLocation couche(Peuple peuple) {
        return COUCHES.get(peuple);
    }

    public static LayerDefinition definition(Peuple peuple) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition racine = mesh.getRoot();
        PartDefinition torse = racine.addOrReplaceChild("torse", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition epaule = racine.addOrReplaceChild("epaule", CubeListBuilder.create(), PartPose.ZERO);
        JsonObject forme = MasqueModel.lire("parures", peuple);
        for (JsonElement piece : forme.getAsJsonArray("torse")) {
            MasqueModel.ajouter(torse, piece.getAsJsonObject());
        }
        for (JsonElement piece : forme.getAsJsonArray("bras_droit")) {
            MasqueModel.ajouter(epaule, piece.getAsJsonObject());
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, Hote hote, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (hote.isInvisible()) {
            return;
        }
        Peuple peuple = hote.peuple();
        var buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(textures.get(peuple)));
        int overlay = LivingEntityRenderer.getOverlayCoords(hote, 0.0F);
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        torses.get(peuple).render(pose, buffer, light, overlay);
        pose.popPose();
        pose.pushPose();
        getParentModel().rightArm.translateAndRotate(pose);
        epaules.get(peuple).render(pose, buffer, light, overlay);
        pose.popPose();
    }
}
