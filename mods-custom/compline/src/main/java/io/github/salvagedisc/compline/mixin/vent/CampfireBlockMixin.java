package io.github.salvagedisc.compline.mixin.vent;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.vent.Smoke;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** La fumée des feux de camp passe par {@link Smoke} côté client : vent, panaches qui se rejoignent, feu des âmes. */
@Mixin(CampfireBlock.class)
abstract class CampfireBlockMixin {
    @Inject(method = "makeParticles", at = @At("HEAD"), cancellable = true)
    private static void compline$smoke(Level level, BlockPos pos, boolean signal, boolean extra, CallbackInfo ci) {
        if (level.isClientSide() && ComplineConfig.ventEnabled()) {
            Smoke.emit(level, pos, signal, extra);
            ci.cancel();
        }
    }
}
