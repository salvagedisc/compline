package io.github.salvagedisc.fallow.zoo.client.guide;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.salvagedisc.fallow.Fallow;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * Les images du zoo dans le jeu : composées par {@link ZooArt} à la première ouverture du guide, à partir des
 * ressources chargées (celles d'Archie, d'Exposure et du jeu, resource packs compris), puis gardées en textures
 * jusqu'au prochain rechargement des ressources.
 */
final class ZooTextures {
    static final ResourceLocation STAMPS = id("stamps");
    static final ResourceLocation BOOKMARK = id("bookmark");
    static final ResourceLocation TABS = id("tabs");
    static final ResourceLocation BANNER = id("banner");
    static final ResourceLocation RIBBONS = id("ribbons");
    static final ResourceLocation FRAMES = id("frames");
    /**
     * Les marque-pages d'Archie, remplacés en mémoire par une copie dont les œufs des onglets de droite sont alignés
     * (son fichier n'est pas touché ; un rechargement des ressources le relit).
     */
    private static final ResourceLocation ARCHIE_MARKERS_OFF =
            ResourceLocation.fromNamespaceAndPath("ec_ad", "textures/gui/guidebook/markers/off.png");
    private static final ResourceLocation ARCHIE_MARKERS_ON =
            ResourceLocation.fromNamespaceAndPath("ec_ad", "textures/gui/guidebook/markers/on.png");

    private static ZooArt.Art art;
    private static boolean failed;

    private ZooTextures() {
    }

    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "dynamic/zoo/" + name);
    }

    /** Les images composées, ou {@code null} si elles n'ont pas pu l'être (le guide écrit alors sans elles). */
    static ZooArt.Art art() {
        if (art == null && !failed) {
            try {
                art = ZooArt.compose(ZooTextures::load);
                register(STAMPS, art.stamps());
                register(BOOKMARK, art.bookmark());
                register(TABS, art.tabs());
                register(BANNER, art.banner());
                register(RIBBONS, art.ribbons());
                register(FRAMES, art.frames());
                register(ARCHIE_MARKERS_OFF, art.markersOff());
                register(ARCHIE_MARKERS_ON, art.markersOn());
            } catch (RuntimeException error) {
                Fallow.LOGGER.warn("zoo : images du guide impossibles à composer", error);
                art = null;
                failed = true;
            }
        }
        return art;
    }

    /** Oublie les images : elles seront recomposées avec les ressources rechargées. */
    static void invalidate() {
        art = null;
        failed = false;
    }

    private static ZooArt.Img load(String namespace, String path) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager()
                .getResource(ResourceLocation.fromNamespaceAndPath(namespace, path));
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            ZooArt.Img img = new ZooArt.Img(image.getWidth(), image.getHeight());
            for (int y = 0; y < img.height; y++) {
                for (int x = 0; x < img.width; x++) {
                    img.set(x, y, swap(image.getPixelRGBA(x, y)));
                }
            }
            return img;
        } catch (Exception error) {
            Fallow.LOGGER.debug("zoo : lecture de {}:{} impossible", namespace, path, error);
            return null;
        }
    }

    private static void register(ResourceLocation id, ZooArt.Img img) {
        NativeImage image = new NativeImage(img.width, img.height, true);
        for (int y = 0; y < img.height; y++) {
            for (int x = 0; x < img.width; x++) {
                image.setPixelRGBA(x, y, swap(img.get(x, y)));
            }
        }
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
    }

    /** ARGB vers ABGR (l'ordre de {@link NativeImage}) et retour. */
    private static int swap(int color) {
        return (color & 0xFF00FF00) | ((color >> 16) & 0xFF) | ((color & 0xFF) << 16);
    }
}
