package io.github.salvagedisc.fallow.sac;

import cn.mlus.thirst.api.ThirstHelper;
import cn.mlus.thirst.content.purity.WaterPurity;
import cn.mlus.thirst.foundation.common.capability.IThirst;
import cn.mlus.thirst.foundation.common.capability.ModAttachment;
import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Effet « eau » du tuyau de Traveler's Backpack pour Thirst Was Reclaimed (TWR).
 *
 * <p>Ce qui est lu dans le code des deux mods :
 * <ul>
 *   <li>le tuyau boit 1000 mB d'un coup ({@code HoseItem.finishUsingItem} retire la plus grande quantité
 *       demandée par les effets de ce fluide ; l'effet d'eau du sac en demande 1000) ;</li>
 *   <li>TWR range la pureté de l'eau dans le composant {@code thirst:purity} du {@code FluidStack}, de 0
 *       (sale) à 3 (purifiée). Les réservoirs du sac sont des {@code FluidTank} NeoForge, que TWR suit
 *       aussi : un mélange garde la pureté la plus basse ;</li>
 *   <li>un seau d'eau vidé dans le réservoir apporte sa pureté (le sac passe par {@code FluidUtil}, donc
 *       par {@code FluidBucketWrapper.getFluid}, que TWR réécrit) ; l'eau aspirée au tuyau directement
 *       dans le monde arrive sans composant ({@code new FluidStack(eau, 1000)} dans {@code HoseItem.use}) ;</li>
 *   <li>TWR lit un composant absent comme une eau purifiée ({@code MISSING_COMPONENT_PURITY = 3}), et
 *       retire le composant quand il vaut 3 : « absent » et « purifiée » ne se distinguent pas. On suit
 *       donc la règle de TWR, via {@link WaterPurity#getPurity(FluidStack)}.</li>
 * </ul>
 *
 * <p>Une gorgée applique les effets de pureté de TWR (nausée, poison selon sa config), puis, s'il le
 * permet, l'hydratation d'une bouteille d'eau multipliée par {@code sac.bottlesPerDrink}.
 */
public final class ThirstHoseEffect extends EffectFluid {
    public static final String ID = Fallow.MOD_ID + ":thirst_water";
    private static final int AMOUNT_PER_DRINK = 1000;

    public ThirstHoseEffect() {
        super(ID, Fluids.WATER, AMOUNT_PER_DRINK);
    }

    @Override
    public boolean canExecuteEffect(FluidStack stack, Level level, Entity entity) {
        return FallowConfig.sacEnabled() && stack.getAmount() >= amountRequired;
    }

    @Override
    public void affectDrinker(FluidStack stack, Level level, Entity entity) {
        if (level.isClientSide() || !(entity instanceof ServerPlayer player) || !FallowConfig.sacEnabled()) {
            return;
        }
        int purity = WaterPurity.getPurity(stack);
        // Faux si l'eau a rendu malade et que TWR ne désaltère pas dans ce cas (quenchThirstWhenDebuffed).
        if (!WaterPurity.givePurityEffects(player, purity)) {
            return;
        }

        ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        if (!ThirstHelper.playerRestoresThirst(waterBottle, player)) {
            return;
        }
        int hydration;
        int quenched;
        if (ThirstHelper.isDrink(waterBottle)) {
            hydration = ThirstHelper.getThirst(waterBottle);
            quenched = ThirstHelper.getQuenched(waterBottle);
        } else {
            // La bouteille d'eau a été retirée des boissons dans la config de TWR : valeurs par défaut d'une boisson.
            hydration = ThirstHelper.defaultDrinkHydration;
            quenched = ThirstHelper.defaultDrinkQuenched;
        }
        int bottles = FallowConfig.sacBottlesPerDrink();
        IThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        thirst.drink(hydration * bottles, quenched * bottles);
        thirst.updateThirstData(player);
    }
}
