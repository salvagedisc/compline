package io.github.salvagedisc.tilth;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Point d'entrée du mod Tilth. Chaque module ne démarre que si les mods qu'il relie sont présents
 * (voir {@link TilthModule}) ; son interrupteur est dans la config.
 */
@Mod(Tilth.MOD_ID)
public final class Tilth {
    public static final String MOD_ID = "tilth";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Tilth(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.COMMON, TilthConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, TilthConfig.CLIENT_SPEC);

        for (TilthModule module : TilthModule.values()) {
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
