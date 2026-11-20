package io.github.salvagedisc.fallow.mixin.dinos;

import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** The layer definitions of the entity models, replaced by a new map on every resource reload. */
@Mixin(EntityModelSet.class)
public interface EntityModelSetAccessor {
    @Accessor("roots")
    Map<ModelLayerLocation, LayerDefinition> fallow$roots();
}
