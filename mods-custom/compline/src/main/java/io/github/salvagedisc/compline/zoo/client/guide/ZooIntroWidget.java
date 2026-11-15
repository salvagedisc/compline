package io.github.salvagedisc.compline.zoo.client.guide;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.UIContext;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.render.RenderState;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.AbstractWidget;
import io.github.salvagedisc.compline.zoo.SpeciesInfo;
import io.github.salvagedisc.compline.zoo.Stamp;
import io.github.salvagedisc.compline.zoo.ZooTab;
import io.github.salvagedisc.compline.zoo.client.ZooClient;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * La page d'accueil du zoo : à gauche, le mot d'Archie et ce que vaut chaque tampon ; à droite, les milieux, avec
 * ce qu'on en a vu et maîtrisé. Un milieu se lit d'un clic.
 */
final class ZooIntroWidget extends AbstractWidget {
    private static final float LEFT = ZooSpreadWidget.LEFT;
    private static final float RIGHT = ZooSpreadWidget.RIGHT;
    private static final float WIDTH = ZooSpreadWidget.WIDTH;
    private static final float TABS_Y = 50.0F;
    private static final float TAB_H = 34.0F;

    ZooIntroWidget(float width, float height) {
        super(0.0F, 0.0F, width, height);
    }

    @Override
    protected void renderContent(UIContext context, RenderState state) {
        super.renderContent(context, state);
        GuiGraphics graphics = context.graphics();
        Font font = context.font();
        double mouseX = context.mouseX();
        double mouseY = context.mouseY();

        ZooText.centered(graphics, font, Component.translatable("zoo.compline.titre"), LEFT + WIDTH / 2.0F, 30.0F, ZooText.INK, 1.25F);
        float y = 46.0F + ZooText.paragraph(graphics, font, Component.translatable("zoo.compline.accueil"),
                LEFT, 46.0F, WIDTH, ZooText.INK, 1.0F);
        y += 8.0F;
        for (Stamp stamp : Stamp.values()) {
            ZooSpreadWidget.drawStamp(context, stamp, ZooTab.TERRE, LEFT, y, 20.0F);
            ZooText.line(graphics, font, Component.translatable(stamp.nameKey()), LEFT + 26.0F, y + 1.0F, ZooText.INK, 1.0F);
            float help = ZooText.paragraph(graphics, font, Component.translatable("zoo.compline.aide." + stamp.id()),
                    LEFT + 26.0F, y + 11.0F, WIDTH - 26.0F, ZooText.INK_LIGHT, 1.0F);
            y += Math.max(26.0F, 15.0F + help);
        }

        ZooText.centered(graphics, font, Component.translatable("zoo.compline.milieux"), RIGHT + WIDTH / 2.0F, 30.0F, ZooText.INK, 1.25F);
        int total = 0;
        int spottedTotal = 0;
        int masteredTotal = 0;
        float rowY = TABS_Y;
        List<Component> tooltip = null;
        for (ZooTab tab : ZooTab.values()) {
            if (!ZooGuide.hasTab(tab)) {
                continue;
            }
            List<SpeciesInfo> species = ZooGuide.speciesOf(tab);
            int spotted = 0;
            int mastered = 0;
            for (SpeciesInfo info : species) {
                spotted += ZooClient.has(info.id(), Stamp.APERCEVOIR) ? 1 : 0;
                mastered += ZooClient.has(info.id(), Stamp.MAITRISER) ? 1 : 0;
            }
            total += species.size();
            spottedTotal += spotted;
            masteredTotal += mastered;
            boolean hovered = ZooSpreadWidget.inside(mouseX, mouseY, RIGHT, rowY, WIDTH, TAB_H - 6.0F);
            ZooSpreadWidget.drawStamp(context, Stamp.MAITRISER, mastered > 0 ? tab : null, RIGHT, rowY + 1.0F, 24.0F);
            Component name = Component.translatable(tab.nameKey());
            if (hovered) {
                name = name.copy().withStyle(ChatFormatting.UNDERLINE);
                tooltip = List.of(Component.translatable("zoo.compline.ouvrir").withStyle(ChatFormatting.GRAY));
            }
            ZooText.line(graphics, font, name, RIGHT + 30.0F, rowY + 2.0F, 0xFF000000 | tab.color(), 1.0F);
            ZooText.line(graphics, font, Component.translatable("zoo.compline.bilan", spotted, mastered, species.size()),
                    RIGHT + 30.0F, rowY + 14.0F, ZooText.INK, 1.0F);
            rowY += TAB_H;
        }
        ZooText.paragraph(graphics, font, Component.translatable("zoo.compline.total", spottedTotal, masteredTotal, total),
                RIGHT, rowY + 4.0F, WIDTH, ZooText.INK_LIGHT, 1.0F);
        ZooText.paragraph(graphics, font, Component.translatable("zoo.compline.touche", ZooClient.guideKey().getTranslatedKeyMessage()),
                RIGHT, 234.0F, WIDTH, ZooText.INK_LIGHT, 1.0F);
        if (tooltip != null) {
            ZooText.tooltip(tooltip);
        }
    }

    @Override
    public boolean mouseClicked(UIContext context, int button) {
        float rowY = TABS_Y;
        for (ZooTab tab : ZooTab.values()) {
            if (!ZooGuide.hasTab(tab)) {
                continue;
            }
            if (ZooSpreadWidget.inside(context.mouseX(), context.mouseY(), RIGHT, rowY, WIDTH, TAB_H - 6.0F)) {
                context.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                return ZooGuide.flipTo(tab);
            }
            rowY += TAB_H;
        }
        return super.mouseClicked(context, button);
    }
}
