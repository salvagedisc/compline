package io.github.salvagedisc.fallow.zoo.client.guide;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.UIContext;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.render.RenderState;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.AbstractWidget;
import io.github.salvagedisc.fallow.zoo.SpeciesInfo;
import io.github.salvagedisc.fallow.zoo.Stamp;
import io.github.salvagedisc.fallow.zoo.ZooTab;
import io.github.salvagedisc.fallow.zoo.client.ZooClient;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

/**
 * Une double page d'espèces, mise comme la page des tampons d'Archie : à gauche la bannière du milieu et trois
 * lignes, à droite quatre. Chaque ligne a le losange de la bête, un ruban à son nom et ses tampons dessous. Une
 * espèce pas encore aperçue a un ruban et un losange éteints, et sa silhouette à l'encre.
 */
final class ZooSpreadWidget extends AbstractWidget {
    /** Le haut des rubans, comme sur la page des tampons d'Archie. */
    static final float[] LEFT_ROWS = {93.0F, 153.0F, 213.0F};
    static final float[] RIGHT_ROWS = {33.0F, 93.0F, 153.0F, 213.0F};
    /** La ligne sous la bannière : ce qui est maîtrisé, et la page. */
    static final float SUMMARY_Y = 56.0F;

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

        ZooPage.banner(context, graphics, font, Component.translatable(tab.nameKey() + ".court"));
        int mastered = 0;
        List<SpeciesInfo> all = ZooGuide.speciesOf(tab);
        for (SpeciesInfo info : all) {
            if (ZooClient.has(info.id(), Stamp.MAITRISER)) {
                mastered++;
            }
        }
        Component summary = count > 1
                ? Component.translatable("zoo.fallow.resume.pages", mastered, all.size(), number, count)
                : Component.translatable("zoo.fallow.maitrisees", mastered, all.size());
        ZooText.centered(graphics, font, summary, ZooPage.PAGE_CENTER, SUMMARY_Y, ZooText.INK_LIGHT, 1.0F);

        List<Component> tooltip = null;
        for (int i = 0; i < species.size(); i++) {
            boolean left = i < LEFT_ROWS.length;
            float offset = left ? 0.0F : ZooPage.RIGHT_PAGE;
            float top = left ? LEFT_ROWS[i] : RIGHT_ROWS[i - LEFT_ROWS.length];
            List<Component> hovered = row(context, graphics, font, species.get(i), offset, top);
            if (hovered != null) {
                tooltip = hovered;
            }
        }
        if (tooltip != null) {
            ZooText.tooltip(tooltip);
        }
    }

    /** Dessine une ligne ; rend l'infobulle de ce que la souris survole, ou {@code null}. */
    private List<Component> row(UIContext context, GuiGraphics graphics, Font font, SpeciesInfo info, float offset, float top) {
        double mouseX = context.mouseX();
        double mouseY = context.mouseY();
        int mask = ZooClient.stamps(info.id());
        boolean spotted = Stamp.APERCEVOIR.in(mask);
        int variant = spotted ? ZooPage.variant(info.tab()) : 0;
        float frameX = ZooPage.FRAME_X + offset;
        List<Component> tooltip = null;

        Component name = spotted ? ZooText.speciesName(info.id()) : Component.translatable("zoo.fallow.inconnue");
        float ribbonWidth = ZooPage.ribbon(context, graphics, font, variant, frameX, top, name);
        ZooPage.frame(context, variant, frameX, top);
        if (!ZooPage.portrait(graphics, info.id(), frameX, top, !spotted)) {
            ZooText.centered(graphics, font, Component.literal("?"), frameX, top + ZooPage.FRAME_DY - 4.0F, ZooText.INK_LIGHT, 1.0F);
        }
        if (ZooPage.onFrame(mouseX, mouseY, frameX, top)
                || ZooPage.inside(mouseX, mouseY, frameX, top, ribbonWidth, ZooArt.RIBBON_H - 2.0F)) {
            tooltip = spotted ? List.of(ZooText.speciesName(info.id()))
                    : List.of(Component.translatable("zoo.fallow.inconnue.aide").withStyle(ChatFormatting.GRAY));
        }

        int slot = 0;
        for (Stamp stamp : Stamp.values()) {
            if (stamp == Stamp.HEBERGER && !info.housable()) {
                continue;
            }
            float sx = ZooPage.STAMPS_X + offset + slot++ * ZooPage.STAMP_STEP;
            float sy = top + ZooPage.STAMPS_DY;
            ZooPage.stamp(context, stamp, stamp.in(mask) ? info.tab() : null, sx, sy);
            if (onStamp(mouseX, mouseY, sx, sy)) {
                tooltip = stampTooltip(info, stamp, mask);
            }
        }
        return tooltip;
    }

    /** La souris est-elle sur le losange d'un tampon (34 de large, sans son ombre) ? */
    static boolean onStamp(double mouseX, double mouseY, float x, float y) {
        double dx = Math.abs(mouseX - (x + ZooArt.STAMP_W / 2.0F));
        double dy = Math.abs(mouseY - (y + ZooArt.STAMP_W / 2.0F));
        return dx + dy <= ZooArt.STAMP_W / 2.0F;
    }

    private List<Component> stampTooltip(SpeciesInfo info, Stamp stamp, int mask) {
        List<Component> lines = new ArrayList<>();
        boolean done = stamp.in(mask);
        lines.add(Component.translatable(stamp.nameKey()).withStyle(style -> done
                ? style.withColor(TextColor.fromRgb(info.tab().color() | 0x404040)) : style.withColor(ChatFormatting.GRAY)));
        lines.add(Component.translatable(done ? "zoo.fallow.obtenu" : "zoo.fallow.aide." + stamp.id())
                .withStyle(ChatFormatting.DARK_GRAY));
        if (stamp == Stamp.MAITRISER && !done) {
            int needed = ZooClient.observeSecondsNeeded();
            int seen = Math.min(needed, ZooClient.observedSeconds(info.id()));
            lines.add(Component.translatable("zoo.fallow.observation", seen, needed).withStyle(ChatFormatting.DARK_GRAY));
        }
        return lines;
    }
}
