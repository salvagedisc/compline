package io.github.salvagedisc.fallow.mixin.postes;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.pockyl.diegesis.api.client.DiegesisClient;

import io.github.salvagedisc.fallow.Fallow;

/**
 * Hands-On Vanilla ouvre EMI à ses postes seulement (ses barres latérales, ses touches recettes et usages sur l'objet survolé, son
 * « + » qui garnit le menu du poste). Nos postes le reçoivent aussi : sa question « est-ce un de nos postes ? » répond oui pour un
 * poste de Fallow, et tout le reste de Hands-On Vanilla s'applique tel quel.
 */
@Pseudo
@Mixin(targets = "com.pockyl.handson_vanilla.client.StationHover", remap = false)
abstract class StationHoverMixin {
    @Inject(method = "isOurs", at = @At("RETURN"), cancellable = true, remap = false, require = 0)
    private static void fallow$nosPostesAussi(Screen screen, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValueZ() && DiegesisClient.isStationScreen(screen) && DiegesisClient.focusedStation()
                .map(view -> view.type().id().getNamespace().equals(Fallow.MOD_ID)).orElse(false)) {
            callback.setReturnValue(true);
        }
    }
}
