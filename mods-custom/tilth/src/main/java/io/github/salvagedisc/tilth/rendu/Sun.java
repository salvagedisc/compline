package io.github.salvagedisc.tilth.rendu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

/** Le soleil et la brume vus de la caméra, pour les modules averse et rayons (client). */
public final class Sun {
    private Sun() {
    }

    /** Direction du soleil (unitaire) : il se lève à l'est (+X), passe au zénith à midi et se couche à l'ouest. */
    public static Vector3f direction(ClientLevel level, float partialTick) {
        float angle = level.getTimeOfDay(partialTick) * Mth.TWO_PI;
        return new Vector3f(-Mth.sin(angle), Mth.cos(angle), 0.0F);
    }

    /** 0 la nuit, 1 dès que le soleil est un peu haut ; monte en douceur à l'aube. */
    public static float daylight(Vector3f sun) {
        return smoothstep(-0.02F, 0.30F, sun.y());
    }

    /** Couleur chaude du soleil : orangée près de l'horizon, blanc doré plus haut. */
    public static float[] colour(Vector3f sun) {
        float high = smoothstep(0.05F, 0.45F, sun.y());
        return new float[] {1.0F, Mth.lerp(high, 0.70F, 0.93F), Mth.lerp(high, 0.45F, 0.78F)};
    }

    /** Part de la brume entre la caméra et un point à cette distance (0 : rien, 1 : tout caché). */
    public static float fogAt(float distance) {
        float start = RenderSystem.getShaderFogStart();
        float end = RenderSystem.getShaderFogEnd();
        if (end <= start) {
            return 0.0F;
        }
        return Mth.clamp((distance - start) / (end - start), 0.0F, 1.0F);
    }

    /** Épaisseur de la brume du moment : 1 dans une brume serrée (fin à 40 blocs), 0 par temps dégagé (300 et plus). */
    public static float mistiness() {
        float end = RenderSystem.getShaderFogEnd();
        return Mth.clamp(1.0F - (end - 40.0F) / 260.0F, 0.0F, 1.0F);
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }
}
