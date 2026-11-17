package io.github.salvagedisc.compline.rendu;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.clairiere.Clearing;
import io.github.salvagedisc.compline.clairiere.ClearingClient;
import io.github.salvagedisc.compline.vent.Wind;
import java.util.Comparator;
import java.util.List;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.gl.uniform.UniformUpdateFrequency;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

/**
 * Les valeurs des uniformes {@code brumaire*}, calculées une fois par image à la première lecture (Iris relit
 * chaque uniforme pour chaque programme du pack : les fournisseurs ne font que rendre l'objet déjà rempli).
 */
public final class ShaderUniforms {
    static final int CLEARINGS = 4;
    static final int GUSTS = 2;
    /** Sur combien de blocs, en entrant dans une clairière, la brume finit de se lever autour de la caméra. */
    private static final double LIFT_BLOCKS = 8.0;
    /** Constante de temps du fondu de brume autour de la caméra, en secondes. */
    private static final double LIFT_SECONDS = 0.8;
    /** Au-delà de cette distance du bord, une clairière n'est pas transmise : la brume la cache. */
    private static final double CLEARING_RANGE = 768.0;
    /** Constante de temps de l'altitude du pied de la brume, en secondes. */
    private static final double ANCHOR_SECONDS = 2.0;
    /** Écart des colonnes sondées autour de la caméra pour trouver le sol, en blocs. */
    private static final int ANCHOR_SPREAD = 12;
    static final int PUSHES = 4;
    /** Rayon autour de la caméra où un être vivant couche les herbes, en blocs. */
    private static final double PUSH_RANGE = 12.0;
    static final int WAKES = 6;
    /** Rayon autour de la caméra où ce qui flotte ou nage ride l'eau, en blocs. */
    private static final double WAKE_RANGE = 28.0;

    private static final Vector4f MIST = new Vector4f();
    private static final Vector4f WIND_VALUE = new Vector4f();
    private static final Vector4f[] CLEARING_VALUES = new Vector4f[CLEARINGS];
    private static final Vector4f[] GUST_VALUES = new Vector4f[GUSTS];
    private static final Vector4f[] PUSH_VALUES = new Vector4f[PUSHES];
    private static final Vector4f[] WAKE_VALUES = new Vector4f[WAKES];
    private static final Vector4f[] WAKE_SPEEDS = new Vector4f[2];
    private static final double[] EDGES = new double[CLEARINGS];

    private static long frame;
    private static long computed = -1;
    private static long lastNanos;
    private static float lift;
    private static float anchor = Float.NaN;

    static {
        for (int i = 0; i < CLEARINGS; i++) {
            CLEARING_VALUES[i] = new Vector4f();
        }
        for (int i = 0; i < GUSTS; i++) {
            GUST_VALUES[i] = new Vector4f();
        }
        for (int i = 0; i < PUSHES; i++) {
            PUSH_VALUES[i] = new Vector4f();
        }
        for (int i = 0; i < WAKES; i++) {
            WAKE_VALUES[i] = new Vector4f();
        }
        WAKE_SPEEDS[0] = new Vector4f();
        WAKE_SPEEDS[1] = new Vector4f();
    }

    private ShaderUniforms() {
    }

    static void nextFrame() {
        frame++;
    }

    /** Appelé par {@code rendu.IrisUniformsMixin} quand Iris monte les uniformes d'un pack. */
    public static void register(UniformHolder uniforms) {
        uniforms.uniform1f(UniformUpdateFrequency.PER_FRAME, "brumaireLink", () -> ComplineConfig.renduEnabled() ? 1.0F : 0.0F);
        uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireMist", () -> current(MIST));
        uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireWind", () -> current(WIND_VALUE));
        for (int i = 0; i < CLEARINGS; i++) {
            Vector4f value = CLEARING_VALUES[i];
            uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireClearing" + i, () -> current(value));
        }
        for (int i = 0; i < GUSTS; i++) {
            Vector4f value = GUST_VALUES[i];
            uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireGust" + i, () -> current(value));
        }
        for (int i = 0; i < PUSHES; i++) {
            Vector4f value = PUSH_VALUES[i];
            uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumairePush" + i, () -> current(value));
        }
        for (int i = 0; i < WAKES; i++) {
            Vector4f value = WAKE_VALUES[i];
            uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireWake" + i, () -> current(value));
        }
        uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireWakeSpeed0", () -> current(WAKE_SPEEDS[0]));
        uniforms.uniform4f(UniformUpdateFrequency.PER_FRAME, "brumaireWakeSpeed1", () -> current(WAKE_SPEEDS[1]));
    }

