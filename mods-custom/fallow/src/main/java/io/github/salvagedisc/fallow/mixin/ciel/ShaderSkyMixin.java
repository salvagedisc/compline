package io.github.salvagedisc.fallow.mixin.ciel;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.fallow.ciel.ShaderSky;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Encadre l'appel à {@code renderSky} pour {@link ShaderSky}. Appliqué seulement avec ATMOSPHERICS et Iris
 * (voir le plugin de mixins).
 */
@Mixin(LevelRenderer.class)
abstract class ShaderSkyMixin {
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/Camera;ZLjava/lang/Runnable;)V"))
    private void fallow$shaderSky(LevelRenderer renderer, Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick,
            Camera camera, boolean isFoggy, Runnable skyFogSetup, Operation<Void> original) {
        boolean previous = ShaderSky.begin();
        try {
            original.call(renderer, frustumMatrix, projectionMatrix, partialTick, camera, isFoggy, skyFogSetup);
        } finally {
            ShaderSky.end(previous);
        }
    }
}
