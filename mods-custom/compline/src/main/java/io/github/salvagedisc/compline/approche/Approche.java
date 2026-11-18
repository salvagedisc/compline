package io.github.salvagedisc.compline.approche;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.vent.WindField;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * La règle de l'approche, côté serveur. Toutes les demi-secondes, chaque bête farouche proche d'un joueur se
 * demande si elle le perçoit, de trois façons :
 * <ul>
 * <li><b>l'odeur</b> : le vent souffle du joueur vers la bête (dans un cône de 70° de part et d'autre de sa
 *     direction) ; la portée croît avec la force du vent et baisse sous la pluie, et le vent contraire n'en porte aucune ;</li>
 * <li><b>la vue</b> : la bête voit le joueur sans obstacle ; un joueur accroupi se voit deux fois moins loin,
 *     un joueur qui court plus loin, et la nuit tout se voit moins ;</li>
 * <li><b>l'ouïe</b> : un joueur qui court s'entend de loin, un joueur accroupi presque pas.</li>
 * </ul>
 * Une bête qui perçoit le joueur de loin lève d'abord la tête et le fixe un instant ; s'il continue d'avancer,
 * ou s'il est déjà près, elle fuit, et ses congénères proches fuient avec elle.
 */
public final class Approche {
    static final int INTERVAL = 10;
    /** Durée d'une fuite, en ticks : la bête reste sur ses gardes après avoir perdu le joueur de vue. */
    static final int FLEE_TICKS = 140;
    /** Durée de l'alerte (tête levée) avant la fuite. */
    static final int ALERT_TICKS = 30;
    /** Délai entre deux indices donnés au même joueur. */
    private static final int HINT_COOLDOWN = 20 * 90;
    /** Cosinus de l'angle du cône d'odeur (70°). */
    private static final double SCENT_COS = Math.cos(Math.toRadians(70.0));

    /** État des bêtes inquiètes (serveur seulement ; une bête déchargée sort d'elle-même de la table). */
    private static final Map<PathfinderMob, Alarm> ALARMS = new WeakHashMap<>();
    private static final Map<UUID, Long> HINTS = new WeakHashMap<>();

    enum Sense {
        ODEUR, VUE, OUIE
    }

    /** Une bête inquiète : de qui, depuis quand, et si elle fuit déjà. */
    static final class Alarm {
        Vec3 from;
        int alertTicks;
        int fleeTicks;

        boolean fleeing() {
            return fleeTicks > 0;
        }
    }

    private Approche() {
    }

    static Alarm alarm(PathfinderMob mob) {
        return ALARMS.get(mob);
    }

