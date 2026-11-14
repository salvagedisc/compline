package io.github.salvagedisc.compline.clairiere;

import io.github.salvagedisc.compline.Compline;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Les mondes d'avant la 0.8.0 : une clairière y remplaçait le biome par {@code compline:clairiere},
 * {@code clairiere_froide} ou {@code clairiere_chaude}. {@code /compline brume <rayon>} rend ces cases au biome
 * que le générateur du monde y place (rien n'était stocké par case : la graine suffit), quelques tronçons par
 * tick. Les mondes créés depuis n'en contiennent aucune.
 */
final class OldClearings {
    private static final int CHUNKS_PER_TICK = 2;
    private static final Deque<Job> JOBS = new ArrayDeque<>();

    private record Job(ResourceKey<Level> dimension, BlockPos center, int radius, Deque<ChunkPos> chunks, int[] cells) {
    }

    private OldClearings() {
    }

    static void restore(ServerLevel level, BlockPos center, int radius) {
        Deque<ChunkPos> chunks = new ArrayDeque<>();
        for (int cx = (center.getX() - radius) >> 4; cx <= (center.getX() + radius) >> 4; cx++) {
            for (int cz = (center.getZ() - radius) >> 4; cz <= (center.getZ() + radius) >> 4; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        JOBS.add(new Job(level.dimension(), center, radius, chunks, new int[1]));
    }

    static void tick(MinecraftServer server) {
        Job job = JOBS.peek();
        if (job == null) {
            return;
        }
        ServerLevel level = server.getLevel(job.dimension);
        if (level == null) {
            JOBS.poll();
            return;
        }
        List<ChunkAccess> changed = new ArrayList<>();
        for (int budget = CHUNKS_PER_TICK; budget > 0 && !job.chunks.isEmpty(); budget--) {
            ChunkPos pos = job.chunks.poll();
            ChunkAccess chunk = level.getChunk(pos.x, pos.z);
            if (restoreChunk(level, chunk, job)) {
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }
        if (!changed.isEmpty()) {
            level.getChunkSource().chunkMap.resendBiomesForChunks(changed);
        }
        if (job.chunks.isEmpty()) {
            JOBS.poll();
            if (job.cells[0] > 0) {
                Compline.LOGGER.info("clairiere : {} anciennes cases de clairière rendues à leur biome autour de {} (rayon {})",
                        job.cells[0], job.center, job.radius);
            }
        }
    }

    private static boolean restoreChunk(ServerLevel level, ChunkAccess chunk, Job job) {
        BiomeSource generator = level.getChunkSource().getGenerator().getBiomeSource();
        Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
        long radius2 = (long) job.radius * job.radius;
        int before = job.cells[0];
        // Coordonnées en quarts de bloc (une case de biome = 4 × 4 × 4 blocs), comme /fillbiome.
        chunk.fillBiomesFromNoise((qx, qy, qz, unused) -> {
            Holder<Biome> current = chunk.getNoiseBiome(qx, qy, qz);
            long dx = (qx << 2) + 2 - job.center.getX(), dz = (qz << 2) + 2 - job.center.getZ();
            if (dx * dx + dz * dz > radius2 || !Clearings.isOldClearing(current)) {
                return current;
            }
            job.cells[0]++;
            return generator.getNoiseBiome(qx, qy, qz, sampler);
        }, sampler);
        return job.cells[0] > before;
    }

    /** Les travaux en cours sont perdus à l'arrêt du serveur : relancer {@code /compline brume}. */
    static void clear() {
        JOBS.clear();
    }
}
