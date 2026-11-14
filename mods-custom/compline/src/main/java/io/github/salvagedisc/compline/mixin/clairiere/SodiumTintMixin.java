package io.github.salvagedisc.compline.mixin.clairiere;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.compline.clairiere.ClearingClient;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Sodium calcule lui-même les teintes des tronçons qu'il dessine, sans passer par {@code ClientLevel} : même
 * détour que {@link ClientLevelTintMixin}, dans son cache de couleurs. Appliqué seulement avec Sodium ;
 * {@code require = 0} : si une version future range ce calcul ailleurs, les clairières perdent leurs couleurs
 * vives, rien d'autre.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.world.biome.LevelColorCache", remap = false)
abstract class SodiumTintMixin {
    @WrapOperation(method = "updateColorBuffers", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ColorResolver;"
                    + "getColor(Lnet/minecraft/world/level/biome/Biome;DD)I"))
    private int compline$clearingLook(ColorResolver resolver, Biome biome, double x, double z, Operation<Integer> original) {
        return ClearingClient.tint(resolver, biome, x, z, original);
    }
}
