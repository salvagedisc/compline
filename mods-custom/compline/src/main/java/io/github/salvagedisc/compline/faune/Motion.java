package io.github.salvagedisc.compline.faune;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * La couche maison partagée par les créatures de la faune : ce que les images clés ne savent pas faire seules.
 * Pour chaque créature à l'écran, on lisse sa vitesse réelle (pour caler la vitesse des pas), son virage (pour pencher
 * le corps), son regard (pour tourner la tête mollement), et on fait vivre deux ressorts (antennes, fanon) poussés
 * par l'accélération et les virages. Tout est calculé dans le temps du jeu, pas par image : le rendu est le même
 * à 30 ou à 144 images par seconde.
 */
public final class Motion {
    private static final Map<LivingEntity, Motion> ALL = new WeakHashMap<>();

    /** Vitesse horizontale lissée, en blocs par tick. */
    public float speed;
    /** Virage lissé, en degrés par tick (positif : vers la gauche de la créature). */
    public float turn;
    /** Regard lissé, en degrés, par rapport au corps. */
    public float lookYaw;
    public float lookPitch;
    /** Poids de la couche maison, entre 0 et 1 (réduit pendant une animation qui pose déjà la tête). */
    public float weight = 1;
    /** Ressorts : balancement d'avant en arrière (accélération) et de côté (virage), en radians. */
    public final Spring pitchSpring = new Spring(0.22F, 0.30F);
    public final Spring rollSpring = new Spring(0.18F, 0.26F);
    /** Mémoire libre pour la créature (déclencheurs d'animations). */
    public final long[] marks = new long[8];
    public float lastSignal;

    private double lastTime = Double.NaN;
    private float lastSpeed;

    private Motion() {
        java.util.Arrays.fill(marks, Long.MAX_VALUE);
    }

    public static Motion of(LivingEntity entity) {
        return ALL.computeIfAbsent(entity, e -> new Motion());
    }

    /**
     * Fait avancer les valeurs lissées jusqu'au temps {@code time} (en ticks, partiel compris). Appelé une fois par
     * passe d'animation ; un second appel au même temps ne change rien.
     */
    public Motion update(LivingEntity entity, double time, float targetWeight) {
        boolean first = Double.isNaN(lastTime);
        float dt = first ? 0 : (float) Mth.clamp(time - lastTime, 0, 3);
        lastTime = time;
        float rawSpeed = (float) Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
        float rawTurn = Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO);
        float rawYaw = Mth.wrapDegrees(entity.yHeadRot - entity.yBodyRot);
        float rawPitch = entity.getXRot();
        if (first) {
            speed = rawSpeed;
            lastSpeed = rawSpeed;
            lookYaw = rawYaw;
            lookPitch = rawPitch;
            weight = targetWeight;
        }
        speed = approach(speed, rawSpeed, dt, 3);
        turn = approach(turn, rawTurn, dt, 4);
        lookYaw = approach(lookYaw, rawYaw, dt, 5);
        lookPitch = approach(lookPitch, rawPitch, dt, 5);
        weight = approach(weight, targetWeight, dt, 4);
        if (dt > 0) {
            float accel = (speed - lastSpeed) / dt;
            pitchSpring.step(Mth.clamp(-accel * 6, -0.6F, 0.6F), dt);
            rollSpring.step(Mth.clamp(turn * Mth.DEG_TO_RAD * 2.5F, -0.6F, 0.6F), dt);
        }
        lastSpeed = speed;
        return this;
    }

    /**
     * Multiplicateur de vitesse d'une animation de marche, d'après la vitesse au sol qu'elle montre (blocs par tick),
     * pour que les pieds ne glissent pas.
     */
    public float stride(float animationSpeed, float min, float max) {
        return Mth.clamp(speed / animationSpeed, min, max);
    }

    private static float approach(float value, float target, float dt, float ticks) {
        return value + (target - value) * (1 - (float) Math.exp(-dt / ticks));
    }

    /** Un ressort amorti : il suit sa cible avec du retard et un peu de rebond. */
    public static final class Spring {
        private final float stiffness;
        private final float damping;
        public float angle;
        private float velocity;

        Spring(float stiffness, float damping) {
            this.stiffness = stiffness;
            this.damping = damping;
        }

        void step(float target, float dt) {
            int steps = Mth.ceil(dt / 0.5F);
            float h = dt / steps;
            for (int i = 0; i < steps; i++) {
                velocity += ((target - angle) * stiffness - velocity * damping) * h;
                angle += velocity * h;
            }
        }
    }
}
