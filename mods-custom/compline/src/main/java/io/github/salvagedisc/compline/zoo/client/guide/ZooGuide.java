package io.github.salvagedisc.compline.zoo.client.guide;

import com.astemir.astemirlib.re.core.math.Easing;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.book.AnimatedBookFlipper;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.book.CategoryTabs;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.CategoryData;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.GuidebookData;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.PageData;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.animation.AppearAnimation;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.animation.ClickAnimation;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.animation.HoverAnimation;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.misc.Sprite;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.AbstractWidget;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.Button;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.Container;
import io.github.salvagedisc.compline.zoo.SpeciesInfo;
import io.github.salvagedisc.compline.zoo.ZooTab;
import io.github.salvagedisc.compline.zoo.client.ZooClient;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

/**
 * Le zoo dans le guide d'Archie's Dinosaurs : une catégorie de plus, glissée avant les crédits, avec une page
 * d'accueil puis les espèces de chaque milieu, sept par double page, mises comme sur la page des tampons des
 * dinosaures (trois à gauche sous la bannière, quatre à droite), quatre tampons chacune. On y entre par un
 * marque-page du bas, en miroir de ceux d'Archie ; des onglets à gauche mènent à chaque milieu. Rien n'est copié du
 * mod : il est relié par ses propres classes ({@code mixin.zoo}), et ses images sont reprises au lancement
 * ({@link ZooTextures}).
 */
public final class ZooGuide {
    static final String CATEGORY_ID = "compline_zoo";
    static final int PER_SPREAD = ZooSpreadWidget.LEFT_ROWS.length + ZooSpreadWidget.RIGHT_ROWS.length;

    static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("ec_ad", "textures/gui/guidebook/badges/background.png");

    private static final float BOOKMARK_SHEET = ZooArt.sheet(ZooArt.BOOKMARK_W, 2);

    private static CategoryData category;
    private static final Map<ZooTab, Integer> firstPage = new EnumMap<>(ZooTab.class);
    private static final Map<ZooTab, List<SpeciesInfo>> sorted = new EnumMap<>(ZooTab.class);
    private static AnimatedBookFlipper flipper;

    private ZooGuide() {
    }

    /** Le guide lu par Archie, avec la catégorie du zoo glissée avant les crédits. */
    public static GuidebookData withZoo(GuidebookData data) {
        if (data == null || ZooClient.species().isEmpty()) {
            category = null;
            return data;
        }
        List<CategoryData> categories = new ArrayList<>(data.categories());
        int at = categories.size();
        for (int i = 0; i < categories.size(); i++) {
            if ("credits".equals(categories.get(i).id())) {
                at = i;
                break;
            }
        }
        categories.add(at, build());
        return new GuidebookData(categories, data.badgesPath());
    }

