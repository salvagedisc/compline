package io.github.salvagedisc.compline.patine;

import io.github.salvagedisc.compline.Compline;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module patine : les armures se salissent quand on les porte (marche, terre, combat) et se lavent dans l'eau. La
 * saleté est un composant de l'objet ({@link #PATINE}, 0 à 100) : elle suit l'armure d'un joueur à l'autre, d'un
 * coffre à un porte-armure. Le serveur la fait varier ({@link Patine}) ; le client salit la texture de l'armure
 * portée ({@code patine.client}).
 */
public final class PatineModule {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Compline.MOD_ID);
    public static final Supplier<DataComponentType<Integer>> PATINE = COMPONENTS.registerComponentType("patine",
            builder -> builder.persistent(ExtraCodecs.intRange(0, Patine.MAX)).networkSynchronized(ByteBufCodecs.VAR_INT));

    private PatineModule() {
    }

    public static void start(IEventBus modBus) {
        COMPONENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, event -> {
            Player player = event.getEntity();
            if (!player.level().isClientSide() && player.tickCount % 20 == 0) {
                Patine.tick(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedOutEvent.class, event -> Patine.forget(event.getEntity()));
        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
            if (!event.getLevel().isClientSide() && !event.loadedFromDisk() && event.getEntity() instanceof Mob mob) {
                Patine.dirtyMob(mob);
            }
        });
        if (FMLEnvironment.dist.isClient()) {
            io.github.salvagedisc.compline.patine.client.PatineClient.start(modBus);
        }
        Compline.LOGGER.info("patine : les armures se salissent et se lavent dans l'eau");
    }
}
