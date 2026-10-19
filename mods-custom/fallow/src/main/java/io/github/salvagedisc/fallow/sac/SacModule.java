package io.github.salvagedisc.fallow.sac;

import io.github.salvagedisc.fallow.Fallow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Module sac : boire au tuyau de Traveler's Backpack désaltère dans Thirst Was Reclaimed.
 *
 * <p>Traveler's Backpack a une API d'effets de fluide ({@code api.fluids.EffectFluid}) : chaque effet
 * enregistré pour un fluide s'exécute quand le joueur boit ce fluide au tuyau. On y ajoute un effet
 * {@link ThirstHoseEffect} sur l'eau, à côté de celui du sac (qui éteint le feu). Pas de mixin.
 *
 * <p>Le sac vide son registre d'effets pendant son {@code FMLCommonSetupEvent} ({@code initEffects}) :
 * on enregistre donc le nôtre plus tard, à la fin du chargement.
 */
public final class SacModule {
    private SacModule() {
    }

    public static void start(IEventBus modBus) {
        modBus.addListener(FMLLoadCompleteEvent.class, event -> event.enqueueWork(SacModule::registerEffect));
    }

    private static void registerEffect() {
        // Le constructeur d'EffectFluid s'enregistre lui-même ; un identifiant déjà pris est ignoré.
        ThirstHoseEffect effect = new ThirstHoseEffect();
        Fallow.LOGGER.info("sac : effet {} enregistré sur l'eau du tuyau (n° {})", effect.getUniqueId(), effect.getEffectID());
    }
}
