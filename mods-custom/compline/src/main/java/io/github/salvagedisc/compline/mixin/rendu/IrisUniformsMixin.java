package io.github.salvagedisc.compline.mixin.rendu;

import io.github.salvagedisc.compline.rendu.ShaderUniforms;
import net.irisshaders.iris.gl.uniform.UniformHolder;
import net.irisshaders.iris.shaderpack.properties.PackDirectives;
import net.irisshaders.iris.uniforms.FrameUpdateNotifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Iris monte ici les uniformes communs d'un pack de shaders, ceux que tout programme peut déclarer. Compline y
 * ajoute les siens ({@code brumaireMist}, {@code brumaireClearing0..3}, {@code brumaireWind}, {@code brumaireGust0..1}),
 * que seul son pack de shaders déclare. Appliqué seulement avec Iris ; {@code require = 0} : une version
 * d'Iris qui renomme la méthode laisse le pack sur ses valeurs de repli, sans erreur.
 */
@Mixin(targets = "net.irisshaders.iris.uniforms.CommonUniforms", remap = false)
abstract class IrisUniformsMixin {
    @Inject(method = "generalCommonUniforms", at = @At("TAIL"), require = 0)
    private static void compline$shaderUniforms(UniformHolder uniforms, FrameUpdateNotifier updateNotifier, PackDirectives directives,
            CallbackInfo ci) {
        ShaderUniforms.register(uniforms);
    }
}
