package io.github.salvagedisc.compline.mixin.ciel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.compline.ciel.MoonOverlay;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Encadre le dessin du ciel pour {@link MoonOverlay}. On n'injecte pas dans le ciel d'ATMOSPHERICS
 * lui-même (c'est un mixin) : on entoure l'appel à {@code renderSky}, et on repère quand le corps
 * vanilla s'exécute, ce qui n'arrive que si ATMOSPHERICS n'a pas annulé {@code renderSky}.
 * Appliqué seulement avec ATMOSPHERICS et Enhanced Celestials (voir le plugin de mixins).
 */
@Mixin(LevelRenderer.class)
abstract class LevelRendererMixin {
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V"))
    private void compline$aroundSky(LevelRenderer renderer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
            Camera camera, boolean isFoggy, Runnable skyFogSetup, Operation<Void> original) {
        MoonOverlay.beginSky();
        try {
            original.call(renderer, frustumMatrix, projectionMatrix, partialTick, camera, isFoggy, skyFogSetup);
        } finally {
            MoonOverlay.endSky(frustumMatrix, partialTick);
        }
    }

    /** Premier appel du corps vanilla de {@code renderSky} ({@code skyFogSetup.run()}), après le crochet NeoForge. */
    @Inject(
            method = "renderSky",
            at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", ordinal = 0))
    private void compline$vanillaSkyRuns(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick, Camera camera,
            boolean isFoggy, Runnable skyFogSetup, CallbackInfo ci) {
        MoonOverlay.markVanillaSky();
    }
}
