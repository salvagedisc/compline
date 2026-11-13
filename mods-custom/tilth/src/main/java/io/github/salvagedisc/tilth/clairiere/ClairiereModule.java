package io.github.salvagedisc.tilth.clairiere;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import io.github.salvagedisc.tilth.Tilth;
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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module clairiere : la brume se lève là où le joueur l'a décidé. Deux blocs à poser, le feu de veille
 * (rayon 16) et la lanterne de clairière (rayon 32), ouvrent une clairière autour d'eux tant qu'ils restent en
 * place : brume légère (si un preset ATMOSPHERICS donne un profil aux biomes de clairière ; aucun depuis la
 * décision 0024), herbe vive, musique des clairières, paysage lointain de Distant
 * Horizons, et plus de monstres. Le troisième palier, le dissipateur (rayon 64), est un bloc Create du module
 * {@code dissipateur}.
 *
 * <p>Une clairière ne change pas le biome ({@link Clearings}) : le serveur retient son disque et l'annonce aux
 * clients ({@link ClearingSync}), qui en font l'apparence ({@link ClearingClient}).
 *
 * <p>Les blocs sont toujours enregistrés (le serveur et les clients doivent avoir les mêmes registres) ;
 * l'interrupteur de la config ne coupe que l'ouverture des clairières.
 */
public final class ClairiereModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Tilth.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Tilth.MOD_ID);

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
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToClient(ClearingSync.TYPE, ClearingSync.STREAM_CODEC,
                        (sync, context) -> context.enqueueWork(() -> ClearingClient.receive(sync))));
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Clearings.sendAll(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(MobSpawnEvent.PositionCheck.class, Clearings::onSpawnCheck);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> OldClearings.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, event -> OldClearings.clear());
        if (FMLEnvironment.dist.isClient()) {
            ClearingClient.start();
        }
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, ClairiereModule::onRegisterCommands);
        Tilth.LOGGER.info("clairiere : feu de veille (16), lanterne de clairière (32)");
    }

    /**
     * {@code /tilth clairiere <rayon>} (opérateurs) : ouvre une clairière autour de la position de la commande.
     * {@code /tilth brume <rayon>} : referme les clairières dont le centre est dans le disque, et rend à leur
     * biome les cases de clairière des mondes d'avant la 0.8.0.
     */
    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Tilth.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("clairiere")
                        .then(Commands.argument("rayon", IntegerArgumentType.integer(4, 256))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int radius = IntegerArgumentType.getInteger(context, "rayon");
                                    Clearings.open(source.getLevel(), BlockPos.containing(source.getPosition()),
                                            radius, source.getPlayer());
                                    source.sendSuccess(() -> Component.literal("Clairière de rayon " + radius + " ouverte"), true);
                                    return radius;
                                })))
                .then(Commands.literal("brume")
                        .then(Commands.argument("rayon", IntegerArgumentType.integer(4, 256))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    int radius = IntegerArgumentType.getInteger(context, "rayon");
                                    int closed = Clearings.closeWithin(source.getLevel(),
                                            BlockPos.containing(source.getPosition()), radius, source.getPlayer());
                                    source.sendSuccess(() -> Component.literal(closed + " clairière(s) refermée(s) sur " + radius + " blocs"), true);
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
