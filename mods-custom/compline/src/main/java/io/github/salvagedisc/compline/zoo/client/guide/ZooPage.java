package io.github.salvagedisc.compline.zoo.client.guide;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.UIContext;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.misc.Sprite;
import io.github.salvagedisc.compline.zoo.Stamp;
import io.github.salvagedisc.compline.zoo.ZooTab;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

/**
 * La mise en page de la page des tampons d'Archie, relevée sur sa planche : bannière du titre en haut à gauche,
 * lignes de bêtes (losange du portrait, ruban du nom, tampons dessous) à des hauteurs fixes, tampons tous les 38.
 * Une unité du livre vaut un texel de ses images.
 */
final class ZooPage {
    /** D'une page à l'autre, tout est décalé de 252. */
    static final float RIGHT_PAGE = 252.0F;
    /** Axe du losange d'un portrait sur la page de gauche. */
    static final float FRAME_X = 94.0F;
    /** Premier tampon d'une ligne, sur la page de gauche, et pas entre deux tampons. */
    static final float STAMPS_X = 117.0F;
    static final float STAMP_STEP = 38.0F;
    /** Sous le haut du ruban : le premier rang des tampons, le centre du losange, le nom. */
    static final float STAMPS_DY = 22.0F;
    static final float FRAME_DY = 18.0F;
    static final float NAME_DX = 40.0F;
    static final float NAME_DY = 6.0F;
    /** De combien un ruban peut s'allonger pour un nom long (le bout reste loin du dos et du bord). */
    static final float RIBBON_MAX_EXTRA = 24.0F;
    /** La bannière du titre, et le centre de la page de gauche (celui de la bannière). */
    static final float BANNER_X = 87.0F;
    static final float BANNER_Y = 23.0F;
    static final float PAGE_CENTER = BANNER_X + (ZooArt.BANNER_W - 1) / 2.0F + 0.5F;
    private static final float BANNER_SCALE_X = 2.0F;
    private static final float BANNER_SCALE_Y = 12.0F / 7.0F;
    /** La boîte où une bête est dessinée, centrée sur son losange. */
    static final float PORTRAIT_W = 46.0F;
    static final float PORTRAIT_H = 40.0F;

    private ZooPage() {
    }

    /** La variante d'image d'un milieu ; 0 pour ce qui n'est pas obtenu. */
    static int variant(ZooTab tab) {
        return tab == null ? 0 : tab.ordinal() + 1;
    }

    static void banner(UIContext context, GuiGraphics graphics, Font font, Component title) {
        if (ZooTextures.art() == null) {
            ZooText.centered(graphics, font, title, PAGE_CENTER, BANNER_Y + 4.0F, ZooText.INK, 2.0F);
            return;
        }
        float w = ZooArt.BANNER_W;
        float h = ZooArt.BANNER_H;
        context.drawSprite(Sprite.of(ZooTextures.BANNER, 0.0F, 0.0F, w, h, w, h), BANNER_X, BANNER_Y, w, h);
        Component text = Component.literal(title.getString().toUpperCase(Locale.FRENCH));
        // Les capitales d'Archie font 12 de haut, dès le rang 5 de la bannière, et avancent de 12 : celles du jeu
        // (7 rangs, 6 d'avance) à l'échelle 2 en largeur et 12/7 en hauteur.
        float x = Math.round(PAGE_CENTER - ZooText.inked(font, text) * BANNER_SCALE_X / 2.0F);
        ZooText.line(graphics, font, text, x, BANNER_Y + 5.0F, 0xFF000000 | ZooTextures.art().bannerInk(),
                BANNER_SCALE_X, BANNER_SCALE_Y);
    }

