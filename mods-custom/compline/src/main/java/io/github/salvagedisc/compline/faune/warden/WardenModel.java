package io.github.salvagedisc.compline.faune.warden;

import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.faune.Bones;
import io.github.salvagedisc.compline.faune.Motion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.warden.AngerLevel;
import net.minecraft.world.entity.monster.warden.Warden;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

/**
 * Le modèle du Warden et sa couche maison. Il est aveugle : pas de regard fixé sur le joueur. Sa tête suit
 * mollement l'endroit vers lequel l'IA le tourne (une vibration, un flairage), avec du roulis ; son buste se penche
 * dans les virages et ses antennes ballottent. Son cœur bat au rythme du jeu (de 30 à 120 battements par minute selon
 * sa colère) dans la lueur, voir {@link WardenGlowLayer}.
 */
@SuppressWarnings("deprecation") // GeckoLib 4 garde ces méthodes abstraites tout en les dépréciant
public final class WardenModel extends GeoModel<WardenAnimatable> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "geo/entity/warden.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "textures/entity/warden.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "animations/entity/warden.animation.json");

    @Override
    public ResourceLocation getModelResource(WardenAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WardenAnimatable animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WardenAnimatable animatable) {
        return ANIMATIONS;
    }

    /** Repos, marche ou poursuite. La poursuite suit la colère, pas seulement la vitesse ; le pas suit la vitesse réelle. */
    static PlayState move(AnimationState<WardenAnimatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof Warden warden)) {
            return PlayState.STOP;
        }
        Motion motion = Motion.of(warden);
        if (motion.speed < 0.008F || warden.hasPose(Pose.EMERGING) || warden.hasPose(Pose.DIGGING)) {
            state.setControllerSpeed(1);
            return state.setAndContinue(WardenAnimatable.IDLE);
        }
        boolean chasing = warden.getClientAngerLevel() >= AngerLevel.ANGRY.getMinimumAnger()
                || motion.speed > 0.11F;
        if (chasing) {
            state.setControllerSpeed(motion.stride(WardenAnimatable.RUN_GROUND_SPEED, 0.6F, 2.0F));
            return state.setAndContinue(WardenAnimatable.RUN);
        }
        state.setControllerSpeed(motion.stride(WardenAnimatable.WALK_GROUND_SPEED, 0.6F, 1.8F));
        return state.setAndContinue(WardenAnimatable.WALK);
    }

    @Override
    public void setCustomAnimations(WardenAnimatable animatable, long instanceId, AnimationState<WardenAnimatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof Warden warden)) {
            return;
        }
        boolean busy = WardenRenderer.isActing(animatable, warden);
        boolean underground = warden.hasPose(Pose.EMERGING) || warden.hasPose(Pose.DIGGING);
        Motion motion = Motion.of(warden).update(warden, warden.tickCount + state.getPartialTick(), underground ? 0 : busy ? 0.35F : 1);
        AnimationProcessor<WardenAnimatable> bones = getAnimationProcessor();
        float w = motion.weight;

        // Tête aveugle : elle suit mollement, avec un roulis, au lieu de fixer. (Seuls la tête, le buste et les antennes
        // sont retouchés : ce sont les os que le repos, la marche et la poursuite font tous tourner, voir Bones.)
        float yaw = Mth.clamp(motion.lookYaw, -60, 60) * Mth.DEG_TO_RAD * w;
        float pitch = Mth.clamp(motion.lookPitch, -30, 30) * Mth.DEG_TO_RAD * w;
        Bones.rotate(bones, "head", -pitch, yaw, -yaw * 0.25F);

        // Le buste se penche dans les virages ; un à-coup en avant quand il démarre.
        float roll = motion.rollSpring.angle * w;
        Bones.rotate(bones, "chest", motion.pitchSpring.angle * 0.35F * w, 0, roll * 0.6F);

        // Les antennes ballottent : elles traînent derrière l'accélération et sortent du virage.
        float sway = motion.rollSpring.angle;
        float lag = -motion.pitchSpring.angle;
        Bones.rotate(bones, "right_tendril", lag * 0.8F, 0, sway * 0.9F);
        Bones.rotate(bones, "left_tendril", lag * 0.8F, 0, sway * 0.9F);
        Bones.rotate(bones, "right_tendril_tip", lag * 0.6F, 0, sway * 0.7F);
        Bones.rotate(bones, "left_tendril_tip", lag * 0.6F, 0, sway * 0.7F);
    }
}
