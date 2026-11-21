package io.github.salvagedisc.fallow.etabli;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

/**
 * La page ouverte de chaque joueur : une table et une recette. Le serveur la tient pour ses joueurs, le client pour le
 * sien (reçue par {@link EtabliPage}) ; les deux en tirent les mêmes fantômes.
 */
public final class Pages {
    private static final Map<UUID, EtabliPage> SERVER = new ConcurrentHashMap<>();
    private static volatile EtabliPage client;

    private Pages() {
    }

    static void set(Player player, EtabliPage page) {
        if (player.level().isClientSide()) {
            client = page;
        } else {
            SERVER.put(player.getUUID(), page);
        }
    }

    public static void setClient(EtabliPage page) {
        client = page;
    }

    public static EtabliPage client() {
        return client;
    }

    static void forget(Player player) {
        SERVER.remove(player.getUUID());
    }

    static EtabliPage get(Player player) {
        return player.level().isClientSide() ? client : SERVER.get(player.getUUID());
    }

    /** La recette de la page ouverte sur cette table, si elle existe encore. */
    static Optional<CraftingRecipe> recipe(Player player, BlockPos table) {
        EtabliPage page = get(player);
        if (page == null || !page.table().equals(table) || page.recipe().isEmpty()) {
            return Optional.empty();
        }
        return recipe(player.level(), page.recipe().get());
    }

    public static Optional<CraftingRecipe> recipe(Level level, ResourceLocation id) {
        Optional<RecipeHolder<?>> holder = level.getRecipeManager().byKey(id);
        return holder.map(RecipeHolder::value).filter(CraftingRecipe.class::isInstance).map(CraftingRecipe.class::cast);
    }

    /** Les fantômes de la page ouverte sur cette table : neuf ingrédients, vides hors de la recette. */
    static Ingredient[] ghosts(Player player, BlockPos table) {
        return recipe(player, table).map(Patron::of).orElse(null);
    }

    static Optional<ResourceLocation> id(EtabliPage page) {
        return page == null ? Optional.empty() : page.recipe();
    }
}
