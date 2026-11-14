package io.github.salvagedisc.compline.dissipateur;

import com.simibubi.create.api.stress.BlockStressValues;
import io.github.salvagedisc.compline.Compline;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module dissipateur : le troisième palier des clairières, un vrai bloc cinétique de Create. Entraîné par
 * un arbre d'au moins 32 tr/min sans surcharge pendant {@link DissipateurBlockEntity#CHARGE_TICKS} ticks,
 * il change en clairière les biomes d'un disque de rayon 64. Il consomme du stress (visible aux lunettes
 * d'ingénieur) tant qu'il tourne.
 */
public final class DissipateurModule {
    /** Impact de stress, en unités par tr/min (le ventilateur de Create en demande 2). */
    static final double STRESS_IMPACT = 8.0;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Compline.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Compline.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Compline.MOD_ID);

    public static final DeferredBlock<DissipateurBlock> DISSIPATEUR = BLOCKS.register("dissipateur",
            () -> new DissipateurBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.TERRACOTTA_YELLOW).strength(4.0F, 10.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.METAL).lightLevel(state -> 10)));
    public static final DeferredItem<BlockItem> DISSIPATEUR_ITEM = ITEMS.registerSimpleBlockItem(DISSIPATEUR);
    @SuppressWarnings("DataFlowIssue") // le type de données est facultatif (null), comme partout en 1.21.1
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DissipateurBlockEntity>> DISSIPATEUR_BE =
            BLOCK_ENTITIES.register("dissipateur",
                    () -> BlockEntityType.Builder.of(DissipateurBlockEntity::new, DISSIPATEUR.get()).build(null));

    private DissipateurModule() {
    }

    public static void start(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        modBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(
                () -> BlockStressValues.IMPACTS.register(DISSIPATEUR.get(), () -> STRESS_IMPACT)));
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(DISSIPATEUR_ITEM);
            }
        });
        Compline.LOGGER.info("dissipateur : bloc Create, rayon {}", DissipateurBlockEntity.RADIUS);
    }
}