    private static Vector4f current(Vector4f value) {
        if (computed != frame) {
            computed = frame;
            compute();
        }
        return value;
    }

    private static void compute() {
        long now = System.nanoTime();
        double seconds = Mth.clamp((now - lastNanos) * 1.0e-9, 0.0, 0.25);
        lastNanos = now;
        for (Vector4f value : CLEARING_VALUES) {
            value.zero();
        }
        for (Vector4f value : GUST_VALUES) {
            value.zero();
        }
        for (Vector4f value : PUSH_VALUES) {
            value.zero();
        }
        for (Vector4f value : WAKE_VALUES) {
            value.zero();
        }
        WAKE_SPEEDS[0].zero();
        WAKE_SPEEDS[1].zero();
        WIND_VALUE.zero();

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !ComplineConfig.renduEnabled()) {
            lift = 0.0F;
            anchor = Float.NaN;
            MIST.zero();
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float partial = minecraft.getTimer().getGameTimeDeltaPartialTick(false);

        float target = clearings(level, camera, partial);
        lift += (target - lift) * (float) (1.0 - Math.exp(-seconds / LIFT_SECONDS));
        float ground = ground(level, camera);
        anchor = Float.isNaN(anchor) || Math.abs(ground - anchor) > 64.0F
                ? ground
                : anchor + (ground - anchor) * (float) (1.0 - Math.exp(-seconds / ANCHOR_SECONDS));
        MIST.set(lift, anchor, 0.0F, 0.0F);

        Wind.fill(WIND_VALUE, GUST_VALUES, camera);
        pushes(level, camera, partial);
        wakes(level, camera, partial);
    }

    /**
     * Ce qui ride la surface de l'eau près de la caméra : barques et autres véhicules, nageurs et bêtes qui
     * pataugent, à cheval sur la surface. (x, y, z) : le point de la surface sous eux depuis la caméra, w : leur
     * demi-largeur ; leur vitesse sur l'eau (blocs par seconde) dans brumaireWakeSpeed0..1. Un passager ne compte pas :
     * sa barque le fait pour lui.
     */
    private static void wakes(ClientLevel level, Vec3 camera, float partial) {
        AABB around = new AABB(camera, camera).inflate(WAKE_RANGE);
        List<Entity> near = level.getEntitiesOfClass(Entity.class, around,
                e -> e.isAlive() && !e.isSpectator() && !e.isPassenger()
                        && (e instanceof LivingEntity || e instanceof VehicleEntity));
        near.sort(Comparator.comparingDouble(e -> e.distanceToSqr(camera)));
        int slot = 0;
        for (Entity entity : near) {
            if (slot >= WAKES) {
                break;
            }
            Vec3 at = entity.getPosition(partial);
            float surface = waterSurface(level, at, entity.getBbHeight());
            if (Float.isNaN(surface) || at.y > surface + 0.35 || at.y + entity.getBbHeight() < surface - 0.2) {
                continue;
            }
            double dx = entity.getX() - entity.xo;
            double dz = entity.getZ() - entity.zo;
            float speed = (float) Math.min(Math.sqrt(dx * dx + dz * dz) * 20.0, 12.0);
            float radius = Mth.clamp(entity.getBbWidth() * 0.5F, 0.25F, 3.0F);
            WAKE_VALUES[slot].set((float) (at.x - camera.x), (float) (surface - camera.y), (float) (at.z - camera.z), radius);
            WAKE_SPEEDS[slot / 4].setComponent(slot % 4, speed);
            slot++;
        }
    }

