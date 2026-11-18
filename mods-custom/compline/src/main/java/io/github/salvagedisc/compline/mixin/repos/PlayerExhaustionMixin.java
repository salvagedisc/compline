package io.github.salvagedisc.compline.mixin.repos;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.repos.ReposModule;
import io.github.salvagedisc.compline.repos.ReposeEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** « Reposé » creuse moins la faim : chaque effort compte un quart de moins. */
@Mixin(Player.class)
abstract class PlayerExhaustionMixin {
    @ModifyVariable(method = "causeFoodExhaustion", at = @At("HEAD"), argsOnly = true)
    private float compline$repose(float exhaustion) {
        Player self = (Player) (Object) this;
        if (ReposModule.REPOSE.isBound() && ComplineConfig.reposEnabled() && self.hasEffect(ReposModule.REPOSE)) {
            return exhaustion * ReposeEffect.HUNGER_FACTOR;
        }
        return exhaustion;
    }
}
