package io.github.salvagedisc.fallow.etabli;

import io.github.salvagedisc.fallow.Fallow;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module etabli (prototype) : fabriquer à la main sur la table de craft de Visual Workbench, et le carnet de l'établi
 * posé à côté qui montre une recette en fantôme sur la table (docs/plan/etabli.md). Le carnet est toujours enregistré ;
 * l'interrupteur {@code etabli.enabled} (faux par défaut) n'arrête que les gestes et les pages.
 */
public final class EtabliModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Fallow.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Fallow.MOD_ID);

    public static final DeferredBlock<CarnetBlock> CARNET = BLOCKS.register("carnet_d_etabli",
            () -> new CarnetBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BROWN).strength(0.3F)
                    .sound(SoundType.WOOL).noOcclusion().pushReaction(PushReaction.DESTROY).ignitedByLava()));
    public static final DeferredItem<BlockItem> CARNET_ITEM = ITEMS.registerSimpleBlockItem(CARNET);

    private EtabliModule() {
    }

    public static void start(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, event -> {
            if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
                event.accept(CARNET_ITEM);
            }
        });
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToClient(EtabliPage.TYPE, EtabliPage.STREAM_CODEC,
                        (page, context) -> context.enqueueWork(() -> Pages.setClient(page))));
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickBlock.class, Etabli::onRightClick);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> Pages.forget(event.getEntity()));
        if (FMLEnvironment.dist == Dist.CLIENT) {
            io.github.salvagedisc.fallow.etabli.client.EtabliClient.register();
        }
        Fallow.LOGGER.info("etabli : gestes sur la table de craft et carnet de l'établi");
    }
}
