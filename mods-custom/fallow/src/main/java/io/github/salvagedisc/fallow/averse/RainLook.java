package io.github.salvagedisc.fallow.averse;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.rendu.Sun;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/**
 * La teinte de la pluie de Where Winds Blow. Il la dessine d'une couleur fixe, éclairée comme le
 * terrain ; ici, juste avant qu'il la dessine, la couleur du shader prend :
 * <ul>
 *   <li>un blanc à peine teinté par la brume du moment (ATMOSPHERICS), plus clair que le ciel ;</li>
 *   <li>un contre-jour : face au soleil, les gouttes s'éclairent d'or et ressortent davantage.</li>
 * </ul>
 * Remise à blanc juste après ({@link #end}).
 */
public final class RainLook {
    private static boolean active;

    private RainLook() {
    }

    public static void begin(ClientLevel level, float partialTick) {
        if (!FallowConfig.averseRainTint()) {
            return;
        }
        float[] fog = RenderSystem.getShaderFogColor();
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f look = camera.getLookVector();
        Vector3f sun = Sun.direction(level, partialTick);
        float thunder = level.getThunderLevel(partialTick);
        float day = Sun.daylight(sun);
        float facing = Math.max(0.0F, look.dot(sun));
        // un contre-jour franc seulement près du soleil, atténué sous l'orage (le ciel est bouché)
        float back = (float) Math.pow(facing, 5.0) * day * (1.0F - 0.65F * thunder);
        float[] warm = Sun.colour(sun);
        // Where Winds Blow draws its drops in a fixed yellowish grey (0.82, 0.86, 0.72): undone here, then a
        // colour close to white with a quarter of the mist in it, so that the streaks stay brighter than the sky
        float r = Mth.lerp(0.25F, 1.0F, fog[0]) / 0.82F;
        float g = Mth.lerp(0.25F, 1.0F, fog[1]) / 0.86F;
        float b = Mth.lerp(0.25F, 1.0F, fog[2]) / 0.72F;
        r = Mth.lerp(back, r, warm[0] / 0.82F);
        g = Mth.lerp(back, g, warm[1] / 0.86F);
        b = Mth.lerp(back, b, warm[2] / 0.72F);
        float alpha = 1.0F + 0.4F * back;
        RenderSystem.setShaderColor(r, g, b, alpha);
        active = true;
    }

    public static void end() {
        if (active) {
            active = false;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
