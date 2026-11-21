package io.github.salvagedisc.fallow.indices;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.mixin.indices.GuiOverlayAccessor;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

/**
 * Les messages de la barre d'action, dessinés en marge à gauche comme des notes : un filet doré, une icône pour les
 * messages de Fallow, le texte couleur parchemin sur un fond sombre qui s'efface vers la droite. Une note glisse en
 * entrant, reste le temps de la lire, puis s'estompe ; trois au plus s'empilent, la plus récente en haut.
 *
 * <p>Chaque image, on lit le message que le jeu vient de poser ({@link GuiOverlayAccessor}) : un nouveau message, ou
 * le même posé de nouveau (son compteur repart à 60), entre dans la pile. La couche du jeu qui l'aurait dessiné au-dessus
 * de la barre d'inventaire n'est plus dessinée.
 */
final class IndicesClient {
    private static final ResourceLocation LAYER = ResourceLocation.fromNamespaceAndPath("fallow", "indices");

    /** Marge gauche du filet, hauteur du haut de la pile (part de l'écran), largeur avant retour à la ligne. */
    private static final int LEFT = 8;
    private static final float TOP = 0.40F;
    private static final int MAX_WIDTH = 280;
    private static final int LINE = 10;
    private static final int GAP = 6;

    private static final int PARCHMENT = 0xECE2C8;
    private static final int GOLD = 0xC4A460;
    private static final int SHADE = 0x12100E;
    private static final int SHADE_ALPHA = 135;

    private static final Notes<Component> NOTES = new Notes<>();
    private static Component lastMessage;
    private static int lastTime;

    private IndicesClient() {
    }

    static void register(IEventBus modBus) {
        modBus.addListener(RegisterGuiLayersEvent.class,
                event -> event.registerAbove(VanillaGuiLayers.OVERLAY_MESSAGE, LAYER, IndicesClient::render));
        NeoForge.EVENT_BUS.addListener(RenderGuiLayerEvent.Pre.class, event -> {
            if (FallowConfig.indicesEnabled() && VanillaGuiLayers.OVERLAY_MESSAGE.equals(event.getName())) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            NOTES.clear();
            lastMessage = null;
            lastTime = 0;
        });
    }

    /** Un message nouveau, ou le même posé de nouveau, depuis l'image précédente. */
    private static void watch(long now) {
        GuiOverlayAccessor gui = (GuiOverlayAccessor) Minecraft.getInstance().gui;
        Component message = gui.fallow$overlayMessage();
        int time = gui.fallow$overlayTime();
        if (message != null && time > 0 && (message != lastMessage || time > lastTime)) {
            String text = message.getString();
            if (!text.isBlank()) {
                NOTES.push(message, key(message, text), text, now);
            }
        }
        lastMessage = message;
        lastTime = time;
    }

    private static String key(Component message, String text) {
        return message.getContents() instanceof TranslatableContents translatable ? translatable.getKey() : Notes.literalKey(text);
    }

    private static void render(GuiGraphics graphics, DeltaTracker delta) {
        Minecraft minecraft = Minecraft.getInstance();
        long now = Util.getMillis();
        if (!FallowConfig.indicesEnabled() || minecraft.player == null) {
            return;
        }
        watch(now);
        if (minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }
        List<Notes.Note<Component>> notes = NOTES.visible(now);
        Font font = minecraft.font;
        float y = graphics.guiHeight() * TOP;
        for (int i = 0; i < notes.size(); i++) {
            Notes.Note<Component> note = notes.get(i);
            List<FormattedCharSequence> lines = font.split(note.message, MAX_WIDTH);
            int height = Math.max(1, lines.size()) * LINE - 2;
            float alpha = note.alpha(now);
            if (alpha > 0.02F) {
                draw(graphics, font, note, lines, height, Mth.floor(y), alpha, now);
            }
            y += (height + GAP + 4) * Math.min(1.0F, alpha * 3.0F);
        }
    }

