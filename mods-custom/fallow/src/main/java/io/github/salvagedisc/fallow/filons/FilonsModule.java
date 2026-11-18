package io.github.salvagedisc.fallow.filons;

import io.github.salvagedisc.fallow.Fallow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module filons : de vrais gisements, rares et liés au biome, avec des affleurements en surface au-dessus
 * (docs/plan/progression.md). Le module n'enregistre que le type d'élément de génération {@code fallow:filon} ;
 * les filons eux-mêmes (minerai, taille, biomes, échantillon de Surface Samples) sont des données du pack.
 *
 * <p>Le type est toujours enregistré : les fichiers du pack le nomment, et un monde ne se charge pas s'il manque.
 */
public final class FilonsModule {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Fallow.MOD_ID);

    public static final DeferredHolder<Feature<?>, FilonFeature> FILON =
            FEATURES.register("filon", () -> new FilonFeature(FilonConfig.CODEC));

    private FilonsModule() {
    }

    public static void start(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
