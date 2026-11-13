package io.github.salvagedisc.tilth.mixin.averse;

import io.github.salvagedisc.tilth.averse.StormLeaves;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Par vent fort et sous l'orage, les arbres perdent davantage de feuilles ({@link StormLeaves}). Avec Particular. */
@Mixin(LeavesBlock.class)
abstract class LeavesBlockMixin {
    @Inject(method = "animateTick", at = @At("HEAD"), require = 0)
    private void tilth$storm(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        StormLeaves.maybeTear(state, level, pos, random);
    }
}
