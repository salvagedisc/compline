package io.github.salvagedisc.compline.patine.client;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.salvagedisc.compline.Compline;
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
import net.minecraft.util.FastColor;

/**
 * Les textures d'armure salies, faites à la demande à partir de la texture d'origine (celle du jeu ou d'un mod) et
 * gardées jusqu'au prochain rechargement des ressources. Trois paliers, qui s'étendent sur le même motif : la terre
 * gagne d'abord les pièces basses, en taches au pixel près, en deux tons ; sur le métal nu, quelques points de
 * rouille au dernier palier.
 */
public final class PatineTextures {
    private static final Map<ResourceLocation, ResourceLocation[]> CACHE = new HashMap<>();
    private static final Set<ResourceLocation> FAILED = new HashSet<>();
    /** Seuil du bruit au-dessus duquel la terre tient, par palier : plus bas, plus de surface couverte. */
    private static final float[] THRESHOLD = {0.74F, 0.60F, 0.46F};

    private PatineTextures() {
    }

    /** La texture à dessiner pour une armure de ce palier (0 : l'originale). */
    public static ResourceLocation get(ResourceLocation original, int level) {
        if (level <= 0 || FAILED.contains(original)) {
            return original;
        }
        ResourceLocation[] levels = CACHE.computeIfAbsent(original, key -> new ResourceLocation[THRESHOLD.length]);
        if (levels[level - 1] == null) {
            ResourceLocation made = make(original, level);
            if (made == null) {
                FAILED.add(original);
                return original;
            }
            levels[level - 1] = made;
        }
        return levels[level - 1];
    }

    static void clear() {
        Minecraft minecraft = Minecraft.getInstance();
        for (ResourceLocation[] levels : CACHE.values()) {
            for (ResourceLocation id : levels) {
                if (id != null) {
                    minecraft.execute(() -> minecraft.getTextureManager().release(id));
                }
            }
        }
        CACHE.clear();
        FAILED.clear();
    }

    private static ResourceLocation make(ResourceLocation original, int level) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(original);
        if (resource.isEmpty()) {
            return null;
        }
        NativeImage image;
        try (InputStream in = resource.get().open()) {
            image = NativeImage.read(in);
        } catch (IOException e) {
            Compline.LOGGER.warn("patine : texture illisible {}", original, e);
            return null;
        }
        soil(image, original, level);
        String path = original.getNamespace() + "/" + original.getPath().replaceAll("[^a-z0-9/._-]", "_");
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "patine/" + path + "/" + level);
        Minecraft.getInstance().getTextureManager().register(id, new DynamicTexture(image));
        return id;
    }

    /** Salit l'image en place. Le motif dépend du nom de la texture : tout le monde voit la même armure. */
    static void soil(NativeImage image, ResourceLocation original, int level) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scale = Math.max(1.0F, width / 64.0F);
        int seed = original.toString().hashCode();
        float threshold = THRESHOLD[level - 1];
        boolean leather = original.getPath().contains("leather");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int c = image.getPixelRGBA(x, y);
                int a = FastColor.ABGR32.alpha(c);
                if (a == 0) {
                    continue;
                }
                int px = (int) (x / scale);
                int py = (int) (y / scale);
                // Des taches de trois pixels environ, frangées au pixel ; plus sur les jambes et les bottes, et dans
                // chaque face vers le bas, moins sur le casque.
                float n = 0.62F * blob(px / 3.0F, py / 3.0F, seed) + 0.38F * hash(px, py, seed + 17);
                n += lowness(px, py);
                if (n < threshold) {
                    continue;
                }
                float r = FastColor.ABGR32.red(c);
                float g = FastColor.ABGR32.green(c);
                float b = FastColor.ABGR32.blue(c);
                float max = Math.max(r, Math.max(g, b));
                float min = Math.min(r, Math.min(g, b));
                boolean bareMetal = !leather && max > 110 && (max - min) < 0.18F * max;
                if (bareMetal && level == 3 && blob(px / 2.0F, py / 2.0F, seed + 91) > 0.74F && hash(px, py, seed + 5) > 0.35F) {
                    // De petits amas de rouille.
                    r = mix(r, 124, 0.7F);
                    g = mix(g, 70, 0.7F);
                    b = mix(b, 40, 0.7F);
                } else if (n > threshold + 0.09F) {
                    // Le cœur de la tache : de la terre, qui couvre la couleur de l'armure.
                    r = mix(r, 98 + 0.15F * r, 0.75F);
                    g = mix(g, 80 + 0.15F * g, 0.75F);
                    b = mix(b, 58 + 0.15F * b, 0.75F);
                } else {
                    // Son bord : l'armure seulement ternie et brunie.
                    r = mix(r, r * 0.72F + 10, 0.55F);
                    g = mix(g, g * 0.64F + 8, 0.55F);
                    b = mix(b, b * 0.52F + 5, 0.55F);
                }
                image.setPixelRGBA(x, y, FastColor.ABGR32.color(a, (int) b, (int) g, (int) r));
            }
        }
    }

    /**
     * Ce qui pousse la terre vers le bas, sur une planche d'armure du jeu (64 × 32) : les jambes et les bottes en bas à
     * gauche, le casque en haut ; dans une face des jambes, plus on descend, plus il y en a.
     */
    private static float lowness(int x, int y) {
        if (y < 16) {
            return -0.06F;
        }
        if (x < 16) {
            return 0.08F + 0.10F * Math.min(1.0F, Math.max(0.0F, (y - 20) / 12.0F));
        }
        return 0.03F;
    }

    private static float mix(float a, float b, float t) {
        return a + (b - a) * t;
    }

    /** Bruit de valeur lissé sur une grille. */
    private static float blob(float x, float y, int seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fx = x - ix;
        float fy = y - iy;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        float top = mix(hash(ix, iy, seed), hash(ix + 1, iy, seed), fx);
        float bottom = mix(hash(ix, iy + 1, seed), hash(ix + 1, iy + 1, seed), fx);
        return mix(top, bottom, fy);
    }

    private static float hash(int x, int y, int seed) {
        int h = x * 374761393 + y * 668265263 + seed * 1442695041;
        h = (h ^ (h >>> 13)) * 1274126177;
        h ^= h >>> 16;
        return (h & 0xFFFFFF) / (float) 0x1000000;
    }
}
