package io.github.salvagedisc.fallow.patine.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.salvagedisc.fallow.Fallow;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

/**
 * Les textures d'armure salies, faites à la demande à partir de la texture d'origine (celle du jeu ou d'un mod) et
 * gardées jusqu'au prochain rechargement des ressources. La peinture elle-même est dans {@link ArmorWear} : la terre,
 * le sable, la neige et l'eau, chacun en quelques paliers qui s'étendent sur le même motif.
 */
public final class PatineTextures {
    private static final Map<ResourceLocation, Map<Integer, ResourceLocation>> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();

    private PatineTextures() {
    }

    /** La clé d'un état visible : paliers de terre, de sable, de neige et d'eau, deux bits chacun. */
    public static int key(int earth, int sand, int snow, int wet) {
        return earth | sand << 2 | snow << 4 | wet << 6;
    }

    /**
     * La texture à dessiner pour une armure dans cet état (clé 0 : l'originale).
     *
     * @param tinted la couche est teinte par le jeu (le cuir) : seulement la terre et l'eau qui fonce
     * @param shape pour la couche non teinte d'une armure teinte, la planche teinte : le sable, la neige et les gouttes
     *        se posent sur toute la matière de l'armure ; sinon {@code null}
     */
    public static ResourceLocation get(ResourceLocation original, int key, boolean tinted, ResourceLocation shape) {
        if (key == 0 || FAILED.contains(original)) {
            return original;
        }
        int full = key | (tinted ? 1 << 8 : 0) | (shape != null ? 1 << 9 : 0);
        Map<Integer, ResourceLocation> made = CACHE.computeIfAbsent(original, k -> new HashMap<>());
        ResourceLocation id = made.get(full);
        if (id == null) {
            id = make(original, full, tinted, shape);
            if (id == null) {
                FAILED.add(original);
                return original;
            }
            made.put(full, id);
        }
        return id;
    }

    static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        for (Map<Integer, ResourceLocation> made : CACHE.values()) {
            for (ResourceLocation id : made.values()) {
                minecraft.execute(() -> minecraft.getTextureManager().release(id));
            }
        }
        CACHE.clear();
        FAILED.clear();
    }

    private static NativeImage read(ResourceLocation id) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(id);
        if (resource.isEmpty()) {
            return null;
        }
        try (InputStream in = resource.get().open()) {
            return NativeImage.read(in);
        } catch (IOException e) {
            Fallow.LOGGER.warn("patine : texture illisible {}", id, e);
            return null;
        }
    }

    private static ResourceLocation make(ResourceLocation original, int full, boolean tinted, ResourceLocation shapeId) {
        NativeImage image = read(original);
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = image.getPixelRGBA(x, y);
            }
        }
        int earth = full & 3;
        int sand = full >> 2 & 3;
        int snow = full >> 4 & 3;
        int wet = full >> 6 & 3;
        int seed = original.toString().hashCode();
        int[] shape = shapeId == null ? null : shapeOf(shapeId, width, height);
        if (shape != null) {
            seed = shapeId.toString().hashCode();
            ArmorWear.paintOver(pixels, shape, width, height, seed, sand, snow, wet);
        } else {
            ArmorWear.paint(pixels, width, height, seed, tinted, original.getPath().contains("leather"), earth, sand, snow, wet);
        }
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixelRGBA(x, y, pixels[y * width + x]);
            }
        }
        String path = original.getNamespace() + "/" + original.getPath().replaceAll("[^a-z0-9/._-]", "_");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "patine/" + path + "/" + full);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
        return id;
    }

    /** La planche teinte, si elle a la même taille que la couche à peindre. */
    private static int[] shapeOf(ResourceLocation id, int width, int height) {
        NativeImage image = read(id);
        if (image == null) {
            return null;
        }
        try (image) {
            if (image.getWidth() != width || image.getHeight() != height) {
                return null;
            }
            int[] shape = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    shape[y * width + x] = image.getPixelRGBA(x, y);
                }
            }
            return shape;
        }
    }
}
