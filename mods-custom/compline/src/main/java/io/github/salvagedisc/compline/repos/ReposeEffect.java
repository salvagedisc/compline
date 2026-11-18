package io.github.salvagedisc.compline.repos;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * « Reposé » : un demi-cœur toutes les 6 secondes tant que la faim n'est pas trop basse (la régénération du jeu en
 * demande bien plus), et un quart de faim en moins ({@link io.github.salvagedisc.compline.mixin.repos.PlayerExhaustionMixin}).
 */
public final class ReposeEffect extends MobEffect {
    /** Couleur des particules : un orange de braise. */
    private static final int COLOR = 0xE8A25A;
    static final int HEAL_INTERVAL = 120;
    /** Fraction de la faim retirée pendant l'effet. */
    public static final float HUNGER_FACTOR = 0.75F;

    ReposeEffect() {
        super(MobEffectCategory.BENEFICIAL, COLOR);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % HEAL_INTERVAL == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() < entity.getMaxHealth()
                && !(entity instanceof Player player && player.getFoodData().getFoodLevel() < 6)) {
            entity.heal(1.0F);
        }
        return true;
    }
}
