package io.github.salvagedisc.fallow.titres;

import io.github.salvagedisc.fallow.Fallow;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module titres (client) : le nom de chaque écran quitte le panneau pour une plaque centrée sur son
 * bord haut, qui en dépasse, en lettres gravées dans les couleurs de l'écran, à la manière de
 * GUI Revision. Le nom suit la langue du jeu et celui qu'on donne à un coffre.
 *
 * <ul>
 *   <li>Les mixins {@code titres.AbstractContainerScreenMixin} et {@code titres.OwnLabelsScreenMixin}
 *       n'écrivent plus les libellés des écrans qui portent la plaque ({@link TitresClient#hidesLabels}) :
 *       ceux dont les libellés viennent du jeu, mods compris. Un écran qui écrit ses libellés lui-même
 *       les garde, sans plaque.</li>
 *   <li>{@link TitresClient} dessine la plaque et le nom. Leur aspect vient du pack fallow-interface
 *       ({@code fallow:titres.json}, écrit par {@code scripts/interface/build.py}) ; sans lui, rien
 *       n'est dessiné.</li>
 * </ul>
 */
public final class TitresModule {
    private TitresModule() {
    }

    public static void start(IEventBus modBus) {
        modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(TitresClient.TABLE_LISTENER));
        NeoForge.EVENT_BUS.addListener(ContainerScreenEvent.Render.Background.class, TitresClient::onBackground);
        Fallow.LOGGER.info("titres : noms des écrans sur une plaque");
    }
}
