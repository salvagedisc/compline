package io.github.salvagedisc.compline.repos;

import io.github.salvagedisc.compline.Compline;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module repos : se poser au foyer repose (docs/plan/repos.md). Immobile ou assis près d'un feu allumé, sous un
 * toit, le joueur gagne l'effet « Reposé » ({@link ReposeEffect}) : il guérit plus vite et a moins faim. L'effet dure
 * d'autant plus que la maison est variée ({@link Confort}).
 */
public final class ReposModule {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Compline.MOD_ID);
    public static final DeferredHolder<MobEffect, ReposeEffect> REPOSE = EFFECTS.register("repose", ReposeEffect::new);
    /** Les feux qui font un foyer, quand ils sont allumés (propriété {@code lit}) ou s'ils n'en ont pas. */
    public static final TagKey<Block> FOYERS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "foyers"));

    private ReposModule() {
    }

    public static void start(IEventBus modBus) {
        EFFECTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> Repos.tick(event.getServer()));
        Compline.LOGGER.info("repos : l'effet « Reposé » au foyer");
    }
}
