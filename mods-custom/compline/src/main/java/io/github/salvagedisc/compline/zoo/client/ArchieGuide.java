package io.github.salvagedisc.compline.zoo.client;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.GuidebookScreen;
import io.github.salvagedisc.compline.zoo.client.guide.ZooGuide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Ouvre le guide d'Archie's Dinosaurs (classe chargée seulement si le mod est là). */
final class ArchieGuide {
    private ArchieGuide() {
    }

    static void open() {
        Minecraft.getInstance().setScreen(new GuidebookScreen());
    }

    /** Avant le dessin d'un écran : si c'est le guide, on y mesure les bêtes en attente. */
    static void beforeRender(Screen screen) {
        if (screen instanceof GuidebookScreen) {
            ZooGuide.beforeRender();
        }
    }
}
