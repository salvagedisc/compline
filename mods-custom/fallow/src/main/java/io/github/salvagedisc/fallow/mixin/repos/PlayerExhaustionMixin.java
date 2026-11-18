package io.github.salvagedisc.fallow.mixin.repos;

import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.repos.ReposModule;
import io.github.salvagedisc.fallow.repos.ReposeEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** « Reposé » creuse moins la faim : chaque effort compte un quart de moins. */
@Mixin(Player.class)
abstract class PlayerExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float fallow$repose(float exhaustion) {
        Player self = (Player) (Object) this;
        if (ReposModule.REPOSE.isBound() && FallowConfig.reposEnabled() && self.hasEffect(ReposModule.REPOSE)) {
            return exhaustion * ReposeEffect.HUNGER_FACTOR;
        }
        return exhaustion;
    }
}
