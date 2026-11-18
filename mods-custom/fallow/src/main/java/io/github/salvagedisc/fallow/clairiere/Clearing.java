package io.github.salvagedisc.fallow.clairiere;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Une clairière : le disque où la brume est levée autour d'un feu de veille, d'une lanterne, d'un dissipateur
 * chargé ou d'une commande. Elle s'ouvre en vague à partir de {@code since} (heure du monde, en ticks), un
 * anneau de {@link #RING} blocs à la fois, et se referme de même, du bord vers le centre, à partir de
 * {@code until}. Tant qu'elle est ouverte, {@code until} vaut {@link #OPEN}.
 */
public record Clearing(BlockPos center, int radius, long since, long until) {
    /** Valeur de {@code until} d'une clairière ouverte. */
    public static final long OPEN = Long.MAX_VALUE;
    /** Largeur d'un anneau de la vague. */
    public static final int RING = 4;

    public static final StreamCodec<ByteBuf, Clearing> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, Clearing::center,
            ByteBufCodecs.VAR_INT, Clearing::radius,
            ByteBufCodecs.VAR_LONG, Clearing::since,
            ByteBufCodecs.VAR_LONG, Clearing::until,
            Clearing::new);

    public boolean closing() {
        return until != OPEN;
    }

    public Clearing closedAt(long time) {
        return new Clearing(center, radius, since, time);
    }

    /** Rayon dégagé à l'heure {@code time}, quand un anneau passe tous les {@code ringTicks} ticks. */
    public int radiusAt(long time, int ringTicks) {
        if (!closing() || time < until) {
            return swept(time - since, ringTicks);
        }
        // La vague qui referme part du rayon atteint à la fermeture, pas du plein rayon : une lanterne reprise
        // avant d'avoir tout dégagé ne continue pas d'ouvrir pendant que la brume revient.
        return Math.max(0, swept(until - since, ringTicks) - swept(time - until, ringTicks));
    }

    /**
     * Même rayon, sans les marches d'un anneau à l'autre : le bord que dessine le pack de shaders, qui avance
     * à chaque image ({@code time} compte la fraction du tick en cours).
     */
    public float smoothRadiusAt(double time, int ringTicks) {
        if (!closing() || time < until) {
            return sweptSmooth(time - since, ringTicks);
        }
        return Math.max(0.0F, sweptSmooth(until - since, ringTicks) - sweptSmooth(time - until, ringTicks));
    }

    private float sweptSmooth(double ticks, int ringTicks) {
        if (ticks < 0) {
            return 0.0F;
        }
        return (float) Math.min(radius, (ticks / Math.max(1, ringTicks) + 1.0) * RING);
    }

    /** Distance parcourue par la vague en {@code ticks} ticks. */
    private int swept(long ticks, int ringTicks) {
        if (ticks < 0) {
            return 0;
        }
        return (int) Math.min(radius, (ticks / Math.max(1, ringTicks) + 1) * RING);
    }

    /** Vrai si {@code (x, z)} est à {@code within} blocs du centre au plus. */
    public boolean covers(double x, double z, int within) {
        double dx = x - center.getX(), dz = z - center.getZ();
        return dx * dx + dz * dz <= (double) within * within;
    }
}
