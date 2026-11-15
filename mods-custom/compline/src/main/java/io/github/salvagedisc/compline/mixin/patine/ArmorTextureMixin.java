package io.github.salvagedisc.compline.mixin.patine;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.patine.Patine;
import io.github.salvagedisc.compline.patine.client.PatineTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * La couche d'armure du jeu demande sa texture à NeoForge, armure par armure : on lui rend la version salie quand la
 * pièce porte de la patine. Vaut pour les armures des mods qui passent par cette couche.
 */
@Mixin(value = ClientHooks.class, remap = false)
abstract class ArmorTextureMixin {
    @Inject(method = "getArmorTexture", at = @At("RETURN"), cancellable = true)
    private static void compline$soiled(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean inner,
            EquipmentSlot slot, CallbackInfoReturnable<ResourceLocation> cir) {
        int level = Patine.level(stack);
        if (level > 0 && ComplineConfig.patineEnabled()) {
            cir.setReturnValue(PatineTextures.get(cir.getReturnValue(), level));
        }
    }
}
