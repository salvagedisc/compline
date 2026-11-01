package io.github.salvagedisc.fallow.averse;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * L'éclair de Fallow, à la place de celui du jeu : un trait blanc fin et net qui zigzague par à-coups,
 * entouré de deux halos bleutés, avec deux à quatre branches qui s'éteignent avant le sol. Chaque
 * éclair garde sa forme (graine de l'entité) ; il scintille comme celui du jeu.
 *
 * <p>Il est dessiné tout de suite, la brume repoussée le temps du dessin : dans la brume de Fallow,
 * l'éclair du jeu se fond dans le gris à vingt blocs. Celui-ci la perce, en s'adoucissant au loin.
 */
public final class Bolt {
    private static final float TOP = 110.0F;
    private static final int SEGMENTS = 44;

    private Bolt() {
    }

    public static void render(LightningBolt bolt, float partialTick, PoseStack poseStack, MultiBufferSource buffers) {
        RandomSource random = RandomSource.create(bolt.seed);
        List<Vector3f> main = new ArrayList<>();
        float x = 0.0F;
        float z = 0.0F;
        for (int i = 0; i <= SEGMENTS; i++) {
            float y = TOP * (1.0F - (float) i / SEGMENTS);
            main.add(new Vector3f(x, y, z));
            // des à-coups : souvent un petit pas, parfois un saut de côté
            float step = random.nextFloat() < 0.25F ? 1.8F : 0.6F;
            x += (random.nextFloat() - 0.5F) * 2.0F * step;
            z += (random.nextFloat() - 0.5F) * 2.0F * step;
        }
        // pont : la dérive est retirée peu à peu pour que le trait tombe sur le point d'impact sans coude final
        Vector3f end = main.get(SEGMENTS);
        float endX = end.x;
        float endZ = end.z;
        for (int i = 0; i <= SEGMENTS; i++) {
            Vector3f p = main.get(i);
            p.x -= endX * i / SEGMENTS;
            p.z -= endZ * i / SEGMENTS;
        }
        List<List<Vector3f>> branches = new ArrayList<>();
        int count = 2 + random.nextInt(3);
        for (int b = 0; b < count; b++) {
            int from = 4 + random.nextInt(SEGMENTS / 2);
            Vector3f p = new Vector3f(main.get(from));
            float dirX = (random.nextFloat() - 0.5F) * 2.0F;
            float dirZ = (random.nextFloat() - 0.5F) * 2.0F;
            List<Vector3f> branch = new ArrayList<>();
            branch.add(new Vector3f(p));
            int length = 6 + random.nextInt(10);
            for (int i = 0; i < length; i++) {
                p.add(dirX * 0.9F + (random.nextFloat() - 0.5F) * 1.2F, -TOP / SEGMENTS * (0.8F + random.nextFloat() * 0.6F),
                        dirZ * 0.9F + (random.nextFloat() - 0.5F) * 1.2F);
                branch.add(new Vector3f(p));
            }
            branches.add(branch);
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        Vec3 origin = bolt.getPosition(partialTick);
        Vector3f eye = new Vector3f((float) (camera.x - origin.x), (float) (camera.y - origin.y), (float) (camera.z - origin.z));
        Matrix4f pose = poseStack.last().pose();
        BufferBuilder consumer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        // halo large, halo serré, cœur blanc
        ribbon(consumer, pose, main, eye, 1.6F, 0.55F, 0.62F, 0.95F, 0.10F);
        ribbon(consumer, pose, main, eye, 0.45F, 0.78F, 0.84F, 1.0F, 0.32F);
        ribbon(consumer, pose, main, eye, 0.12F, 1.0F, 1.0F, 1.0F, 0.95F);
        for (List<Vector3f> branch : branches) {
            ribbon(consumer, pose, branch, eye, 0.9F, 0.55F, 0.62F, 0.95F, 0.06F);
            ribbon(consumer, pose, branch, eye, 0.07F, 0.95F, 0.96F, 1.0F, 0.7F);
        }
        MeshData mesh = consumer.build();
        if (mesh == null) {
            return;
        }
        float fogStart = RenderSystem.getShaderFogStart();
        float fogEnd = RenderSystem.getShaderFogEnd();
        RenderSystem.setShaderFogStart(Math.max(fogStart, 0.0F) + 96.0F);
        RenderSystem.setShaderFogEnd(fogEnd + 320.0F);
        RenderSystem.setShader(GameRenderer::getRendertypeLightningShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferUploader.drawWithShader(mesh);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderFogStart(fogStart);
        RenderSystem.setShaderFogEnd(fogEnd);
    }

    /** Un ruban tourné vers la caméra le long d'une ligne brisée ; l'opacité baisse vers le bout. */
    private static void ribbon(VertexConsumer consumer, Matrix4f pose, List<Vector3f> points, Vector3f eye, float width,
            float r, float g, float b, float alpha) {
        int n = points.size();
        for (int i = 0; i < n - 1; i++) {
            Vector3f p0 = points.get(i);
            Vector3f p1 = points.get(i + 1);
            Vector3f axis = new Vector3f(p1).sub(p0);
            Vector3f mid = new Vector3f(p0).add(p1).mul(0.5F);
            Vector3f toEye = new Vector3f(eye).sub(mid);
            Vector3f side = axis.cross(toEye, new Vector3f());
            if (side.lengthSquared() < 1.0E-6F) {
                continue;
            }
            side.normalize().mul(width * 0.5F);
            float a0 = alpha * (1.0F - 0.5F * i / (n - 1));
            float a1 = alpha * (1.0F - 0.5F * (i + 1) / (n - 1));
            consumer.addVertex(pose, p0.x - side.x, p0.y - side.y, p0.z - side.z).setColor(r, g, b, a0);
            consumer.addVertex(pose, p0.x + side.x, p0.y + side.y, p0.z + side.z).setColor(r, g, b, a0);
            consumer.addVertex(pose, p1.x + side.x, p1.y + side.y, p1.z + side.z).setColor(r, g, b, a1);
            consumer.addVertex(pose, p1.x - side.x, p1.y - side.y, p1.z - side.z).setColor(r, g, b, a1);
        }
    }

    /** Intensité de l'éclair de ciel (0 à 1) d'après {@code skyFlashTime}, que le jeu met à 2 à chaque coup de foudre. */
    public static float flash(float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0.0F;
        }
        int ticks = minecraft.level.getSkyFlashTime();
        return ticks <= 0 ? 0.0F : Mth.clamp((ticks - partialTick) / 2.0F, 0.0F, 1.0F);
    }
}
