package io.github.salvagedisc.fallow.postes.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import com.pockyl.diegesis.api.client.MenuAccess;
import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.state.SlotRef;

import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.client.ForgingBookCategory;
import net.stirdrem.overgeared.recipe.ForgingRecipe;
import net.stirdrem.overgeared.recipe.ModRecipeTypes;

/**
 * Le livre de forge, à côté de l'enclume : les recettes de forge d'Overgeared que cette enclume peut faire (son rang ou moins),
 * par onglet d'Overgeared (têtes d'outils, armures, divers), chacune avec sa grille, le nombre de coups et le plan qu'elle
 * demande. Choisir une page pose ses pièces dans la grille de l'enclume, depuis l'inventaire, par de simples clics que le serveur
 * vérifie ; ce qui manque est dit en légende.
 */
final class ForgeBook implements RecipeBook.Source {
    private static final int GRILLE = 3;

    private final AnvilTier tier;
    /** La case du menu où commence la grille : 1 sur l'enclume de pierre, 2 après le plan sur les autres. */
    private final int debutGrille;
    private List<RecipeHolder<ForgingRecipe>> madeFrom = List.of();
    private List<RecipeBook.Page> made = List.of();

    ForgeBook(AnvilTier tier, int debutGrille) {
        this.tier = tier;
        this.debutGrille = debutGrille;
    }

