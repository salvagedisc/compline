package io.github.salvagedisc.fallow.mixin.zoo;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.GuidebookData;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.salvagedisc.fallow.zoo.client.guide.GuidePatches;
import io.github.salvagedisc.fallow.zoo.client.guide.ZooGuide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.w3c.dom.Document;

/**
 * Le guide d'Archie's Dinosaurs tel qu'il se charge : nos corrections de textes sur le XML lu, puis la catégorie du
 * zoo glissée avant les crédits. Le guide est relu à chaque ouverture, donc le zoo est toujours à jour.
 */
@Mixin(value = GuidebookData.class, remap = false)
public abstract class GuidebookDataMixin {
    @ModifyExpressionValue(method = "parseXML", require = 0, at = @At(value = "INVOKE",
            target = "Ljavax/xml/parsers/DocumentBuilder;parse(Ljava/io/InputStream;)Lorg/w3c/dom/Document;"))
    private static Document fallow$patchTexts(Document document) {
        return GuidePatches.apply(document);
    }

    @Inject(method = "loadData", require = 0, at = @At("RETURN"), cancellable = true)
    private static void fallow$addZoo(CallbackInfoReturnable<GuidebookData> cir) {
        cir.setReturnValue(ZooGuide.withZoo(cir.getReturnValue()));
    }
}
