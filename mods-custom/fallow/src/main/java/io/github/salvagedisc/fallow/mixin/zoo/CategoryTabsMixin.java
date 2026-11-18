package io.github.salvagedisc.fallow.mixin.zoo;

import com.eternal_creations.archies_dino_mod.client.screen.guidebook.book.AnimatedBookFlipper;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.book.CategoryTabs;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.CategoryData;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.data.GuidebookData;
import com.eternal_creations.archies_dino_mod.client.screen.guidebook.ui.widget.AbstractWidget;
import io.github.salvagedisc.fallow.zoo.client.guide.ZooGuide;
import java.util.Map;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Les marque-pages du guide d'Archie, plus celui du zoo et les onglets de ses milieux ({@link ZooGuide#decorate}). */
@Mixin(value = CategoryTabs.class, remap = false)
public abstract class CategoryTabsMixin {
    @Shadow
    @Final
    private AnimatedBookFlipper book;

    @Shadow
    @Final
    private Map<CategoryData, AbstractWidget> categorySubTabs;

    @Inject(method = "<init>", require = 0, at = @At("TAIL"))
    private void fallow$addZooTabs(float width, float height, AnimatedBookFlipper flipper, GuidebookData data, CallbackInfo ci) {
        ZooGuide.decorate((CategoryTabs) (Object) this, book, categorySubTabs);
    }
}
