package io.github.salvagedisc.fallow.rayons;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.rendu.Sun;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Les rayons de soleil sous les feuillages. Toutes les deux secondes, on cherche autour du joueur les
 * trouées : une colonne sans feuilles entourée de feuillage épais (au moins trois blocs de feuilles,
 * plus haut que le sol de la trouée, sur trois des quatre côtés). Par chaque trouée tombe un rayon, incliné comme le soleil, qui
 * s'allume au lever, s'éteint sous la pluie et ressort d'autant plus que la brume est épaisse :
 * c'est la poussière en suspension qui rend la lumière visible.
 *
 * <p>Au plus 24 rayons, les plus proches ; deux quads chacun.
 */
public final class SunShafts {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/environment/sun_shaft.png");
    private static final int RADIUS = 22;
    private static final int MAX = 24;
    private static final int[][] AROUND = {{3, 0}, {-3, 0}, {0, 3}, {0, -3}};

    /** Une trouée : le trou dans le feuillage (x, haut, z) et le sol sous lui. */
    private record Gap(float x, float z, float canopy, float ground, float phase) {
    }

    private static List<Gap> gaps = List.of();
    private static long scannedTick = -1000L; // pas MIN_VALUE : tick - scannedTick déborderait

    private SunShafts() {
    }

    static void scan(ClientLevel level, BlockPos centre) {
        long tick = level.getGameTime();
        if (!FallowConfig.rayonsShafts() || tick - scannedTick < 40) {
            return;
        }
        scannedTick = tick;
        List<Gap> found = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -RADIUS; dx <= RADIUS; dx += 2) {
            for (int dz = -RADIUS; dz <= RADIUS; dz += 2) {
                int x = centre.getX() + dx;
                int z = centre.getZ() + dz;
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    continue;
                }
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                if (leafDepth(level, pos, x, z, top) > 0) {
                    continue; // des feuilles au-dessus : pas de trouée ici
                }
                int covered = 0;
                int canopy = 0;
                for (int[] o : AROUND) {
                    int nx = x + o[0];
                    int nz = z + o[1];
                    int nTop = level.getHeight(Heightmap.Types.MOTION_BLOCKING, nx, nz);
                    if (nTop > top + 3 && leafDepth(level, pos, nx, nz, nTop) >= 3) {
                        covered++;
                        canopy += nTop;
                    }
                }
                if (covered >= 3) {
                    float phase = (float) ((x * 31 + z * 17) & 63) / 10.0F;
                    found.add(new Gap(x + 0.5F, z + 0.5F, (float) canopy / covered, top, phase));
                }
            }
        }
        double cx = centre.getX() + 0.5;
        double cz = centre.getZ() + 0.5;
        found.sort(Comparator.comparingDouble(g -> Mth.square(g.x - cx) + Mth.square(g.z - cz)));
        gaps = found.size() > MAX ? List.copyOf(found.subList(0, MAX)) : List.copyOf(found);
    }

    /**
     * Épaisseur de feuilles (0 à 4) sous le haut d'une colonne. Le client ne tient pas la carte des
     * hauteurs sans feuilles ({@code MOTION_BLOCKING_NO_LEAVES} n'est connue que du serveur) : on
     * descend donc bloc par bloc.
     */
    private static int leafDepth(ClientLevel level, BlockPos.MutableBlockPos pos, int x, int z, int top) {
        int depth = 0;
        while (depth < 4 && level.getBlockState(pos.set(x, top - 1 - depth, z)).is(BlockTags.LEAVES)) {
            depth++;
        }
        return depth;
    }

    static void render(ClientLevel level, Vec3 cam, float partialTick) {
        if (!FallowConfig.rayonsShafts() || gaps.isEmpty()) {
            return;
        }
        Vector3f sun = Sun.direction(level, partialTick);
        float day = Sun.daylight(sun);
        float clear = 1.0F - level.getRainLevel(partialTick);
        if (day <= 0.01F || clear <= 0.05F || sun.y() < 0.12F) {
            return;
        }
        float base = 0.30F * day * clear * clear * (0.45F + 0.55F * Sun.mistiness()) * (float) FallowConfig.rayonsShaftStrength();
        float[] colour = Sun.colour(sun);
        // pas de rayon trop couché : au-delà, il traverserait tout le sous-bois
        float lean = Math.min(1.4F, (float) Math.sqrt(sun.x() * sun.x() + sun.z() * sun.z()) / sun.y());
        Vector3f down = new Vector3f(-sun.x(), -sun.y(), -sun.z()).normalize();
        float time = level.getGameTime() + partialTick;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        boolean any = false;
        for (Gap gap : gaps) {
            float height = gap.canopy - gap.ground;
            if (height < 3.0F) {
                continue;
            }
            float length = height / Math.max(0.2F, -down.y());
            if (lean > 1.39F) {
                length = Math.min(length, height * 1.7F);
            }
            Vector3f top = new Vector3f((float) (gap.x - cam.x), (float) (gap.canopy - cam.y), (float) (gap.z - cam.z));
            Vector3f bottom = new Vector3f(down).mul(length).add(top);
            Vector3f mid = new Vector3f(top).add(bottom).mul(0.5F);
            float distance = mid.length();
            // les feuilles bougent : le rayon respire lentement
            float sway = 0.75F + 0.25F * Mth.sin(time * 0.035F + gap.phase);
            float near = Sun.smoothstep(1.5F, 5.0F, distance);
            float alpha = base * sway * near * (1.0F - 0.6F * Sun.fogAt(distance));
            if (alpha <= 0.003F) {
                continue;
            }
            quad(buffer, top, bottom, mid, 1.7F, colour, alpha);
            quad(buffer, top, bottom, mid, 0.7F, colour, alpha * 0.8F);
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

    /** Un quad tourné vers la caméra autour de l'axe du rayon. */
    private static void quad(BufferBuilder buffer, Vector3f top, Vector3f bottom, Vector3f mid, float width, float[] c, float alpha) {
        Vector3f axis = new Vector3f(bottom).sub(top);
        Vector3f side = axis.cross(new Vector3f(mid).negate(), new Vector3f());
        if (side.lengthSquared() < 1.0E-6F) {
            return;
        }
        side.normalize().mul(width * 0.5F);
        buffer.addVertex(top.x - side.x, top.y - side.y, top.z - side.z).setUv(0.0F, 0.0F).setColor(c[0], c[1], c[2], alpha);
        buffer.addVertex(top.x + side.x, top.y + side.y, top.z + side.z).setUv(1.0F, 0.0F).setColor(c[0], c[1], c[2], alpha);
        buffer.addVertex(bottom.x + side.x, bottom.y + side.y, bottom.z + side.z).setUv(1.0F, 1.0F).setColor(c[0], c[1], c[2], alpha);
        buffer.addVertex(bottom.x - side.x, bottom.y - side.y, bottom.z - side.z).setUv(0.0F, 1.0F).setColor(c[0], c[1], c[2], alpha);
    }
}
