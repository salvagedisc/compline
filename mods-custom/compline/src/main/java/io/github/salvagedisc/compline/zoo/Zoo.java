package io.github.salvagedisc.compline.zoo;

import io.github.salvagedisc.compline.ComplineConfig;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Le zoo côté serveur : qui a quel tampon, et comment on les gagne.
 *
 * <p>Apercevoir : toutes les demi-secondes, chaque joueur regarde autour de lui ; une bête dans son champ de vision
 * (un cône de 110°), sans obstacle, à moins de {@code spotDistance} blocs, vaut le tampon. La même vue, à moins de
 * 16 blocs, compte comme temps d'observation pour Maîtriser. Les autres tampons arrivent par les événements que
 * branche {@link ZooModule} (apprivoisement, naissance, seau, photo d'Exposure).
 */
public final class Zoo {
    private static final int SCAN_INTERVAL = 10;
    private static final double OBSERVE_DISTANCE = 16.0;
    /** Cosinus du demi-angle du champ de vision retenu (55°). */
    private static final double VIEW_COS = Math.cos(Math.toRadians(55.0));

    private Zoo() {
    }

    public static ZooRecord record(ServerPlayer player) {
        return player.getData(ZooModule.RECORD.get());
    }

    /** Donne un tampon d'une bête précise (elle inscrit son espèce si besoin). */
    public static void grant(ServerPlayer player, Entity entity, Stamp stamp) {
        SpeciesInfo info = ZooSpecies.admit(entity);
        if (info != null) {
            grant(player, info, stamp);
        }
    }

    /** Donne un tampon ; vérifie ensuite Maîtriser. Ne fait rien si le joueur l'a déjà. */
    public static void grant(ServerPlayer player, SpeciesInfo info, Stamp stamp) {
        if (!ComplineConfig.zooEnabled() || player.isSpectator()) {
            return;
        }
        if (stamp == Stamp.HEBERGER && !info.housable()) {
            return;
        }
        ZooRecord record = record(player);
        boolean added = record.add(info.id(), stamp);
        if (stamp != Stamp.APERCEVOIR && record.add(info.id(), Stamp.APERCEVOIR)) {
            added = true;
        }
        if (!added) {
            return;
        }
        if (stamp != Stamp.MAITRISER) {
            announce(player, info, stamp);
        }
        checkMastery(player, info, record);
        send(player, info.id(), record);
    }

    private static void checkMastery(ServerPlayer player, SpeciesInfo info, ZooRecord record) {
        if (record.has(info.id(), Stamp.MAITRISER)) {
            return;
        }
        int required = info.masterRequirement();
        boolean observed = record.observedTicks(info.id()) >= ComplineConfig.zooObserveSeconds() * 20;
        if ((record.stamps(info.id()) & required) == required && observed) {
            record.add(info.id(), Stamp.MAITRISER);
            announce(player, info, Stamp.MAITRISER);
        }
    }

    private static void announce(ServerPlayer player, SpeciesInfo info, Stamp stamp) {
        Component species = speciesName(info.id());
        if (stamp == Stamp.MAITRISER) {
            player.sendSystemMessage(Component.translatable("zoo.compline.message.maitrise", species)
                    .withStyle(ChatFormatting.GOLD));
            player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5F, 1.4F);
        } else {
            player.displayClientMessage(Component.translatable("zoo.compline.message.tampon",
                    Component.translatable(stamp.nameKey()), species), true);
            player.playNotifySound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.7F, 1.0F);
        }
    }

    static Component speciesName(ResourceLocation id) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> (Component) type.getDescription())
                .orElse(Component.literal(id.toString()));
    }

    /** Regarde autour de chaque joueur (appelé à chaque tick du serveur). */
    static void tick(MinecraftServer server) {
        if (!ComplineConfig.zooEnabled() || server.getTickCount() % SCAN_INTERVAL != 0) {
            return;
        }
        int spotDistance = ComplineConfig.zooSpotDistance();
        int observeCap = ComplineConfig.zooObserveSeconds() * 20;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            scan(player, spotDistance, observeCap);
        }
    }

    private static void scan(ServerPlayer player, int spotDistance, int observeCap) {
        ZooRecord record = record(player);
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        double maxSq = (double) spotDistance * spotDistance;
        Set<ResourceLocation> observedNow = new HashSet<>();
        List<SpeciesInfo> changed = new ArrayList<>();
        List<Mob> nearby = player.serverLevel().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(spotDistance), mob -> mob.isAlive() && !mob.isInvisible());
        for (Mob mob : nearby) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
            int mask = record.stamps(id);
            boolean wantsSpot = !Stamp.APERCEVOIR.in(mask);
            boolean wantsObserve = !Stamp.MAITRISER.in(mask) && record.observedTicks(id) < observeCap && !observedNow.contains(id);
            if (!wantsSpot && !wantsObserve) {
                continue;
            }
            double distSq = mob.getEyePosition().distanceToSqr(eye);
            if (distSq > maxSq || !inView(eye, look, mob) || !player.hasLineOfSight(mob)) {
                continue;
            }
            SpeciesInfo info = ZooSpecies.admit(mob);
            if (info == null) {
                continue;
            }
            if (wantsSpot) {
                grant(player, info, Stamp.APERCEVOIR);
            }
            if (wantsObserve && distSq <= OBSERVE_DISTANCE * OBSERVE_DISTANCE) {
                observedNow.add(id);
                int before = record.observedTicks(id) / 20;
                record.observe(id, SCAN_INTERVAL, observeCap);
                if (record.observedTicks(id) >= observeCap) {
                    checkMastery(player, info, record);
                    changed.add(info);
                } else if (record.observedTicks(id) / 20 != before && (record.observedTicks(id) / 20) % 5 == 0) {
                    changed.add(info);
                }
            }
        }
        for (SpeciesInfo info : changed) {
            send(player, info.id(), record);
        }
    }

    private static boolean inView(Vec3 eye, Vec3 look, LivingEntity target) {
        Vec3 to = target.getBoundingBox().getCenter().subtract(eye);
        double length = to.length();
        return length < 2.0 || look.dot(to) / length >= VIEW_COS;
    }

    static void send(ServerPlayer player, ResourceLocation species, ZooRecord record) {
        PacketDistributor.sendToPlayer(player, new ZooSync(false, 0, List.of(), Map.of(species, record.packed(species))));
    }

    static void sendAll(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player,
                new ZooSync(true, ComplineConfig.zooObserveSeconds(), List.copyOf(ZooSpecies.all()), record(player).packedAll()));
    }

    static void broadcastSpecies(MinecraftServer server, SpeciesInfo info) {
        PacketDistributor.sendToAllPlayers(new ZooSync(false, 0, List.of(info), Map.of()));
    }

    /** Efface le carnet d'un joueur (commande). */
    static void clear(ServerPlayer player) {
        record(player).clear();
        sendAll(player);
    }
}
