package io.github.salvagedisc.compline.zoo.client.guide;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

/** L'écriture du zoo dans le guide : les encres d'Archie, sans ombre, à l'échelle voulue. */
final class ZooText {
    static final int INK = 0xFF573D19;
    static final int INK_LIGHT = 0xFF947546;

    private ZooText() {
    }

    static Component speciesName(ResourceLocation id) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).map(type -> (Component) type.getDescription())
                .orElse(Component.literal(id.getPath()));
    }

    /** Écrit une ligne ; renvoie sa largeur à l'écran. */
    static float line(GuiGraphics graphics, Font font, Component text, float x, float y, int color, float scale) {
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
        return font.width(text) * scale;
    }

    /** Écrit une ligne centrée sur {@code centerX}. */
    static void centered(GuiGraphics graphics, Font font, Component text, float centerX, float y, int color, float scale) {
        line(graphics, font, text, centerX - font.width(text) * scale / 2.0F, y, color, scale);
    }

    /** Écrit un paragraphe coupé à {@code width} ; renvoie sa hauteur à l'écran. */
    static float paragraph(GuiGraphics graphics, Font font, FormattedText text, float x, float y, float width, int color, float scale) {
        List<FormattedCharSequence> lines = font.split(text, (int) (width / scale));
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        int lineY = 0;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, 0, lineY, color, false);
            lineY += font.lineHeight + 1;
        }
        graphics.pose().popPose();
        return lineY * scale;
    }

    /** Coupe un nom trop long pour sa place, avec des points de suspension. */
    static Component fit(Font font, Component text, float width, float scale) {
        int max = (int) (width / scale);
        if (font.width(text) <= max) {
            return text;
        }
        String cut = font.plainSubstrByWidth(text.getString(), max - font.width("…"));
        return Component.literal(cut.stripTrailing() + "…");
    }

    /**
     * Confie l'infobulle à l'écran du guide : dessinée dans la page, elle serait coupée au bord de la feuille et
     * passerait sous les onglets.
     */
    static void tooltip(List<Component> lines) {
        Screen screen = Minecraft.getInstance().screen;
        if (screen != null) {
            screen.setTooltipForNextRenderPass(lines.stream().map(Component::getVisualOrderText).toList());
        }
    }
}
