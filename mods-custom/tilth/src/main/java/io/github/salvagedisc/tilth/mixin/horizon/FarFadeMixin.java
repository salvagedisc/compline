package io.github.salvagedisc.tilth.mixin.horizon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seibel.distanthorizons.common.render.openGl.postProcessing.fade.GlDhFarFadeShader_neoforge;
import io.github.salvagedisc.tilth.horizon.HorizonFog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Le fondu lointain de Distant Horizons efface les LOD dans le ciel entre 50 % et 90 % de son plan de coupe
 * lointain, c'est-à-dire au bout du rayon LOD (1 536 blocs dans Tilth). On lui donne à la place la distance du
 * cercle de brume ({@link HorizonFog#farFadeDistance}) : au-delà, le relief disparaît au lieu de se découper en
 * silhouette couleur de brume sur le ciel. {@code require = 0} : sans effet si une version future change ce code.
 */
@Mixin(value = GlDhFarFadeShader_neoforge.class, remap = false)
abstract class FarFadeMixin {
    @WrapOperation(method = "onApplyUniforms", require = 0,
            at = @At(value = "INVOKE",
                    target = "Lcom/seibel/distanthorizons/core/util/RenderUtil;getFarClipPlaneDistanceInBlocks()F"))
    private float tilth$fadeAtFogRing(Operation<Float> original) {
        float farClip = original.call();
        try {
            return HorizonFog.farFadeDistance(farClip);
        } catch (RuntimeException e) {
            return farClip;
        }
    }
}
