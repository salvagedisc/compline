package io.github.salvagedisc.fallow.clairiere;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module clairiere : la brume se lève là où le joueur l'a décidé. Deux blocs à poser, le feu de veille
 * (rayon 16) et la lanterne de clairière (rayon 32), changent les biomes autour d'eux en clairière tant qu'ils
 * restent en place : brume légère (preset ATMOSPHERICS), herbe vive (Polytone), musique des clairières, et le
 * paysage lointain de Distant Horizons. Le troisième palier, le dissipateur (rayon 64), est un bloc Create
 * du module {@code dissipateur}.
 *
 * <p>Les blocs sont toujours enregistrés (le serveur et les clients doivent avoir les mêmes registres) ;
 * l'interrupteur de la config ne coupe que la conversion.
 */
public final class ClairiereModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Fallow.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Fallow.MOD_ID);

    public static final DeferredBlock<ClearingBlock> FEU_DE_VEILLE = BLOCKS.register("feu_de_veille",
            () -> new ClearingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(2.0F, 6.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.STONE).lightLevel(state -> 13).noOcclusion(),
                    16, Block.box(2, 0, 2, 14, 9, 14), true));
    public static final DeferredBlock<ClearingBlock> LANTERNE_DE_CLAIRIERE = BLOCKS.register("lanterne_de_clairiere",
            () -> new ClearingBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL).strength(3.5F, 8.0F).requiresCorrectToolForDrops()
                    .sound(SoundType.LANTERN).lightLevel(state -> 15).noOcclusion(),
                    32, Block.box(4, 0, 4, 12, 16, 12), false));

    public static final DeferredItem<BlockItem> FEU_DE_VEILLE_ITEM = ITEMS.registerSimpleBlockItem(FEU_DE_VEILLE);
    public static final DeferredItem<BlockItem> LANTERNE_DE_CLAIRIERE_ITEM = ITEMS.registerSimpleBlockItem(LANTERNE_DE_CLAIRIERE);

    private ClairiereModule() {
    }

    public static void start(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, ClairiereModule::onCreativeTab);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class,
                event -> ClearingConverter.tick(event.getServer(), FallowConfig.clairiereChunksPerTick()));
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> ClearingConverter.clear());
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, ClairiereModule::onRegisterCommands);
        Fallow.LOGGER.info("clairiere : feu de veille (16), lanterne de clairière (32)");
    }

    /**
     * {@code /fallow clairiere <rayon>} (opérateurs) : convertit autour de la position de la commande.
     * {@code /fallow brume <rayon>} : rend la brume au disque, et aux clairières dont la source y est.
     */
    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Fallow.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clairiere")
                        .then(Commands.argument("rayon", IntegerArgumentType.integer(4, 256))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int radius = IntegerArgumentType.getInteger(context, "rayon");
                                    ClearingConverter.convert(source.getLevel(), BlockPos.containing(source.getPosition()),
                                            radius, source.getPlayer());
                                    source.sendSuccess(() -> Component.literal("Clairière de rayon " + radius + " programmée"), true);
                                    return radius;
                                })))
                .then(Commands.literal("brume")
                        .then(Commands.argument("rayon", IntegerArgumentType.integer(4, 256))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int radius = IntegerArgumentType.getInteger(context, "rayon");
                                    ClearingConverter.restoreWithin(source.getLevel(), BlockPos.containing(source.getPosition()),
                                            radius, source.getPlayer());
                                    source.sendSuccess(() -> Component.literal("Retour de la brume programmé sur " + radius + " blocs"), true);
                                    return radius;
                                }))));
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(FEU_DE_VEILLE_ITEM);
            event.accept(LANTERNE_DE_CLAIRIERE_ITEM);
        }
    }
}
