package io.github.salvagedisc.fallow.mixin.averse;

import io.github.salvagedisc.fallow.averse.RainLook;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Encadre le dessin de la pluie de Where Winds Blow : la couleur du shader prend la teinte de
 * {@link RainLook} avant, et revient au blanc après. Appliqué seulement avec Where Winds Blow ;
 * sans effet si la méthode change ({@code require = 0}).
 */
@Pseudo
@Mixin(targets = "com.jvn.wherewindsblow.client.weather.WindReactivePrecipitationRenderer", remap = false)
abstract class WwbRainMixin {
    @Inject(method = "render", at = @At("HEAD"), require = 0)
    private static void fallow$tint(ClientLevel level, int ticks, float[] rainSizeX, float[] rainSizeZ, LightTexture lightTexture,
            float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        RainLook.begin(level, partialTick);
    }

    @Inject(method = "render", at = @At("RETURN"), require = 0)
    private static void fallow$untint(ClientLevel level, int ticks, float[] rainSizeX, float[] rainSizeZ, LightTexture lightTexture,
            float partialTick, double camX, double camY, double camZ, CallbackInfo ci) {
        RainLook.end();
    }
}
