package io.github.salvagedisc.fallow.mixin.titres;

import io.github.salvagedisc.fallow.titres.TitresClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Les écrans vanilla qui écrivent leurs titres eux-mêmes, sans rien d'autre : l'inventaire du
 * joueur (« Fabrication ») et le villageois (son nom et son niveau, « Échanges », « Inventaire »).
 */
@Mixin({InventoryScreen.class, MerchantScreen.class})
abstract class OwnLabelsScreenMixin {
    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void fallow$hideLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (TitresClient.hidesLabels((AbstractContainerScreen<?>) (Object) this)) {
            ci.cancel();
        }
    }
}
