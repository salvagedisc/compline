package io.github.salvagedisc.compline.rendu;

import com.jvn.wherewindsblow.client.wind.DynamicWindManager;
import com.jvn.wherewindsblow.client.wind.GlobalWindState;
import com.jvn.wherewindsblow.client.wind.GustFrontState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;

/**
 * Le vent de Where Winds Blow, lu pour le pack de shaders. Sous un shader, Where Winds Blow coupe lui-même le
 * balancement des plantes (il passe par les shaders de Sodium, qu'Iris remplace) : le pack de Compline le refait,
 * avec la même direction, la même force et les mêmes fronts de rafale que la pluie, la fumée et les feuilles
 * qui volent. Chargé seulement si Where Winds Blow est présent.
 */
final class WindLink {
    private WindLink() {
    }

    /**
     * {@code wind} : direction (x, z), force, turbulence. {@code gusts} : pour chaque front retenu,
     * (direction / largeur en x et z, distance de la caméra au front / largeur, force au passage) ; le pack
     * calcule alors la rafale en un point p (depuis la caméra) par {@code w * exp(-1.65 * (dot(p.xz, xy) + z)²)},
     * la bande que Where Winds Blow applique lui-même.
     */
    static void fill(Vector4f wind, Vector4f[] gusts, Vec3 camera) {
        GlobalWindState state = DynamicWindManager.currentState();
        if (state == null) {
            return;
        }
        wind.set(state.directionX(), state.directionZ(), state.ambientStrength(), state.turbulence());

        float time = DynamicWindManager.simulationTime();
        float[] felt = new float[gusts.length];
        for (int i = 0; i < DynamicWindManager.MAX_ACTIVE_GUSTS; i++) {
            GustFrontState gust = DynamicWindManager.gustFront(i);
            if (gust == null || !gust.isAlive(time)) {
                continue;
            }
            float age = time - gust.startTime();
            float width = Math.max(gust.width(), 0.001F);
            double frontX = gust.originX() + gust.directionX() * gust.speed() * age;
            double frontZ = gust.originZ() + gust.directionZ() * gust.speed() * age;
            // Force de la rafale sur sa crête, enveloppe dans le temps comprise.
            float crest = gust.strengthAt(frontX, frontZ, time);
            if (crest <= 1.0e-4F) {
                continue;
            }
            float offset = (float) (((camera.x - frontX) * gust.directionX() + (camera.z - frontZ) * gust.directionZ()) / width);
            // On garde les fronts qui pèsent le plus près de la caméra ; un front à plus de six largeurs ne se voit pas.
            float weight = crest * (float) Math.exp(-0.12 * offset * offset);
            int slot = -1;
            for (int j = 0; j < gusts.length; j++) {
                if (weight > felt[j] && (slot < 0 || felt[j] < felt[slot])) {
                    slot = j;
                }
            }
            if (slot >= 0) {
                felt[slot] = weight;
                gusts[slot].set(gust.directionX() / width, gust.directionZ() / width, offset, crest);
            }
        }
    }
}
