package io.github.salvagedisc.fallow.titres;

import io.github.salvagedisc.fallow.Fallow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module titres (client) : le titre des écrans vanilla (four, enclume, coffre…) quitte le panneau,
 * où il tombait sur les cadres et les ornements du pack d'interface, pour une plaque posée sur le
 * bord haut de l'écran.
 *
 * <ul>
 *   <li>Les mixins {@code titres.AbstractContainerScreenMixin} et {@code titres.OwnLabelsScreenMixin}
 *       n'écrivent plus les titres des écrans vanilla ({@link TitresClient#hidesLabels}).</li>
 *   <li>{@link TitresClient} dessine la plaque ({@code fallow:plaque}, en neuf parties) et le titre
 *       dessus, à l'encre mousse. Le bord haut de chaque panneau est lu dans
 *       {@code assets/fallow/plaque.json}, écrit par {@code scripts/interface/build.py} dans le pack
 *       fallow-interface ; sans lui, la plaque se pose au bord de l'image (panneaux vanilla).</li>
 * </ul>
 *
 * Le libellé « Inventaire » et celui des échanges du villageois sont vides dans le pack d'interface.
 */
public final class TitresModule {
    private TitresModule() {
    }

    public static void start(IEventBus modBus) {
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(TitresClient.TOPS));
        NeoForge.EVENT_BUS.addListener(ContainerScreenEvent.Render.Background.class, TitresClient::onBackground);
        Fallow.LOGGER.info("titres : titres des écrans vanilla sur une plaque");
    }
}
