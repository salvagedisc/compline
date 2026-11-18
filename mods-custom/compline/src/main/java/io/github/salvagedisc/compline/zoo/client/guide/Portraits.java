package io.github.salvagedisc.compline.zoo.client.guide;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.salvagedisc.compline.Compline;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Les bêtes dessinées dans le guide : une bête de chaque espèce, créée pour l'occasion (jamais ajoutée au monde),
 * vue de trois quarts. Une espèce pas encore aperçue est dessinée à l'encre, en silhouette.
 *
 * <p>Pour que chacune remplisse sa case sans en sortir, on la mesure une fois : elle est dessinée hors écran, à une
 * échelle connue, et on relève le rectangle de ses pixels (cornes, queue et nageoires comprises, ce que la boîte de
 * collision ne dit pas). Son échelle et sa place dans la case en découlent. Tant qu'elle n'est pas mesurée, la boîte
 * de collision sert d'estimation.
 */
final class Portraits {
    /** Taille de l'image de mesure, et échelle de départ qui y fait tenir une bête d'un bloc avec de la marge. */
    private static final int CANVAS = 256;
    private static final float PROBE = 64.0F;

    /**
     * Ce qu'occupe une bête autour de son point d'ancrage, pour une échelle de 1 : gauche, droite, haut, bas, et la
     * plus grande distance en losange (|dx| + |dy|) d'un de ses pixels au centre de ce rectangle.
     */
    private record Extent(float left, float right, float top, float bottom, float reach) {
    }

    private static final Map<ResourceLocation, LivingEntity> cache = new HashMap<>();
    private static final Map<ResourceLocation, Extent> measured = new HashMap<>();
    private static final Set<ResourceLocation> broken = new HashSet<>();
    private static final ArrayDeque<ResourceLocation> pending = new ArrayDeque<>();
    private static ClientLevel cachedLevel;
    private static TextureTarget canvas;

    private Portraits() {
    }

    /** Monde quitté : les bêtes d'exemple appartiennent à son niveau, on les lâche. */
    static void forgetWorld() {
        cache.clear();
        cachedLevel = null;
        pending.clear();
    }

    /** Ressources rechargées : les modèles ont pu changer, chaque bête sera remesurée. */
    static void forgetMeasures() {
        measured.clear();
        broken.clear();
        pending.clear();
    }

    private static Quaternionf pose() {
        return new Quaternionf().rotateZ((float) Math.PI).mul(camera());
    }

    private static Quaternionf camera() {
        return new Quaternionf().rotateX((float) Math.toRadians(12.0));
    }

