package io.github.salvagedisc.compline.faune.warden;

import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Le Warden de Compline, tel que GeckoLib l'anime. Trois contrôleurs, du plus faible au plus fort :
 * {@code move} (repos, marche, poursuite, en boucle), {@code listen} (le sursaut quand il entend) et {@code action}
 * (les gestes du jeu : sortir de terre, s'enfouir, flairer, rugir, frapper, onde sonique). Un contrôleur plus fort
 * recouvre les os qu'il anime ; les gestes sont déclenchés par {@link WardenRenderer} d'après l'état du Warden.
 */
public final class WardenAnimatable implements GeoReplacedEntity {
    static final String PREFIX = "animation.compline.warden.";
    static final RawAnimation IDLE = RawAnimation.begin().thenLoop(PREFIX + "idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop(PREFIX + "walk");
    static final RawAnimation RUN = RawAnimation.begin().thenLoop(PREFIX + "run");
    /** Vitesse au sol que montrent la marche et la course, en blocs par tick (amplitude des cuisses × longueur de jambe). */
    static final float WALK_GROUND_SPEED = 0.052F;
    static final float RUN_GROUND_SPEED = 0.141F;
    /** Les gestes, dans l'ordre des {@code AnimationState} lus par {@link WardenRenderer}. */
    static final String[] ACTIONS = {"emerge", "dig", "roar", "sniff", "attack", "sonic_boom"};

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public EntityType<?> getReplacingEntityType() {
        return EntityType.WARDEN;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 5, WardenModel::move));
        controllers.add(new AnimationController<>(this, "listen", 2, state -> PlayState.STOP)
                .triggerableAnim("listen", RawAnimation.begin().thenPlay(PREFIX + "listen")));
        AnimationController<WardenAnimatable> action = new AnimationController<>(this, "action", 5, state -> PlayState.STOP);
        for (String name : ACTIONS) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay(PREFIX + name));
        }
        controllers.add(action);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
