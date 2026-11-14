package io.github.salvagedisc.compline.relief;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.clairiere.ClearingClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

/**
 * La texture {@code compline:textures/relief.png} : 256 x 256 colonnes, une par bloc, rangées « en tore » (la colonne du monde
 * (x, z) est le texel (x & 255, z & 255)). Rien n'est donc à recentrer quand la caméra avance : les tronçons qui
 * entrent dans la fenêtre remplacent ceux qui en sortent.
 *
 * <p>Un texel : rouge et vert = hauteur de la surface au-dessus du bas du monde (octet faible, octet fort),
 * bleu = ce qu'il y a au sommet (255 feuillage, 128 eau, 0 autre), alpha = 200 pour une colonne connue (0 sinon).
 * Le texel (0, 0) est l'en-tête : rouge = force du soleil (la brume levée autour de la caméra), vert = bas du monde
 * + 128, bleu = 255 quand la caméra est sous l'eau, alpha = 201 (202 quand un pack de shaders d'Iris dessine l'image). Le pack de ressources reconnaît la texture à cet alpha.</p>
 *
 * <p>Les 256 tronçons de la fenêtre sont relus en boucle, {@value #CHUNKS_PER_TICK} par tic : un tour complet toutes
 * les {@code 256 / CHUNKS_PER_TICK} tics, ce qui suffit pour un arbre abattu ou un mur posé.</p>
 */
final class ReliefTexture {
    static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "textures/relief.png");
    private static final int SIZE = 256;
    private static final int MASK = SIZE - 1;
    private static final int WINDOW_CHUNKS = SIZE / 16;
    private static final int CHUNKS_PER_TICK = 16;
    private static final int KNOWN = 200;
    private static final int HEADER = 201;
    /** En-tête quand un pack de shaders dessine l'image : le pack de ressources laisse alors l'image telle quelle. */
    private static final int HEADER_SHADER_PACK = 202;
    private static final boolean IRIS = ModList.get().isLoaded(ModIds.IRIS);
    /** Force du soleil dans la brume, et temps que met la brume levée d'une clairière à la porter à 1. */
    private static final float MIST_SUN = 0.6F;
    private static final float LIFT_PER_TICK = 1.0F / 30.0F;

    private static DynamicTexture texture;
    private static ClientLevel known;
    private static int cursor;
    private static float lift;

    private ReliefTexture() {
    }

    static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ComplineConfig.reliefEnabled()) {
            if (known != null && texture != null) {
                clear();
                texture.upload();
            }
            known = null;
            return;
        }
        if (texture == null) {
            texture = new DynamicTexture(SIZE, SIZE, true);
            minecraft.getTextureManager().register(ID, texture);
        }
        NativeImage image = texture.getPixels();
        if (image == null) {
            return;
        }
        if (known != level) {
            known = level;
            clear();
            cursor = 0;
            lift = 0.0F;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        int centerX = Mth.floor(camera.x) >> 4;
        int centerZ = Mth.floor(camera.z) >> 4;
        int floor = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < CHUNKS_PER_TICK; i++) {
            int chunkX = centerX - WINDOW_CHUNKS / 2 + (cursor & (WINDOW_CHUNKS - 1));
            int chunkZ = centerZ - WINDOW_CHUNKS / 2 + (cursor / WINDOW_CHUNKS);
            cursor = (cursor + 1) % (WINDOW_CHUNKS * WINDOW_CHUNKS);
            write(image, level, chunkX, chunkZ, floor, pos);
        }

        boolean cleared = ClearingClient.clears(level, BlockPos.containing(camera));
        lift = Mth.clamp(lift + (cleared ? LIFT_PER_TICK : -LIFT_PER_TICK), 0.0F, 1.0F);
        int sun = Math.round(255.0F * Mth.lerp(lift, MIST_SUN, 1.0F));
        boolean underwater = minecraft.gameRenderer.getMainCamera().getFluidInCamera() == FogType.WATER;
        boolean shaderPack = IRIS && ShaderPackCheck.inUse();
        image.setPixelRGBA(0, 0, pack(sun, Mth.clamp(floor + 128, 0, 255), underwater ? 255 : 0, shaderPack ? HEADER_SHADER_PACK : HEADER));
        texture.upload();
    }

    private static void write(NativeImage image, ClientLevel level, int chunkX, int chunkZ, int floor, BlockPos.MutableBlockPos pos) {
        LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int x = (chunkX << 4) + dx, z = (chunkZ << 4) + dz;
                int texel = 0;
                if (chunk != null) {
                    int top = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING, dx, dz);
                    int height = Mth.clamp(top + 1 - floor, 0, 65535);
                    BlockState state = chunk.getBlockState(pos.set(x, top, z));
                    int kind = state.is(BlockTags.LEAVES) ? 255 : state.getFluidState().is(FluidTags.WATER) ? 128 : 0;
                    texel = pack(height & 255, height >> 8, kind, KNOWN);
                }
                if ((x & MASK) != 0 || (z & MASK) != 0) {
                    image.setPixelRGBA(x & MASK, z & MASK, texel);
                }
            }
        }
    }

    private static void clear() {
        NativeImage image = texture.getPixels();
        if (image != null) {
            image.fillRect(0, 0, SIZE, SIZE, 0);
        }
    }

    /** L'ordre des octets d'une {@link NativeImage} : alpha, bleu, vert, rouge. */
    private static int pack(int red, int green, int blue, int alpha) {
        return alpha << 24 | blue << 16 | green << 8 | red;
    }
}
