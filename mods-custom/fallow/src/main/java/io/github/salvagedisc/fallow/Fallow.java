package io.github.salvagedisc.fallow;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Point d'entrée du mod Fallow. Chaque module ne démarre que si les mods qu'il relie sont présents
 * (voir {@link FallowModule}) ; son interrupteur est dans la config.
 */
@Mod(Fallow.MOD_ID)
public final class Fallow {
    public static final String MOD_ID = "fallow";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Fallow(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.COMMON, FallowConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, FallowConfig.CLIENT_SPEC);

        for (FallowModule module : FallowModule.values()) {
            if (!module.runsOn(dist)) {
                continue;
            }
            if (module.isAvailable()) {
                LOGGER.info("Module {} chargé ({})", module.id(), String.join(", ", module.requiredMods()));
                module.start(modBus);
            } else {
                LOGGER.info("Module {} ignoré : il lui manque {}", module.id(), String.join(", ", module.missingMods()));
            }
        }
    }
}
