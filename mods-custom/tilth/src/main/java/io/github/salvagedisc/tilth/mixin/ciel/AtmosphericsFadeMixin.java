package io.github.salvagedisc.tilth.mixin.ciel;

import com.beash.atmospherics.util.AtmosphericEnvironmentVisuals;
import io.github.salvagedisc.tilth.ciel.FogCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * La brume lissée d'ATMOSPHERICS ({@code getSmoothedFog} rend un tableau neuf {@code r, g, b, début, fin})
 * passe par {@link FogCache#smooth}, qui ralentit le changement de distance quand une clairière s'ouvre
 * ou se referme. Appliqué seulement avec ATMOSPHERICS ; sans effet si la méthode change ({@code require = 0}).
 */
@Mixin(value = AtmosphericEnvironmentVisuals.class, remap = false)
abstract class AtmosphericsFadeMixin {
    @Inject(method = "getSmoothedFog", at = @At("RETURN"), require = 0)
    private static void tilth$slowFade(ClientLevel world, Vec3 pos, CallbackInfoReturnable<float[]> cir) {
        float[] fog = cir.getReturnValue();
        if (fog != null && fog.length >= 5) {
            FogCache.smooth(fog);
        }
    }
}
