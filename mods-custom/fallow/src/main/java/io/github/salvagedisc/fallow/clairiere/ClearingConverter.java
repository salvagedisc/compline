package io.github.salvagedisc.fallow.clairiere;

import io.github.salvagedisc.fallow.Fallow;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.levelgen.Heightmap;
import org.jetbrains.annotations.Nullable;

/**
 * Change les biomes d'un disque en clairière, comme {@code /fillbiome} mais sans sa limite de taille, et
 * les rend à la brume quand ce qui les a levés disparaît. Le changement avance en vague, un anneau de
 * 4 blocs à la fois : du centre vers le bord quand la brume se lève, du bord vers le centre quand elle
 * revient, avec des volutes au sol sur le front. Chaque anneau est envoyé aux clients dès qu'il est fait
 * (le module ciel vide alors le cache de brume d'ATMOSPHERICS et ralentit son fondu).
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
        boolean covered = false;
        for (ClearingSources.Source source : ClearingSources.get(level).removeWithin(center, radius)) {
            // Une source qui couvre tout le disque le rend déjà : un second passage ne trouverait plus rien.
            boolean whole = !covered && source.center().equals(center) && source.radius() >= radius;
            covered |= whole;
            JOBS.add(new Job(level.dimension(), source.center(), source.radius(),
                    chunksAround(source.center(), source.radius()), whole ? uuid(player) : null, true));
        }
        if (!covered) {
            JOBS.add(new Job(level.dimension(), center, radius, chunksAround(center, radius), uuid(player), true));
        }
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

    /**
     * Appelé à chaque fin de tick serveur. Un travail charge d'abord ses tronçons ({@code chunksPerTick}
     * par tick), puis change le disque anneau par anneau, un anneau de 4 blocs tous les {@code ringTicks}.
     */
    static void tick(MinecraftServer server, int chunksPerTick, int ringTicks) {
        Job job = JOBS.peek();
        if (job == null) {
            return;
        }
        ServerLevel level = server.getLevel(job.dimension);
        if (level == null) {
            JOBS.poll();
            return;
        }
        if (job.loaded < job.chunks.size()) {
            for (int budget = chunksPerTick; budget > 0 && job.loaded < job.chunks.size(); budget--) {
                ChunkPos pos = job.chunks.get(job.loaded++);
                level.getChunk(pos.x, pos.z);
            }
            return;
        }
        if (job.wait-- > 0) {
            return;
        }
        job.wait = ringTicks - 1;
        job.step(level);
        if (job.done()) {
            JOBS.poll();
            job.finish(level);
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
        /** Largeur d'un anneau : une case de biome. */
        private static final int RING = 4;
        /** Volutes envoyées au plus par anneau, et distance à laquelle les joueurs les voient. */
        private static final int MAX_PUFFS = 160;
        private static final double PUFF_RANGE = 160.0;

        final ResourceKey<Level> dimension;
        final BlockPos center;
        final int radius;
        final List<ChunkPos> chunks;
        @Nullable final UUID player;
        /** Vrai : la brume revient, du bord vers le centre ; faux : elle se lève, du centre vers le bord. */
        final boolean restore;
        final int rings;
        int loaded;
        int ring;
        int wait;
        int cells;

        Job(ResourceKey<Level> dimension, BlockPos center, int radius, List<ChunkPos> chunks, @Nullable UUID player,
                boolean restore) {
            this.dimension = dimension;
            this.center = center;
            this.radius = radius;
            this.chunks = chunks;
            this.player = player;
            this.restore = restore;
            this.rings = radius / RING + 1;
        }

        boolean done() {
            return ring >= rings;
        }

        /** Change l'anneau suivant dans tous les tronçons qu'il traverse, puis l'envoie aux clients. */
        void step(ServerLevel level) {
            int index = restore ? rings - 1 - ring : ring;
            ring++;
            long lo = (long) index * RING, hi = lo + RING;
            long lo2 = lo * lo, hi2 = Math.min(hi * hi, (long) radius * radius + 1);
            LongSet columns = new LongOpenHashSet();
            List<ChunkAccess> changed = new ArrayList<>();
            for (ChunkPos pos : chunks) {
                if (!crosses(pos, lo, hi)) {
                    continue;
                }
                ChunkAccess chunk = level.getChunk(pos.x, pos.z);
                if (restore ? restoreChunk(level, chunk, lo2, hi2, columns) : convertChunk(level, chunk, lo2, hi2, columns)) {
                    chunk.setUnsaved(true);
                    changed.add(chunk);
                }
            }
            if (!changed.isEmpty()) {
                level.getChunkSource().chunkMap.resendBiomesForChunks(changed);
                puffs(level, columns);
            }
        }

        /** Vrai si le tronçon a au moins une case entre {@code lo} et {@code hi} blocs du centre. */
        private boolean crosses(ChunkPos pos, long lo, long hi) {
            long nx = Math.max(pos.getMinBlockX(), Math.min(center.getX(), pos.getMaxBlockX())) - center.getX();
            long nz = Math.max(pos.getMinBlockZ(), Math.min(center.getZ(), pos.getMaxBlockZ())) - center.getZ();
            long fx = Math.max(Math.abs(pos.getMinBlockX() - center.getX()), Math.abs(pos.getMaxBlockX() - center.getX()));
            long fz = Math.max(Math.abs(pos.getMinBlockZ() - center.getZ()), Math.abs(pos.getMaxBlockZ() - center.getZ()));
            return nx * nx + nz * nz <= hi * hi && fx * fx + fz * fz >= lo * lo;
        }

        /** Distance au carré entre le milieu de la case {@code (qx, qz)} et le centre. */
        private long distance2(int qx, int qz) {
            long dx = (qx << 2) + 2 - center.getX();
            long dz = (qz << 2) + 2 - center.getZ();
            return dx * dx + dz * dz;
        }

        private boolean restoreChunk(ServerLevel level, ChunkAccess chunk, long lo2, long hi2, LongSet columns) {
            ClearingSources sources = ClearingSources.get(level);
            BiomeSource generator = level.getChunkSource().getGenerator().getBiomeSource();
            Climate.Sampler sampler = level.getChunkSource().randomState().sampler();
            int before = cells;
            chunk.fillBiomesFromNoise((qx, qy, qz, unused) -> {
                Holder<Biome> current = chunk.getNoiseBiome(qx, qy, qz);
                long d2 = distance2(qx, qz);
                if (d2 < lo2 || d2 >= hi2 || !isClearing(current) || sources.covers((qx << 2) + 2, (qz << 2) + 2)) {
                    return current;
                }
                cells++;
                columns.add(ChunkPos.asLong(qx, qz));
                return generator.getNoiseBiome(qx, qy, qz, sampler);
            }, sampler);
            return cells > before;
        }

        private boolean convertChunk(ServerLevel level, ChunkAccess chunk, long lo2, long hi2, LongSet columns) {
            Registry<Biome> biomes = level.registryAccess().registryOrThrow(Registries.BIOME);
            Holder<Biome> clearing = biomes.getHolderOrThrow(CLEARING);
            Holder<Biome> warm = biomes.getHolderOrThrow(CLEARING_WARM);
            Holder<Biome> cold = biomes.getHolderOrThrow(CLEARING_COLD);
            int before = cells;
            // Coordonnées en quarts de bloc (une case de biome = 4 × 4 × 4 blocs), comme /fillbiome.
            chunk.fillBiomesFromNoise((qx, qy, qz, sampler) -> {
                Holder<Biome> current = chunk.getNoiseBiome(qx, qy, qz);
                long d2 = distance2(qx, qz);
                if (d2 < lo2 || d2 >= hi2 || !current.is(CONVERTIBLE)) {
                    return current;
                }
                cells++;
                columns.add(ChunkPos.asLong(qx, qz));
                float temperature = current.value().getBaseTemperature();
                return temperature < COLD_BELOW ? cold : temperature > WARM_ABOVE ? warm : clearing;
            }, level.getChunkSource().randomState().sampler());
            return cells > before;
        }

        /**
         * Des volutes de brume au sol, sur les colonnes que l'anneau vient de changer : poussées vers
         * l'extérieur quand la brume se lève, vers le centre quand elle revient.
         */
        private void puffs(ServerLevel level, LongSet columns) {
            List<ServerPlayer> viewers = level.getPlayers(p -> p.distanceToSqr(center.getX(), p.getY(), center.getZ())
                    < (radius + PUFF_RANGE) * (radius + PUFF_RANGE));
            if (viewers.isEmpty()) {
                return;
            }
            int every = Math.max(1, columns.size() / MAX_PUFFS);
            int i = 0;
            for (long column : columns) {
                if (i++ % every != 0) {
                    continue;
                }
                double x = (ChunkPos.getX(column) << 2) + level.random.nextDouble() * 4;
                double z = (ChunkPos.getZ(column) << 2) + level.random.nextDouble() * 4;
                double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) x, (int) z) + 0.4
                        + level.random.nextDouble() * 1.5;
                double dx = x - center.getX(), dz = z - center.getZ();
                double length = Math.max(1.0, Math.sqrt(dx * dx + dz * dz));
                double push = restore ? -0.12 : 0.12;
                for (ServerPlayer viewer : viewers) {
                    level.sendParticles(viewer, ParticleTypes.CLOUD, true, x, y, z, 0,
                            dx / length * push, 0.02, dz / length * push, 1.0);
                }
            }
        }

        void finish(ServerLevel level) {
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
