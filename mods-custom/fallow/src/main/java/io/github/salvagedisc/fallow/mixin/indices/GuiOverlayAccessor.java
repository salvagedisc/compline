package io.github.salvagedisc.fallow.mixin.indices;

import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Le message de la barre d'action tel que le jeu le garde : tout ce qui l'écrit (le serveur, les mods côté client, le
 * juke-box) passe par {@code Gui.setOverlayMessage}, qui pose ces deux champs. Le module indices les lit pour dessiner
 * le message en marge.
 */
@Mixin(Gui.class)
public interface GuiOverlayAccessor {
    @Accessor("overlayMessageString")
    Component fallow$overlayMessage();

    @Accessor("overlayMessageTime")
    int fallow$overlayTime();
}
