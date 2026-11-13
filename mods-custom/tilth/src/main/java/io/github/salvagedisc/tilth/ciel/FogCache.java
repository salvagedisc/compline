package io.github.salvagedisc.tilth.ciel;

import io.github.salvagedisc.tilth.TilthConfig;
import io.github.salvagedisc.tilth.mixin.ciel.BiomeBlendUtilInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

/**
 * Le cache de brume d'ATMOSPHERICS. {@code BiomeBlendUtil.collect} garde les biomes échantillonnés
 * autour du joueur tant qu'il reste dans la même cellule de 4 blocs, et ne les relit pas quand un
 * biome change : après un {@code /fillbiome}, la brume ne bouge que quand le joueur change de cellule.
 * Le vider force une nouvelle lecture à l'image suivante ; la transition reste lissée par ATMOSPHERICS.
 *
 * <p>Après un changement de biomes, la distance de la brume suit en douceur pendant quelques secondes
 * ({@link #smooth}) : quand une clairière s'ouvre, la brume se retire peu à peu au lieu de tomber en une
 * seconde, et revient de même quand elle se referme. ATMOSPHERICS lisse la distance de façon linéaire :
 * de 40 à 500 blocs, la brume paraît partie dès les premiers pour cent. Ici le lissage se fait sur
 * l'inverse de la distance, la densité : à 30 blocs, la part de brume baisse alors à vitesse régulière.
 *
 * <p>Ne charger cette classe que si ATMOSPHERICS est présent.
 */
public final class FogCache {
    /** Durée du fondu ralenti après le dernier anneau reçu : la vague, puis la fin du fondu. */
    private static final long SLOW_NANOS = 15_000_000_000L;
    private static volatile long slowUntil;
    /** Écart de densité entre brume pleine (32 blocs) et ciel dégagé (512), parcouru en une durée de fondu. */
    private static final float SPAN = 1.0F / 32.0F - 1.0F / 512.0F;
    private static float density = Float.NaN;
    private static long last;

    private FogCache() {
    }

    /** Ralentit le fondu de la brume pendant les secondes qui suivent. */
    public static void slowFade() {
        slowUntil = System.nanoTime() + SLOW_NANOS;
    }

    /**
     * Reçoit la brume calculée par ATMOSPHERICS ({@code r, g, b, début, fin}) et, pendant le fondu ralenti,
     * remplace début et fin : la densité (1 / fin) va vers sa cible à vitesse bornée, puis ralentit à
     * l'approche ; le début garde la proportion voulue par ATMOSPHERICS.
     */
    public static void smooth(float[] fog) {
        long now = System.nanoTime();
        float seconds = TilthConfig.cielClearingFadeSeconds();
        float dt = Math.min(0.5F, (now - last) / 1.0E9F);
        last = now;
        if (fog[4] <= 0.0F) {
            return;
        }
        float target = 1.0F / fog[4];
        if (seconds <= 0.0F || now - slowUntil > 0 || Float.isNaN(density)) {
            density = target;
            return;
        }
        float ease = (target - density) * (1.0F - (float) Math.exp(-4.0F * dt / seconds));
        float cap = SPAN * dt / seconds;
        density += Mth.clamp(ease, -cap, cap);
        float end = 1.0F / density;
        // Le début suit la fin en proportion : sinon il passerait de 0 à 40 blocs en une seconde et
        // dégagerait tout le premier plan avant que la brume lointaine ait bougé.
        float start = end * fog[3] / fog[4];
        fog[3] = start;
        fog[4] = Math.max(end, start + 1.0F);
    }

    /** Vide le cache, sur le fil de rendu (repoussé à la prochaine tâche du client sinon). */
    public static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isSameThread()) {
            BiomeBlendUtilInvoker.tilth$resetCache();
        } else {
            minecraft.execute(BiomeBlendUtilInvoker::tilth$resetCache);
        }
    }
}
