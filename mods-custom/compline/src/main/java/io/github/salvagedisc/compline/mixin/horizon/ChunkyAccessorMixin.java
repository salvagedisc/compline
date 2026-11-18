package io.github.salvagedisc.compline.mixin.horizon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seibel.distanthorizons.core.wrapperInterfaces.modAccessor.AbstractChunkyAccessor;
import io.github.salvagedisc.compline.Compline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Distant Horizons 3.3.3 s'abonne aux événements de Chunky au premier chunk chargé. Sur NeoForge, le monde
 * charge ses chunks d'apparition avant que Chunky démarre : {@code ChunkyProvider.get()} lève
 * « Chunky is not loaded » et le serveur plante à la création du monde (vu sur le serveur de test de Compline).
 * On rattrape l'erreur et on remet le drapeau à faux : Distant Horizons réessaie au chunk suivant, une fois
 * Chunky prêt. {@code require = 0} et le contrôle des membres de {@code ComplineMixinPlugin} : sans effet si une
 * version future change ce code.
 */
@Mixin(value = AbstractChunkyAccessor.class, remap = false)
abstract class ChunkyAccessorMixin {
    @Shadow
    private boolean listenerBound;

    @WrapOperation(method = "tryRunFirstTimeSetup", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lcom/seibel/distanthorizons/core/wrapperInterfaces/modAccessor/AbstractChunkyAccessor;bindOnGenerationProgressEvent()V"))
    private void compline$bindWhenChunkyReady(AbstractChunkyAccessor accessor, Operation<Void> original) {
        try {
            original.call(accessor);
        } catch (IllegalStateException e) {
            listenerBound = false;
            Compline.LOGGER.debug("horizon : Chunky pas encore prêt, Distant Horizons réessaiera ({})", e.getMessage());
        }
    }
}
