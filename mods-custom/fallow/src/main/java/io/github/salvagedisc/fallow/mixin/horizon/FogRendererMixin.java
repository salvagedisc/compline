package io.github.salvagedisc.fallow.mixin.horizon;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.fallow.horizon.TerrainFog;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Relève la brume du terrain à la fin de {@code setupFog}. La priorité, plus haute que celle par défaut,
 * place ce code après ceux d'ATMOSPHERICS et de Distant Horizons au même point : on lit la valeur finale.
 * Appliqué seulement avec Distant Horizons.
 */
@Mixin(value = FogRenderer.class, priority = 1500)
abstract class FogRendererMixin {
    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void fallow$captureTerrainFog(Camera camera, FogRenderer.FogMode fogMode, float farPlaneDistance,
                                                 boolean shouldCreateFog, float partialTick, CallbackInfo ci) {
        if (fogMode == FogRenderer.FogMode.FOG_TERRAIN) {
            TerrainFog.capture(RenderSystem.getShaderFogStart(), RenderSystem.getShaderFogEnd());
        }
    }
}
