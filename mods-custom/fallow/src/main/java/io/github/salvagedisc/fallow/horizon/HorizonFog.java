package io.github.salvagedisc.fallow.horizon;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.enums.rendering.EDhApiFogFalloff;
import com.seibel.distanthorizons.api.interfaces.config.IDhApiConfig;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeFogRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiCancelableEventParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiMutableFogRenderParam;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiRenderParam;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

/**
 * La brume lointaine de Distant Horizons suit celle du terrain, et se referme toujours en cercle.
 *
 * <p>Distant Horizons dessine les LOD à part, puis leur applique son propre brouillard, mesuré en fraction du
 * rayon LOD ; il ne connaît pas ATMOSPHERICS. On lui donne donc, à chaque image, la brume du terrain
 * ({@link TerrainFog}) convertie dans son unité, en courbe linéaire d'épaisseur 0 à 1 : un LOD à la distance
 * {@code d} est aussi noyé qu'un bloc du terrain à la même distance.
 *
 * <ul>
 *   <li>Brume plus courte que la distance de rendu : tout LOD est au-delà de la fin de brume, donc invisible.
 *       On annule alors tout le rendu LOD de l'image (aucun coût GPU), et le fondu de Distant Horizons entre
 *       terrain et LOD (mixin {@code horizon.VanillaFadeMixin}), qui sinon mêlerait au bord du terrain la
 *       dernière image de LOD dessinée : des reliefs fantômes dans la brume.</li>
 *   <li>Brume levée (fin au-delà de la distance de rendu) : les LOD apparaissent progressivement, sans couture,
 *       jusqu'au cercle de brume.</li>
 *   <li>Le cercle : un LOD noyé dans la brume garde la couleur de la brume, et se découpe en silhouette sur le
 *       ciel, plus clair ; sans plus, on verrait les montagnes jusqu'au bout du rayon LOD. Le fondu lointain de
 *       Distant Horizons (mixin {@code horizon.FarFadeMixin}) les efface donc dans le ciel, de 55 % à 100 % de
 *       la fin de la brume, plafonnée à {@code horizon.ringMaxDistance} blocs.</li>
 *   <li>Sous l'eau, dans la lave, la neige poudreuse, aveuglé ou dans l'obscurité : on laisse Distant Horizons,
 *       qui noie déjà tout.</li>
 * </ul>
 *
 * Sans ce module (coupé ou en erreur), la config livrée garde une épaisseur minimale de 1 : LOD cachés.
 */
public final class HorizonFog {
    /** Le fondu lointain de Distant Horizons va de 50 % à 90 % de la distance qu'on lui rend. */
    private static final float FAR_FADE_END = 0.9f;

    private boolean errorLogged;

    DhApiBeforeRenderEvent beforeRender() {
        return new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
                try {
                    if (skipLods()) {
                        event.cancelEvent();
                    }
                } catch (RuntimeException e) {
                    logOnce(e);
                }
            }
        };
    }

    DhApiBeforeFogRenderEvent beforeFog() {
        return new DhApiBeforeFogRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<EventParam> event) {
                try {
                    mirror(event.value.getFogRenderParam());
                } catch (RuntimeException e) {
                    logOnce(e);
                }
            }
        };
    }

    /**
     * Vrai si les LOD de cette image ne sont pas dessinés : la brume du terrain finit avant le bord du terrain
     * normal, aucun LOD ne peut se voir. Lu aussi par {@code horizon.VanillaFadeMixin}.
     */
    public static boolean skipLods() {
        if (!FallowConfig.horizonSkipHiddenLods() || !active()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float vanillaEdge = minecraft.options.getEffectiveRenderDistance() * 16f;
        return TerrainFog.end() < vanillaEdge;
    }

    /**
     * La distance que le fondu lointain de Distant Horizons doit prendre pour effacer les LOD dans le ciel au
     * cercle de brume, au lieu du bout du rayon LOD. Lu par {@code horizon.FarFadeMixin}.
     */
    public static float farFadeDistance(float farClip) {
        if (!active()) {
            return farClip;
        }
        return Math.min(farClip, ringEnd() / FAR_FADE_END);
    }

    /** Fin de la brume lointaine : celle du terrain, plafonnée pour que le cercle se referme toujours. */
    private static float ringEnd() {
        return Math.min(TerrainFog.end(), FallowConfig.horizonRingMaxDistance());
    }

    private static boolean active() {
        return FallowConfig.horizonEnabled() && TerrainFog.captured() && !specialFog();
    }

    private void mirror(DhApiMutableFogRenderParam fog) {
        if (!active()) {
            return;
        }
        IDhApiConfig configs = DhApi.Delayed.configs;
        if (configs == null) {
            return;
        }
        float radius = configs.graphics().chunkRenderDistance().getValue() * 16f;
        float start = Math.min(TerrainFog.start(), FallowConfig.horizonRingMaxDistance() * 0.5f);
        float end = ringEnd();
        if (radius <= 0f || !(end > start)) {
            return;
        }
        fog.setFarFogFalloff(EDhApiFogFalloff.LINEAR);
        fog.setFarFogStartPercent(start / radius);
        fog.setFarFogEndPercent(end / radius);
        fog.setFarFogMinThickness(0f);
        fog.setFarFogMaxThickness(1f);
    }

    /** Les cas où Distant Horizons noie déjà ses LOD lui-même. */
    private static boolean specialFog() {
        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        if (camera.getFluidInCamera() != FogType.NONE) {
            return true;
        }
        return camera.getEntity() instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
    }

    private void logOnce(RuntimeException e) {
        if (!errorLogged) {
            errorLogged = true;
            Fallow.LOGGER.error("horizon : erreur en reliant la brume lointaine (affichée une seule fois)", e);
        }
    }
}
