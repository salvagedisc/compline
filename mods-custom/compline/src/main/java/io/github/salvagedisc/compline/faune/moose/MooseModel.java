package io.github.salvagedisc.compline.faune.moose;

import com.github.alexthe666.alexsmobs.citadel.animation.Animation;
import com.github.alexthe666.alexsmobs.entity.EntityMoose;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.faune.Bones;
import io.github.salvagedisc.compline.faune.Motion;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;

/**
 * Le modèle de l'élan et sa couche maison. L'élan voit : sa tête se tourne en souplesse vers ce que regarde son IA
 * (un joueur proche), partagée entre le cou et la tête. Le corps roule dans les virages, le fanon ballotte, et pendant
 * la joute la tête suit l'angle de poussée d'Alex's Mobs. Les bois sont cachés quand il les a perdus, la neige montrée
 * quand il est enneigé ; le petit a une plus grosse tête et pas de bois.
 */
@SuppressWarnings("deprecation") // GeckoLib 4 garde ces méthodes abstraites tout en les dépréciant
public final class MooseModel extends GeoModel<MooseAnimatable> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "geo/entity/moose.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "textures/entity/moose.png");
    private static final ResourceLocation ANIMATIONS = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID,
            "animations/entity/moose.animation.json");
    private static final String[] SNOW = {"body_snow", "chest_snow", "head_snow"};

    @Override
    public ResourceLocation getModelResource(MooseAnimatable animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(MooseAnimatable animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(MooseAnimatable animatable) {
        return ANIMATIONS;
    }

    static PlayState move(AnimationState<MooseAnimatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof EntityMoose moose)) {
            return PlayState.STOP;
        }
        Motion motion = Motion.of(moose);
        if (moose.isInWater() && !moose.onGround()) {
            state.setControllerSpeed(motion.stride(MooseAnimatable.SWIM_SPEED, 0.7F, 1.5F));
            return state.setAndContinue(MooseAnimatable.SWIM);
        }
        if (moose.isJostling()) {
            state.setControllerSpeed(1);
            return state.setAndContinue(MooseAnimatable.JOSTLE);
        }
        if (motion.speed > 0.16F) {
            state.setControllerSpeed(motion.stride(MooseAnimatable.RUN_GROUND_SPEED, 0.6F, 1.6F));
            return state.setAndContinue(MooseAnimatable.RUN);
        }
        if (motion.speed > 0.008F) {
            state.setControllerSpeed(motion.stride(MooseAnimatable.WALK_GROUND_SPEED, 0.5F, 2.3F));
            return state.setAndContinue(MooseAnimatable.WALK);
        }
        state.setControllerSpeed(1);
        return state.setAndContinue(MooseAnimatable.IDLE);
    }

    /** Brouter et charger suivent l'animation d'Alex's Mobs : mêmes durées (30 et 15 ticks), dégâts au même instant. */
    static PlayState action(AnimationState<MooseAnimatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof EntityMoose moose)) {
            return PlayState.STOP;
        }
        Animation current = moose.getAnimation();
        if (current == EntityMoose.ANIMATION_EAT_GRASS) {
            return state.setAndContinue(MooseAnimatable.EAT);
        }
        if (current == EntityMoose.ANIMATION_ATTACK) {
            return state.setAndContinue(MooseAnimatable.ATTACK);
        }
        return PlayState.STOP;
    }

    @Override
    public void setCustomAnimations(MooseAnimatable animatable, long instanceId, AnimationState<MooseAnimatable> state) {
        if (!(state.getData(DataTickets.ENTITY) instanceof EntityMoose moose)) {
            return;
        }
        float partialTick = state.getPartialTick();
        AnimationController<?> action = animatable.getAnimatableInstanceCache().getManagerForId(instanceId)
                .getAnimationControllers().get("action");
        boolean busy = moose.isJostling() || action != null && action.getAnimationState() != AnimationController.State.STOPPED;
        Motion motion = Motion.of(moose).update(moose, moose.tickCount + partialTick, busy ? 0.15F : 1);
        AnimationProcessor<MooseAnimatable> bones = getAnimationProcessor();
        float w = motion.weight;

        float yaw = Mth.clamp(motion.lookYaw, -50, 50) * Mth.DEG_TO_RAD * w;
        float pitch = Mth.clamp(motion.lookPitch, -25, 25) * Mth.DEG_TO_RAD * w;
        Bones.rotate(bones, "neck", -pitch * 0.4F, yaw * 0.4F, 0);
        Bones.rotate(bones, "head", -pitch * 0.6F, yaw * 0.6F, -yaw * 0.1F);

        float roll = motion.rollSpring.angle;
        Bones.rotate(bones, "body", 0, 0, roll * 0.25F * w);
        Bones.rotate(bones, "bell", -motion.pitchSpring.angle * 1.2F, 0, roll * 1.2F);
        Bones.rotate(bones, "tail", 0, roll * 0.8F, 0);

        if (moose.isJostling()) {
            float jostle = Mth.lerp(partialTick, moose.prevJostleAngle, moose.getJostleAngle()) * Mth.DEG_TO_RAD;
            Bones.rotate(bones, "neck", 0, 0, jostle * 0.4F);
            Bones.rotate(bones, "head", 0, 0, jostle * 0.6F);
        }

        boolean baby = moose.isBaby();
        Bones.hide(bones, "right_antler", baby || !moose.isAntlered());
        Bones.hide(bones, "left_antler", baby || !moose.isAntlered());
        for (String snow : SNOW) {
            Bones.hide(bones, snow, !moose.isSnowy());
        }
        Bones.setScale(bones, "head", baby ? 1.35F : 1);
    }
}
