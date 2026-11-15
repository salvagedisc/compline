package io.github.salvagedisc.compline.faune.moose;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import software.bernie.geckolib.animatable.GeoReplacedEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * L'élan de Compline, tel que GeckoLib l'anime. Deux contrôleurs : {@code move} (repos, marche, trot, nage, joute,
 * en boucle) et {@code action} (brouter, charger), qui suit l'animation en cours d'Alex's Mobs et recouvre le premier.
 */
public final class MooseAnimatable implements GeoReplacedEntity {
    static final String PREFIX = "animation.compline.moose.";
    static final RawAnimation IDLE = RawAnimation.begin().thenLoop(PREFIX + "idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop(PREFIX + "walk");
    static final RawAnimation RUN = RawAnimation.begin().thenLoop(PREFIX + "run");
    static final RawAnimation SWIM = RawAnimation.begin().thenLoop(PREFIX + "swim");
    static final RawAnimation JOSTLE = RawAnimation.begin().thenLoop(PREFIX + "jostle");
    static final RawAnimation EAT = RawAnimation.begin().thenPlay(PREFIX + "eat");
    static final RawAnimation ATTACK = RawAnimation.begin().thenPlay(PREFIX + "attack");
    /** Vitesse au sol que montrent la marche, le trot et la nage, en blocs par tick. */
    static final float WALK_GROUND_SPEED = 0.07F;
    static final float RUN_GROUND_SPEED = 0.19F;
    static final float SWIM_SPEED = 0.09F;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public EntityType<?> getReplacingEntityType() {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath("alexsmobs", "moose"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "move", 5, MooseModel::move));
        controllers.add(new AnimationController<>(this, "action", 4, MooseModel::action));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
