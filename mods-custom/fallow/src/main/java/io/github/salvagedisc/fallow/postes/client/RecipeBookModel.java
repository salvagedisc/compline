// Adapted from Hands-On Vanilla by Pocky (MIT License, https://github.com/Pocky-l/handson-vanilla, branch 1.21.1,
// version 1.2.2). The licence travels with the mod: META-INF/LICENSE_handson_vanilla.
package io.github.salvagedisc.fallow.postes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

import io.github.salvagedisc.fallow.Fallow;

/**
 * The stations' cookbook: a green cloth book, two boards and a rounded spine standing a little proud of them, two blocks of pages exactly
 * the size of the paper drawn on them, framed by the covers with a pixel all round, and two loose pages. Its parts are those of the
 * enchanting table's book, laid out the same way (closed along +x from the spine, the front cover on +z, opening about y), so it opens
 * as that one does. The shape is Blockbench's Modded Entity export of {@code art/models/recipe_book/recipe_book.bbmodel}; its texture
 * ({@code art/textures/entity/recipe_book.aseprite}) has a texel to a pixel, as vanilla's entities do. A board, and each layer of the
 * spine's back (the full width, then a narrower crown), is a flat box inflated to its thickness: its faces then take whole texels (a
 * quarter-pixel-thick box would shift its cloth by a quarter texel), and its edges take the texels that the texture repeats beside each
 * net. The layer is built and baked here, so nothing is registered.
 */
final class RecipeBookModel {
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/entity/livre_de_recettes.png");
    /** The export's space is Blockbench's turned half a turn about z, as entity renderers turn it back. */
    private static final Quaternionf FROM_EXPORT = new Quaternionf().rotationZ(Mth.PI);

    private final ModelPart root;
    private final ModelPart frontCover;
    private final ModelPart backCover;
    private final ModelPart frontPages;
    private final ModelPart backPages;
    private final ModelPart frontFlip;
    private final ModelPart backFlip;

    private RecipeBookModel(ModelPart root) {
        this.root = root;
        frontCover = root.getChild("front_cover");
        backCover = root.getChild("back_cover");
        frontPages = root.getChild("front_pages");
        backPages = root.getChild("back_pages");
        frontFlip = root.getChild("front_flip");
        backFlip = root.getChild("back_flip");
    }

    static RecipeBookModel bake() {
        return new RecipeBookModel(createBodyLayer().bakeRoot());
    }

    /** The export, unchanged but for line breaks: a part per moving piece, its cubes by texture offset, sizes in pixels. */
    private static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("spine", CubeListBuilder.create()
                .texOffs(28, 0).addBox(0.0F, -5.0F, -1.0F, 0.25F, 10.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(0.3125F, -5.0F, -1.0F, 0.0F, 10.0F, 2.0F, new CubeDeformation(0.0625F))
                .texOffs(6, 12).addBox(0.4375F, -5.0F, -0.5F, 0.0F, 10.0F, 1.0F, new CubeDeformation(0.0625F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("front_cover", CubeListBuilder.create()
                .texOffs(1, 1).addBox(-5.875F, -5.0F, 0.125F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.135F)),
                PartPose.offset(0.0F, 0.0F, 1.0F));
        root.addOrReplaceChild("back_cover", CubeListBuilder.create()
                .texOffs(15, 1).addBox(-5.875F, -5.0F, -0.125F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.135F)),
                PartPose.offset(0.0F, 0.0F, -1.0F));
        root.addOrReplaceChild("front_pages", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-6.0F, -4.0F, 0.0F, 6.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("back_pages", CubeListBuilder.create()
                .texOffs(49, 0).addBox(-6.0F, -4.0F, -1.0F, 6.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("front_flip", CubeListBuilder.create()
                .texOffs(34, 10).addBox(-6.0F, -4.0F, 0.0F, 6.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("back_flip", CubeListBuilder.create()
                .texOffs(49, 10).addBox(-6.0F, -4.0F, 0.0F, 6.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    /**
     * Opens the book by {@code angle}, radians each cover turns from shut (a right angle lies it flat). The pages rise off the spine as
     * the enchanting table's do, so their blocks lie on the open covers; the loose pages lie on their blocks.
     */
    void setupAnim(float angle) {
        // A turn about y in the export's space is the opposite one in Blockbench's, and its x points the other way.
        float rise = -Mth.sin(angle);
        frontCover.yRot = angle;
        backCover.yRot = -angle;
        frontPages.x = rise;
        frontPages.yRot = angle;
        backPages.x = rise;
        backPages.yRot = -angle;
        frontFlip.x = rise;
        frontFlip.yRot = angle;
        backFlip.x = rise;
        backFlip.yRot = -angle;
    }

    void render(PoseStack poseStack, VertexConsumer consumer, int light, int overlay) {
        poseStack.pushPose();
        poseStack.mulPose(FROM_EXPORT);
        root.render(poseStack, consumer, light, overlay);
        poseStack.popPose();
    }
}
