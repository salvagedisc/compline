package io.github.salvagedisc.fallow.mixin.ciel;

import com.beash.atmospherics.Atmospherics;
import io.github.salvagedisc.fallow.ciel.FriendMoonRelay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pendant le rêve de la Lune Amie, ATMOSPHERICS se comporte comme quand la Lune Amie est levée : il laisse le ciel
 * et la brume à No Man's Land. Sinon, selon l'ordre des mixins, son ciel pouvait passer devant celui du rêve.
 * Appliqué seulement avec ATMOSPHERICS et No Man's Land.
 */
@Mixin(value = Atmospherics.class, remap = false)
abstract class AtmosphericsDreamMixin {
    @Inject(method = "isFriendMoonActive", at = @At("HEAD"), cancellable = true)
    private static void fallow$dreamCountsAsFriendMoon(CallbackInfoReturnable<Boolean> cir) {
        if (FriendMoonRelay.dreamRenders()) {
            cir.setReturnValue(true);
        }
    }
}
