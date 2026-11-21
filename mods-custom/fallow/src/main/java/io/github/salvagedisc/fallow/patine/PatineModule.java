package io.github.salvagedisc.fallow.patine;

import io.github.salvagedisc.fallow.Fallow;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
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
 * Module patine : les armures se salissent quand on les porte (marche, terre, combat) et se lavent dans l'eau ; elles
 * prennent aussi le sable, la neige et l'eau ({@link #COAT}). La
 * saleté est un composant de l'objet ({@link #PATINE}, 0 à 100) : elle suit l'armure d'un joueur à l'autre, d'un
 * coffre à un porte-armure. Le serveur la fait varier ({@link Patine}) ; le client salit la texture de l'armure
 * portée ({@code patine.client}).
 */
public final class PatineModule {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Fallow.MOD_ID);
    public static final Supplier<DataComponentType<Integer>> PATINE = COMPONENTS.registerComponentType("patine",
            builder -> builder.persistent(ExtraCodecs.intRange(0, Patine.MAX)).networkSynchronized(ByteBufCodecs.VAR_INT));
    /** Le sable, la neige et l'eau pris par l'armure ({@link Coat}). */
    public static final Supplier<DataComponentType<Coat>> COAT = COMPONENTS.registerComponentType("coat",
            builder -> builder.persistent(Coat.CODEC).networkSynchronized(Coat.STREAM_CODEC));

    /** Les créatures qui ne se salissent pas à leur apparition (les Hôtes, nés de la brume). */
    public static final TagKey<EntityType<?>> PROPRES =
            TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "patine/propres"));

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
            if (!event.getLevel().isClientSide() && !event.loadedFromDisk() && event.getEntity() instanceof Mob mob
                    && !mob.getType().is(PROPRES)) {
                Patine.dirtyMob(mob);
            }
        });
        if (FMLEnvironment.dist.isClient()) {
            io.github.salvagedisc.fallow.patine.client.PatineClient.start(modBus);
        }
        Fallow.LOGGER.info("patine : les armures se salissent, prennent le sable, la neige et l'eau");
    }
}
