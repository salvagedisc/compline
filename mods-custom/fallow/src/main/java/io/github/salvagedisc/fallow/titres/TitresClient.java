package io.github.salvagedisc.fallow.titres;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import java.io.Reader;
import java.lang.reflect.Method;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;

/**
 * Le nom de chaque écran, centré sur son bord haut, sur une plaque qui dépasse du panneau, en
 * lettres gravées. Tout l'aspect vient du pack d'interface : la plaque de chaque famille de
 * couleurs ({@code fallow:titre/<famille>}, en neuf parties), les lettres
 * ({@code fallow:textures/gui/titre/lettres.png}) et la table {@code fallow:titres.json} (familles,
 * encres, bord haut des panneaux, place des lettres). Chargée seulement chez le client.
 */
public final class TitresClient {
    private static final ResourceLocation TABLE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "titres.json");
    private static final ResourceLocation LETTRES = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "textures/gui/titre/lettres.png");
    private static final String VANILLA = "net.minecraft.";
    /** Hauteur de la plaque, marge de part et d'autre du nom, ligne des lettres (accents compris). */
    private static final int HEIGHT = 14;
    private static final int PADDING = 13;
    private static final int TEXT_TOP = 3;
    /** Lignes de la plaque au-dessus du bord haut du panneau, si la table n'en dit pas plus. */
    private static final int RAISE = 3;

    static final Table TABLE_LISTENER = new Table();
    private static final ClassValue<Boolean> OWN_LABELS = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            return writesLabelsItself(type);
        }
    };

    private TitresClient() {
    }

    /**
     * L'écran écrit-il ses libellés par une méthode d'un mod ? On laisse alors son titre où il le
     * met, sans plaque : seuls les écrans dont les libellés viennent du jeu prennent la plaque.
     */
    private static boolean writesLabelsItself(Class<?> type) {
        for (Class<?> c = type; c != null && c != AbstractContainerScreen.class; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals("renderLabels") && m.getParameterCount() == 3
                        && m.getParameterTypes()[0] == GuiGraphics.class) {
                    return !c.getName().startsWith(VANILLA);
                }
            }
        }
        return false;
    }

    /**
     * L'écran porte-t-il la plaque ? Pas l'inventaire du joueur ni le créatif, dont le haut est pris ;
     * l'écran d'un mod seulement si la table nomme son mod, dont le pack a vu le panneau.
     */
    private static boolean hasPlaque(AbstractContainerScreen<?> screen) {
        return FallowConfig.titresEnabled() && TABLE_LISTENER.loaded() && !(screen instanceof CreativeModeInventoryScreen)
                && !(screen instanceof InventoryScreen) && !OWN_LABELS.get(screen.getClass())
                && !TABLE_LISTENER.excluded(screen)
                && (screen.getClass().getName().startsWith(VANILLA) || TABLE_LISTENER.modFamily(screen) != null);
    }

    /** L'écran n'écrit plus ses libellés : ceux qui portent la plaque, et l'inventaire du joueur. */
    public static boolean hidesLabels(AbstractContainerScreen<?> screen) {
        return hasPlaque(screen)
                || (FallowConfig.titresEnabled() && TABLE_LISTENER.loaded() && screen instanceof InventoryScreen);
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
        String name = title(screen).getString().strip().toUpperCase(Locale.ROOT);
        if (name.isEmpty()) {
            return;
        }
        Table table = TABLE_LISTENER;
        Screen place = table.place(screen);
        int textWidth = table.width(name);
        int width = textWidth + 2 * PADDING;
        int centre = place.centre() >= 0 ? place.centre() : screen.getXSize() / 2;
        int x = screen.getGuiLeft() + centre - width / 2;
        int y = screen.getGuiTop() + place.top() - place.raise();
        GuiGraphics graphics = event.getGuiGraphics();
        Family family = table.family(place.family());
        // la plaque et l'ombre qu'elle porte, un pixel en bas à droite
        graphics.blitSprite(family.plaque(), x, y, width + 1, HEIGHT + 1);
        int tx = x + PADDING;
        int ty = y + TEXT_TOP;
        // la lumière sous chaque trait, puis l'encre par-dessus : la gravure de GUI Revision
        table.write(graphics, name, tx, ty + 1, family.light());
        table.write(graphics, name, tx, ty, family.ink());
        graphics.setColor(1, 1, 1, 1);
    }

    record Family(ResourceLocation plaque, int ink, int light) {
    }

    /** Où va la plaque d'un écran : bord haut du panneau dans l'image, lignes au-dessus, centre, famille. */
    record Screen(int top, int raise, int centre, String family) {
    }

    /** La table du pack, relue à chaque rechargement des ressources. */
    static final class Table implements ResourceManagerReloadListener {
        private Map<String, Screen> screens = Map.of();
        private Map<String, String> mods = Map.of();
        private Map<String, Family> families = Map.of();
        private Map<Integer, int[]> letters = Map.of();
        private java.util.Set<String> excluded = java.util.Set.of();
        private int atlasWidth = 1;
        private int letterHeight = 7;
        private int space = 3;

        /** La table du pack est-elle là ? Sans elle, les écrans gardent les libellés du jeu. */
        boolean loaded() {
            return !families.isEmpty();
        }

        boolean excluded(AbstractContainerScreen<?> screen) {
            return excluded.contains(screen.getClass().getSimpleName());
        }

        Screen place(AbstractContainerScreen<?> screen) {
            Screen own = screens.get(screen.getClass().getSimpleName());
            if (own != null) {
                return own;
            }
            String family = modFamily(screen);
            return new Screen(0, RAISE, -1, family != null ? family : "brume");
        }

        /** La famille que la table donne au mod de l'écran (par son menu), ou null. */
        String modFamily(AbstractContainerScreen<?> screen) {
            try {
                MenuType<?> type = screen.getMenu().getType();
                ResourceLocation key = type == null ? null : BuiltInRegistries.MENU.getKey(type);
                if (key != null) {
                    return mods.getOrDefault(key.toString(), mods.get(key.getNamespace()));
                }
            } catch (UnsupportedOperationException e) {
                // un menu sans type (l'inventaire du joueur)
            }
            return null;
        }

        Family family(String name) {
            Family f = families.get(name);
            return f != null ? f : families.getOrDefault("brume",
                    new Family(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "titre/brume"), 0xFF525C52, 0xFFE8E5D0));
        }

        private int[] letter(int cp) {
            int[] l = letters.get(cp);
            if (l == null) {
                // une lettre accentuée que les lettres n'ont pas : sa lettre de base
                String base = Normalizer.normalize(new String(Character.toChars(cp)), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "");
                if (!base.isEmpty()) {
                    l = letters.get(base.codePointAt(0));
                }
            }
            return l;
        }

        int width(String text) {
            int x = 0;
            for (int cp : text.codePoints().toArray()) {
                if (cp == ' ') {
                    x += space + 1;
                    continue;
                }
                int[] l = letter(cp);
                if (l != null) {
                    x += l[1] + 1;
                }
            }
            return Math.max(x - 1, 0);
        }

        void write(GuiGraphics graphics, String text, int x, int y, int colour) {
            graphics.setColor((colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f, (colour & 255) / 255f, 1);
            for (int cp : text.codePoints().toArray()) {
                if (cp == ' ') {
                    x += space + 1;
                    continue;
                }
                int[] l = letter(cp);
                if (l == null) {
                    continue;
                }
                graphics.blit(LETTRES, x, y, l[0], 0, l[1], letterHeight, atlasWidth, letterHeight);
                x += l[1] + 1;
            }
        }

        @Override
        public void onResourceManagerReload(ResourceManager manager) {
            Map<String, Screen> readScreens = new HashMap<>();
            Map<String, String> readMods = new HashMap<>();
            Map<String, Family> readFamilies = new HashMap<>();
            Map<Integer, int[]> readLetters = new HashMap<>();
            java.util.Set<String> readExcluded = new java.util.HashSet<>();
            manager.getResource(TABLE).ifPresent(resource -> {
                try (Reader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    json.getAsJsonObject("ecrans").entrySet().forEach(e -> {
                        JsonObject s = e.getValue().getAsJsonObject();
                        readScreens.put(e.getKey(), new Screen(intOr(s, "haut", 0), intOr(s, "dessus", RAISE),
                                intOr(s, "centre", -1), s.has("famille") ? s.get("famille").getAsString() : "brume"));
                    });
                    json.getAsJsonObject("mods").entrySet().forEach(e -> readMods.put(e.getKey(), e.getValue().getAsString()));
                    json.getAsJsonObject("familles").entrySet().forEach(e -> {
                        JsonObject f = e.getValue().getAsJsonObject();
                        readFamilies.put(e.getKey(), new Family(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "titre/" + e.getKey()),
                                colour(f.get("encre")), colour(f.get("lumiere"))));
                    });
                    JsonObject l = json.getAsJsonObject("lettres");
                    atlasWidth = l.get("largeur").getAsInt();
                    letterHeight = l.get("hauteur").getAsInt();
                    space = l.get("espace").getAsInt();
                    l.getAsJsonObject("table").entrySet().forEach(e -> readLetters.put(e.getKey().codePointAt(0),
                            new int[] {e.getValue().getAsJsonArray().get(0).getAsInt(), e.getValue().getAsJsonArray().get(1).getAsInt()}));
                    json.getAsJsonArray("sans").forEach(e -> readExcluded.add(e.getAsString()));
                } catch (Exception e) {
                    Fallow.LOGGER.warn("titres : {} illisible, noms sans lettres", TABLE, e);
                }
            });
            screens = Map.copyOf(readScreens);
            mods = Map.copyOf(readMods);
            families = Map.copyOf(readFamilies);
            letters = Map.copyOf(readLetters);
            excluded = java.util.Set.copyOf(readExcluded);
        }

        private static int intOr(JsonObject o, String key, int fallback) {
            return o.has(key) ? o.get(key).getAsInt() : fallback;
        }

        private static int colour(JsonElement e) {
            return 0xFF000000 | Integer.parseInt(e.getAsString().substring(1), 16);
        }
    }
}
