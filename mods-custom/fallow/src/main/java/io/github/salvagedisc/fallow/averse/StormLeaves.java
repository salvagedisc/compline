package io.github.salvagedisc.fallow.averse;

import com.leclowndu93150.particular.CommonClass;
import io.github.salvagedisc.fallow.FallowConfig;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Feuilles arrachées par le vent : en plus des feuilles que Particular fait tomber, un arbre en perd
 * d'autant plus que le vent forcit, jusqu'à quatre fois plus sous l'orage. Elles partent déjà
 * lancées dans le sens du vent. Seulement avec Particular (ses feuilles, ses couleurs par essence).
 */
public final class StormLeaves {
    private StormLeaves() {
    }

    public static void maybeTear(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level instanceof ClientLevel client) || !FallowConfig.averseWindblownLeaves()) {
            return;
        }
        float[] wind = Wind.here(client);
        float thunder = client.getThunderLevel(1.0F);
        float chance = Math.min(0.07F, Math.max(0.0F, wind[2] - 0.9F) * 0.025F + thunder * 0.035F);
        if (chance <= 0.0F || random.nextFloat() >= chance) {
            return;
        }
        BlockPos below = pos.below();
        BlockState under = level.getBlockState(below);
        double x;
        double y;
        double z;
        if (!Block.isFaceFull(under.getCollisionShape(level, below), Direction.UP)) {
            x = pos.getX() + 0.02 + random.nextDouble() * 0.96;
            y = pos.getY() - 0.05;
            z = pos.getZ() + 0.02 + random.nextDouble() * 0.96;
        } else {
            // sous le vent, une feuille peut aussi partir par le flanc de l'arbre
            int dx = Math.round(wind[0]);
            int dz = Math.round(wind[1]);
            BlockPos side = pos.offset(dx, 0, dz);
            if ((dx == 0 && dz == 0) || !level.getBlockState(side).isAir()) {
                return;
            }
            x = pos.getX() + 0.5 + dx * 0.55 + (random.nextDouble() - 0.5) * 0.8;
            y = pos.getY() + random.nextDouble() * 0.9;
            z = pos.getZ() + 0.5 + dz * 0.55 + (random.nextDouble() - 0.5) * 0.8;
        }
        CommonClass.LeafData data = CommonClass.getLeafData(state.getBlock());
        ParticleOptions options = data == null ? null : data.getParticle();
        if (options == null) {
            return;
        }
        Particle leaf = Minecraft.getInstance().particleEngine.createParticle(options, x, y, z, 0.0, 0.0, 0.0);
        if (leaf == null) {
            return;
        }
        Color color = data.getColor(level, pos);
        leaf.setColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F);
        float speed = Math.min(wind[2], 3.0F) * 0.05F;
        leaf.setParticleSpeed(wind[0] * speed, 0.0, wind[1] * speed);
    }
}
