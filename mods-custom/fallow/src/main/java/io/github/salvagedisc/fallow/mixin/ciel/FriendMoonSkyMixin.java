package io.github.salvagedisc.fallow.mixin.ciel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.fallow.ciel.FriendMoonRelay;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Encadre le dessin du ciel pour {@link FriendMoonRelay}, comme {@code LevelRendererMixin} le fait pour la lune
 * d'Enhanced Celestials : on entoure l'appel à {@code renderSky} et sa mise en place de la brume, et on repère
 * quand le corps vanilla s'exécute. Appliqué seulement avec ATMOSPHERICS et No Man's Land (voir le plugin de mixins).
 */
@Mixin(LevelRenderer.class)
abstract class FriendMoonSkyMixin {
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V"))
    private void fallow$friendMoonAroundSky(LevelRenderer renderer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
            Camera camera, boolean isFoggy, Runnable skyFogSetup, Operation<Void> original) {
        Runnable tracked = FriendMoonRelay.beginSky(skyFogSetup);
        try {
            original.call(renderer, frustumMatrix, projectionMatrix, partialTick, camera, isFoggy, tracked);
        } finally {
            FriendMoonRelay.endSky(frustumMatrix, projectionMatrix, partialTick);
        }
    }

    /** Premier appel du corps vanilla de {@code renderSky} ({@code skyFogSetup.run()}), après le crochet NeoForge. */
    @Inject(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", ordinal = 0))
    private void fallow$friendMoonVanillaSkyRuns(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera,
            boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        FriendMoonRelay.markVanillaSky();
    }
}