    /**
     * Hauteur de la surface de l'eau que traverse une chose, du dessous de ses pieds au-dessus de sa tête : le haut du
     * premier bloc d'eau sans eau au-dessus. NaN si elle n'en traverse aucune (à sec, ou tout entière sous l'eau).
     */
    private static float waterSurface(ClientLevel level, Vec3 at, float height) {
        int bottom = Mth.floor(at.y - 0.6);
        int top = Mth.floor(at.y + height + 0.4);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = bottom; y <= top; y++) {
            pos.set(at.x, y, at.z);
            FluidState fluid = level.getFluidState(pos);
            if (!fluid.is(FluidTags.WATER)) {
                continue;
            }
            pos.setY(y + 1);
            if (!level.getFluidState(pos).is(FluidTags.WATER)) {
                pos.setY(y);
                return y + fluid.getHeight(level, pos);
            }
        }
        return Float.NaN;
    }

    /**
     * Les êtres vivants les plus proches de la caméra, qui couchent les herbes en passant : (x, y, z) de leurs pieds
     * depuis la caméra, et le rayon qu'ils écartent (0 : rien).
     */
    private static void pushes(ClientLevel level, Vec3 camera, float partial) {
        AABB around = new AABB(camera, camera).inflate(PUSH_RANGE);
        List<LivingEntity> near = level.getEntitiesOfClass(LivingEntity.class, around,
                e -> e.isAlive() && !e.isSpectator());
        near.sort(Comparator.comparingDouble(e -> e.distanceToSqr(camera)));
        int slot = 0;
        for (LivingEntity entity : near) {
            if (slot >= PUSHES) {
                break;
            }
            Vec3 feet = entity.getPosition(partial).subtract(camera);
            float radius = Math.min(entity.getBbWidth() * 0.75F + 0.35F, 2.0F);
            PUSH_VALUES[slot++].set((float) feet.x, (float) feet.y, (float) feet.z, radius);
        }
    }

    /**
     * L'altitude où poser le pied de la nappe de brume : la surface autour de la caméra, sans jamais passer
     * au-dessus d'elle (sous un couvert d'arbres, la surface est la cime).
     */
    private static float ground(ClientLevel level, Vec3 camera) {
        int x = Mth.floor(camera.x), z = Mth.floor(camera.z);
        int floor = level.getMinBuildHeight();
        float sum = 0.0F;
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx != 0 && dz != 0) {
                    continue;
                }
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x + dx * ANCHOR_SPREAD, z + dz * ANCHOR_SPREAD);
                if (y > floor) {
                    sum += y;
                    count++;
                }
            }
        }
        float feet = (float) camera.y - 1.5F;
        return count == 0 ? feet : Math.min(sum / count, feet);
    }

    /**
     * Remplit les clairières les plus proches et rend la part de brume levée autour de la caméra : 0 hors de
     * toute clairière, 1 à {@link #LIFT_BLOCKS} blocs à l'intérieur du bord dégagé.
     */
    private static float clearings(ClientLevel level, Vec3 camera, float partial) {
        List<Clearing> open = ClearingClient.open(level);
        if (open.isEmpty()) {
            return 0.0F;
        }
        int count = 0;
        double inside = 0.0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (Clearing clearing : open) {
            float radius = ClearingClient.radius(clearing, partial);
            if (radius <= 0.0F) {
                continue;
            }
            double dx = clearing.center().getX() + 0.5 - camera.x, dz = clearing.center().getZ() + 0.5 - camera.z;
            double edge = Math.sqrt(dx * dx + dz * dz) - radius;
            if (edge > CLEARING_RANGE || !ClearingClient.clears(level, clearing.center())) {
                continue;
            }
            inside = Math.max(inside, -edge);
            // Tri par insertion sur le bord le plus proche : quatre places au plus.
            int slot = count;
            if (count == CLEARINGS) {
                if (edge >= EDGES[CLEARINGS - 1]) {
                    continue;
                }
                slot = CLEARINGS - 1;
            } else {
                count++;
            }
            while (slot > 0 && EDGES[slot - 1] > edge) {
                EDGES[slot] = EDGES[slot - 1];
                CLEARING_VALUES[slot].set(CLEARING_VALUES[slot - 1]);
                slot--;
            }
            EDGES[slot] = edge;
            CLEARING_VALUES[slot].set((float) dx, (float) dz, radius, 1.0F);
        }
        if (inside <= 0.0 || !ClearingClient.clears(level, pos.set(camera.x, camera.y, camera.z))) {
            return 0.0F;
        }
        float t = (float) Mth.clamp(inside / LIFT_BLOCKS, 0.0, 1.0);
        return t * t * (3.0F - 2.0F * t);
    }
}
