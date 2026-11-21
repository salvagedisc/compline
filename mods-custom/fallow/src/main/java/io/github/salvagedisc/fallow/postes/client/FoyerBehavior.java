package io.github.salvagedisc.fallow.postes.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;

import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;

/**
 * Le poste du foyer de Cold Sweat : la case du combustible devant la grille, et sur la façade, au-dessus, les deux jauges de la
 * fenêtre de Cold Sweat, le chaud à gauche, le froid à droite, avec leur part en infobulle. L'inventaire grise ce qui ne brûle
 * ni ne refroidit ici (les potions passent : le foyer en répand l'effet).
 */
final class FoyerBehavior implements StationBehavior {
    private static final ResourceLocation CHAUD = cs("textures/gui/sprites/hearth/fuel_gauge_hot.png");
    private static final ResourceLocation CHAUD_VIDE = cs("textures/gui/sprites/hearth/fuel_gauge_hot_empty.png");
    private static final ResourceLocation FROID = cs("textures/gui/sprites/hearth/fuel_gauge_cold.png");
    private static final ResourceLocation FROID_VIDE = cs("textures/gui/sprites/hearth/fuel_gauge_cold_empty.png");
    /** Le côté d'une jauge sur la façade, en pixels du bloc. */
    private static final float COTE = 3.5F;

    /** La part de chaque combustible, de 0 à 1, lue à chaque tick ; les jauges et leurs infobulles la montrent. */
    private float chaud;
    private float froid;
    private int chaudLu = -1;
    private int froidLu = -1;

    private static ResourceLocation cs(String path) {
        return ResourceLocation.fromNamespaceAndPath("cold_sweat", path);
    }

    /** Vrai si cet objet, au foyer de {@code pos}, est un combustible (chaud ou froid) : un clic le jette au foyer. */
    static boolean combustible(Level level, BlockPos pos, ItemStack stack) {
        return !stack.isEmpty() && level.getBlockEntity(pos) instanceof HearthBlockEntity hearth && hearth.getItemFuel(stack) != 0;
    }

    @Override
    public void onSceneReady(StationView view) {
        view.node("jauge_chaud").ifPresent(node -> node.setPainter((pose, buffers, light) -> jauge(pose, buffers, CHAUD_VIDE, CHAUD, chaud)));
        view.node("jauge_froid").ifPresent(node -> node.setPainter((pose, buffers, light) -> jauge(pose, buffers, FROID_VIDE, FROID, froid)));
    }

    @Override
    public void onOpen(StationView view) {
        view.setInventoryFilter(stack -> combustible(view.level(), view.pos(), stack) || stack.is(Items.MILK_BUCKET)
                || stack.has(DataComponents.POTION_CONTENTS));
        chaudLu = -1;
        froidLu = -1;
        lire(view);
    }

    @Override
    public void tick(StationView view) {
        lire(view);
    }

    private void lire(StationView view) {
        if (!(view.level().getBlockEntity(view.pos()) instanceof HearthBlockEntity hearth) || hearth.getMaxFuel() <= 0) {
            return;
        }
        chaud = Math.min(1.0F, hearth.getHotFuel() / (float) hearth.getMaxFuel());
        froid = Math.min(1.0F, hearth.getColdFuel() / (float) hearth.getMaxFuel());
        int chaudPct = Math.round(chaud * 100.0F);
        int froidPct = Math.round(froid * 100.0F);
        if (chaudPct != chaudLu) {
            chaudLu = chaudPct;
            view.node("jauge_chaud").ifPresent(node -> node.setTooltip(Component.translatable("postes.fallow.foyer.chaud", chaudPct)));
        }
        if (froidPct != froidLu) {
            froidLu = froidPct;
            view.node("jauge_froid").ifPresent(node -> node.setTooltip(Component.translatable("postes.fallow.foyer.froid", froidPct)));
        }
    }

    /**
     * Une jauge à plat contre la façade, tournée vers l'avant (z négatif) : la vide en entier, puis la pleine du bas jusqu'à sa
     * part, comme la fenêtre de Cold Sweat (deux pixels sur quatorze dès qu'il y a du combustible). Elle luit un peu.
     */
    private static void jauge(PoseStack pose, MultiBufferSource buffers, ResourceLocation vide, ResourceLocation pleine, float part) {
        int light = LightTexture.FULL_BRIGHT;
        carre(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(vide)), 1.0F, light, 0.0F);
        if (part > 0.0F) {
            float haut = (2.0F + 12.0F * part) / 14.0F;
            carre(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(pleine)), haut, light, -0.01F);
        }
    }

    /** Le bas de la texture sur {@code haut} (de 0 à 1) de la hauteur, centré sur le nœud. */
    private static void carre(PoseStack pose, VertexConsumer out, float haut, int light, float z) {
        Matrix4f m = pose.last().pose();
        float x0 = -COTE / 2.0F;
        float x1 = COTE / 2.0F;
        float y0 = -COTE / 2.0F;
        float y1 = y0 + COTE * haut;
        float v0 = 1.0F - haut;
        // Vu de devant (z négatif), le côté de x grand est à gauche : u y commence.
        sommet(pose, out, m, x1, y0, z, 0.0F, 1.0F, light);
        sommet(pose, out, m, x0, y0, z, 1.0F, 1.0F, light);
        sommet(pose, out, m, x0, y1, z, 1.0F, v0, light);
        sommet(pose, out, m, x1, y1, z, 0.0F, v0, light);
    }

    private static void sommet(PoseStack pose, VertexConsumer out, Matrix4f m, float x, float y, float z, float u, float v, int light) {
        out.addVertex(m, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }
}
