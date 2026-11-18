package io.github.salvagedisc.compline.climat.client;

import net.minecraft.util.Mth;

/**
 * Ce que la tempête fait à la brume, lu par la brume d'ATMOSPHERICS (module ciel). Cette classe ne dépend d'aucun
 * autre mod : le module ciel peut la lire même quand le module climat n'est pas chargé, elle reste alors à zéro.
 */
public final class StormView {
    /** Couleur de l'air dans le sable, et dans la neige soufflée, en plein jour. */
    private static final float[] SAND = {0.80F, 0.66F, 0.47F};
    private static final float[] SNOW = {0.86F, 0.89F, 0.93F};
    /** Distance où la brume se ferme au plus fort de la tempête, en blocs. */
    private static final float SAND_END = 22.0F;
    private static final float SNOW_END = 36.0F;

    /** Force de la tempête de sable vue de la caméra, de 0 à 1 (lissée). */
    static volatile float sand;
    /** Force de la tempête de neige vue de la caméra, de 0 à 1 (lissée). */
    static volatile float snow;

    private StormView() {
    }

    /**
     * Ferme la brume ({@code r, g, b, début, fin}) et la teinte selon la tempête en cours. La teinte garde la
     * lumière du moment : la nuit, le sable reste sombre.
     */
    public static void apply(float[] fog) {
        float s = sand;
        float n = snow;
        if (s <= 0.001F && n <= 0.001F) {
            return;
        }
        boolean isSand = s >= n;
        float strength = isSand ? s : n;
        float[] tint = isSand ? SAND : SNOW;
        float end = isSand ? SAND_END : SNOW_END;

        float light = Math.max(fog[0], Math.max(fog[1], fog[2]));
        float mix = 0.85F * strength;
        for (int i = 0; i < 3; i++) {
            fog[i] = Mth.lerp(mix, fog[i], tint[i] * light);
        }
        // La densité (1 / fin) monte vers celle de la tempête : sans à-coup, même quand la brume du lieu est déjà proche.
        float density = Mth.lerp(strength, 1.0F / Math.max(fog[4], 1.0F), Math.max(1.0F / end, 1.0F / Math.max(fog[4], 1.0F)));
        float newEnd = 1.0F / density;
        fog[3] = Mth.lerp(strength, fog[3], 0.0F) * newEnd / Math.max(fog[4], 1.0F);
        fog[4] = Math.max(newEnd, fog[3] + 1.0F);
    }
}