    /**
     * Un ruban avec un nom, en capitales grasses comme ceux d'Archie, depuis l'axe du losange {@code frameX} ;
     * s'allonge si le nom est long. Rend la largeur du ruban.
     */
    static float ribbon(UIContext context, GuiGraphics graphics, Font font, int variant, float frameX, float top, Component name) {
        Component text = Component.literal(name.getString().toUpperCase(Locale.FRENCH)).withStyle(ChatFormatting.BOLD);
        float room = ZooArt.RIBBON_W - NAME_DX - 8.0F;
        float extra = Math.max(0.0F, Math.min(RIBBON_MAX_EXTRA, font.width(text) - room));
        ZooArt.Art art = ZooTextures.art();
        int ink = ZooText.INK;
        if (art != null) {
            float cut = ZooArt.RIBBON_TILE_X + ZooArt.RIBBON_TILE_W;
            ribbonPart(context, variant, 0.0F, cut, frameX, top);
            for (float done = 0.0F; done < extra; done += ZooArt.RIBBON_TILE_W) {
                ribbonPart(context, variant, ZooArt.RIBBON_TILE_X, Math.min(ZooArt.RIBBON_TILE_W, extra - done), frameX + cut + done, top);
            }
            ribbonPart(context, variant, cut, ZooArt.RIBBON_W - cut, frameX + cut + extra, top);
            ink = 0xFF000000 | art.ribbonInk()[variant];
        }
        ZooText.line(graphics, font, ZooText.fit(font, text, room + extra, 1.0F), frameX + NAME_DX, top + NAME_DY, ink, 1.0F);
        return ZooArt.RIBBON_W + extra;
    }

    private static void ribbonPart(UIContext context, int variant, float u, float width, float x, float y) {
        float h = ZooArt.RIBBON_H;
        context.drawSprite(Sprite.of(ZooTextures.RIBBONS, u, ZooArt.at(variant, ZooArt.RIBBON_H), width, h, ZooArt.RIBBON_W,
                        ZooArt.sheet(ZooArt.RIBBON_H, ZooArt.VARIANTS)),
                x, y, width, h);
    }

    /** Le losange d'un portrait, centré sur l'axe {@code frameX}, son milieu à {@link #FRAME_DY} sous le ruban. */
    static void frame(UIContext context, int variant, float frameX, float top) {
        if (ZooTextures.art() == null) {
            return;
        }
        float w = ZooArt.FRAME_W;
        float h = ZooArt.FRAME_H;
        context.drawSprite(Sprite.of(ZooTextures.FRAMES, 0.0F, ZooArt.at(variant, ZooArt.FRAME_H), w, h, w,
                        ZooArt.sheet(ZooArt.FRAME_H, ZooArt.VARIANTS)),
                frameX - w / 2.0F, top + FRAME_DY - (h - 1) / 2.0F, w, h);
    }

    /** Un tampon : éteint ({@code tab} nul) ou aux couleurs du milieu. */
    static void stamp(UIContext context, Stamp stamp, ZooTab tab, float x, float y) {
        float w = ZooArt.STAMP_W;
        float h = ZooArt.STAMP_H;
        context.drawSprite(Sprite.of(ZooTextures.STAMPS, ZooArt.at(stamp.ordinal(), ZooArt.STAMP_W),
                ZooArt.at(variant(tab), ZooArt.STAMP_H), w, h, ZooArt.sheet(ZooArt.STAMP_W, 4),
                ZooArt.sheet(ZooArt.STAMP_H, ZooArt.VARIANTS)), x, y, w, h);
    }

    /** Une bête dans la boîte de son losange. */
    static boolean portrait(GuiGraphics graphics, net.minecraft.resources.ResourceLocation id, float frameX, float top, boolean ink) {
        return Portraits.draw(graphics, id, frameX - PORTRAIT_W / 2.0F, top + FRAME_DY - PORTRAIT_H / 2.0F,
                PORTRAIT_W, PORTRAIT_H, ink);
    }

    static boolean inside(double mouseX, double mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    /** La souris est-elle sur le losange (centré sur {@code frameX}) ? */
    static boolean onFrame(double mouseX, double mouseY, float frameX, float top) {
        double dx = Math.abs(mouseX - frameX);
        double dy = Math.abs(mouseY - (top + FRAME_DY));
        return dx + dy <= ZooArt.FRAME_W / 2.0F;
    }
}
