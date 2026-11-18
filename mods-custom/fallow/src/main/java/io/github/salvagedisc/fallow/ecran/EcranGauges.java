package io.github.salvagedisc.fallow.ecran;

import io.github.salvagedisc.fallow.Fallow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Les jauges de survie des mods du pack, lues sans en dépendre : la soif de Thirst Was Reclaimed (0 à 20, comme la
 * faim) et la température du corps de Cold Sweat (-150 à 150, dégâts au-delà de ±100), telles que leurs jauges les
 * montrent. Si une lecture échoue (mod absent, nom changé), la jauge est oubliée.
 */
final class EcranGauges {
    /** Valeur rendue quand la jauge n'existe pas. */
    static final int NONE = Integer.MIN_VALUE;

    private static boolean thirstReady;
    private static AttachmentType<?> thirstType;
    private static Method thirstGet;

    private static boolean tempReady;
    private static Field tempField;

    private EcranGauges() {
    }

    /** La soif du joueur, 0 à 20, ou {@link #NONE}. */
    static int thirst(Player player) {
        if (!thirstReady) {
            thirstReady = true;
            if (ModList.get().isLoaded("thirst")) {
                thirstType = NeoForgeRegistries.ATTACHMENT_TYPES.get(ResourceLocation.fromNamespaceAndPath("thirst", "player_thirst"));
            }
        }
        if (thirstType == null) {
            return NONE;
        }
        try {
            Object data = player.getData(thirstType);
            if (thirstGet == null) {
                thirstGet = data.getClass().getMethod("getThirst");
            }
            return (Integer) thirstGet.invoke(data);
        } catch (ReflectiveOperationException | RuntimeException e) {
            Fallow.LOGGER.warn("ecran : soif de Thirst illisible, ignorée", e);
            thirstType = null;
            return NONE;
        }
    }

    /**
     * La température du corps telle que la jauge de Cold Sweat l'affiche, -150 à 150, ou {@link #NONE}. On lit le champ
     * de sa jauge ({@code Overlays.BLEND_BODY_TEMP}) : la valeur même que le joueur verrait.
     */
    static int bodyTemperature() {
        if (!tempReady) {
            tempReady = true;
            if (ModList.get().isLoaded("cold_sweat")) {
                try {
                    tempField = Class.forName("com.momosoftworks.coldsweat.client.gui.Overlays").getField("BLEND_BODY_TEMP");
                } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
                    Fallow.LOGGER.warn("ecran : température de Cold Sweat introuvable, ignorée", e);
                }
            }
        }
        if (tempField == null) {
            return NONE;
        }
        try {
            return (int) Math.round(tempField.getDouble(null));
        } catch (ReflectiveOperationException | RuntimeException e) {
            Fallow.LOGGER.warn("ecran : température de Cold Sweat illisible, ignorée", e);
            tempField = null;
            return NONE;
        }
    }
}
