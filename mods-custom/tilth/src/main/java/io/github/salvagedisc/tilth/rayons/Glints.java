package io.github.salvagedisc.tilth.rayons;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.salvagedisc.tilth.Tilth;
import io.github.salvagedisc.tilth.TilthConfig;
import io.github.salvagedisc.tilth.rendu.Sun;
import java.util.Arrays;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Les éclats de soleil sur l'eau. Chaque bloc d'eau à ciel ouvert autour du joueur porte quatre éclats,
 * qui ne s'allument que là où l'eau, ridée par de petites vagues, renvoie le soleil vers l'œil : on
 * voit donc une traînée scintillante entre soi et le soleil, plus large quand il est bas. Chaque
 * éclat clignote à son rythme et change de place à chaque battement.
 */
public final class Glints {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Tilth.MOD_ID, "textures/environment/glint.png");
    private static final int RADIUS = 28;
    private static final int MAX = 2500;
    /** Éclats par bloc d'eau, chacun avec sa place, sa ride et son rythme. */
    private static final int PER_BLOCK = 4;

    /** Surfaces d'eau : x, y (haut de l'eau), z, à la suite. */
    private static float[] water = new float[0];
    private static int count;
    private static long scannedTick = -1000L; // pas MIN_VALUE : tick - scannedTick déborderait

    private Glints() {
    }

    static void scan(ClientLevel level, BlockPos centre) {
        long tick = level.getGameTime();
        if (!TilthConfig.rayonsGlints() || tick - scannedTick < 20) {
            return;
        }
        scannedTick = tick;
        float[] found = new float[MAX * 3];
        int n = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS && n < MAX; dx++) {
            for (int dz = -RADIUS; dz <= RADIUS && n < MAX; dz++) {
                if (dx * dx + dz * dz > RADIUS * RADIUS) {
                    continue;
                }
                int x = centre.getX() + dx;
                int z = centre.getZ() + dz;
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
                pos.set(x, y, z);
                if (!level.getFluidState(pos).is(FluidTags.WATER) || !level.getBlockState(pos.above()).isAir()) {
                    continue;
                }
                found[n * 3] = x;
                found[n * 3 + 1] = y + level.getFluidState(pos).getHeight(level, pos);
                found[n * 3 + 2] = z;
                n++;
            }
        }
        water = Arrays.copyOf(found, n * 3);
        count = n;
    }

    static void render(ClientLevel level, Vec3 cam, float partialTick) {
        if (!TilthConfig.rayonsGlints() || count == 0) {
            return;
        }
        Vector3f sun = Sun.direction(level, partialTick);
        float day = Sun.daylight(sun);
        float clear = 1.0F - 0.85F * level.getRainLevel(partialTick);
        if (day <= 0.01F || sun.y() < 0.02F) {
            return;
        }
        float base = day * clear * (float) TilthConfig.rayonsGlintStrength();
        float[] colour = Sun.colour(sun);
        float time = level.getGameTime() + partialTick;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f left = camera.getLeftVector();
        Vector3f up = camera.getUpVector();
        // plus le soleil est bas, plus la traînée s'élargit (les vagues le renvoient de plus loin)
        float spread = Mth.lerp(Sun.smoothstep(0.1F, 0.8F, sun.y()), 0.20F, 0.12F);

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean any = false;
        Vector3f view = new Vector3f();
        for (int k = 0; k < count * PER_BLOCK; k++) {
            int i = k / PER_BLOCK;
            float bx = water[i * 3];
            float by = water[i * 3 + 1];
            float bz = water[i * 3 + 2];
            int seed = Mth.murmurHash3Mixer(((int) bx * 73856093 ^ (int) bz * 19349663) + k % PER_BLOCK * 83492791);
            float rate = 0.08F + (seed & 15) * 0.01F;
            float cycle = time * rate + (seed >>> 8 & 255) / 40.0F;
            int beat = Mth.floor(cycle);
            float pulse = Mth.sin((cycle - beat) * Mth.PI);
            // une place et une ride différentes à chaque battement
            int h = Mth.murmurHash3Mixer(seed + beat * 668265263);
            float ox = (h & 255) / 255.0F;
            float oz = (h >>> 8 & 255) / 255.0F;
            float nx = ((h >>> 16 & 255) / 255.0F - 0.5F) * 2.0F * spread;
            float nz = ((h >>> 24 & 255) / 255.0F - 0.5F) * 2.0F * spread;
            float px = (float) (bx + ox - cam.x);
            float py = (float) (by + 0.01F - cam.y);
            float pz = (float) (bz + oz - cam.z);
            view.set(px, py, pz);
            float distance = view.length();
            if (distance < 2.0F) {
                continue;
            }
            view.div(distance);
            // reflet de la vue sur une surface à peine inclinée (n ~ (nx, 1, nz))
            float ny = 1.0F;
            float norm = Mth.invSqrt(nx * nx + ny * ny + nz * nz);
            float dot = (view.x * nx + view.y * ny + view.z * nz) * norm;
            float rx = view.x - 2.0F * dot * nx * norm;
            float ry = view.y - 2.0F * dot * ny * norm;
            float rz = view.z - 2.0F * dot * nz * norm;
            float match = rx * sun.x() + ry * sun.y() + rz * sun.z();
            float glint = Sun.smoothstep(0.965F, 0.992F, match);
            if (glint <= 0.0F) {
                continue;
            }
            float alpha = Math.min(1.0F, base * glint * pulse * (1.0F - 0.6F * Sun.fogAt(distance)));
            if (alpha <= 0.01F) {
                continue;
            }
            float size = (0.12F + 0.014F * distance) * (0.6F + 0.4F * pulse);
            float lx = left.x() * size;
            float ly = left.y() * size;
            float lz = left.z() * size;
            float ux = up.x() * size;
            float uy = up.y() * size;
            float uz = up.z() * size;
            py += size * 0.8F; // posé sur l'eau : sinon la surface coupe la moitié basse de l'éclat
            buffer.addVertex(px - lx - ux, py - ly - uy, pz - lz - uz).setUv(0.0F, 1.0F).setColor(colour[0], colour[1], colour[2], alpha);
            buffer.addVertex(px + lx - ux, py + ly - uy, pz + lz - uz).setUv(1.0F, 1.0F).setColor(colour[0], colour[1], colour[2], alpha);
            buffer.addVertex(px + lx + ux, py + ly + uy, pz + lz + uz).setUv(1.0F, 0.0F).setColor(colour[0], colour[1], colour[2], alpha);
            buffer.addVertex(px - lx + ux, py - ly + uy, pz - lz + uz).setUv(0.0F, 0.0F).setColor(colour[0], colour[1], colour[2], alpha);
            any = true;
        }
        MeshData mesh = buffer.build();
        if (mesh != null && any) {
            BufferUploader.drawWithShader(mesh);
        } else if (mesh != null) {
            mesh.close();
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }
}
