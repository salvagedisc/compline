package io.github.salvagedisc.fallow.outils;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Module outils : à mains nues, on ramasse mais on ne coupe pas. Un bloc du tag {@code fallow:outil_requis}
 * (tout ce qui se travaille à la hache : troncs, planches, meubles, coffres) ou un bloc qui ne lâcherait rien
 * sans le bon outil (roches, minerais, briques) ne se casse plus du tout tant que le joueur ne tient pas ce
 * bon outil : sa vitesse de minage tombe à zéro, chez le client comme sur le serveur, et un message dit quel
 * outil il faut.
 *
 * <p>Le tag {@code fallow:sans_outil} garde à la main ce qui pousse (plantes, cultures, citrouilles, melons,
 * cacao, bambou) et la neige. Le créatif et les faux joueurs (déployeurs de Create) ne sont pas touchés.
 * La règle se règle par les tags, sans recompiler.
 */
public final class OutilsModule {
    public static final TagKey<Block> OUTIL_REQUIS = BlockTags.create(id("outil_requis"));
    public static final TagKey<Block> SANS_OUTIL = BlockTags.create(id("sans_outil"));

    /** Un message toutes les deux secondes au plus, tant que le joueur frappe. */
    private static final long HINT_INTERVAL = 40;
    private static long lastHint = Long.MIN_VALUE;

    private OutilsModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(PlayerEvent.BreakSpeed.class, OutilsModule::onBreakSpeed);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.HarvestCheck.class, OutilsModule::onHarvestCheck);
        // La config n'est pas encore chargée ici : ses valeurs sont lues à chaque usage.
        Fallow.LOGGER.info("outils : le bois et la roche demandent le bon outil (selon config/fallow-common.toml)");
    }

    /** Vrai si ce bloc demande un outil que le joueur n'a pas en main. */
    static boolean lacksTool(Player player, BlockState state) {
        if (!FallowConfig.outilsEnabled() || player.isCreative() || player instanceof FakePlayer) {
            return false;
        }
        if (state.is(SANS_OUTIL) || !(state.is(OUTIL_REQUIS) || state.requiresCorrectToolForDrops())) {
            return false;
        }
        return !player.getMainHandItem().isCorrectToolForDrops(state);
    }

    /** Tourne à chaque tick de minage, chez le client et sur le serveur. */
    private static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        BlockState state = event.getState();
        if (!lacksTool(player, state)) {
            return;
        }
        event.setNewSpeed(0.0F);
        if (player.level().isClientSide()) {
            long now = player.level().getGameTime();
            if (now - lastHint >= HINT_INTERVAL || now < lastHint) {
                lastHint = now;
                player.displayClientMessage(hint(player.getMainHandItem(), state), true);
            }
        }
    }

    /** Un tronc cassé autrement (explosion d'un mod, bloc qui cède sous le joueur) ne lâche rien sans hache. */
    private static void onHarvestCheck(PlayerEvent.HarvestCheck event) {
        if (event.canHarvest() && lacksTool(event.getEntity(), event.getTargetBlock())) {
            event.setCanHarvest(false);
        }
    }

    /** « Il te faut une hache », ou « Ton outil n'est pas assez solide » si c'est le bon genre d'outil. */
    static Component hint(ItemStack held, BlockState state) {
        String tool = toolFor(state);
        if (tool != null && held.is(toolTag(tool))) {
            return Component.translatable("message.fallow.outils.trop_faible");
        }
        return Component.translatable("message.fallow.outils." + (tool == null ? "outil" : tool));
    }

    private static String toolFor(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
            return "hache";
        }
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return "pioche";
        }
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return "pelle";
        }
        if (state.is(BlockTags.MINEABLE_WITH_HOE)) {
            return "houe";
        }
        return null;
    }

    private static TagKey<Item> toolTag(String tool) {
        return switch (tool) {
            case "hache" -> ItemTags.AXES;
            case "pioche" -> ItemTags.PICKAXES;
            case "pelle" -> ItemTags.SHOVELS;
            default -> ItemTags.HOES;
        };
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, path);
    }
}