    private static LivingEntity sample(ResourceLocation id) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        if (level != cachedLevel) {
            cache.clear();
            cachedLevel = level;
        }
        if (broken.contains(id)) {
            return null;
        }
        return cache.computeIfAbsent(id, key -> {
            try {
                EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(key);
                Entity entity = type.create(level);
                if (entity instanceof LivingEntity living) {
                    living.setYBodyRot(200.0F);
                    living.setYRot(200.0F);
                    living.setYHeadRot(200.0F);
                    living.yHeadRotO = 200.0F;
                    living.yBodyRotO = 200.0F;
                    living.setXRot(0.0F);
                    return living;
                }
            } catch (Throwable error) {
                Compline.LOGGER.debug("zoo : portrait de {} impossible", key, error);
            }
            broken.add(key);
            return null;
        });
    }

    /**
     * Dessine l'espèce dans la boîte ({@code x}, {@code y}, {@code width}, {@code height}), centrée et aussi grande
     * qu'elle y tient sans qu'aucun de ses pixels ne sorte du losange de demi-diagonale {@code diamond} centré sur la
     * boîte ; faux si elle n'a pas de portrait.
     */
    static boolean draw(GuiGraphics graphics, ResourceLocation id, float x, float y, float width, float height, float diamond,
            boolean ink) {
        LivingEntity entity = sample(id);
        if (entity == null) {
            return false;
        }
        Extent extent = measured.get(id);
        if (extent == null) {
            if (!pending.contains(id)) {
                pending.add(id);
            }
            extent = estimate(entity);
        }
        float scale = Math.min(Math.min(width / (extent.right() - extent.left()), height / (extent.bottom() - extent.top())),
                diamond / extent.reach());
        float anchorX = x + width / 2.0F - scale * (extent.left() + extent.right()) / 2.0F;
        float anchorY = y + height / 2.0F - scale * (extent.top() + extent.bottom()) / 2.0F;
        if (ink) {
            RenderSystem.setShaderColor(0.30F, 0.21F, 0.09F, 1.0F);
        }
        try {
            InventoryScreen.renderEntityInInventory(graphics, anchorX, anchorY, scale, new Vector3f(), pose(), camera(), entity);
        } catch (Throwable error) {
            Compline.LOGGER.debug("zoo : dessin de {} impossible", id, error);
            broken.add(id);
            cache.remove(id);
            return false;
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }
        return true;
    }

    /** L'estimation d'avant la mesure : la boîte de collision, un peu élargie par le trois quarts. */
    private static Extent estimate(LivingEntity entity) {
        float height = Math.max(entity.getBbHeight(), 0.2F);
        float width = Math.max(entity.getBbWidth(), 0.2F) * 0.8F;
        return new Extent(-width, width, -height, 0.0F, width + height / 2.0F);
    }

    /**
     * Mesure les bêtes vues dans le guide et pas encore mesurées, dans la limite de {@code budgetNanos}. Appelé avant
     * le dessin de l'écran du guide, quand rien d'autre n'est en cours de rendu.
     */
    static void measurePending(long budgetNanos) {
        if (pending.isEmpty()) {
            return;
        }
        long end = System.nanoTime() + budgetNanos;
        Minecraft minecraft = Minecraft.getInstance();
        if (canvas == null) {
            canvas = new TextureTarget(CANVAS, CANVAS, true, Minecraft.ON_OSX);
        }
        RenderSystem.backupProjectionMatrix();
        try {
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, CANVAS, CANVAS, 0.0F, 1000.0F, 21000.0F),
                    VertexSorting.ORTHOGRAPHIC_Z);
            while (!pending.isEmpty() && System.nanoTime() < end) {
                ResourceLocation id = pending.poll();
                LivingEntity entity = sample(id);
                if (entity != null && !measured.containsKey(id)) {
                    Extent extent = measure(minecraft, entity, PROBE / Math.max(1.0F, Math.max(entity.getBbHeight(), entity.getBbWidth())));
                    if (extent != null) {
                        measured.put(id, extent);
                    }
                }
            }
        } catch (Throwable error) {
            Compline.LOGGER.debug("zoo : mesure des portraits impossible", error);
            pending.clear();
        } finally {
            RenderSystem.restoreProjectionMatrix();
            minecraft.getMainRenderTarget().bindWrite(true);
        }
    }

    /** Dessine la bête sur le canevas et relève le rectangle de ses pixels ; recommence plus petit si elle déborde. */
    private static Extent measure(Minecraft minecraft, LivingEntity entity, float probe) {
        for (int attempt = 0; attempt < 3; attempt++, probe /= 2.0F) {
            float anchorX = CANVAS / 2.0F;
            float anchorY = CANVAS * 0.7F;
            canvas.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            canvas.clear(Minecraft.ON_OSX);
            canvas.bindWrite(true);
            GuiGraphics graphics = new GuiGraphics(minecraft, minecraft.renderBuffers().bufferSource());
            InventoryScreen.renderEntityInInventory(graphics, anchorX, anchorY, probe, new Vector3f(), pose(), camera(), entity);
            graphics.flush();
            int[] box;
            float reach;
            try (NativeImage image = new NativeImage(CANVAS, CANVAS, false)) {
                RenderSystem.bindTexture(canvas.getColorTextureId());
                image.downloadTexture(0, false);
                box = bounds(image);
                reach = box == null ? 0.0F : reach(image, box);
            }
            if (box == null) {
                return null;
            }
            boolean clipped = box[0] <= 0 || box[1] <= 0 || box[2] >= CANVAS - 1 || box[3] >= CANVAS - 1;
            if (!clipped || attempt == 2) {
                // L'image lue est à l'envers (rangs du bas en premier).
                float top = CANVAS - 1 - box[3];
                float bottom = CANVAS - box[1];
                return new Extent((box[0] - anchorX) / probe, (box[2] + 1 - anchorX) / probe,
                        (top - anchorY) / probe, (bottom - anchorY) / probe, Math.max(reach, 1.0F) / probe);
            }
        }
        return null;
    }

    /** La plus grande distance en losange d'un pixel non transparent au centre du rectangle {@code box}. */
    private static float reach(NativeImage image, int[] box) {
        float cx = (box[0] + box[2] + 1) / 2.0F;
        float cy = (box[1] + box[3] + 1) / 2.0F;
        float reach = 0.0F;
        for (int y = box[1]; y <= box[3]; y++) {
            for (int x = box[0]; x <= box[2]; x++) {
                if ((image.getPixelRGBA(x, y) >>> 24) > 16) {
                    float dx = Math.max(Math.abs(x - cx), Math.abs(x + 1 - cx));
                    float dy = Math.max(Math.abs(y - cy), Math.abs(y + 1 - cy));
                    reach = Math.max(reach, dx + dy);
                }
            }
        }
        return reach;
    }

    /** Le rectangle des pixels non transparents : {minX, minY, maxX, maxY}, ou {@code null}. */
    private static int[] bounds(NativeImage image) {
        int minX = CANVAS, minY = CANVAS, maxX = -1, maxY = -1;
        for (int y = 0; y < CANVAS; y++) {
            for (int x = 0; x < CANVAS; x++) {
                if ((image.getPixelRGBA(x, y) >>> 24) > 16) {
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        return maxX < 0 ? null : new int[] {minX, minY, maxX, maxY};
    }
}
