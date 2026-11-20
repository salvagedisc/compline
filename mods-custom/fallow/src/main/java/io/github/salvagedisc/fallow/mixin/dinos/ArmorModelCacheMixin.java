package io.github.salvagedisc.fallow.mixin.dinos;

import com.eternal_creations.archies_dino_mod.client.model.armor.ADArmorModel;
import com.eternal_creations.archies_dino_mod.common.items.util.ADArmorItem;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Archie's Dinosaurs bakes a whole new armour model every time a piece of its armour is drawn: four bakes per wearer
 * per frame, all garbage a moment later. EMF also gives up on the model after 64 creations. The model holds no state
 * of its own between draws (NeoForge copies the pose and part visibility onto it before each draw, as vanilla does
 * with its shared armour models), so one model per layer is kept and reused. The cache is dropped when a resource
 * reload replaces the layer definitions.
 */
@Mixin(value = ADArmorItem.class, remap = false)
public abstract class ArmorModelCacheMixin {
    @Unique
    private static final Map<ModelLayerLocation, ADArmorModel> FALLOW$MODELS = new HashMap<>();
    @Unique
    private static Map<ModelLayerLocation, LayerDefinition> fallow$roots;

    @Shadow
    public abstract ModelLayerLocation getLayerLocation();

    @Inject(method = "initArmorModel", require = 0, at = @At("HEAD"), cancellable = true)
    private void fallow$reuse(CallbackInfoReturnable<ADArmorModel> cir) {
        Map<ModelLayerLocation, LayerDefinition> roots =
                ((EntityModelSetAccessor) Minecraft.getInstance().getEntityModels()).fallow$roots();
        if (roots != fallow$roots) {
            FALLOW$MODELS.clear();
            fallow$roots = roots;
        }
        ADArmorModel model = FALLOW$MODELS.get(getLayerLocation());
        if (model != null) {
            cir.setReturnValue(model);
        }
    }

    @Inject(method = "initArmorModel", require = 0, at = @At("RETURN"))
    private void fallow$keep(CallbackInfoReturnable<ADArmorModel> cir) {
        FALLOW$MODELS.put(getLayerLocation(), cir.getReturnValue());
    }
}
