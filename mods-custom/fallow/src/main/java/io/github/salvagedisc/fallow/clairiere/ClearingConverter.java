package io.github.salvagedisc.fallow.clairiere;

import io.github.salvagedisc.fallow.Fallow;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Change les biomes d'un disque en clairière, comme {@code /fillbiome} mais sans sa limite de taille, et
 * les rend à la brume quand ce qui les a levés disparaît. Le travail est étalé : quelques tronçons par
 * tick, puis les clients reçoivent les nouveaux biomes (le module ciel vide alors le cache de brume
 * d'ATMOSPHERICS).
 *
 * <p>Seuls les biomes du tag {@code fallow:peut_devenir_clairiere} changent : rivières, océans, plages et
 * grottes restent. Le biome d'arrivée suit la température du biome d'origine : {@code fallow:clairiere_froide},
 * {@code fallow:clairiere} ou {@code fallow:clairiere_chaude}.
 *
 * <p>Pour rendre la brume, une case redevient le biome que le générateur du monde y place, sauf si une
 * autre source ({@link ClearingSources}) la couvre encore. Rien n'est stocké par case : la graine suffit.
 */
public final class ClearingConverter {
    public static final TagKey<Biome> CONVERTIBLE =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "peut_devenir_clairiere"));
    static final ResourceKey<Biome> CLEARING = biome("clairiere");
    static final ResourceKey<Biome> CLEARING_WARM = biome("clairiere_chaude");
    static final ResourceKey<Biome> CLEARING_COLD = biome("clairiere_froide");

    /** En dessous : clairière froide (la neige continue de tomber). Au-dessus de {@link #WARM_ABOVE} : chaude. */
    private static final float COLD_BELOW = 0.3F;
    private static final float WARM_ABOVE = 1.0F;

    private static final Deque<Job> JOBS = new ArrayDeque<>();

    private ClearingConverter() {
    }

    private static boolean isClearing(Holder<Biome> biome) {
        return biome.is(CLEARING) || biome.is(CLEARING_WARM) || biome.is(CLEARING_COLD);
    }

    private static ResourceKey<Biome> biome(String path) {
        return ResourceKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, path));
    }

    /** Programme la conversion du disque de rayon {@code radius} autour de {@code center}, et retient la source. */
    public static void convert(ServerLevel level, BlockPos center, int radius, @Nullable ServerPlayer player) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        ClearingSources.get(level).add(center, radius);
        JOBS.add(new Job(level.dimension(), center, radius, chunksAround(center, radius), uuid(player), false));
    }

    /** La source posée en {@code center} disparaît : la brume revient dans son disque, sauf là où une autre la retient. */
    public static void restore(ServerLevel level, BlockPos center, int radius, @Nullable ServerPlayer player) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        ClearingSources.get(level).remove(center);
        JOBS.add(new Job(level.dimension(), center, radius, chunksAround(center, radius), uuid(player), true));
    }

    /** Retire toutes les sources dont le centre est dans le disque et rend la brume au disque et aux leurs. */
    static void restoreWithin(ServerLevel level, BlockPos center, int radius, @Nullable ServerPlayer player) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        for (ClearingSources.Source source : ClearingSources.get(level).removeWithin(center, radius)) {
            JOBS.add(new Job(level.dimension(), source.center(), source.radius(),
                    chunksAround(source.center(), source.radius()), null, true));
        }
        JOBS.add(new Job(level.dimension(), center, radius, chunksAround(center, radius), uuid(player), true));
    }

    @Nullable
    private static UUID uuid(@Nullable ServerPlayer player) {
        return player == null ? null : player.getUUID();
    }

    private static List<ChunkPos> chunksAround(BlockPos center, int radius) {
        List<ChunkPos> chunks = new ArrayList<>();
        int minX = (center.getX() - radius) >> 4, maxX = (center.getX() + radius) >> 4;
        int minZ = (center.getZ() - radius) >> 4, maxZ = (center.getZ() + radius) >> 4;
        for (int cx = minX; cx <= maxX; cx++) {
            for (int cz = minZ; cz <= maxZ; cz++) {
                chunks.add(new ChunkPos(cx, cz));
            }
        }
        return chunks;
    }

    /** Appelé à chaque fin de tick serveur. */
    static void tick(MinecraftServer server, int chunksPerTick) {
        int budget = chunksPerTick;
        while (budget > 0 && !JOBS.isEmpty()) {
            Job job = JOBS.peek();
            ServerLevel level = server.getLevel(job.dimension);
            if (level == null) {
                JOBS.poll();
                continue;
            }
            while (budget > 0 && job.next < job.chunks.size()) {
                job.processChunk(level, job.chunks.get(job.next++));
                budget--;
            }
            if (job.next >= job.chunks.size()) {
                JOBS.poll();
                job.finish(level);
            }
        }
    }

    /**
     * Les travaux en cours sont perdus à l'arrêt du serveur : reposer le bloc relance une conversion,
     * {@code /fallow brume} un retour de la brume.
     */
    static void clear() {
        JOBS.clear();
    }

    private static final class Job {
        final ResourceKey<Level> dimension;
        final BlockPos center;
        final int radius;
        final List<ChunkPos> chunks;
        @Nullable final UUID player;
        /** Vrai : la brume revient ; faux : elle se lève. */
        final boolean restore;
        final List<ChunkAccess> changed = new ArrayList<>();
        int next;
        int cells;

        Job(ResourceKey<Level> dimension, BlockPos center, int radius, List<ChunkPos> chunks, @Nullable UUID player,
                boolean restore) {
            this.dimension = dimension;
            this.center = center;
            this.radius = radius;
            this.chunks = chunks;
            this.player = player;
            this.restore = restore;
        }

        void processChunk(ServerLevel level, ChunkPos pos) {
            if (restore) {
                restoreChunk(level, pos);
            } else {
                convertChunk(level, pos);
            }
        }

        void restoreChunk(ServerLevel level, ChunkPos pos) {
            ClearingSources sources = ClearingSources.get(level);
            BiomeSource generator = level.getChunkSource().getGenerator().getBiomeSource();
            Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
            ChunkAccess chunk = level.getChunk(pos.x, pos.z);
            long r2 = (long) radius * radius;
            int before = cells;
            chunk.fillBiomesFromNoise((qx, qy, qz, unused) -> {
                Holder<Biome> current = chunk.getNoiseBiome(qx, qy, qz);
                long x = (qx << 2) + 2, z = (qz << 2) + 2;
                long dx = x - center.getX(), dz = z - center.getZ();
                if (dx * dx + dz * dz > r2 || !isClearing(current) || sources.covers(x, z)) {
                    return current;
                }
                cells++;
                return generator.getNoiseBiome(qx, qy, qz, sampler);
            }, sampler);
            if (cells > before) {
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }

        void convertChunk(ServerLevel level, ChunkPos pos) {
            Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> clearing = biomes.getHolderOrThrow(CLEARING);
            Holder<Biome> warm = biomes.getHolderOrThrow(CLEARING_WARM);
            Holder<Biome> cold = biomes.getHolderOrThrow(CLEARING_COLD);
            ChunkAccess chunk = level.getChunk(pos.x, pos.z);
            long r2 = (long) radius * radius;
            int before = cells;
            // Coordonnées en quarts de bloc (une case de biome = 4 × 4 × 4 blocs), comme /fillbiome.
            chunk.fillBiomesFromNoise((qx, qy, qz, sampler) -> {
                Holder<Biome> current = chunk.getNoiseBiome(qx, qy, qz);
                long dx = (qx << 2) + 2 - center.getX();
                long dz = (qz << 2) + 2 - center.getZ();
                if (dx * dx + dz * dz > r2 || !current.is(CONVERTIBLE)) {
                    return current;
                }
                cells++;
                float temperature = current.value().getBaseTemperature();
                return temperature < COLD_BELOW ? cold : temperature > WARM_ABOVE ? warm : clearing;
            }, level.getChunkSource().randomState().sampler());
            if (cells > before) {
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }

        void finish(ServerLevel level) {
            if (!changed.isEmpty()) {
                level.getChunkSource().chunkMap.resendBiomesForChunks(changed);
            }
            Fallow.LOGGER.info("clairiere : {} cases de biome {} autour de {} (rayon {})", cells,
                    restore ? "rendues à la brume" : "converties", center, radius);
            ServerPlayer target = player == null ? null : level.getServer().getPlayerList().getPlayer(player);
            if (target != null && (cells > 0 || !restore)) {
                target.displayClientMessage(Component.translatable(restore ? "message.fallow.clairiere.retour"
                        : cells > 0 ? "message.fallow.clairiere.dissipee" : "message.fallow.clairiere.rien"), true);
            }
        }
    }
}
