package io.github.salvagedisc.compline.mixin.dinos;

import com.eternal_creations.archies_dino_mod.common.items.armor.ChorusoatlArmorItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * L'armure du Chorusoatl ne téléporte plus (docs/plan/progression.md) : 45 blocs d'un coup effaceraient les hauteurs
 * qu'elle doit ouvrir. Elle garde la chute lente. Sans capacité, la touche d'armure répond le message
 * « pas de capacité » du mod.
 */
@Mixin(value = ChorusoatlArmorItem.class, remap = false)
public abstract class ChorusoatlArmorMixin {
    @Inject(method = "hasAbility", require = 0, at = @At("HEAD"), cancellable = true)
    private void compline$noAbility(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }

    @Inject(method = "activateServerSetAbility", require = 0, at = @At("HEAD"), cancellable = true)
    private void compline$noTeleport(ServerPlayer player, Vec3 position, Vec3 look, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
    }
}
