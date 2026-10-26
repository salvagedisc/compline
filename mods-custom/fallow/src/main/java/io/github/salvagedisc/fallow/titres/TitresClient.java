package io.github.salvagedisc.fallow.titres;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

/** La plaque des titres et le choix des écrans qui la portent. Chargée seulement chez le client. */
public final class TitresClient {
    private static final ResourceLocation PLAQUE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "plaque");
    private static final ResourceLocation TOPS_FILE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "plaque.json");
    private static final String VANILLA_SCREENS = "net.minecraft.client.gui.screens.inventory.";
    /** Hauteur de la plaque ; ses quatre dernières lignes sont son bord bas. */
    private static final int HEIGHT = 18;
    /** Lignes du panneau que la plaque recouvre, à partir de son bord haut. */
    private static final int OVERLAP = 3;
    /** Marge de chaque côté du titre : les rivets et un peu d'air. */
    private static final int PADDING = 12;
    private static final int TEXT_Y = 5;
    /** L'encre mousse du pack d'interface (TITLE_INK de build.py). */
    private static final int INK = 0xFF3C4C38;

    /** Le bord haut du panneau de chaque écran, par nom de classe (assets/fallow/plaque.json). */
    static final Tops TOPS = new Tops();

    private TitresClient() {
    }

    /** Les écrans vanilla, l'inventaire du mode créatif excepté (ses onglets occupent le haut). */
    private static boolean isVanilla(AbstractContainerScreen<?> screen) {
        Class<?> type = screen.getClass();
        return type.getName().startsWith(VANILLA_SCREENS) && !(screen instanceof CreativeModeInventoryScreen);
    }

    /** L'écran n'écrit plus ses titres : ceux des écrans vanilla. */
    public static boolean hidesLabels(AbstractContainerScreen<?> screen) {
        return FallowConfig.titresEnabled() && isVanilla(screen);
    }

    /** La plaque, sur les mêmes écrans sauf l'inventaire du joueur, qui n'a pas de titre à montrer. */
    private static boolean hasPlaque(AbstractContainerScreen<?> screen) {
        return hidesLabels(screen) && !(screen instanceof InventoryScreen);
    }

    /** Le titre de l'écran ; celui du villageois porte aussi son niveau, comme le jeu l'écrit. */
    private static Component title(AbstractContainerScreen<?> screen) {
        if (screen instanceof MerchantScreen merchant) {
            int level = merchant.getMenu().getTraderLevel();
            if (level > 0 && level <= 5 && merchant.getMenu().showProgressBar()) {
                return Component.translatable("merchant.title", screen.getTitle(),
                        Component.translatable("merchant.level." + level));
            }
        }
        return screen.getTitle();
    }

    static void onBackground(ContainerScreenEvent.Render.Background event) {
        AbstractContainerScreen<?> screen = event.getContainerScreen();
        if (!hasPlaque(screen)) {
            return;
        }
        Component title = title(screen);
        if (title.getString().isBlank()) {
            return;
        }
        Font font = Minecraft.getInstance().font;
        GuiGraphics graphics = event.getGuiGraphics();
        int width = font.width(title) + 2 * PADDING;
        int x = screen.getGuiLeft() + (screen.getXSize() - width) / 2;
        int y = screen.getGuiTop() + TOPS.top(screen) + OVERLAP - HEIGHT;
        graphics.blitSprite(PLAQUE, x, y, width, HEIGHT);
        graphics.drawString(font, title, x + PADDING, y + TEXT_Y, INK, false);
    }

    /** Relit {@code assets/fallow/plaque.json} à chaque rechargement des ressources. */
    static final class Tops implements ResourceManagerReloadListener {
        private Map<String, Integer> tops = Map.of();

        int top(AbstractContainerScreen<?> screen) {
            return tops.getOrDefault(screen.getClass().getSimpleName(), 0);
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            Map<String, Integer> read = new HashMap<>();
            manager.getResource(TOPS_FILE).ifPresent(resource -> {
                try (Reader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    json.entrySet().forEach(e -> read.put(e.getKey(), e.getValue().getAsInt()));
                } catch (Exception e) {
                    Fallow.LOGGER.warn("titres : {} illisible, plaques au bord des écrans", TOPS_FILE, e);
                }
            });
            tops = Map.copyOf(read);
        }
    }
}