    private static void draw(GuiGraphics graphics, Font font, Notes.Note<Component> note, List<FormattedCharSequence> lines,
            int height, int y, float alpha, long now) {
        ItemStack icon = icon(note.message);
        int textX = LEFT + 6 + (icon.isEmpty() ? 0 : 15);
        int width = 0;
        for (FormattedCharSequence line : lines) {
            width = Math.max(width, font.width(line));
        }
        float slide = (1.0F - note.slide(now)) * -10.0F;
        graphics.pose().pushPose();
        graphics.pose().translate(slide, 0.0F, 0.0F);

        int top = y - 4;
        int bottom = y + height + 3;
        shade(graphics, LEFT, top, textX + width + 36, bottom, alpha);
        graphics.fill(LEFT, top, LEFT + 1, bottom, argb(GOLD, alpha * 0.9F));

        if (!icon.isEmpty() && alpha > 0.4F) {
            graphics.pose().pushPose();
            graphics.pose().translate(LEFT + 5, y + (height - 12) / 2.0F, 0.0F);
            graphics.pose().scale(0.75F, 0.75F, 1.0F);
            graphics.renderItem(icon, 0, 0);
            graphics.pose().popPose();
        }
        int color = argb(PARCHMENT, alpha * 0.95F);
        for (int i = 0; i < lines.size(); i++) {
            graphics.drawString(font, lines.get(i), textX, y + i * LINE, color, true);
        }
        graphics.pose().popPose();
    }

    /** Le fond : sombre contre le filet, transparent vers la droite. */
    private static void shade(GuiGraphics graphics, int x0, int y0, int x1, int y1, float alpha) {
        Matrix4f pose = graphics.pose().last().pose();
        VertexConsumer buffer = graphics.bufferSource().getBuffer(RenderType.gui());
        int dark = argb(SHADE, alpha * SHADE_ALPHA / 255.0F);
        int mid = argb(SHADE, alpha * SHADE_ALPHA * 0.45F / 255.0F);
        int clear = argb(SHADE, 0.0F);
        int xm = x0 + (x1 - x0) * 2 / 5;
        quad(buffer, pose, x0, y0, xm, y1, dark, mid);
        quad(buffer, pose, xm, y0, x1, y1, mid, clear);
        graphics.flush();
    }

    private static void quad(VertexConsumer buffer, Matrix4f pose, int x0, int y0, int x1, int y1, int left, int right) {
        buffer.addVertex(pose, x0, y0, 0.0F).setColor(left);
        buffer.addVertex(pose, x0, y1, 0.0F).setColor(left);
        buffer.addVertex(pose, x1, y1, 0.0F).setColor(right);
        buffer.addVertex(pose, x1, y0, 0.0F).setColor(right);
    }

    private static int argb(int rgb, float alpha) {
        return Mth.clamp(Math.round(alpha * 255.0F), 0, 255) << 24 | rgb;
    }

    /** L'icône d'un message de Fallow, selon son sujet ; rien pour ceux des autres mods. */
    static ItemStack icon(Component message) {
        if (!(message.getContents() instanceof TranslatableContents translatable)) {
            return ItemStack.EMPTY;
        }
        String key = translatable.getKey();
        if (key.startsWith("message.fallow.outils.")) {
            return new ItemStack(switch (key.substring("message.fallow.outils.".length())) {
                case "hache" -> Items.STONE_AXE;
                case "pioche", "trop_faible" -> Items.STONE_PICKAXE;
                case "pelle" -> Items.STONE_SHOVEL;
                case "houe" -> Items.STONE_HOE;
                default -> Items.FLINT;
            });
        }
        if (key.startsWith("approche.fallow.")) {
            return new ItemStack(Items.FEATHER);
        }
        if (key.startsWith("repos.fallow.")) {
            return new ItemStack(Items.CAMPFIRE);
        }
        if (key.startsWith("tannerie.fallow.")) {
            return new ItemStack(Items.LEATHER);
        }
        if (key.startsWith("message.fallow.clairiere.") || key.startsWith("message.fallow.bord.")) {
            return new ItemStack(Items.LANTERN);
        }
        if (key.startsWith("zoo.fallow.")) {
            return new ItemStack(Items.SPYGLASS);
        }
        return ItemStack.EMPTY;
    }
}
