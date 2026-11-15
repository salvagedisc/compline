package io.github.salvagedisc.compline.relief;

import net.irisshaders.iris.api.v0.IrisApi;

/**
 * Demande à Iris si un pack de shaders dessine l'image. Cette classe n'est chargée que si Iris est là
 * ({@link ReliefTexture} ne l'appelle pas sinon).
 */
final class ShaderPackCheck {
    private ShaderPackCheck() {
    }

    static boolean inUse() {
        return IrisApi.getInstance().isShaderPackInUse();
    }
}
