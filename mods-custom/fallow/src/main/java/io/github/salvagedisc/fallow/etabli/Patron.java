package io.github.salvagedisc.fallow.etabli;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Où va chaque ingrédient d'une recette sur la grille : une recette en forme commence au coin de la grille, une
 * recette sans forme remplit les cases dans l'ordre. Une case sans ingrédient vaut {@link Ingredient#EMPTY}.
 */
public final class Patron {
    private Patron() {
    }

    public static Ingredient[] of(CraftingRecipe recipe) {
        Ingredient[] cells = new Ingredient[9];
        java.util.Arrays.fill(cells, Ingredient.EMPTY);
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (recipe instanceof ShapedRecipe shaped) {
            int width = shaped.getWidth();
            for (int i = 0; i < ingredients.size() && i < 9; i++) {
                int row = i / width;
                int column = i % width;
                if (row < 3 && column < 3) {
                    cells[row * 3 + column] = ingredients.get(i);
                }
            }
        } else {
            int next = 0;
            for (Ingredient ingredient : ingredients) {
                if (!ingredient.isEmpty() && next < 9) {
                    cells[next++] = ingredient;
                }
            }
        }
        return cells;
    }

    /** Les ingrédients non vides d'une recette. */
    public static List<Ingredient> needed(CraftingRecipe recipe) {
        List<Ingredient> needed = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            if (!ingredient.isEmpty()) {
                needed.add(ingredient);
            }
        }
        return needed;
    }

    /** Vrai si les objets donnés suffisent à la recette (chaque ingrédient prend un objet). */
    public static boolean canMake(CraftingRecipe recipe, List<ItemStack> available) {
        List<ItemStack> pool = new ArrayList<>();
        for (ItemStack stack : available) {
            if (!stack.isEmpty()) {
                pool.add(stack.copy());
            }
        }
        for (Ingredient ingredient : needed(recipe)) {
            boolean found = false;
            for (ItemStack stack : pool) {
                if (!stack.isEmpty() && ingredient.test(stack)) {
                    stack.shrink(1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /** Vrai si chaque objet posé sur la table trouve sa place dans la recette. */
    public static boolean uses(CraftingRecipe recipe, List<ItemStack> onTable) {
        List<Ingredient> free = new ArrayList<>(needed(recipe));
        for (ItemStack stack : onTable) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean found = false;
            for (int i = 0; i < free.size(); i++) {
                if (free.get(i).test(stack)) {
                    free.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }
}
