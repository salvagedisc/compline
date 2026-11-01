package io.github.salvagedisc.fallow.averse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Brins d'herbe projetés quand le joueur court dans les hautes herbes : de petits morceaux de la
 * texture de la plante traversée (donc de sa couleur et de celle du pack), lancés vers l'avant et vers
 * le haut, freinés par l'air et emportés par le vent.
 */
public final class GrassBits {
    private GrassBits() {
    }

    static void tick(ClientLevel level, LocalPlayer player) {
        if (!player.onGround() || player.isPassenger() || player.isSpectator()) {
            return;
        }
        Vec3 motion = player.getDeltaMovement();
        double speed = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (speed < 0.08) {
            return;
        }
        BlockPos feet = player.blockPosition();
        BlockState state = level.getBlockState(feet);
        if (!(state.getBlock() instanceof BushBlock) || !state.canBeReplaced()) {
            return;
        }
        RandomSource random = level.random;
        // en marchant, un brin de temps en temps ; en courant, deux ou trois par tick
        float rate = (float) Mth.clamp((speed - 0.08) / 0.2, 0.0, 1.0) * 2.5F;
        int count = (int) rate + (random.nextFloat() < rate - (int) rate ? 1 : 0);
        float[] wind = Wind.at(level, player.getX(), player.getY(), player.getZ());
        for (int i = 0; i < count; i++) {
            double x = player.getX() + (random.nextDouble() - 0.5) * 0.8;
            double y = feet.getY() + 0.2 + random.nextDouble() * 0.5;
            double z = player.getZ() + (random.nextDouble() - 0.5) * 0.8;
            double vx = motion.x * 0.6 + (random.nextDouble() - 0.5) * 0.08;
            double vy = 0.06 + random.nextDouble() * 0.08;
            double vz = motion.z * 0.6 + (random.nextDouble() - 0.5) * 0.08;
            Minecraft.getInstance().particleEngine.add(new Bit(level, x, y, z, vx, vy, vz, state, feet, wind));
        }
    }

    /** Un brin : un éclat de la texture de la plante, plus léger qu'un débris de bloc. */
    static final class Bit extends TerrainParticle {
        private final float windX;
        private final float windZ;

        Bit(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, BlockState state, BlockPos pos, float[] wind) {
            super(level, x, y, z, vx, vy, vz, state, pos);
            this.xd = vx;
            this.yd = vy;
            this.zd = vz;
            this.gravity = 0.35F;
            this.friction = 0.92F;
            this.lifetime = 25 + this.random.nextInt(25);
            this.quadSize *= 0.8F;
            float strength = Math.min(wind[2], 3.0F);
            this.windX = wind[0] * strength * 0.02F;
            this.windZ = wind[1] * strength * 0.02F;
        }

        @Override
        public void tick() {
            super.tick();
            if (!this.onGround) {
                this.xd += this.windX * 0.15;
                this.zd += this.windZ * 0.15;
            }
        }
    }
}
