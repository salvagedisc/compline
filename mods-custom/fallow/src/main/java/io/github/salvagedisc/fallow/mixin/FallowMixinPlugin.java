package io.github.salvagedisc.fallow.mixin;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.ModIds;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

/**
 * N'applique un mixin que si les mods qu'il vise sont présents. La table {@link #REQUIRED} associe
 * chaque mixin (nom relatif au paquet {@code mixin}) aux identifiants de mods qu'il suppose.
 * Un mixin absent de la table s'applique toujours.
 */
public final class FallowMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "io.github.salvagedisc.fallow.mixin.";

    private static final Map<String, List<String>> REQUIRED = Map.of(
            "ciel.LevelRendererMixin", List.of(ModIds.ATMOSPHERICS, ModIds.ENHANCED_CELESTIALS),
            "ciel.RenderSystemMixin", List.of(ModIds.ATMOSPHERICS, ModIds.ENHANCED_CELESTIALS),
            "ciel.FriendMoonSkyMixin", List.of(ModIds.ATMOSPHERICS, ModIds.NO_MANS_LAND),
            "ciel.AtmosphericsDreamMixin", List.of(ModIds.ATMOSPHERICS, ModIds.NO_MANS_LAND),
            "ciel.BiomeBlendUtilInvoker", List.of(ModIds.ATMOSPHERICS),
            "ciel.ClientPacketListenerMixin", List.of(ModIds.ATMOSPHERICS),
            "horizon.FogRendererMixin", List.of(ModIds.DISTANT_HORIZONS),
            "horizon.GLStateMixin", List.of(ModIds.DISTANT_HORIZONS),
            "horizon.ChunkyAccessorMixin", List.of(ModIds.DISTANT_HORIZONS, ModIds.CHUNKY));

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String shortName = mixinClassName.startsWith(PACKAGE) ? mixinClassName.substring(PACKAGE.length()) : mixinClassName;
        List<String> mods = REQUIRED.getOrDefault(shortName, List.of());
        for (String mod : mods) {
            if (!ModIds.isLoaded(mod)) {
                Fallow.LOGGER.debug("Mixin {} écarté : {} absent", shortName, mod);
                return false;
            }
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
