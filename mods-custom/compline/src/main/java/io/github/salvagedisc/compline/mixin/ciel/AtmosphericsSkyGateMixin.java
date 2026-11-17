package io.github.salvagedisc.compline.mixin.ciel;

import com.beash.atmospherics.Atmospherics;
import io.github.salvagedisc.compline.ciel.ShaderSky;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ATMOSPHERICS remplace le ciel vanilla, sauf si No Man's Land est chargé et sa lune amie levée. Tant que
 * {@link ShaderSky#gateOpen()} tient (ciel en cours de dessin, pack de shaders actif), les deux questions
 * répondent oui : le ciel vanilla s'exécute et Iris le confie au pack. Partout ailleurs, et sans pack, rien
 * ne change. Appliqué seulement avec ATMOSPHERICS et Iris ; sans effet si les méthodes changent
 * ({@code require = 0}).
 */
@Mixin(value = Atmospherics.class, remap = false)
abstract class AtmosphericsSkyGateMixin {
    @Inject(method = "isNoMansLandLoaded", at = @At("HEAD"), cancellable = true, require = 0)
    private static void compline$noMansLand(CallbackInfoReturnable<Boolean> cir) {
        if (ShaderSky.gateOpen()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "isFriendMoonActive", at = @At("HEAD"), cancellable = true, require = 0)
    private static void compline$friendMoon(CallbackInfoReturnable<Boolean> cir) {
        if (ShaderSky.gateOpen()) {
            cir.setReturnValue(true);
        }
    }
}
