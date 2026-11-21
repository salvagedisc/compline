package io.github.salvagedisc.fallow.mixin.compat;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Punchy! rejoue la sortie de main dès qu'un composant de l'objet tenu change. Il compare la valeur que
 * l'itération des composants donne (celle de l'objet de base : 64) à celle que {@code get} donne, que
 * Sensible Stackables remplace par la taille de pile du pack (32, 16...). Les deux ne s'accordent jamais :
 * la main redescendait et remontait à chaque tick, sans montrer l'objet. La taille de pile ne change
 * jamais l'objet en main, Punchy! l'ignore donc ici, comme il ignore déjà l'usure.
 * Appliqué seulement avec Punchy! et Sensible Stackables.
 */
@Mixin(targets = "punchy.client.state.HandEquipStateMachine", remap = false)
abstract class PunchyEquipMixin {
    @Inject(method = "shouldIgnoreComponentForEquipDiff", at = @At("HEAD"), cancellable = true, require = 0)
    private void fallow$ignoreStackSize(DataComponentType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (type == DataComponents.MAX_STACK_SIZE) {
            cir.setReturnValue(true);
        }
    }
}
