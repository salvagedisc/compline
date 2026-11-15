package io.github.salvagedisc.compline.faune.moose;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.faune.FauneModule;
import net.minecraft.resources.ResourceLocation;

/** L'élan d'Alex's Mobs. Classe chargée seulement si Alex's Mobs est là. */
public final class MooseReplacement {
    private MooseReplacement() {
    }

    public static void register() {
        FauneModule.add(ResourceLocation.fromNamespaceAndPath(ModIds.ALEXS_MOBS, "moose"), ComplineConfig::fauneMoose, MooseRenderer::new);
    }
}
