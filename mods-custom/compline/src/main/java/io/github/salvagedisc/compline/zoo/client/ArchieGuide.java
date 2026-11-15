package io.github.salvagedisc.compline.zoo.client;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.GuidebookScreen;
import net.minecraft.client.Minecraft;

/** Ouvre le guide d'Archie's Dinosaurs (classe chargée seulement si le mod est là). */
final class ArchieGuide {
    private ArchieGuide() {
    }

    static void open() {
        Minecraft.getInstance().setScreen(new GuidebookScreen());
    }
}
