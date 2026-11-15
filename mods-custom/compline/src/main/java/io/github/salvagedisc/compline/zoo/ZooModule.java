package io.github.salvagedisc.compline.zoo;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ModIds;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.AnimalTameEvent;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Module zoo : les quatre tampons du guide d'Archie's Dinosaurs (Apercevoir, Photographier, Apprivoiser ou héberger,
 * Maîtriser) pour toutes les bêtes du monde, sans jamais tuer. Le serveur tient la liste des espèces
 * ({@link ZooSpecies}) et le carnet de chaque joueur ({@link ZooRecord}) ; le client les montre dans le guide
 * d'Archie, sous un marque-page de plus ({@code zoo.client}).
 */
public final class ZooModule {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Compline.MOD_ID);
    public static final Supplier<AttachmentType<ZooRecord>> RECORD = ATTACHMENTS.register("zoo",
            () -> AttachmentType.builder(ZooRecord::new).serialize(ZooRecord.CODEC).copyOnDeath().build());

    private ZooModule() {
    }

    public static void start(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> event.registrar("1")
                .playToClient(ZooSync.TYPE, ZooSync.STREAM_CODEC,
                        (sync, context) -> context.enqueueWork(() -> io.github.salvagedisc.compline.zoo.client.ZooClient.receive(sync))));
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> ZooSpecies.rebuild(event.getServer()));
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, ZooModule::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                Zoo.sendAll(player);
            }
        });
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post.class, event -> Zoo.tick(event.getServer()));
        NeoForge.EVENT_BUS.addListener(AnimalTameEvent.class, event -> {
            if (event.getTamer() instanceof ServerPlayer player) {
                Zoo.grant(player, event.getAnimal(), Stamp.HEBERGER);
            }
        });
        NeoForge.EVENT_BUS.addListener(BabyEntitySpawnEvent.class, event -> {
            if (event.getCausedByPlayer() instanceof ServerPlayer player) {
                Zoo.grant(player, event.getParentA(), Stamp.HEBERGER);
            }
        });
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.EntityInteract.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof Bucketable
                    && event.getTarget().isAlive() && event.getItemStack().is(Items.WATER_BUCKET)) {
                Zoo.grant(player, event.getTarget(), Stamp.HEBERGER);
            }
        });
        if (ModIds.isLoaded(ModIds.EXPOSURE)) {
            ZooPhoto.register();
        }
        NeoForge.EVENT_BUS.addListener(RegisterCommandsEvent.class, ZooModule::onRegisterCommands);
        if (FMLEnvironment.dist.isClient()) {
            io.github.salvagedisc.compline.zoo.client.ZooClient.start(modBus);
        }
        Compline.LOGGER.info("zoo : tampons {}", Arrays.toString(Stamp.values()));
    }

    private static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            ZooSpecies.rebuild(event.getPlayerList().getServer());
            event.getPlayerList().getPlayers().forEach(Zoo::sendAll);
        }
    }

    /**
     * {@code /compline zoo especes} : le nombre d'espèces par milieu. {@code /compline zoo tamponner <joueurs> <espèce>
     * <tampon>} et {@code /compline zoo effacer <joueurs>} (opérateurs) servent aux essais.
     */
    private static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal(Compline.MOD_ID)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("zoo")
                        .then(Commands.literal("especes").executes(ZooModule::countSpecies))
                        .then(Commands.literal("tamponner")
                                .then(Commands.argument("joueurs", EntityArgument.players())
                                        .then(Commands.argument("espece", ResourceLocationArgument.id())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                        ZooSpecies.all().stream().map(SpeciesInfo::id), builder))
                                                .then(Commands.argument("tampon", StringArgumentType.word())
                                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                                Arrays.stream(Stamp.values()).map(Stamp::id), builder))
                                                        .executes(ZooModule::stamp)))))
                        .then(Commands.literal("effacer")
                                .then(Commands.argument("joueurs", EntityArgument.players())
                                        .executes(context -> {
                                            Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "joueurs");
                                            players.forEach(Zoo::clear);
                                            context.getSource().sendSuccess(() -> Component.literal(
                                                    "Carnet du zoo effacé pour " + players.size() + " joueur(s)"), true);
                                            return players.size();
                                        })))));
    }

    private static int countSpecies(CommandContext<CommandSourceStack> context) {
        Map<ZooTab, Integer> counts = new EnumMap<>(ZooTab.class);
        ZooSpecies.all().forEach(info -> counts.merge(info.tab(), 1, Integer::sum));
        StringBuilder text = new StringBuilder(ZooSpecies.all().size() + " espèces :");
        counts.forEach((tab, count) -> text.append(' ').append(tab.id()).append(' ').append(count));
        context.getSource().sendSuccess(() -> Component.literal(text.toString()), false);
        return ZooSpecies.all().size();
    }

    private static int stamp(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(context, "espece");
        SpeciesInfo info = ZooSpecies.get(id);
        Stamp stamp = Stamp.byId(StringArgumentType.getString(context, "tampon"));
        if (info == null || stamp == null) {
            context.getSource().sendFailure(Component.literal("Espèce ou tampon inconnu"));
            return 0;
        }
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "joueurs");
        for (ServerPlayer player : players) {
            Zoo.grant(player, info, stamp);
        }
        context.getSource().sendSuccess(() -> Component.literal("Tampon " + stamp.id() + " de " + id + " donné"), true);
        return players.size();
    }
}
