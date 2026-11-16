package io.github.salvagedisc.compline.zoo.client.guide;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.UIContext;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.render.RenderState;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.AbstractWidget;
import io.github.salvagedisc.compline.zoo.SpeciesInfo;
import io.github.salvagedisc.compline.zoo.Stamp;
import io.github.salvagedisc.compline.zoo.ZooTab;
import io.github.salvagedisc.compline.zoo.client.ZooClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

/**
 * La page d'accueil du zoo, mise comme la page des tampons d'Archie. À gauche, sous la bannière, le mot d'Archie et
 * ce que vaut chaque tampon. À droite, une ligne par milieu : son losange (avec la première bête aperçue), son ruban,
 * et sous le ruban les tampons Apercevoir et Maîtriser avec leurs comptes ; un clic sur le losange ou le ruban ouvre
 * le milieu.
 */
final class ZooIntroWidget extends AbstractWidget {
    /** Le texte de la page de gauche, centré sous la bannière. */
    private static final float TEXT_X = 72.0F;
    private static final float TEXT_W = 2 * (ZooPage.PAGE_CENTER - TEXT_X);
    private static final float QUOTE_Y = 52.0F;
    private static final float LEGEND_STEP = 36.0F;
    private static final float LEGEND_TEXT_DX = ZooArt.STAMP_W + 6.0F;
    /** Le bilan d'un milieu, sous son ruban : le tampon Apercevoir et le tampon Maîtriser, chacun avec son compte. */
    private static final Stamp[] BILAN = {Stamp.APERCEVOIR, Stamp.MAITRISER};
    private static final float COUNT_DX = ZooArt.STAMP_W + 2.0F;
    private static final float COUNT_DY = ZooPage.STAMPS_DY + ZooArt.STAMP_W / 2.0F - 3.5F;
    private static final float KEY_Y = 263.0F;

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

        ZooPage.banner(context, graphics, font, Component.translatable("zoo.compline.titre"));
        float y = QUOTE_Y + ZooText.paragraphCentered(graphics, font, Component.translatable("zoo.compline.accueil"),
                ZooPage.PAGE_CENTER, QUOTE_Y, TEXT_W, ZooText.INK);
        y = (float) Math.ceil(y + 4.0F);
        List<Component> tooltip = null;
        for (Stamp stamp : Stamp.values()) {
            ZooPage.stamp(context, stamp, ZooTab.TERRE, TEXT_X, y);
            float textX = TEXT_X + LEGEND_TEXT_DX;
            ZooText.line(graphics, font, Component.translatable(stamp.nameKey()), textX, y + 7.0F, ZooText.INK, 1.0F);
            ZooText.paragraph(graphics, font, Component.translatable("zoo.compline.aide." + stamp.id()),
                    textX, y + 18.0F, TEXT_X + TEXT_W - textX, ZooText.INK_LIGHT, 1.0F);
            y += LEGEND_STEP;
        }

        int row = 0;
        for (ZooTab tab : ZooTab.values()) {
            if (!ZooGuide.hasTab(tab) || row >= ZooSpreadWidget.RIGHT_ROWS.length) {
                continue;
            }
            float top = ZooSpreadWidget.RIGHT_ROWS[row++];
            List<SpeciesInfo> species = ZooGuide.sortedSpecies(tab);
            int spotted = 0;
            int mastered = 0;
            SpeciesInfo face = null;
            for (SpeciesInfo info : species) {
                boolean seen = ZooClient.has(info.id(), Stamp.APERCEVOIR);
                spotted += seen ? 1 : 0;
                mastered += ZooClient.has(info.id(), Stamp.MAITRISER) ? 1 : 0;
                if (seen && face == null) {
                    face = info;
                }
            }
            float frameX = ZooPage.FRAME_X + ZooPage.RIGHT_PAGE;
            float width = ZooPage.ribbon(context, graphics, font, ZooPage.variant(tab), frameX, top,
                    Component.translatable(tab.nameKey() + ".court"));
            ZooPage.frame(context, spotted > 0 ? ZooPage.variant(tab) : 0, frameX, top);
            SpeciesInfo shown = face != null ? face : species.isEmpty() ? null : species.get(0);
            if (shown != null) {
                ZooPage.portrait(graphics, shown.id(), frameX, top, face == null);
            }
            int[] counts = {spotted, mastered};
            for (int i = 0; i < BILAN.length; i++) {
                float sx = ZooPage.STAMPS_X + ZooPage.RIGHT_PAGE + 2 * i * ZooPage.STAMP_STEP;
                float sy = top + ZooPage.STAMPS_DY;
                ZooPage.stamp(context, BILAN[i], counts[i] > 0 ? tab : null, sx, sy);
                ZooText.line(graphics, font, Component.translatable("zoo.compline.compte", counts[i], species.size()),
                        sx + COUNT_DX, top + COUNT_DY, ZooText.INK, 1.0F);
                if (ZooSpreadWidget.onStamp(mouseX, mouseY, sx, sy)) {
                    tooltip = List.of(Component.translatable(BILAN[i].nameKey()),
                            Component.translatable("zoo.compline.compte.aide", counts[i], species.size())
                                    .withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            if (hovered(mouseX, mouseY, frameX, top, width)) {
                List<Component> lines = new ArrayList<>();
                lines.add(Component.translatable(tab.nameKey()));
                lines.add(Component.translatable("zoo.compline.ouvrir").withStyle(ChatFormatting.GRAY));
                tooltip = lines;
            }
        }
        ZooText.paragraphCentered(graphics, font,
                Component.translatable("zoo.compline.touche", ZooClient.guideKey().getTranslatedKeyMessage()),
                ZooPage.PAGE_CENTER, Math.max(KEY_Y, y + 6.0F), TEXT_W, ZooText.INK_LIGHT);
        if (tooltip != null) {
            ZooText.tooltip(tooltip);
        }
    }

    private static boolean hovered(double mouseX, double mouseY, float frameX, float top, float ribbonWidth) {
        return ZooPage.onFrame(mouseX, mouseY, frameX, top)
                || ZooPage.inside(mouseX, mouseY, frameX, top, ribbonWidth, ZooArt.RIBBON_H - 2.0F);
    }

    @Override
    public boolean mouseClicked(UIContext context, int button) {
        int row = 0;
        for (ZooTab tab : ZooTab.values()) {
            if (!ZooGuide.hasTab(tab) || row >= ZooSpreadWidget.RIGHT_ROWS.length) {
                continue;
            }
            float top = ZooSpreadWidget.RIGHT_ROWS[row++];
            float frameX = ZooPage.FRAME_X + ZooPage.RIGHT_PAGE;
            if (hovered(context.mouseX(), context.mouseY(), frameX, top, ZooArt.RIBBON_W)) {
                context.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                return ZooGuide.flipTo(tab);
            }
        }
        return super.mouseClicked(context, button);
    }
}
