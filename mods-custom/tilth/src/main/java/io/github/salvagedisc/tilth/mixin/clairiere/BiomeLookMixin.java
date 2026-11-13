package io.github.salvagedisc.tilth.mixin.clairiere;

import io.github.salvagedisc.tilth.clairiere.ClearingClient;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Pendant le calcul d'une teinte dans une clairière ({@link ClearingClient#tint}), les couleurs de base du vrai
 * biome sont celles du biome de clairière. Ce qui s'ajoute à ces couleurs d'après le vrai biome, comme les
 * teintes de saison d'Ecliptic Seasons, reste donc à sa place. Hors de ce calcul, rien ne change.
 */
@Mixin(Biome.class)
abstract class BiomeLookMixin {
    @Inject(method = "getGrassColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void tilth$clearingGrass(double x, double z, CallbackInfoReturnable<Integer> callback) {
        Biome look = ClearingClient.painted((Biome) (Object) this);
        if (look != null) {
            callback.setReturnValue(look.getGrassColor(x, z));
        }
    }

    @Inject(method = "getFoliageColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void tilth$clearingFoliage(CallbackInfoReturnable<Integer> callback) {
        Biome look = ClearingClient.painted((Biome) (Object) this);
        if (look != null) {
            callback.setReturnValue(look.getFoliageColor());
        }
    }

    @Inject(method = "getWaterColor", at = @At("HEAD"), cancellable = true, require = 0)
    private void tilth$clearingWater(CallbackInfoReturnable<Integer> callback) {
        Biome look = ClearingClient.painted((Biome) (Object) this);
        if (look != null) {
            callback.setReturnValue(look.getWaterColor());
        }
    }
}
