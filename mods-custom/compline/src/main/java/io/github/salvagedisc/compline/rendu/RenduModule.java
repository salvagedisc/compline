package io.github.salvagedisc.compline.rendu;

import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.Compline;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module rendu (client) : il donne au pack de shaders de Compline ce qu'un shader ne peut pas lire seul.
 * Iris ne connaît que le biome du joueur ; les clairières et le vent du pack sont des données de mods.
 * {@link ShaderUniforms} les publie donc à chaque image sous forme d'uniformes {@code brumaire*}, qu'Iris
 * propose à tous les programmes d'un pack (mixin {@code rendu.IrisUniformsMixin}) :
 *
 * <ul>
 *   <li>{@code brumaireLink} : 1 quand le mod alimente les uniformes (0 sans lui : le pack garde ses valeurs de repli) ;</li>
 *   <li>{@code brumaireMist} : x = part de brume levée autour de la caméra (0 dans la brume, 1 au cœur d'une clairière) ;</li>
 *   <li>{@code brumaireClearing0..3} : les clairières les plus proches, (x, z) depuis la caméra, rayon dégagé, force ;</li>
 *   <li>{@code brumaireWind} : direction (x, z), force et turbulence du vent de Where Winds Blow ;</li>
 *   <li>{@code brumaireGust0..1} : ses fronts de rafale les plus proches, déjà réduits à une bande à traverser.</li>
 * </ul>
 *
 * Un pack qui ne déclare pas ces uniformes n'en voit rien : le module ne change le rendu d'aucun autre pack.
 */
public final class RenduModule {
    private RenduModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RenderFrameEvent.Pre.class, event -> ShaderUniforms.nextFrame());
        Compline.LOGGER.info("rendu : clairières{} publiés pour le pack de shaders",
                ModIds.isLoaded(ModIds.WHERE_WINDS_BLOW) ? " et vent de Where Winds Blow" : "");
    }
}
