package io.github.salvagedisc.fallow.averse;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Les rideaux de pluie : des anneaux de grands pans de pluie autour du joueur, de 24 à 84 blocs,
 * bien au-delà de la pluie de Where Winds Blow (rayon de 10 blocs), et légers : ils ne doivent pas
 * boucher la vue. Ils penchent avec le vent, défilent vers le bas et prennent la couleur de la brume ; le brouillard du shader les efface au loin, si bien
 * qu'on les voit surtout là où la brume se lève, comme une averse qui traverse la vallée.
 *
 * <p>La hauteur du sol et la pluie de chaque pan sont relues quand le joueur a bougé de deux blocs ou
 * toutes les secondes ; le dessin coûte environ 180 quads par image.
 */
public final class RainCurtains {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/environment/rain_curtain.png");
    private static final float[] RADII = {24.0F, 34.0F, 48.0F, 64.0F, 84.0F};
    /** Opacité de chaque anneau : les proches restent légers, les lointains s'épaississent avant que la brume les prenne. */
    private static final float[] WEIGHT = {0.4F, 0.6F, 0.8F, 0.9F, 1.0F};
    private static final int SEGMENTS = 36;

    /** Sol (y) de chaque pan, ou {@link Integer#MIN_VALUE} s'il n'y pleut pas. */
    private static final int[][] ground = new int[RADII.length][SEGMENTS];
    private static final int[][] light = new int[RADII.length][SEGMENTS];
    private static BlockPos sampledAt;
    private static long sampledTick = Long.MIN_VALUE;

    private RainCurtains() {
    }

    public static void render(ClientLevel level, Vec3 cam, float partialTick) {
        if (!FallowConfig.averseCurtains()) {
            return;
        }
        float rain = level.getRainLevel(partialTick);
        if (rain < 0.05F) {
            return;
        }
        float thunder = level.getThunderLevel(partialTick);
        int camSurface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(cam.x), Mth.floor(cam.z));
        if (cam.y < camSurface - 12) {
            return; // sous terre : les rideaux traverseraient les galeries
        }
        sample(level, cam);

        float[] wind = Wind.at(level, cam.x, cam.y, cam.z);
        float slope = 0.06F + Math.min(wind[2], 3.0F) * 0.08F + thunder * 0.15F;
        float[] fog = RenderSystem.getShaderFogColor();
        float r = Mth.lerp(0.7F, fog[0], 0.92F);
        float g = Mth.lerp(0.7F, fog[1], 0.94F);
        float b = Mth.lerp(0.7F, fog[2], 0.96F);
        float strength = rain * (0.2F + 0.15F * thunder) * (float) FallowConfig.averseCurtainOpacity();
        float time = (level.getGameTime() % 24000L) + partialTick;
        float scroll = -(time * (0.09F + thunder * 0.04F)) % 1.0F;

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.setShader(GameRenderer::getParticleShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        boolean any = false;
        for (int ring = 0; ring < RADII.length; ring++) {
            float radius = RADII[ring];
            float alpha = strength * WEIGHT[ring];
            for (int i = 0; i < SEGMENTS; i++) {
                int y0 = ground[ring][i];
                if (y0 == Integer.MIN_VALUE) {
                    continue;
                }
                double a0 = i * Mth.TWO_PI / SEGMENTS;
                double a1 = (i + 1) * Mth.TWO_PI / SEGMENTS;
                // pans posés autour de la position échantillonnée, pour qu'ils ne glissent pas avec la tête
                double ox = sampledAt.getX() + 0.5 - cam.x;
                double oz = sampledAt.getZ() + 0.5 - cam.z;
                float x0 = (float) (ox + Math.cos(a0) * radius);
                float z0 = (float) (oz + Math.sin(a0) * radius);
                float x1 = (float) (ox + Math.cos(a1) * radius);
                float z1 = (float) (oz + Math.sin(a1) * radius);
                float bottom = (float) (y0 - 2 - cam.y);
                float top = (float) (Math.max(y0, cam.y) + 40.0 - cam.y);
                float drift = Math.min((top - bottom) * slope, 12.0F);
                float dx = -wind[0] * drift;
                float dz = -wind[1] * drift;
                float u0 = (float) (a0 * radius / 12.0);
                float u1 = (float) (a1 * radius / 12.0);
                float v0 = bottom * 0.05F + scroll;
                float v1 = top * 0.05F + scroll;
                int lit = light[ring][i];
                buffer.addVertex(x0 + dx, top, z0 + dz).setUv(u0, v1).setColor(r, g, b, alpha).setLight(lit);
                buffer.addVertex(x1 + dx, top, z1 + dz).setUv(u1, v1).setColor(r, g, b, alpha).setLight(lit);
                buffer.addVertex(x1, bottom, z1).setUv(u1, v0).setColor(r, g, b, alpha).setLight(lit);
                buffer.addVertex(x0, bottom, z0).setUv(u0, v0).setColor(r, g, b, alpha).setLight(lit);
                any = true;
            }
        }
        MeshData mesh = buffer.build();
        if (mesh != null && any) {
            BufferUploader.drawWithShader(mesh);
        } else if (mesh != null) {
            mesh.close();
        }
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        minecraft.gameRenderer.lightTexture().turnOffLightLayer();
    }

    private static void sample(ClientLevel level, Vec3 cam) {
        BlockPos here = BlockPos.containing(cam);
        long tick = level.getGameTime();
        if (sampledAt != null && here.distManhattan(sampledAt) < 2 && tick - sampledTick < 20) {
            return;
        }
        sampledAt = here;
        sampledTick = tick;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int ring = 0; ring < RADII.length; ring++) {
            for (int i = 0; i < SEGMENTS; i++) {
                double a = (i + 0.5) * Mth.TWO_PI / SEGMENTS;
                int x = Mth.floor(here.getX() + 0.5 + Math.cos(a) * RADII[ring]);
                int z = Mth.floor(here.getZ() + 0.5 + Math.sin(a) * RADII[ring]);
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    ground[ring][i] = Integer.MIN_VALUE;
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                pos.set(x, y, z);
                Biome biome = level.getBiome(pos).value();
                boolean raining = biome.hasPrecipitation() && biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN;
                ground[ring][i] = raining ? y : Integer.MIN_VALUE;
                light[ring][i] = LevelRenderer.getLightColor(level, pos);
            }
        }
    }
}