    static void tick(MinecraftServer server) {
        if (!ComplineConfig.approcheEnabled()) {
            ALARMS.clear();
            return;
        }
        if (server.getTickCount() % INTERVAL != 0) {
            return;
        }
        ALARMS.values().removeIf(alarm -> {
            alarm.fleeTicks = Math.max(0, alarm.fleeTicks - INTERVAL);
            alarm.alertTicks = Math.max(0, alarm.alertTicks - INTERVAL);
            return alarm.fleeTicks == 0 && alarm.alertTicks == 0;
        });
        double reach = maxReach();
        for (ServerLevel level : server.getAllLevels()) {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                continue;
            }
            for (ServerPlayer player : players) {
                if (player.isSpectator() || player.isCreative() || !player.isAlive() || player.isInvisible()) {
                    continue;
                }
                AABB box = player.getBoundingBox().inflate(reach, 12.0, reach);
                for (PathfinderMob mob : level.getEntitiesOfClass(PathfinderMob.class, box, Approche::wild)) {
                    Sense sense = perceives(level, mob, player);
                    if (sense != null) {
                        startle(level, mob, player, sense);
                    }
                }
            }
        }
    }

    /** Une bête farouche, libre, que personne ne tient. */
    static boolean wild(PathfinderMob mob) {
        if (!mob.isAlive() || !mob.getType().is(ApprocheModule.FAROUCHES) || mob.isLeashed() || mob.isVehicle()
                || mob.isPassenger() || mob.isNoAi()) {
            return false;
        }
        if (mob instanceof OwnableEntity owned && owned.getOwnerUUID() != null) {
            return false;
        }
        return !(mob instanceof AbstractHorse horse && horse.isTamed());
    }

    static double maxReach() {
        return Math.max(ComplineConfig.approcheScentRange() * 1.3,
                Math.max(ComplineConfig.approcheSightRange() * 1.3, ComplineConfig.approcheHearingRange() * 2.0));
    }

    /** Le sens par lequel la bête perçoit le joueur, ou {@code null}. */
    static Sense perceives(ServerLevel level, PathfinderMob mob, ServerPlayer player) {
        Vec3 to = mob.position().subtract(player.position());
        double flat = Math.sqrt(to.x * to.x + to.z * to.z);
        double distance = to.length();
        Alarm alarm = ALARMS.get(mob);
        // Une bête déjà inquiète perçoit le joueur de plus loin.
        double wary = alarm != null ? 1.25 : 1.0;

        // L'ouïe.
        double hearing = ComplineConfig.approcheHearingRange() * (player.isCrouching() ? 0.3 : player.isSprinting() ? 2.0 : 1.0);
        if (distance < hearing * wary) {
            return Sense.OUIE;
        }

        // L'odeur, portée par le vent de fond du pack.
        if (flat > 0.5) {
            float rain = level.getRainLevel(1.0F);
            float thunder = level.getThunderLevel(1.0F);
            double angle = WindField.angle(level.getGameTime(), thunder);
            double along = (to.x * Math.cos(angle) + to.z * Math.sin(angle)) / flat;
            if (along > SCENT_COS) {
                float strength = WindField.strength(level.getGameTime(), rain, thunder, player.getY());
                double carry = Math.min(1.3, 0.6 + 0.6 * strength) * (1.0 - 0.3 * rain);
                double cone = (along - SCENT_COS) / (1.0 - SCENT_COS);
                double scent = ComplineConfig.approcheScentRange() * carry * (0.35 + 0.65 * cone);
                if (distance < scent * wary) {
                    return Sense.ODEUR;
                }
            }
        }

        // La vue.
        double sight = ComplineConfig.approcheSightRange();
        if (player.isCrouching()) {
            sight *= 0.45;
        } else if (player.isSprinting()) {
            sight *= 1.3;
        }
        int light = level.getBrightness(LightLayer.SKY, player.blockPosition()) - level.getSkyDarken();
        light = Math.max(light, level.getBrightness(LightLayer.BLOCK, player.blockPosition()));
        if (light < 7) {
            sight *= 0.6;
        }
        if (distance < sight * wary && mob.hasLineOfSight(player)) {
            return Sense.VUE;
        }
        return null;
    }

    private static void startle(ServerLevel level, PathfinderMob mob, ServerPlayer player, Sense sense) {
        Alarm alarm = ALARMS.computeIfAbsent(mob, m -> new Alarm());
        alarm.from = player.position();
        if (alarm.fleeing()) {
            alarm.fleeTicks = FLEE_TICKS;
            return;
        }
        boolean close = mob.distanceTo(player) < ComplineConfig.approcheHearingRange() * 1.5;
        if (alarm.alertTicks == 0 && !close) {
            // Elle lève la tête : le joueur a un instant pour s'arrêter ou s'accroupir.
            alarm.alertTicks = ALERT_TICKS + INTERVAL;
            return;
        }
        if (alarm.alertTicks > INTERVAL && !close) {
            return;
        }
        flee(mob, alarm, player.position());
        // Le troupeau suit : les congénères proches fuient aussi.
        for (PathfinderMob other : level.getEntitiesOfClass(PathfinderMob.class, mob.getBoundingBox().inflate(12.0),
                o -> o != mob && o.getType() == mob.getType() && wild(o))) {
            Alarm herd = ALARMS.computeIfAbsent(other, m -> new Alarm());
            flee(other, herd, player.position());
        }
        hint(player, sense);
    }

    private static void flee(PathfinderMob mob, Alarm alarm, Vec3 from) {
        alarm.from = from;
        alarm.alertTicks = 0;
        alarm.fleeTicks = FLEE_TICKS;
    }

    /** Un mot au joueur, de temps en temps, pour qu'il comprenne ce qui l'a trahi. */
    private static void hint(ServerPlayer player, Sense sense) {
        if (!ComplineConfig.approcheHints()) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        Long last = HINTS.get(player.getUUID());
        if (last != null && now - last < HINT_COOLDOWN) {
            return;
        }
        HINTS.put(player.getUUID(), now);
        player.displayClientMessage(Component.translatable("approche.compline.indice." + sense.name().toLowerCase()), true);
    }
}
