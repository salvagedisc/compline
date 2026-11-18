package io.github.salvagedisc.fallow.vent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * La fumée des feux de camp : la même particule que le jeu, mais menée par le vent du pack, plus grosse et
 * plus durable quand plusieurs feux brûlent côte à côte (leurs fumées se rejoignent en un seul panache), plus
 * sombre et bleuie en montant au-dessus d'un feu des âmes.
 */
public final class Smoke {
    /** Les blocs qui, posés sur un feu, cachent sa fumée (marmites et fourneaux de Farm & Charm). */
    private static final TagKey<Block> SUPPRESS = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath("farm_and_charm", "suppress_campfire_smoke_particles"));
    /** Rayon, en blocs, dans lequel les feux voisins grossissent le panache. */
    private static final int REACH = 2;

    private Smoke() {
    }

    /** Remplace {@code CampfireBlock.makeParticles} côté client. */
    public static void emit(Level level, BlockPos pos, boolean signal, boolean extra) {
        if (level.getBlockState(pos.above()).is(SUPPRESS)) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RandomSource random = level.getRandom();
        BlockState here = level.getBlockState(pos);
        boolean soul = here.is(Blocks.SOUL_CAMPFIRE);

        int count = 0;
        double sumX = 0.0, sumZ = 0.0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -REACH; dx <= REACH; dx++) {
            for (int dz = -REACH; dz <= REACH; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockState state = level.getBlockState(cursor.setWithOffset(pos, dx, dy, dz));
                    if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT)) {
                        count++;
                        sumX += cursor.getX() + 0.5;
                        sumZ += cursor.getZ() + 0.5;
                    }
                }
            }
        }
        count = Math.max(count, 1);
        float plume = Math.min(1.0F + 0.3F * (count - 1), 2.5F);
        double mergeX = count > 1 ? sumX / count : pos.getX() + 0.5;
        double mergeZ = count > 1 ? sumZ / count : pos.getZ() + 0.5;

        double x = pos.getX() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1);
        double y = pos.getY() + random.nextDouble() + random.nextDouble();
        double z = pos.getZ() + 0.5 + random.nextDouble() / 3.0 * (random.nextBoolean() ? 1 : -1);
        Particle particle = minecraft.particleEngine.createParticle(
                signal ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0, 0.07, 0.0);
        if (particle instanceof SmokePlume smoke) {
            smoke.fallow$plume(plume, soul, mergeX, mergeZ);
        }
        if (extra) {
            level.addParticle(ParticleTypes.SMOKE, pos.getX() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1),
                    pos.getY() + 0.4, pos.getZ() + 0.5 + random.nextDouble() / 4.0 * (random.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
        }
    }
}
