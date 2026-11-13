package io.github.salvagedisc.tilth.mixin.clairiere;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.tilth.clairiere.ClearingClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * True Adaptive Music choisit la musique d'après le biome du joueur. Dans une clairière, il reçoit le biome de
 * clairière : la musique des clairières y joue comme avant, alors que le vrai biome est resté. Appliqué
 * seulement avec True Adaptive Music.
 */
@Pseudo
@Mixin(targets = "liltojustice.trueadaptivemusic.client.trigger.predicate.types.BiomePredicate", remap = false)
abstract class MusicBiomeMixin {
    @WrapOperation(method = "test", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;"
                    + "getBiome(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Holder;"))
    private Holder<Biome> tilth$clearingLook(ClientLevel level, BlockPos pos, Operation<Holder<Biome>> original) {
        return ClearingClient.look(pos, original.call(level, pos));
    }
}
