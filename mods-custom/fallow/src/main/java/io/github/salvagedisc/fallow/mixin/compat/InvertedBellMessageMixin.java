package io.github.salvagedisc.fallow.mixin.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * No Man's Land écrit en dur, en anglais, le message « Successfully linked bell » quand on pose une
 * cloche inversée liée à une autre. Le message passe ici par une clé de langue, traduite par le pack.
 * Appliqué seulement avec No Man's Land.
 */
@Mixin(targets = "com.farcr.nomansland.common.block.InvertedBellBlock", remap = false)
abstract class InvertedBellMessageMixin {
    @Redirect(method = "setPlacedBy", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/chat/Component;literal(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"))
    private MutableComponent fallow$translatedLinkMessage(String text) {
        return Component.translatable("message.fallow.nomansland.cloche_liee");
    }
}
