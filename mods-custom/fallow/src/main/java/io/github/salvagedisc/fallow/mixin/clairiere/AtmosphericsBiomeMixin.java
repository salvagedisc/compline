package io.github.salvagedisc.fallow.mixin.clairiere;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.fallow.clairiere.ClearingClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * ATMOSPHERICS choisit la brume, la brume de l'air, la météo et le voile d'horizon d'après le biome lu autour
 * du joueur. Dans une clairière le vrai biome est resté : ces lectures reçoivent à sa place le biome de
 * clairière, qui porte le profil dégagé d'un preset s'il y en a un (aucun depuis la décision 0024). Les particules propres à un biome (poussière du désert,
 * buissons roulants) gardent le vrai biome. Appliqué seulement avec ATMOSPHERICS ; {@code require = 0} : une
 * méthode renommée par une version future laisse sa lecture telle quelle.
 */
@Mixin(targets = {
        "com.beash.atmospherics.util.BiomeBlendUtil",
        "com.beash.atmospherics.util.AtmosphericEnvironmentVisuals",
        "com.beash.atmospherics.particle.AirHazeBiomeSettings",
        "com.beash.atmospherics.util.HorizonHazeCometRenderer"}, remap = false)
abstract class AtmosphericsBiomeMixin {
    @WrapOperation(method = {"collect", "getBiomeId", "smooth", "renderHaze", "getCurrentBiomeSettings"}, require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;"
                    + "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private static Holder<Biome> fallow$clearingLook(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        return ClearingClient.look(pos, original.call(level, pos));
    }
}
