package io.github.salvagedisc.fallow.postes.client;

import com.pockyl.diegesis.api.client.StationView;

/**
 * Ce qui fait vivre la scène d'un poste de la tannerie, à part de ce qu'on y fait : le bloc redessiné, ce qu'il contient, les
 * bruits et les particules. Lu dans l'état du bloc à chaque image ; rien n'est envoyé au serveur.
 */
interface Allure {
    default void onSceneReady(StationView view) {
    }

    default void onOpen(StationView view) {
    }

    default void frame(StationView view, float dt) {
    }

    default void tick(StationView view) {
    }

    /** Un objet lâché sur le bloc depuis le curseur, juste avant que le serveur le reçoive : le geste se voit tout de suite. */
    default void geste(StationView view, net.minecraft.world.item.ItemStack stack) {
    }

    /** Le même geste, fait par un autre joueur : le serveur l'a annoncé à ceux qui voient le poste. */
    default void gesteAutre(StationView view, net.minecraft.world.item.ItemStack stack) {
        geste(view, stack);
    }

    Allure AUCUNE = new Allure() {
    };
}
