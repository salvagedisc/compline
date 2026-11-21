package io.github.salvagedisc.fallow.postes.client;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import net.minecraft.world.item.Item;

import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.scene.Visual;

/**
 * Ce qui sort d'un poste se lève du bloc et s'efface : l'objet fini monte un peu, à sa taille dans la scène, et fond. Diegesis
 * le fait ensuite voler jusqu'à sa case de la barre (les « gains » de la scène, avec le « +1 »), si bien qu'on voit une seule
 * chose quitter le bloc et arriver dans l'inventaire. Un nœud {@code envol} de la scène, caché, le dessine.
 */
final class Envol {
    private static final String NOEUD = "envol";
    /** Le temps de l'envol. */
    private static final float VOL = 0.25F;

    private float temps = -1.0F;

    /** Fait partir {@code item} du point {@code at} de la scène (pixels, avant le tour de la façade). */
    void part(StationView view, Item item, Vector3fc at) {
        view.node(NOEUD).ifPresent(node -> {
            Vector3f depart = new Vector3f(at).sub(node.definition().transform().pos());
            node.setVisual(Visual.ofItem(item));
            node.moveTo(depart.x, depart.y, depart.z, 0.0F);
            node.scaleTo(1.0F, 0.0F);
            node.moveTo(depart.x, depart.y + 3.0F, depart.z, VOL);
            node.scaleTo(0.0F, VOL);
            node.setVisible(true);
            temps = 0.0F;
        });
    }

    void frame(StationView view, float dt) {
        if (temps < 0.0F) {
            return;
        }
        temps += dt;
        if (temps >= VOL) {
            view.node(NOEUD).ifPresent(node -> node.setVisible(false));
            temps = -1.0F;
        }
    }
}
