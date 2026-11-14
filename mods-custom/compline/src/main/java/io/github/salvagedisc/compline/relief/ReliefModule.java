package io.github.salvagedisc.compline.relief;

import io.github.salvagedisc.compline.Compline;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module relief (client) : il donne au pack de ressources {@code compline-ambiance} la forme du monde autour de la
 * caméra, pour que ses post-effets (Polytone) dessinent l'ombre du soleil sans pack de shaders.
 *
 * <p>Un post-effet ne voit que l'image et sa profondeur : il ne sait pas ce qui se trouve hors de l'écran, donc pas
 * si la cime d'un arbre cache le soleil. {@link ReliefTexture} tient à jour une petite texture, {@code compline:relief},
 * qui contient la hauteur de la surface (feuillages compris) de chaque colonne à 128 blocs à la ronde. Le pack de
 * ressources la lit ({@code samplers} de son {@code post_chains/rendu.json}) et avance de chaque point vers le soleil
 * à travers ce relief. Sans le mod, la texture manque et le pack ne dessine pas d'ombres.</p>
 */
public final class ReliefModule {
    private ReliefModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> ReliefTexture.tick());
        Compline.LOGGER.info("relief : hauteurs du monde publiées pour les ombres du pack de ressources");
    }
}
