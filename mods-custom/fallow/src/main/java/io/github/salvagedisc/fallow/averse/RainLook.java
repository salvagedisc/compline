package io.github.salvagedisc.fallow.averse;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;

/**
 * La teinte de la pluie de Where Winds Blow. Il la dessine d'une couleur fixe, éclairée comme le
 * terrain ; ici, juste avant qu'il la dessine, la couleur du shader prend :
 * un gris clair teinté par la brume du moment (ATMOSPHERICS), sans couleur propre face au soleil, et
 * une opacité un peu plus faible que celle du jeu.
 * Remise à blanc juste après ({@link #end}).
 */
public final class RainLook {
    /** Opacité des gouttes : un peu moins que celle du jeu, pour voir le paysage à travers l'averse. */
    private static final float RAIN_ALPHA = 0.7F;
    private static boolean active;

    private RainLook() {
    }

    public static void begin(ClientLevel level, float partialTick) {
        if (!FallowConfig.averseRainTint()) {
            return;
        }
        float[] fog = RenderSystem.getShaderFogColor();
        // Where Winds Blow draws its drops in a fixed yellowish grey (0.82, 0.86, 0.72): undone here, then a
        // light grey with a third of the mist in it. No colour of its own against the sun: the drops must
        // never hide the landscape. Slightly thinner than Where Winds Blow's own rain.
        float r = Mth.lerp(0.35F, 0.88F, fog[0]) / 0.82F;
        float g = Mth.lerp(0.35F, 0.89F, fog[1]) / 0.86F;
        float b = Mth.lerp(0.35F, 0.90F, fog[2]) / 0.72F;
        RenderSystem.setShaderColor(r, g, b, RAIN_ALPHA);
        active = true;
    }

    public static void end() {
        if (active) {
            active = false;
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}
