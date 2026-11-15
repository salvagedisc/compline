package io.github.salvagedisc.compline.mixin.xp;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.salvagedisc.compline.ComplineConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * L'enclume sans niveaux d'expérience. Vanilla ne rend l'objet que si
 * {@code player.experienceLevel >= coût} et {@code coût > 0} ({@code AnvilMenu.mayPickup}).
 * Quand le module xp est actif, le niveau lu ici vaut l'infini : seul le test « coût > 0 » reste.
 *
 * <p>Le coût garde son rôle : la limite de 40 (« Trop cher ! », renommé « Trop usé ! ») est posée
 * par {@code createResult}, qui n'est pas touché. Le même calcul tourne chez le client (couleur du
 * coût, droit de prendre l'objet) et sur le serveur : le mixin s'applique des deux côtés.
 * {@code onTake} retire toujours {@code coût} niveaux, ce qui ne fait rien à 0 niveau.
 */
@Mixin(AnvilMenu.class)
abstract class AnvilMenuMixin {
    @ModifyExpressionValue(
            method = "mayPickup",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;experienceLevel:I"))
    private int compline$ignoreLevels(int experienceLevel, Player player, boolean hasStack) {
        return ComplineConfig.xpEnabled() ? Integer.MAX_VALUE : experienceLevel;
    }
}
