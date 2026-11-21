package io.github.salvagedisc.fallow.vent;

import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.ModIds;
import io.github.salvagedisc.fallow.climat.client.StormView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Le vent qu'on lit sans pack de shaders. Sous Brumaire, les herbes et les feuilles ondulent au vent du pack ; sans
 * lui, rien ne bouge que la fumée des feux et les feuilles de Particular, alors que le vent porte l'odeur du joueur
 * vers les bêtes (module approche). À l'air libre sur la Surface, de fins brins pâles (poussière, pollen) naissent
 * près du sol autour de la caméra et filent avec le vent : plus nombreux et plus rapides quand il forcit, presque
 * aucun par temps calme. Rien la nuit noire, rien sous l'eau, rien dans une tempête (le module climat a sa poussière).
 */
public final class WindMotes {
    /** Rayon de la boîte où naissent les brins, autour de la caméra. */
    private static final int RADIUS = 12;
    private static final RandomSource RANDOM = RandomSource.create();
    private static final boolean IRIS = ModIds.isLoaded(ModIds.IRIS);

    private WindMotes() {
    }

    static void tick(ClientLevel level, Vec3 camera) {
        if (!FallowConfig.ventMotes() || level.dimension() != Level.OVERWORLD || shaderInUse()
                || StormView.sand() > 0.1F || StormView.snow() > 0.1F) {
            return;
        }
        float force = Wind.strength();
        // D'un brin tous les huit ticks par temps calme (force 0,18) à trois par tick dans un vent fort.
        float rate = Math.min(3.0F, Math.max(0.0F, force - 0.15F) * 4.0F);
        if (level.isThundering()) {
            rate *= 0.5F;
        }
        // La nuit, la poussière ne se voit plus guère : peu de brins, plus sombres.
        float light = Math.max(0.25F, 1.0F - level.getSkyDarken(1.0F) / 11.0F);
        int count = (int) rate + (RANDOM.nextFloat() < rate - (int) rate ? 1 : 0);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < count; i++) {
            double x = camera.x + (RANDOM.nextDouble() * 2.0 - 1.0) * RADIUS;
            double z = camera.z + (RANDOM.nextDouble() * 2.0 - 1.0) * RADIUS;
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) Math.floor(x), (int) Math.floor(z));
            if (Math.abs(ground - camera.y) > 10.0) {
                continue;
            }
            double y = ground + 0.2 + RANDOM.nextDouble() * 2.5;
            pos.set(x, y, z);
            if (!level.getBlockState(pos).isAir() || !level.canSeeSky(pos)) {
                continue;
            }
            Vec3 wind = Wind.velocity(x, z).scale(1.0 / 20.0);
            float shade = (0.82F + RANDOM.nextFloat() * 0.18F) * light;
            DustParticleOptions mote = new DustParticleOptions(
                    new Vector3f(0.93F * shade, 0.91F * shade, 0.84F * shade), 0.35F + RANDOM.nextFloat() * 0.35F);
            // Les grains de poussière du jeu ralentissent leur vitesse de départ au dixième : on la donne décuplée.
            level.addParticle(mote, x, y, z, (wind.x + RANDOM.nextGaussian() * 0.01) * 10.0,
                    RANDOM.nextGaussian() * 0.05, (wind.z + RANDOM.nextGaussian() * 0.01) * 10.0);
        }
    }

    private static boolean shaderInUse() {
        return IRIS && IrisHolder.inUse();
    }

    /** Iris n'est chargé qu'ici : sans lui, la classe n'est jamais lue. */
    private static final class IrisHolder {
        static boolean inUse() {
            return net.irisshaders.iris.api.v0.IrisApi.getInstance().isShaderPackInUse();
        }
    }
}
