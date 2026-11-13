package io.github.salvagedisc.fallow.monuments;

import io.github.salvagedisc.fallow.Fallow;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module monuments : un type de structure, {@code fallow:terrain_checked}, qui enveloppe une structure
 * existante (ici les monuments d'Ancient Remnants) et ne la pose que sur un terrain qui lui convient :
 * pente faible, pas d'eau, assez de place sous un monolithe qui flotte, et pas de toit coupé par le plafond
 * du monde. Un emplacement refusé ne donne pas de monument dans sa case.
 *
 * <p>Le type est toujours enregistré : les fichiers de structure du pack le nomment, et un monde ne se charge
 * pas si un type manque.
 */
public final class MonumentsModule {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, Fallow.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<TerrainCheckedStructure>> TERRAIN_CHECKED =
            STRUCTURE_TYPES.register("terrain_checked", () -> () -> TerrainCheckedStructure.CODEC);

    private MonumentsModule() {
    }

    public static void start(IEventBus modBus) {
        STRUCTURE_TYPES.register(modBus);
        Fallow.LOGGER.info("monuments : type de structure fallow:terrain_checked");
    }
}
