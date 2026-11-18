package io.github.salvagedisc.compline.repos;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.commun.Foyers;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.Vec3;

/**
 * La règle du repos, côté serveur, toutes les 2 secondes pour chaque joueur : près d'un feu allumé (5 blocs),
 * sous un toit, immobile ou assis. Après {@code restSeconds} de repos, il reçoit « Reposé » pour
 * {@code baseMinutes} + {@code minutesPerComfort} par famille de confort trouvée à 8 blocs ; tant qu'il reste au
 * repos, l'effet reste plein.
 */
public final class Repos {
    private static final int INTERVAL = 40;
    private static final int FIRE_RADIUS = 5;
    private static final int COMFORT_RADIUS = 8;
    private static final int COMFORT_HEIGHT = 4;
    /** Déplacement en blocs, entre deux contrôles, au-delà duquel le joueur ne se repose pas. */
    private static final double STILL = 0.6;
    /** Le confort compté reste valable 15 secondes tant que le joueur n'a pas bougé de plus de 2 blocs. */
    private static final int COMFORT_TTL = 300;

    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        Vec3 last;
        int restTicks;
        boolean announced;
        BlockPos comfortAt;
        int comfortTick;
        int comfort;
    }

    private Repos() {
    }

    static void tick(MinecraftServer server) {
        if (!ComplineConfig.reposEnabled() || server.getTickCount() % INTERVAL != 0) {
            return;
        }
        // Les joueurs partis n'ont plus d'état.
        STATES.keySet().removeIf(id -> server.getPlayerList().getPlayer(id) == null);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || player.isSpectator()) {
                STATES.remove(player.getUUID());
                continue;
            }
            State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
            Vec3 here = player.position();
            boolean still = player.isPassenger() || (state.last != null && state.last.distanceTo(here) < STILL);
            state.last = here;
            ServerLevel level = player.serverLevel();
            if (!still || !sheltered(level, player) || !nearFire(level, player.blockPosition())) {
                state.restTicks = 0;
                state.announced = false;
                continue;
            }
            state.restTicks += INTERVAL;
            if (state.restTicks < ComplineConfig.reposRestSeconds() * 20) {
                continue;
            }
            int comfort = comfort(state, level, player.blockPosition(), server.getTickCount());
            int duration = (int) ((ComplineConfig.reposBaseMinutes() + ComplineConfig.reposMinutesPerComfort() * comfort) * 60 * 20);
            MobEffectInstance current = player.getEffect(ReposModule.REPOSE);
            if (current == null || current.getDuration() < duration - INTERVAL * 2) {
                player.addEffect(new MobEffectInstance(ReposModule.REPOSE, duration, 0, true, true, true));
            }
            if (!state.announced) {
                state.announced = true;
                player.displayClientMessage(Component.translatable(comfort >= 5 ? "repos.compline.bien_repose" : "repos.compline.repose"), true);
            }
        }
    }

    /** Un toit au-dessus de la tête : le ciel n'est pas visible. */
    static boolean sheltered(ServerLevel level, ServerPlayer player) {
        return !level.canSeeSky(BlockPos.containing(player.getEyePosition()).above());
    }

    static boolean nearFire(ServerLevel level, BlockPos center) {
        return Foyers.near(level, center, FIRE_RADIUS, 2);
    }

    /** Les familles de confort autour du joueur, recomptées toutes les 15 secondes ou quand il a bougé. */
    private static int comfort(State state, ServerLevel level, BlockPos pos, int tick) {
        if (state.comfortAt == null || tick - state.comfortTick >= COMFORT_TTL || state.comfortAt.distManhattan(pos) > 2) {
            state.comfort = Confort.count(level, pos, COMFORT_RADIUS, COMFORT_HEIGHT);
            state.comfortAt = pos.immutable();
            state.comfortTick = tick;
        }
        return state.comfort;
    }
}
