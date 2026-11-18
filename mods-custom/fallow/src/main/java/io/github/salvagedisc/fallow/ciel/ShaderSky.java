package io.github.salvagedisc.fallow.ciel;

import net.irisshaders.iris.api.v0.IrisApi;

/**
 * Laisse le pack de shaders dessiner le ciel quand il y en a un. ATMOSPHERICS annule le {@code renderSky}
 * vanilla et dessine son propre dôme : sous Iris, les programmes du ciel du pack ne tournent jamais et le
 * ciel devient gris foncé, étoilé en plein midi. ATMOSPHERICS s'efface de lui-même quand la lune amie de
 * No Man's Land est levée : pendant le dessin du ciel avec un pack actif, {@link #gateOpen()} le lui fait
 * croire, et seulement là. Appliqué seulement avec ATMOSPHERICS et Iris (voir le plugin de mixins).
 */
public final class ShaderSky {
    private static boolean drawing;

    private ShaderSky() {
    }

    /** Appelé autour de l'appel à {@code renderSky} ; rend l'état précédent, à remettre ensuite. */
    public static boolean begin() {
        boolean previous = drawing;
        drawing = IrisApi.getInstance().isShaderPackInUse();
        return previous;
    }

    public static void end(boolean previous) {
        drawing = previous;
    }

    /** Vrai seulement pendant le dessin du ciel, avec un pack de shaders actif. */
    public static boolean gateOpen() {
        return drawing;
    }
}
