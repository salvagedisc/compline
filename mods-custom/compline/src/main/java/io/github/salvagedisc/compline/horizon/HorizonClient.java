package io.github.salvagedisc.compline.horizon;

import com.seibel.distanthorizons.api.DhApi;
import com.seibel.distanthorizons.api.methods.events.DhApiEventRegister;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiAfterDhInitEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeFogRenderEvent;
import com.seibel.distanthorizons.api.methods.events.abstractEvents.DhApiBeforeRenderEvent;
import com.seibel.distanthorizons.api.methods.events.sharedParameterObjects.DhApiEventParam;
import io.github.salvagedisc.compline.Compline;

/** Branchement côté client : les événements de Distant Horizons ne s'enregistrent qu'une fois DH prêt. */
final class HorizonClient {
    /** Version majeure de l'API de Distant Horizons contre laquelle le module est écrit. */
    private static final int API_MAJOR = 7;

    private HorizonClient() {
    }

    static void start() {
        int major = DhApi.getApiMajorVersion();
        if (major != API_MAJOR) {
            Compline.LOGGER.warn("horizon : API Distant Horizons {} (attendue {}), brume lointaine non reliée", major, API_MAJOR);
            return;
        }
        DhApiEventRegister.on(DhApiAfterDhInitEvent.class, new DhApiAfterDhInitEvent() {
            @Override
            public void afterDistantHorizonsInit(DhApiEventParam<Void> event) {
                HorizonFog fog = new HorizonFog();
                DhApiEventRegister.on(DhApiBeforeRenderEvent.class, fog.beforeRender());
                DhApiEventRegister.on(DhApiBeforeFogRenderEvent.class, fog.beforeFog());
                Compline.LOGGER.info("horizon : brume lointaine de Distant Horizons {} reliée à la brume du terrain", DhApi.getModVersion());
            }
        });
    }
}