    private static CategoryData build() {
        CategoryData zoo = new CategoryData(CATEGORY_ID, BACKGROUND, null, null).disableMarkers();
        PageData intro = new PageData(null);
        intro.addElementFactory((width, height) -> new ZooIntroWidget(width, height));
        zoo.addPage(intro);
        firstPage.clear();
        sorted.clear();

        Collator collator = Collator.getInstance(Locale.FRENCH);
        collator.setStrength(Collator.PRIMARY);
        Map<ZooTab, List<SpeciesInfo>> byTab = new EnumMap<>(ZooTab.class);
        for (SpeciesInfo info : ZooClient.species()) {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(info.id())) {
                byTab.computeIfAbsent(info.tab(), tab -> new ArrayList<>()).add(info);
            }
        }
        int page = 1;
        for (ZooTab tab : ZooTab.values()) {
            List<SpeciesInfo> list = byTab.getOrDefault(tab, List.of());
            if (list.isEmpty()) {
                continue;
            }
            list.sort(Comparator.comparing(info -> ZooText.speciesName(info.id()).getString(), collator));
            firstPage.put(tab, page);
            sorted.put(tab, List.copyOf(list));
            int spreads = (list.size() + PER_SPREAD - 1) / PER_SPREAD;
            for (int s = 0; s < spreads; s++) {
                List<SpeciesInfo> chunk = List.copyOf(list.subList(s * PER_SPREAD, Math.min(list.size(), (s + 1) * PER_SPREAD)));
                int number = s + 1;
                PageData data = new PageData(null);
                data.addElementFactory((width, height) -> new ZooSpreadWidget(width, height, tab, chunk, number, spreads));
                zoo.addPage(data);
                page++;
            }
        }
        category = zoo;
        return zoo;
    }

    /** Les espèces d'un milieu. */
    static List<SpeciesInfo> speciesOf(ZooTab tab) {
        return ZooClient.species().stream().filter(info -> info.tab() == tab).toList();
    }

    /** Avant chaque image de l'écran du guide : mesure les bêtes en attente, au plus 6 ms par image. */
    public static void beforeRender() {
        ZooTextures.art();
        Portraits.measurePending(6_000_000L);
    }

    /** Les ressources ont été rechargées : les images du zoo seront recomposées avec elles. */
    public static void resourcesReloaded() {
        ZooTextures.invalidate();
    }

    /** Les espèces d'un milieu dans l'ordre alphabétique des pages. */
    static List<SpeciesInfo> sortedSpecies(ZooTab tab) {
        return sorted.getOrDefault(tab, List.of());
    }

    static boolean hasTab(ZooTab tab) {
        return firstPage.containsKey(tab);
    }

    /** À l'ouverture par la touche du zoo : le livre tourne jusqu'à l'accueil du zoo. */
    public static void openOnZoo() {
        flipTo(null);
    }

    /** Tourne les pages jusqu'au début d'un milieu ({@code null} : la page d'accueil du zoo). */
    static boolean flipTo(ZooTab tab) {
        if (flipper == null || category == null) {
            return false;
        }
        int page = tab == null ? 0 : firstPage.getOrDefault(tab, 0);
        return flipper.flipToCategory(category, page);
    }

    /**
     * Ajoute aux onglets d'Archie le marque-page du zoo (en bas, à droite du fermoir, en miroir de ceux d'Archie et loin de ses flèches) et les onglets des
     * milieux (à gauche du livre, visibles sur les pages du zoo). Appelé à la fin du constructeur de {@link CategoryTabs}.
     */
    public static void decorate(CategoryTabs tabs, AnimatedBookFlipper book, Map<CategoryData, AbstractWidget> subTabs) {
        flipper = book;
        if (category == null) {
            return;
        }
        ZooTextures.art();
        tabs.addChild(Button.builder(320.0F, 272.0F, 32.0F, 48.0F)
                .sprites(Sprite.of(ZooTextures.BOOKMARK, 0.0F, 0.0F, 32.0F, 48.0F, BOOKMARK_SHEET, 48.0F),
                        Sprite.of(ZooTextures.BOOKMARK, ZooArt.at(1, ZooArt.BOOKMARK_W), 0.0F, 32.0F, 48.0F, BOOKMARK_SHEET, 48.0F))
                .appearAnimation(AppearAnimation.verticalBottomSlide(0.3F, Easing.Simple.SINE_OUT))
                .clickAnimation(ClickAnimation.ding(10.0F, 3.0F, 0.1F, Easing.Simple.SINE_OUT))
                .clicked((context, element) -> {
                    context.playSound(SoundEvents.BOOK_PAGE_TURN, 1.0F, 1.0F);
                    if (book.isOnFakePage()) {
                        book.flipBackFromFake(false);
                    }
                    return flipTo(null);
                })
                .build());

        Container container = tabs.addChild(Container.of(0.0F, 0.0F, tabs.bounds().width(), tabs.bounds().height()));
        int row = 0;
        for (ZooTab tab : ZooTab.values()) {
            if (hasTab(tab)) {
                container.addChild(sideTab(row++, tab.ordinal() + 1, tab));
            }
        }
        container.setVisible(false);
        subTabs.put(category, container);
    }

    /** Un onglet de milieu, à la place et aux mesures de ceux des dinosaures (64 x 32, tous les 48, dès 32). */
    private static Button sideTab(int row, int sprite, ZooTab tab) {
        float w = ZooArt.TAB_W;
        float h = ZooArt.TAB_H;
        float sheetW = ZooArt.sheet(ZooArt.TAB_W, 2);
        float sheetH = ZooArt.sheet(ZooArt.TAB_H, ZooArt.VARIANTS);
        return Button.builder(0.0F, 32.0F + row * 48.0F, w, h)
                .sprites(Sprite.of(ZooTextures.TABS, 0.0F, ZooArt.at(sprite, ZooArt.TAB_H), w, h, sheetW, sheetH),
                        Sprite.of(ZooTextures.TABS, ZooArt.at(1, ZooArt.TAB_W), ZooArt.at(sprite, ZooArt.TAB_H), w, h, sheetW, sheetH))
                .hitbox(0.0F, 0.0F, 48.0F, h)
                .hoverAnimation(HoverAnimation.leftSlide(48.0F, 8.0F, 0.3F, Easing.Simple.SINE_OUT))
                .appearAnimation(AppearAnimation.leftShift(48.0F, 0.1F, Easing.Simple.SINE_OUT))
                .clicked((context, element) -> flipTo(tab))
                .build();
    }
}
