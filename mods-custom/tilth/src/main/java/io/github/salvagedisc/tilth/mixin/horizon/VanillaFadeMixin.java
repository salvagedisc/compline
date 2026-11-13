package io.github.salvagedisc.tilth.mixin.horizon;

import com.seibel.distanthorizons.core.api.internal.ClientApi;
import io.github.salvagedisc.tilth.horizon.HorizonFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Distant Horizons 3.3.3 fond le bord du terrain normal dans les LOD : il mêle, au bord de la distance de rendu,
 * la couleur du terrain à celle de sa texture de LOD. Quand le module horizon annule le rendu LOD (dans la
 * brume), cette texture n'est plus effacée ni redessinée et garde la dernière image de LOD : en sortant d'une
 * clairière, des montagnes figées apparaissent dans la brume au bord du terrain, et ne suivent pas la caméra.
 * On saute donc ce fondu dans les images sans LOD. {@code require = 0} : sans effet si une version future
 * change ces méthodes.
 */
@Mixin(value = ClientApi.class, remap = false)
abstract class VanillaFadeMixin {
    @Inject(method = {"renderFadeOpaque", "renderFadeTransparent"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void tilth$noFadeWithoutLods(CallbackInfo ci) {
        try {
            if (HorizonFog.skipLods()) {
                ci.cancel();
            }
        } catch (RuntimeException e) {
            // Le rendu de Distant Horizons ne doit jamais tomber à cause de Tilth : on laisse le fondu.
        }
    }
}
