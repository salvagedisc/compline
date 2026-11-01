package io.github.salvagedisc.fallow.averse;

import com.jvn.wherewindsblow.client.wind.DynamicWindManager;
import com.jvn.wherewindsblow.client.wind.WindSample;

/**
 * Lecture du vent de Where Winds Blow. Il n'a pas d'API publique : on appelle sa méthode statique
 * {@code DynamicWindManager.sampleWind}. Ne charger cette classe que si Where Winds Blow est présent.
 */
final class WwbWind {
    private WwbWind() {
    }

    static float[] sample(double x, double y, double z) {
        WindSample wind = DynamicWindManager.sampleWind(x, y, z);
        return new float[] {wind.directionX(), wind.directionZ(), Math.max(0.0F, wind.strength())};
    }
}
