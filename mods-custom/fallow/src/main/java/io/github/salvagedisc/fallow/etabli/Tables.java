package io.github.salvagedisc.fallow.etabli;

import io.github.salvagedisc.fallow.Fallow;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Les tables de craft de Visual Workbench, vues sans dépendre de ses classes : un {@link Container} de neuf cases dont
 * le type vient de {@code visualworkbench}, avec un résultat ({@code getResultItems}, {@code getCraftingResult}) et,
 * côté client, l'animation qui tourne la grille ({@code getAnimationController}).
 */
public final class Tables {
    private static final String NAMESPACE = "visualworkbench";
    private static Method resultItems;
    private static Method craftingResult;
    private static Method animation;
    private static boolean broken;
    private static Object clientConfig;
    private static java.lang.reflect.Field flatRendering;
    private static boolean configMissing;

    private Tables() {
    }

    /** La table de Visual Workbench à cette position, ou null. */
    public static Container at(Level level, BlockPos pos) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (!(entity instanceof Container container) || container.getContainerSize() != 9) {
            return null;
        }
        ResourceLocation type = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType());
        return type != null && NAMESPACE.equals(type.getNamespace()) ? container : null;
    }

    /** Le résultat que la table montre au-dessus de sa grille. */
    public static ItemStack result(Container table) {
        Object value = call(craftingResult(table), table);
        return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
    }

    /** Donne à la table le résultat à montrer (côté serveur ; il part au client avec la table). */
    @SuppressWarnings("unchecked")
    public static void setResult(Container table, ItemStack result) {
        Object value = call(resultItems(table), table);
        if (value instanceof List<?> list && !list.isEmpty()) {
            ((List<ItemStack>) list).set(0, result);
        }
    }

    /** L'animation de la grille (client), ou null. */
    public static Object animation(Container table) {
        return call(animationMethod(table), table);
    }

    /** Vrai si Visual Workbench couche les objets à plat sur la table ({@code flat_rendering}) au lieu de les faire flotter. */
    public static boolean flat() {
        if (configMissing) {
            return false;
        }
        try {
            if (clientConfig == null) {
                Class<?> mod = Class.forName("fuzs.visualworkbench.VisualWorkbench");
                Class<?> type = Class.forName("fuzs.visualworkbench.config.ClientConfig");
                Object holder = mod.getField("CONFIG").get(null);
                clientConfig = holder.getClass().getMethod("get", Class.class).invoke(holder, type);
                flatRendering = type.getField("flatRendering");
            }
            return flatRendering.getBoolean(clientConfig);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            configMissing = true;
            return false;
        }
    }

    private static Method craftingResult(Container table) {
        if (craftingResult == null) {
            craftingResult = find(table, "getCraftingResult");
        }
        return craftingResult;
    }

    private static Method resultItems(Container table) {
        if (resultItems == null) {
            resultItems = find(table, "getResultItems");
        }
        return resultItems;
    }

    private static Method animationMethod(Container table) {
        if (animation == null) {
            animation = find(table, "getAnimationController");
        }
        return animation;
    }

    private static Method find(Container table, String name) {
        try {
            return table.getClass().getMethod(name);
        } catch (ReflectiveOperationException exception) {
            if (!broken) {
                broken = true;
                Fallow.LOGGER.warn("etabli : Visual Workbench n'a plus {}", name);
            }
            return null;
        }
    }

    private static Object call(Method method, Container table) {
        if (method == null) {
            return null;
        }
        try {
            return method.invoke(table);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }
}
