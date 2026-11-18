package io.github.salvagedisc.compline.approche;

import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/**
 * Le comportement d'une bête farouche inquiète ({@link Approche}) : tête levée vers le joueur pendant l'alerte,
 * puis course loin de lui tant que la fuite dure. Ajouté au-dessus de ses autres buts, il les suspend le temps
 * de l'inquiétude ; la panique des coups (vanilla) garde sa place.
 */
final class FuiteGoal extends Goal {
    private static final double SPEED = 1.5;
    private final PathfinderMob mob;
    private int repath;

    FuiteGoal(PathfinderMob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        Approche.Alarm alarm = Approche.alarm(mob);
        return alarm != null && alarm.from != null && (alarm.fleeing() || alarm.alertTicks > 0);
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        repath = 0;
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Approche.Alarm alarm = Approche.alarm(mob);
        if (alarm == null || alarm.from == null) {
            return;
        }
        if (!alarm.fleeing()) {
            mob.getNavigation().stop();
            mob.getLookControl().setLookAt(alarm.from.x, alarm.from.y + 1.6, alarm.from.z);
            return;
        }
        if (--repath > 0 && !mob.getNavigation().isDone()) {
            return;
        }
        repath = 20;
        Vec3 target = DefaultRandomPos.getPosAway(mob, 20, 7, alarm.from);
        if (target != null) {
            mob.getNavigation().moveTo(target.x, target.y, target.z, SPEED);
        }
    }
}
