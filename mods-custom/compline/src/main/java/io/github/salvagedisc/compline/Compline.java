package io.github.salvagedisc.compline;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Point d'entrée du mod Compline. Chaque module ne démarre que si les mods qu'il relie sont présents
 * (voir {@link ComplineModule}) ; son interrupteur est dans la config.
 */
@Mod(Compline.MOD_ID)
public final class Compline {
    public static final String MOD_ID = "compline";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Compline(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.COMMON, ComplineConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, ComplineConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.SERVER, ComplineConfig.SERVER_SPEC);
        BootCheck.register();

        for (ComplineModule module : ComplineModule.values()) {
            if (!module.runsOn(dist)) {
                continue;
            }
            if (module.isAvailable()) {
                LOGGER.info("Module {} chargé ({})", module.id(),
                        module.requiredMods().isEmpty() ? "aucun mod requis" : String.join(", ", module.requiredMods()));
                module.start(modBus);
            } else {
                LOGGER.info("Module {} ignoré : il lui manque {}", module.id(), String.join(", ", module.missingMods()));
            }
        }
    }
}
