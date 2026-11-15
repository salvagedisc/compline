package io.github.salvagedisc.compline.faune.warden;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.salvagedisc.compline.faune.Motion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.monster.warden.Warden;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.renderer.GeoReplacedEntityRenderer;

/**
 * Le rendu du Warden de Compline. Avant chaque image, il lit les six {@link AnimationState} publics du Warden,
 * comme le modèle du jeu, et déclenche le geste correspondant au moment où l'un d'eux redémarre. Les durées des
 * gestes sont celles du jeu : l'onde sonique part quand le serveur la lance.
 */
public final class WardenRenderer extends GeoReplacedEntityRenderer<Warden, WardenAnimatable> {
    /** Un geste vu plus tard que ça après son départ (le Warden entrait dans le champ) n'est pas joué. */
    private static final long LATE_MS = 400;

    public WardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(), new WardenAnimatable());
        addRenderLayer(new WardenGlowLayer(this));
        this.shadowRadius = 0.9F;
    }

    @Override
    public void render(Warden warden, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource,
            int packedLight) {
        Motion motion = Motion.of(warden);
        float age = warden.tickCount + partialTick;
        AnimationState[] states = {warden.emergeAnimationState, warden.diggingAnimationState, warden.roarAnimationState,
                warden.sniffAnimationState, warden.attackAnimationState, warden.sonicBoomAnimationState};
        for (int i = 0; i < states.length; i++) {
            AnimationState state = states[i];
            state.updateTime(age, 1);
            if (!state.isStarted()) {
                motion.marks[i] = Long.MAX_VALUE;
                continue;
            }
            long elapsed = state.getAccumulatedTime();
            if (elapsed < motion.marks[i] && elapsed < LATE_MS) {
                animatable.triggerAnim(warden, "action", WardenAnimatable.ACTIONS[i]);
            }
            motion.marks[i] = elapsed;
        }
        float tendrils = warden.getTendrilAnimation(partialTick);
        if (tendrils > 0.8F && motion.lastSignal <= 0.8F && !isActing(animatable, warden)) {
            animatable.triggerAnim(warden, "listen", "listen");
        }
        motion.lastSignal = tendrils;
        super.render(warden, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    /** Vrai si un geste du jeu est en cours : la couche maison s'efface alors en partie. */
    static boolean isActing(WardenAnimatable animatable, Warden warden) {
        AnimationController<?> action = animatable.getAnimatableInstanceCache().getManagerForId(warden.getId())
                .getAnimationControllers().get("action");
        return action != null && action.isPlayingTriggeredAnimation();
    }
}
