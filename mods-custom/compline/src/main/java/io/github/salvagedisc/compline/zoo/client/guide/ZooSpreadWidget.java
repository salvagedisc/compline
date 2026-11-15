package io.github.salvagedisc.compline.zoo.client.guide;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.UIContext;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.misc.Sprite;
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
import net.minecraft.network.chat.TextColor;

/**
 * Une double page d'espèces : seize au plus, huit par page, chacune avec son portrait, son nom et ses quatre
 * tampons, comme la page des tampons d'Archie. Une espèce pas encore aperçue n'a qu'une silhouette à l'encre.
 */
final class ZooSpreadWidget extends AbstractWidget {
    static final float LEFT = 60.0F;
    static final float RIGHT = 304.0F;
    static final float WIDTH = 214.0F;
    static final float HEADER_Y = 32.0F;
    static final float ROWS_Y = 52.0F;
    static final float ROW_H = 26.0F;
    static final float STAMP = 22.0F;
    static final float STAMP_STEP = 24.0F;
    static final float STAMPS_X = WIDTH - 4 * STAMP_STEP + 2.0F;

    private final ZooTab tab;
    private final List<SpeciesInfo> species;
    private final int number;
    private final int count;

    ZooSpreadWidget(float width, float height, ZooTab tab, List<SpeciesInfo> species, int number, int count) {
        super(0.0F, 0.0F, width, height);
        this.tab = tab;
        this.species = species;
        this.number = number;
        this.count = count;
    }

    @Override
    protected void renderContent(UIContext context, RenderState state) {
        super.renderContent(context, state);
        GuiGraphics graphics = context.graphics();
        Font font = context.font();
        double mouseX = context.mouseX();
        double mouseY = context.mouseY();

        ZooText.line(graphics, font, Component.translatable(tab.nameKey()), LEFT, HEADER_Y, 0xFF000000 | tab.color(), 1.0F);
        Component page = Component.translatable("zoo.compline.page", number, count);
        ZooText.line(graphics, font, page, RIGHT + WIDTH - font.width(page), HEADER_Y, ZooText.INK_LIGHT, 1.0F);
        int mastered = 0;
        List<SpeciesInfo> all = ZooGuide.speciesOf(tab);
        for (SpeciesInfo info : all) {
            if (ZooClient.has(info.id(), Stamp.MAITRISER)) {
                mastered++;
            }
        }
        Component progress = Component.translatable("zoo.compline.maitrisees", mastered, all.size());
        ZooText.line(graphics, font, progress, LEFT + WIDTH - font.width(progress), HEADER_Y, ZooText.INK_LIGHT, 1.0F);
        graphics.fill((int) LEFT, (int) HEADER_Y + 12, (int) (LEFT + WIDTH), (int) HEADER_Y + 13, ZooText.INK_LIGHT);
        graphics.fill((int) RIGHT, (int) HEADER_Y + 12, (int) (RIGHT + WIDTH), (int) HEADER_Y + 13, ZooText.INK_LIGHT);

        List<Component> tooltip = null;
        for (int i = 0; i < species.size(); i++) {
            float x = i < ZooGuide.PER_PAGE ? LEFT : RIGHT;
            float y = ROWS_Y + (i % ZooGuide.PER_PAGE) * ROW_H;
            List<Component> hovered = row(context, graphics, font, species.get(i), x, y, mouseX, mouseY);
            if (hovered != null) {
                tooltip = hovered;
            }
        }
        if (tooltip != null) {
            ZooText.tooltip(tooltip);
        }
    }

    /** Dessine une ligne ; renvoie l'infobulle de ce que la souris survole, ou {@code null}. */
    private List<Component> row(UIContext context, GuiGraphics graphics, Font font, SpeciesInfo info,
                                float x, float y, double mouseX, double mouseY) {
        int mask = ZooClient.stamps(info.id());
        boolean spotted = Stamp.APERCEVOIR.in(mask);
        List<Component> tooltip = null;

        context.drawSprite(Sprite.of(ZooGuide.FRAME, 0.0F, 0.0F, 24.0F, 24.0F, 24.0F, 24.0F), x, y, 24.0F, 24.0F);
        if (!Portraits.draw(graphics, info.id(), x + 2.0F, y + 2.0F, 20.0F, !spotted)) {
            ZooText.centered(graphics, font, Component.literal("?"), x + 12.0F, y + 8.0F, ZooText.INK_LIGHT, 1.0F);
        }
        Component name = spotted ? ZooText.speciesName(info.id()) : Component.translatable("zoo.compline.inconnue");
        ZooText.line(graphics, font, ZooText.fit(font, name, STAMPS_X - 32.0F, 1.0F), x + 28.0F, y + 8.0F,
                spotted ? ZooText.INK : ZooText.INK_LIGHT, 1.0F);
        if (inside(mouseX, mouseY, x, y, STAMPS_X - 4.0F, 24.0F)) {
            tooltip = spotted ? List.of(ZooText.speciesName(info.id()))
                    : List.of(Component.translatable("zoo.compline.inconnue.aide").withStyle(ChatFormatting.GRAY));
        }

        for (Stamp stamp : Stamp.values()) {
            if (stamp == Stamp.HEBERGER && !info.housable()) {
                continue;
            }
            float sx = x + STAMPS_X + stamp.ordinal() * STAMP_STEP;
            float sy = y + 1.0F;
            drawStamp(context, stamp, stamp.in(mask) ? info.tab() : null, sx, sy, STAMP);
            if (inside(mouseX, mouseY, sx, sy, STAMP, STAMP)) {
                tooltip = stampTooltip(info, stamp, mask);
            }
        }
        return tooltip;
    }

    /** Dessine un tampon : à l'encre pâle s'il manque ({@code tab} nul), aux couleurs du milieu s'il est obtenu. */
    static void drawStamp(UIContext context, Stamp stamp, ZooTab tab, float x, float y, float size) {
        float row = tab == null ? 0.0F : tab.ordinal() + 1.0F;
        context.drawSprite(Sprite.of(ZooGuide.STAMPS, stamp.ordinal() * 24.0F, row * 24.0F, 24.0F, 24.0F, 96.0F, 120.0F),
                x, y, size, size);
    }

    private List<Component> stampTooltip(SpeciesInfo info, Stamp stamp, int mask) {
        List<Component> lines = new ArrayList<>();
        boolean done = stamp.in(mask);
        lines.add(Component.translatable(stamp.nameKey()).withStyle(style -> done
                ? style.withColor(TextColor.fromRgb(info.tab().color() | 0x404040)) : style.withColor(ChatFormatting.GRAY)));
        lines.add(Component.translatable(done ? "zoo.compline.obtenu" : "zoo.compline.aide." + stamp.id())
                .withStyle(ChatFormatting.DARK_GRAY));
        if (stamp == Stamp.MAITRISER && !done) {
            int needed = ZooClient.observeSecondsNeeded();
            int seen = Math.min(needed, ZooClient.observedSeconds(info.id()));
            lines.add(Component.translatable("zoo.compline.observation", seen, needed).withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }

    static boolean inside(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }
}