    @Override
    public List<RecipeBook.Page> pages(StationView view) {
        Level level = view.level();
        List<RecipeHolder<ForgingRecipe>> ici = new ArrayList<>();
        for (RecipeHolder<ForgingRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.FORGING.get())) {
            if (seForgeIci(holder.value())) {
                ici.add(holder);
            }
        }
        if (memes(ici)) {
            return made;
        }
        List<RecipeBook.Page> pages = new ArrayList<>();
        for (RecipeHolder<ForgingRecipe> holder : ici) {
            ForgingRecipe recipe = holder.value();
            ItemStack result = recipe.getResultItem(level.registryAccess());
            if (result.isEmpty()) {
                continue;
            }
            List<ItemStack> foot = new ArrayList<>();
            foot.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("overgeared", "smithing_hammer"))));
            if (recipe.requiresBlueprint()) {
                foot.add(new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("overgeared", "blueprint"))));
            }
            pages.add(new RecipeBook.Page(holder.id(), result.copy(), grille(recipe), foot,
                    Component.translatable("postes.fallow.livre.forge", recipe.getHammeringRequired()), false,
                    recipe.getRecipeBookTab().getFolderName(), recipe.getGroup()));
        }
        madeFrom = ici;
        made = List.copyOf(pages);
        return made;
    }

    /** Overgeared refuse une recette d'un rang au-dessus de l'enclume ; un rang inconnu aussi. */
    private boolean seForgeIci(ForgingRecipe recipe) {
        AnvilTier demande = AnvilTier.fromDisplayName(recipe.getAnvilTier());
        return demande != null && demande.ordinal() <= tier.ordinal();
    }

    private boolean memes(List<RecipeHolder<ForgingRecipe>> ici) {
        if (ici.size() != madeFrom.size()) {
            return false;
        }
        for (int i = 0; i < ici.size(); i++) {
            if (ici.get(i) != madeFrom.get(i)) {
                return false;
            }
        }
        return true;
    }

    /** Les pièces telles qu'elles se posent, en rangées de trois, les trous vides. */
    private static List<Ingredient> grille(ForgingRecipe recipe) {
        List<Ingredient> ingredients = recipe.getIngredients();
        List<Ingredient> grille = new ArrayList<>();
        for (int row = 0; row < recipe.getHeight(); row++) {
            for (int column = 0; column < GRILLE; column++) {
                int at = row * recipe.getWidth() + column;
                grille.add(column < recipe.getWidth() && at < ingredients.size() ? ingredients.get(at) : Ingredient.EMPTY);
            }
        }
        return grille;
    }

    @Override
    public List<ItemStack> inStation(StationView view) {
        List<ItemStack> there = new ArrayList<>();
        for (int cell = 0; cell < GRILLE * GRILLE; cell++) {
            there.add(view.state().getItem(SlotRef.of(PostesForge.GRILLE, cell)));
        }
        return there;
    }

    @Override
    public void picked(StationView view, RecipeBook.Page page) {
        MenuAccess menu = view.menu();
        LocalPlayer player = Minecraft.getInstance().player;
        if (menu == null || player == null || !(page.key() instanceof ResourceLocation id)) {
            return;
        }
        RecipeHolder<?> holder = player.level().getRecipeManager().byKey(id).orElse(null);
        if (holder == null || !(holder.value() instanceof ForgingRecipe recipe)) {
            return;
        }
        List<Ingredient> voulu = grille(recipe);
        List<Component> manque = poser(menu, voulu);
        if (!manque.isEmpty()) {
            view.showCaption(Component.translatable("postes.fallow.forge.manque", manque.get(0)), 2.5F);
        }
    }

    /**
     * Pose une recette, une pièce par case : ce qui n'a rien à faire dans une case retourne à l'inventaire, puis chaque case vide
     * reçoit une pièce prise dans l'inventaire (prendre la pile, en poser une d'un clic droit, rendre le reste). Rien ne se fait
     * avec un objet au curseur. Rend ce qui manque.
     */
    private List<Component> poser(MenuAccess menu, List<Ingredient> voulu) {
        AbstractContainerMenu handle = menu.handle();
        List<Component> manque = new ArrayList<>();
        if (!handle.getCarried().isEmpty()) {
            return manque;
        }
        for (int cell = 0; cell < GRILLE * GRILLE; cell++) {
            Ingredient want = cell < voulu.size() ? voulu.get(cell) : Ingredient.EMPTY;
            ItemStack there = handle.getSlot(debutGrille + cell).getItem();
            if (!there.isEmpty() && (want.isEmpty() || !want.test(there) || there.getCount() > 1)) {
                menu.click(debutGrille + cell, 0, ClickType.QUICK_MOVE);
            }
        }
        for (int cell = 0; cell < GRILLE * GRILLE && cell < voulu.size(); cell++) {
            Ingredient want = voulu.get(cell);
            if (want.isEmpty() || !handle.getSlot(debutGrille + cell).getItem().isEmpty()) {
                continue;
            }
            int source = source(handle, want);
            if (source < 0) {
                ItemStack[] items = want.getItems();
                Component name = items.length > 0 ? items[0].getHoverName() : new ItemStack(Items.BARRIER).getHoverName();
                if (manque.stream().noneMatch(name::equals)) {
                    manque.add(name);
                }
                continue;
            }
            menu.click(source, 0, ClickType.PICKUP);
            menu.click(debutGrille + cell, 1, ClickType.PICKUP);
            menu.click(source, 0, ClickType.PICKUP);
            if (!handle.getCarried().isEmpty()) {
                return manque;
            }
        }
        return manque;
    }

    /** La case du menu, dans l'inventaire du joueur, qui tient une pièce qui convient ; -1 sinon. */
    private static int source(AbstractContainerMenu handle, Ingredient want) {
        LocalPlayer player = Minecraft.getInstance().player;
        for (Slot slot : handle.slots) {
            if (player != null && slot.container == player.getInventory() && !slot.getItem().isEmpty() && want.test(slot.getItem())) {
                return slot.index;
            }
        }
        return -1;
    }

    @Override
    public List<RecipeBook.Tab> tabs(StationView view) {
        List<RecipeBook.Tab> tabs = new ArrayList<>();
        tabs.add(new RecipeBook.Tab(null, new ItemStack(Items.COMPASS), Component.translatable("postes.fallow.livre.onglet.tout")));
        tabs.add(new RecipeBook.Tab(RecipeBook.RECENT, new ItemStack(Items.CLOCK),
                Component.translatable("postes.fallow.livre.onglet.recent")));
        for (ForgingBookCategory category : ForgingBookCategory.values()) {
            String name = category.getFolderName();
            tabs.add(new RecipeBook.Tab(name, new ItemStack(icone(category)),
                    Component.translatable("postes.fallow.livre.onglet." + name.toLowerCase(Locale.ROOT))));
        }
        return tabs;
    }

    private static net.minecraft.world.item.Item icone(ForgingBookCategory category) {
        return switch (category) {
            case TOOL_HEADS -> Items.IRON_PICKAXE;
            case ARMORS -> Items.IRON_CHESTPLATE;
            default -> Items.IRON_INGOT;
        };
    }

    @Override
    public String memory() {
        return "forge";
    }
}
