package io.github.salvagedisc.fallow.mixin.titres;

import io.github.salvagedisc.fallow.titres.TitresClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Le titre et le libellé de l'inventaire, écrits par défaut dans le panneau : plus sur les écrans
 * vanilla, dont la plaque porte le titre. Ce que les écrans ajoutent avant ou après (le coût de
 * l'enclume) reste.
 */
@Mixin(AbstractContainerScreen.class)
abstract class AbstractContainerScreenMixin {
    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void fallow$hideLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (TitresClient.hidesLabels((AbstractContainerScreen<?>) (Object) this)) {
            ci.cancel();
        }
    }
}
