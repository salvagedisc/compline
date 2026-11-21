package io.github.salvagedisc.fallow.postes.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.client.StationView;

import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.postes.PostesTannerie;
import io.github.salvagedisc.fallow.tannerie.ChevaletBlock;
import io.github.salvagedisc.fallow.tannerie.CuveBlock;
import io.github.salvagedisc.fallow.tannerie.SechoirBlock;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/**
 * Le livre de la tannerie, le même aux trois postes : racler la peau, la tanner, la sécher. Il s'ouvre à la page du bloc où l'on
 * est ; choisir cette page garnit le bloc depuis l'inventaire (Maj : la cuve autant qu'elle en tient), choisir une autre page dit
 * à quel bloc elle se fait.
 */
final class TannerieBook implements RecipeBook.Source {
    static final String RACLAGE = "raclage";
    static final String TANNAGE = "tannage";
    static final String SECHAGE = "sechage";

    /** La page du bloc où est ce livre. */
    private final String ici;

    TannerieBook(String ici) {
        this.ici = ici;
    }

    @Override
    public List<RecipeBook.Page> pages(StationView view) {
        List<RecipeBook.Page> pages = new ArrayList<>();
        List<ItemStack> gras = BuiltInRegistries.ITEM.getOptional(TannerieModule.GRAS).filter(item -> item != Items.AIR)
                .map(item -> List.of(new ItemStack(item))).orElse(List.of());
        pages.add(new RecipeBook.Page(RACLAGE, new ItemStack(TannerieModule.PEAU_RACLEE.get()),
                List.of(Ingredient.of(TannerieModule.PEAU_BRUTE.get()), Ingredient.of(TannerieModule.COUTEAUX)), gras,
                Component.translatable("postes.fallow.livre.raclage", Math.min(15, FallowConfig.tannerieStrokes())), false));
        pages.add(new RecipeBook.Page(TANNAGE, new ItemStack(TannerieModule.CUIR_MOUILLE.get()),
                List.of(Ingredient.of(Items.WATER_BUCKET), Ingredient.of(TannerieModule.TANINS),
                        Ingredient.of(TannerieModule.PEAU_RACLEE.get())),
                List.of(new ItemStack(Items.CLOCK)), Component.translatable("postes.fallow.livre.tannage",
                        minutes(FallowConfig.tannerieTanningMinutes())), false));
        pages.add(new RecipeBook.Page(SECHAGE, new ItemStack(Items.LEATHER),
                List.of(Ingredient.of(TannerieModule.CUIR_MOUILLE.get())), List.of(new ItemStack(Items.CAMPFIRE)),
                Component.translatable("postes.fallow.livre.sechage"), false));
        return pages;
    }

    private static String minutes(double value) {
        return value == Math.rint(value) ? Long.toString(Math.round(value)) : String.format(java.util.Locale.FRANCE, "%.1f", value);
    }

    /** Ce que porte le bloc compte comme à soi : la peau tendue, l'eau et l'écorce de la cuve, le cuir sur le séchoir. */
    @Override
    public List<ItemStack> inStation(StationView view) {
        BlockState state = view.level().getBlockState(view.pos());
        List<ItemStack> there = new ArrayList<>();
        if (state.getBlock() instanceof ChevaletBlock && state.getValue(ChevaletBlock.PEAU)) {
            there.add(new ItemStack(TannerieModule.PEAU_BRUTE.get()));
        } else if (state.getBlock() instanceof CuveBlock) {
            if (state.getValue(CuveBlock.EAU)) {
                there.add(new ItemStack(Items.WATER_BUCKET));
            }
            int tanin = state.getValue(CuveBlock.TANIN) - state.getValue(CuveBlock.PEAUX);
            Item bark = TannerieBehavior.premier(TannerieModule.TANINS);
            if (bark != null && tanin > 0) {
                there.add(new ItemStack(bark, tanin));
            }
        } else if (state.getBlock() instanceof SechoirBlock && state.getValue(SechoirBlock.ETAT) == SechoirBlock.Etat.HUMIDE) {
            there.add(new ItemStack(TannerieModule.CUIR_MOUILLE.get()));
        }
        return there;
    }

    @Override
    public Object current(StationView view) {
        return ici;
    }

    @Override
    public void picked(StationView view, RecipeBook.Page page) {
        if (!ici.equals(page.key())) {
            view.showCaption(Component.translatable("postes.fallow.livre.ailleurs." + page.key()), 2.5F);
            return;
        }
        CompoundTag garnir = new CompoundTag();
        garnir.putBoolean(PostesTannerie.TOUT, Screen.hasShiftDown());
        view.sendAction(PostesTannerie.GARNIR, garnir);
    }

    @Override
    public String memory() {
        return "tannerie";
    }
}
