package io.github.salvagedisc.compline.mixin.ciel;

import com.beash.atmospherics.util.BiomeBlendUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Accès à {@code BiomeBlendUtil.resetCache()} d'ATMOSPHERICS (méthode privée). Appliqué seulement avec ATMOSPHERICS. */
@Mixin(BiomeBlendUtil.class)
public interface BiomeBlendUtilInvoker {
    @Invoker("resetCache")
    static void compline$resetCache() {
        throw new AssertionError("mixin non appliqué");
    }
}
