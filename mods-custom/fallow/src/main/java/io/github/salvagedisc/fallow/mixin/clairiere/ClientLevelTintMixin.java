package io.github.salvagedisc.fallow.mixin.clairiere;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.fallow.clairiere.ClearingClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Là où le jeu calcule la teinte d'un bloc (herbe, feuillage, eau), il interroge une règle de couleur avec le
 * biome de chaque point. Dans une clairière, la règle reçoit toujours le vrai biome, mais ses couleurs de base
 * sont celles du biome de clairière le temps de l'appel ({@link ClearingClient#tint}). On passe par l'appel, et
 * non par les constantes de {@code BiomeColors} : Ecliptic Seasons les remplace à leur tour.
 */
@Mixin(ClientLevel.class)
abstract class ClientLevelTintMixin {
    @WrapOperation(method = "calculateBlockTint", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/ColorResolver;"
                    + "getColor(Lnet/minecraft/world/level/biome/Biome;DD)I"))
    private int fallow$clearingLook(ColorResolver resolver, Biome biome, double x, double z, Operation<Integer> original) {
        return ClearingClient.tint(resolver, biome, x, z, original);
    }
}
