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
 * La brume lointaine de Distant Horizons suit celle du terrain.
 *
 * <p>Distant Horizons dessine les LOD à part, puis leur applique son propre brouillard, mesuré en fraction du
 * rayon LOD ; il ne connaît pas ATMOSPHERICS. On lui donne donc, à chaque image, la brume du terrain
 * ({@link TerrainFog}) convertie dans son unité, en courbe linéaire d'épaisseur 0 à 1 : un LOD à la distance
 * {@code d} est aussi noyé qu'un bloc du terrain à la même distance.
 *
 * <ul>
 *   <li>Brume plus courte que la distance de rendu : tout LOD est au-delà de la fin de brume, donc invisible.
 *       On annule alors tout le rendu LOD de l'image (aucun coût GPU).</li>
 *   <li>Brume levée (fin au-delà de la distance de rendu) : les LOD apparaissent progressivement, sans couture.</li>
 *   <li>Sous l'eau, dans la lave, la neige poudreuse, aveuglé ou dans l'obscurité : on laisse Distant Horizons,
 *       qui noie déjà tout.</li>
 * </ul>
 *
 * Sans ce module (coupé ou en erreur), la config livrée garde une épaisseur minimale de 1 : LOD cachés.
 */
final class HorizonFog {
    private boolean errorLogged;

    DhApiBeforeRenderEvent beforeRender() {
        return new DhApiBeforeRenderEvent() {
            @Override
            public void beforeRender(DhApiCancelableEventParam<DhApiRenderParam> event) {
                try {
                    if (FallowConfig.horizonSkipHiddenLods() && lodsHidden()) {
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

    /** Vrai si la brume du terrain finit avant le bord du terrain normal : aucun LOD ne peut se voir. */
    private boolean lodsHidden() {
        if (!FallowConfig.horizonEnabled() || !TerrainFog.captured() || specialFog()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        float vanillaEdge = minecraft.options.getEffectiveRenderDistance() * 16f;
        return TerrainFog.end() < vanillaEdge;
    }

    private void mirror(DhApiMutableFogRenderParam fog) {
        if (!FallowConfig.horizonEnabled() || !TerrainFog.captured() || specialFog()) {
            return;
        }
        IDhApiConfig configs = DhApi.Delayed.configs;
        if (configs == null) {
            return;
        }
        float radius = configs.graphics().chunkRenderDistance().getValue() * 16f;
        float start = TerrainFog.start();
        float end = TerrainFog.end();
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
