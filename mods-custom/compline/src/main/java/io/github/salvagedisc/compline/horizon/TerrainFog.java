package io.github.salvagedisc.compline.horizon;

/**
 * La brume du terrain de la dernière image : début et fin en blocs, tels que les laisse
 * {@code FogRenderer.setupFog} pour le terrain, après ATMOSPHERICS. Écrite et lue sur le fil de rendu.
 */
public final class TerrainFog {
    private static float start;
    private static float end;
    private static boolean captured;

    private TerrainFog() {
    }

    public static void capture(float fogStart, float fogEnd) {
        start = fogStart;
        end = fogEnd;
        captured = true;
    }

    static boolean captured() {
        return captured;
    }

    static float start() {
        return start;
    }

    static float end() {
        return end;
    }
}
