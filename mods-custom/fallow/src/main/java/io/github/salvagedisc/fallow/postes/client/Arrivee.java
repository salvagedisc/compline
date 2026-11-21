package io.github.salvagedisc.fallow.postes.client;

import java.util.Optional;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.scene.Visual;

/**
 * Ce qui arrive sur un poste sans passer par le curseur (Maj + clic dans l'inventaire, le livre qui garnit) vole de sa case de la
 * barre jusqu'à sa place, en arc, comme chez Hands-On Vanilla ({@code StationHands.fly}, MIT) : 0,32 seconde, une flèche de 2,5
 * pixels, un petit bruit qui monte d'un objet à l'autre. Ce qu'on lâche du curseur, on l'a vu partir de la main : il ne vole pas.
 */
final class Arrivee {
    static final float VOL = 0.32F;
    private static final float FLECHE = 2.5F;
    /** Le temps du jeu au dernier objet lâché du curseur sur un poste : ce qui arrive juste après vient de la main. */
    private static long curseurA = -1000L;
    /** La case de l'inventaire envoyée la dernière par Maj + clic, et quand : elle peut s'être vidée avant le vol. */
    private static int depart = -1;
    private static long departA = -1000L;

    private Arrivee() {
    }

    /** Un objet vient d'être lâché du curseur sur le poste. */
    static void parLeCurseur(StationView view) {
        curseurA = view.level().getGameTime();
    }

    /** Une case de l'inventaire vient d'être envoyée au poste par Maj + clic. */
    static void depuis(StationView view, int slot) {
        depart = slot;
        departA = view.level().getGameTime();
    }

    /** Vrai si ce qui arrive maintenant vient du curseur (lâché il y a moins d'une demi-seconde). */
    static boolean duCurseur(StationView view) {
        return view.level().getGameTime() - curseurA < 10L;
    }

    /** Fait voler le nœud d'une case (qui montre déjà l'objet) depuis la case de la barre qui tient {@code item}. */
    static void caseDepuisLaBarre(StationView view, String nodeId, Item item, int rang) {
        view.node(nodeId).ifPresent(node -> {
            Vector3fc base = node.definition().transform().pos();
            surLaScene(view, barre(view, item), base.y() + 2.0F).map(on -> on.sub(base, new Vector3f())).filter(from -> from.length() <= 40.0F)
                    .ifPresent(from -> {
                        node.moveTo(from.x, from.y, from.z, 0.0F);
                        node.hopTo(0.0F, 0.0F, 0.0F, FLECHE, VOL);
                    });
            son(view, nodeId, rang);
        });
    }

    /**
     * Fait voler {@code item}, dessiné par un nœud caché {@code nodeId} de la scène, depuis la case de la barre qui le tient jusqu'au
     * point {@code at} (pixels de la scène). Le nœud se cache de lui-même au bout du vol (voir {@link Vol}).
     */
    static void objetDepuisLaBarre(StationView view, Vol vol, Item item, Vector3fc at, int rang) {
        view.node(vol.noeud).ifPresent(node -> {
            Vector3fc base = node.definition().transform().pos();
            Optional<Vector3f> from = surLaScene(view, barre(view, item), at.y() + 2.0F).map(on -> on.sub(base, new Vector3f()))
                    .filter(f -> f.length() <= 40.0F);
            if (from.isEmpty()) {
                return;
            }
            node.setVisual(Visual.ofItem(item));
            node.moveTo(from.get().x, from.get().y, from.get().z, 0.0F);
            node.scaleTo(0.7F, 0.0F);
            node.hopTo(at.x() - base.x(), at.y() - base.y(), at.z() - base.z(), FLECHE, VOL);
            node.scaleTo(1.0F, VOL);
            node.setVisible(true);
            vol.temps = 0.0F;
            son(view, vol.noeud, rang);
        });
    }

    private static void son(StationView view, String nodeId, int rang) {
        Son.joue(view, SoundEvents.ITEM_FRAME_ADD_ITEM, nodeId, 0.35F, 1.0F + 0.08F * Math.min(rang, 8));
    }

    /**
     * La case de la barre d'où part {@code item} : celle du dernier Maj + clic s'il est tout frais, sinon la première qui en tient ;
     * -1 hors de la barre (le vol part alors du milieu du bas de l'écran).
     */
    static int barre(StationView view, Item item) {
        if (view.level().getGameTime() - departA < 10L) {
            return depart >= 0 && depart < Inventory.getSelectionSize() ? depart : -1;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return -1;
        }
        for (int i = 0; i < Inventory.getSelectionSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                return i;
            }
        }
        return -1;
    }

    static Optional<Vector3f> surLaScene(StationView view, int slot, float height) {
        Window window = Minecraft.getInstance().getWindow();
        float width = window.getGuiScaledWidth();
        float tall = window.getGuiScaledHeight();
        float x = slot >= 0 && slot < Inventory.getSelectionSize() ? (width / 2.0F - 80.0F + 20.0F * slot) / width : 0.5F;
        return view.screenOn(x, (tall - 11.0F) / tall, height);
    }

    /** Un nœud de vol : il se cache quand son vol est fini. */
    static final class Vol {
        final String noeud;
        float temps = -1.0F;

        Vol(String noeud) {
            this.noeud = noeud;
        }

        void frame(StationView view, float dt) {
            if (temps < 0.0F) {
                return;
            }
            temps += dt;
            if (temps >= VOL) {
                view.node(noeud).ifPresent(node -> node.setVisible(false));
                temps = -1.0F;
            }
        }
    }

    /** Les trois nœuds de vol d'une scène de la tannerie ({@code arrivee_0} à {@code arrivee_2}), pris à tour de rôle. */
    static final class Volee {
        private final Vol[] vols = { new Vol("arrivee_0"), new Vol("arrivee_1"), new Vol("arrivee_2") };
        private int suivant;
        private int rang;
        private long rangA = -1000L;

        void frame(StationView view, float dt) {
            for (Vol vol : vols) {
                vol.frame(view, dt);
            }
        }

        /** Vrai tant qu'un objet de la volée est en l'air. */
        boolean enVol() {
            for (Vol vol : vols) {
                if (vol.temps >= 0.0F) {
                    return true;
                }
            }
            return false;
        }

        /** {@code item} arrive au point {@code at} de la scène : il vole de la barre, sauf s'il vient du curseur. */
        void arrive(StationView view, Item item, Vector3fc at) {
            // Ce qui arrive part de la barre de celui qui travaille : les autres ne le voient pas voler de la leur.
            if (item == null || duCurseur(view) || !view.isFocused()) {
                return;
            }
            long now = view.level().getGameTime();
            rang = now - rangA < 4L ? rang + 1 : 0;
            rangA = now;
            objetDepuisLaBarre(view, vols[suivant], item, at, rang);
            suivant = (suivant + 1) % vols.length;
        }
    }
}
