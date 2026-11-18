package io.github.salvagedisc.fallow.mixin.ciel;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.fallow.ciel.MoonOverlay;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Cache la lune vanilla d'ATMOSPHERICS : pendant son ciel seulement (drapeaux de {@link MoonOverlay}),
 * la texture {@code moon_phases.png} est remplacée par une texture transparente. En dehors, la texture
 * passe telle quelle (un test de booléen par appel).
 */
@Mixin(RenderSystem.class)
abstract class RenderSystemMixin {
    @ModifyVariable(
            method = "setShaderTexture(ILnet/minecraft/resources/ResourceLocation;)V",
            at = @At("HEAD"),
            argsOnly = true)
    private static ResourceLocation fallow$hideAtmosphericsMoon(ResourceLocation texture) {
        return MoonOverlay.swapMoonTexture(texture);
    }
}
