package io.github.salvagedisc.compline.zoo.client.guide;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.salvagedisc.compline.Compline;
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
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Les bêtes dessinées dans le guide : une bête de chaque espèce, créée pour l'occasion (jamais ajoutée au monde),
 * vue de trois quarts. Une espèce pas encore aperçue est dessinée à l'encre, en silhouette.
 */
final class Portraits {
    private static final Map<ResourceLocation, LivingEntity> cache = new HashMap<>();
    private static final Set<ResourceLocation> broken = new HashSet<>();
    private static ClientLevel cachedLevel;

    private Portraits() {
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

    /** Dessine l'espèce dans un carré de {@code size} au coin ({@code x}, {@code y}) ; faux si elle n'a pas de portrait. */
    static boolean draw(GuiGraphics graphics, ResourceLocation id, float x, float y, float size, boolean ink) {
        LivingEntity entity = sample(id);
        if (entity == null) {
            return false;
        }
        float height = Math.max(entity.getBbHeight(), 0.2F);
        float width = Math.max(entity.getBbWidth(), 0.2F);
        float scale = Math.min(size * 0.74F / Math.max(height, width * 1.6F), size * 1.4F);
        Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf camera = new Quaternionf().rotateX((float) Math.toRadians(12.0));
        pose.mul(camera);
        if (ink) {
            RenderSystem.setShaderColor(0.30F, 0.21F, 0.09F, 1.0F);
        }
        try {
            InventoryScreen.renderEntityInInventory(graphics, x + size / 2.0F, y + size / 2.0F + height * scale / 2.0F,
                    scale, new Vector3f(), pose, camera, entity);
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
}
