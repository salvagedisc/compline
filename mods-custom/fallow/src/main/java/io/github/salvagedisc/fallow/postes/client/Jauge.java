package io.github.salvagedisc.fallow.postes.client;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Une petite jauge d'avancement au-dessus d'un bloc, comme le badge de la marmite de Hands-On Delight : une barre de 8 pixels
 * sur 1, cernée de sombre, qui se remplit d'une couleur à l'autre. Elle grandit à l'arrivée de la caméra, et la part montrée
 * rejoint doucement la vraie (le bloc avance par paliers). Dessinée par le peintre d'un nœud, dans son plan x y, vers z négatif.
 */
final class Jauge {
    private static final ResourceLocation BLANC = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    private static final float LONG = 8.0F;
    private static final float HAUT = 1.0F;

    private final int debut;
    private final int fin;
    private float montre = -1.0F;
    private float vise;
    private float parait;

    /** {@code debut} et {@code fin} : la couleur du plein au départ et à la fin (RGB). */
    Jauge(int debut, int fin) {
        this.debut = debut;
        this.fin = fin;
    }

    /** À chaque image : la part visée (0 à 1), ou une valeur négative pour cacher la jauge. */
    void frame(float part, float dt) {
        vise = part;
        if (part < 0.0F) {
            parait = Math.max(0.0F, parait - dt * 6.0F);
            return;
        }
        parait = Math.min(1.0F, parait + dt * 6.0F);
        montre = montre < 0.0F ? part : montre + (part - montre) * Math.min(1.0F, dt * 2.0F);
    }

    void paint(PoseStack pose, MultiBufferSource buffers) {
        if (parait <= 0.01F || montre < 0.0F) {
            return;
        }
        float s = Flottant.sortie(parait);
        pose.pushPose();
        pose.scale(s, s, s);
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(BLANC));
        float x0 = -LONG / 2.0F;
        float y0 = -HAUT / 2.0F;
        // Le cerne, le fond, puis le plein qui va d'une couleur à l'autre, avec un liseré clair en haut.
        quad(pose, out, x0 - 0.35F, y0 - 0.35F, LONG + 0.7F, HAUT + 0.7F, 0x2A1A10, 0.0F);
        quad(pose, out, x0, y0, LONG, HAUT, 0x5A4636, -0.01F);
        float part = Mth.clamp(montre, 0.0F, 1.0F);
        if (part > 0.0F) {
            int couleur = melange(debut, fin, part);
            quad(pose, out, x0, y0, LONG * part, HAUT, couleur, -0.02F);
            quad(pose, out, x0, y0 + HAUT - 0.3F, LONG * part, 0.3F, melange(couleur, 0xFFFFFF, 0.35F), -0.03F);
        }
        pose.popPose();
    }

    /** Un rectangle plein de {@code w} sur {@code h} depuis (x, y) ; vu de devant, x grandit vers la gauche : on le retourne. */
    private static void quad(PoseStack pose, VertexConsumer out, float x, float y, float w, float h, int rgb, float z) {
        Matrix4f m = pose.last().pose();
        float a = -x;
        float b = -(x + w);
        int r = rgb >> 16 & 0xFF;
        int g = rgb >> 8 & 0xFF;
        int bl = rgb & 0xFF;
        int light = LightTexture.FULL_BRIGHT;
        sommet(pose, out, m, a, y, z, r, g, bl, light);
        sommet(pose, out, m, b, y, z, r, g, bl, light);
        sommet(pose, out, m, b, y + h, z, r, g, bl, light);
        sommet(pose, out, m, a, y + h, z, r, g, bl, light);
    }

    private static void sommet(PoseStack pose, VertexConsumer out, Matrix4f m, float x, float y, float z, int r, int g, int b, int light) {
        out.addVertex(m, x, y, z).setColor(r, g, b, 255).setUv(0.0F, 0.0F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }

    static int melange(int a, int b, float t) {
        int r = (int) Mth.lerp(t, a >> 16 & 0xFF, b >> 16 & 0xFF);
        int g = (int) Mth.lerp(t, a >> 8 & 0xFF, b >> 8 & 0xFF);
        int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
        return r << 16 | g << 8 | bl;
    }
}
