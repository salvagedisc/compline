package io.github.salvagedisc.fallow.postes.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.pockyl.diegesis.api.client.DiegesisClient;
import com.pockyl.diegesis.api.client.NodeView;

/**
 * Ce qu'un poste a fini, qui flotte au-dessus du bloc comme le résultat de l'enclume de Hands-On Vanilla (MIT) : il apparaît en
 * grandissant, monte et descend doucement en se balançant, gonfle et s'éclaire sous le curseur, et son infobulle est celle de
 * l'objet. Dessiné par le peintre d'un nœud, en pixels de bloc.
 */
final class Flottant {
    private final float taille;
    private ItemStack vu = ItemStack.EMPTY;
    private float apparu;
    private float survol;

    /** {@code taille} : le grand côté de l'objet, en pixels. */
    Flottant(float taille) {
        this.taille = taille;
    }

    /** À chaque image : l'infobulle et le survol du nœud. */
    void frame(NodeView node, ItemStack fait, boolean montre, float dt) {
        node.setTooltipItem(montre ? fait : ItemStack.EMPTY);
        float vise = montre && !fait.isEmpty() && node.isHovered() ? 1.0F : 0.0F;
        survol = Mth.lerp(Math.min(1.0F, dt * 14.0F), survol, vise);
    }

    void paint(Level level, ItemStack fait, float horloge, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!ItemStack.matches(fait, vu)) {
            if (vu.isEmpty() || fait.isEmpty() || !ItemStack.isSameItem(fait, vu)) {
                apparu = horloge;
            }
            vu = fait.copy();
        }
        if (fait.isEmpty()) {
            return;
        }
        float scale = taille * sortie((horloge - apparu) / 0.12F) * (1.0F + 0.1F * survol);
        boolean bloc = Minecraft.getInstance().getItemRenderer().getModel(fait, level, null, 0).isGui3d();
        pose.pushPose();
        pose.translate(0.0F, scale * (bloc ? 0.25F : 0.5F) + 0.5F * (1.0F + Mth.sin(horloge * 2.2F)), 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F + 18.0F * Mth.sin(horloge * 1.3F)));
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(fait, ItemDisplayContext.FIXED,
                survol > 0.5F ? LightTexture.FULL_BRIGHT : light, OverlayTexture.NO_OVERLAY, pose, DiegesisClient.items(buffers), level, 0);
        pose.popPose();
    }

    /** L'ouvrage qui se lève de l'enclume au coup de marteau : 3 pixels en 0,3 seconde, un peu gros au départ. */
    static void leve(Level level, ItemStack forge, float age, float taille, PoseStack pose, MultiBufferSource buffers, int light) {
        if (forge.isEmpty() || age < 0.0F || age >= 0.3F) {
            return;
        }
        float t = sortie(age / 0.3F);
        float scale = taille * (1.0F + 0.15F * (1.0F - t));
        boolean bloc = Minecraft.getInstance().getItemRenderer().getModel(forge, level, null, 0).isGui3d();
        pose.pushPose();
        pose.translate(0.0F, 3.0F * t + scale * (bloc ? 0.25F : 0.5F), 0.0F);
        pose.mulPose(Axis.YP.rotationDegrees(180.0F));
        pose.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(forge, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose,
                DiegesisClient.items(buffers), level, 0);
        pose.popPose();
    }

    static float sortie(float t) {
        float c = 1.0F - Mth.clamp(t, 0.0F, 1.0F);
        return 1.0F - c * c * c;
    }
}
