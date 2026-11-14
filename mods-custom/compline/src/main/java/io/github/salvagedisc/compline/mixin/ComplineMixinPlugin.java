package io.github.salvagedisc.compline.mixin;

import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ModIds;
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
public final class ComplineMixinPlugin implements IMixinConfigPlugin {
    private static final String PACKAGE = "io.github.salvagedisc.compline.mixin.";

    private static final Map<String, List<String>> REQUIRED = Map.ofEntries(
            Map.entry("ciel.LevelRendererMixin", List.of(ModIds.ATMOSPHERICS, ModIds.ENHANCED_CELESTIALS)),
            Map.entry("ciel.RenderSystemMixin", List.of(ModIds.ATMOSPHERICS, ModIds.ENHANCED_CELESTIALS)),
            Map.entry("ciel.BiomeBlendUtilInvoker", List.of(ModIds.ATMOSPHERICS)),
            Map.entry("ciel.ClientPacketListenerMixin", List.of(ModIds.ATMOSPHERICS)),
            Map.entry("ciel.AtmosphericsFadeMixin", List.of(ModIds.ATMOSPHERICS)),
            Map.entry("clairiere.AtmosphericsBiomeMixin", List.of(ModIds.ATMOSPHERICS)),
            Map.entry("clairiere.MusicBiomeMixin", List.of(ModIds.TRUE_ADAPTIVE_MUSIC)),
            Map.entry("clairiere.SodiumTintMixin", List.of(ModIds.SODIUM)),
            Map.entry("horizon.FogRendererMixin", List.of(ModIds.DISTANT_HORIZONS)),
            Map.entry("horizon.GLStateMixin", List.of(ModIds.DISTANT_HORIZONS)),
            Map.entry("horizon.VanillaFadeMixin", List.of(ModIds.DISTANT_HORIZONS)),
            Map.entry("horizon.FarFadeMixin", List.of(ModIds.DISTANT_HORIZONS)),
            Map.entry("horizon.ChunkyAccessorMixin", List.of(ModIds.DISTANT_HORIZONS, ModIds.CHUNKY)),
            Map.entry("compat.PunchyEquipMixin", List.of(ModIds.PUNCHY, ModIds.SENSIBLE_STACKABLES)),
            Map.entry("compat.IceThawMixin", List.of(ModIds.STREAMS_REFLOWING, ModIds.SPELUNKERY)),
            Map.entry("compat.InStructureMixin", List.of(ModIds.LITHOSTITCHED)));

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
                Compline.LOGGER.debug("Mixin {} écarté : {} absent", shortName, mod);
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
