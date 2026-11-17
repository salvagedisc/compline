package io.github.salvagedisc.compline.mixin.patine;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.patine.Coat;
import io.github.salvagedisc.compline.patine.Patine;
import io.github.salvagedisc.compline.patine.client.PatineTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * La couche d'armure du jeu demande sa texture à NeoForge, armure par armure : on lui rend la version salie quand la
 * pièce porte de la terre, du sable, de la neige ou de l'eau. Vaut pour les armures des mods qui passent par cette
 * couche. Une armure teinte (le cuir) garde sur sa couche teinte la terre et l'eau qui fonce ; le sable, la neige et
 * les gouttes vont sur sa couche non teinte, sinon la teinte les rendrait bruns.
 */
@Mixin(value = ClientHooks.class, remap = false)
abstract class ArmorTextureMixin {
    @Inject(method = "getArmorTexture", at = @At("RETURN"), cancellable = true)
    private static void compline$soiled(Entity entity, ItemStack stack, ArmorMaterial.Layer layer, boolean inner,
            EquipmentSlot slot, CallbackInfoReturnable<ResourceLocation> cir) {
        if (!ComplineConfig.patineEnabled()) {
            return;
        }
        Coat coat = Patine.coat(stack);
        int key = PatineTextures.key(Patine.level(stack), coat.sandLevel(), coat.snowLevel(), coat.wetLevel());
        if (key == 0) {
            return;
        }
        boolean tinted = layer.dyeable();
        if (tinted && (key & 3) == 0 && coat.wetLevel() == 0) {
            return;
        }
        ResourceLocation shape = null;
        if (!tinted && stack.getItem() instanceof ArmorItem armor) {
            for (ArmorMaterial.Layer other : armor.getMaterial().value().layers()) {
                if (other.dyeable()) {
                    shape = other.texture(inner);
                    break;
                }
            }
            if (shape != null && (key & ~3) == 0) {
                return;
            }
        }
        cir.setReturnValue(PatineTextures.get(cir.getReturnValue(), key, tinted, shape));
    }
}
