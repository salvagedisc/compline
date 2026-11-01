package io.github.salvagedisc.fallow.averse;

import io.github.salvagedisc.fallow.ModIds;
import net.minecraft.client.multiplayer.ClientLevel;

/**
 * Le vent vu par Fallow : celui de Where Winds Blow quand il est là (le même qui penche sa pluie et
 * balance ses herbes), sinon un vent simple qui forcit avec la pluie et l'orage.
 *
 * <p>Résultat : {@code {directionX, directionZ, force}}, la direction étant celle vers laquelle le vent
 * souffle (vecteur unitaire), la force de 0 (calme) à 3 environ (rafale d'orage).
 */
public final class Wind {
    private static boolean wwb = ModIds.isLoaded(ModIds.WHERE_WINDS_BLOW);
    private static float[] cached = {1.0F, 0.0F, 0.0F};
    private static long cachedTick = Long.MIN_VALUE;

    private Wind() {
    }

    public static float[] at(ClientLevel level, double x, double y, double z) {
        if (wwb) {
            try {
                return WwbWind.sample(x, y, z);
            } catch (Throwable t) {
                // une mise à jour de Where Winds Blow a changé sa classe de vent : on retombe sur le vent simple
                disableWwb(t);
            }
        }
        float rain = level.getRainLevel(1.0F);
        float thunder = level.getThunderLevel(1.0F);
        double angle = (level.getGameTime() / 24000.0) * 0.7;
        return new float[] {(float) Math.cos(angle), (float) Math.sin(angle), 0.35F + rain * 0.5F + thunder * 1.4F};
    }

    /**
     * Le vent à la caméra, relu une fois par tick : pour les feuilles, qui le demandent des centaines
     * de fois par tick et n'ont pas besoin du vent bloc par bloc.
     */
    public static float[] here(ClientLevel level) {
        long tick = level.getGameTime();
        if (tick != cachedTick) {
            cachedTick = tick;
            net.minecraft.world.phys.Vec3 cam = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            cached = at(level, cam.x, cam.y, cam.z);
        }
        return cached;
    }

    private static void disableWwb(Throwable t) {
        if (wwb) {
            wwb = false;
            io.github.salvagedisc.fallow.Fallow.LOGGER.warn("averse : vent de Where Winds Blow illisible, vent simple utilisé", t);
        }
    }
}
