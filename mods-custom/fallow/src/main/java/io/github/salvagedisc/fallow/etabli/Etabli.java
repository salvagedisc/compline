package io.github.salvagedisc.fallow.etabli;

import io.github.salvagedisc.fallow.FallowConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Les gestes sur la table de craft (docs/plan/etabli.md), avec la grammaire du geste (décision 0042) :
 *
 * <ul>
 *   <li>un objet en main, sur le dessus : on le pose, un à la fois, dans la case visée ;</li>
 *   <li>main vide sur une case pleine : on reprend ce qui y est ; sur une case vide où le carnet montre un fantôme :
 *       on y pose l'ingrédient depuis son inventaire ; ailleurs : la fenêtre de la table, comme avant ;</li>
 *   <li>accroupi, main vide : on fabrique ce que la table montre, une fois par clic ; si rien n'est prêt et qu'une page
 *       du carnet est ouverte, on pose d'un coup tous les ingrédients qui manquent ;</li>
 *   <li>accroupi, un objet en main : le jeu de toujours (poser un bloc sur la table).</li>
 * </ul>
 *
 * <p>Le client et le serveur décident de la même façon ({@link #decide}) ; seul le serveur change la table, qui part
 * ensuite au client avec Visual Workbench.
 */
public final class Etabli {
    enum Geste { POSER, CASE_PRISE, REPRENDRE, GARNIR, GARNIR_TOUT, FABRIQUER, RIEN }

    private Etabli() {
    }

    static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!FallowConfig.etabliEnabled() || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockHitResult hit = event.getHitVec();
        BlockPos pos = event.getPos();
        Container table = Tables.at(level, pos);
        if (table == null || hit.getDirection() != Direction.UP || player.isSpectator()) {
            return;
        }
        int cell = cell(player, pos, hit.getLocation());
        Geste geste = decide(player, table, pos, cell);
        if (geste == Geste.RIEN) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
        if (level.isClientSide()) {
            return;
        }
        switch (geste) {
            case POSER -> put(player, table, pos, cell);
            case CASE_PRISE -> player.displayClientMessage(Component.translatable("etabli.fallow.case_prise"), true);
            case REPRENDRE -> take(player, table, pos, cell);
            case GARNIR -> fill(player, table, pos, cell);
            case GARNIR_TOUT -> {
                for (int i = 0; i < 9; i++) {
                    fill(player, table, pos, i);
                }
            }
            case FABRIQUER -> craft(player, table, pos);
            default -> {
            }
        }
    }

    static int cell(Player player, BlockPos pos, Vec3 location) {
        Vec3 center = pos.getCenter();
        int secteur = Grille.secteur(player.getX() - center.x, player.getZ() - center.z);
        return Grille.caseVisee(location.x - pos.getX(), location.z - pos.getZ(), secteur);
    }

    static Geste decide(Player player, Container table, BlockPos pos, int cell) {
        ItemStack held = player.getMainHandItem();
        Ingredient[] ghosts = Pages.ghosts(player, pos);
        if (player.isSecondaryUseActive()) {
            if (!held.isEmpty()) {
                return Geste.RIEN;
            }
            if (!Tables.result(table).isEmpty()) {
                return Geste.FABRIQUER;
            }
            return ghosts != null ? Geste.GARNIR_TOUT : Geste.RIEN;
        }
        if (cell < 0) {
            return Geste.RIEN;
        }
        ItemStack there = table.getItem(cell);
        if (!held.isEmpty()) {
            if (there.isEmpty() || ItemStack.isSameItemSameComponents(there, held) && there.getCount() < there.getMaxStackSize()) {
                return Geste.POSER;
            }
            return Geste.CASE_PRISE;
        }
        if (!there.isEmpty()) {
            return Geste.REPRENDRE;
        }
        if (ghosts != null && !ghosts[cell].isEmpty() && find(player, ghosts[cell]) >= 0) {
            return Geste.GARNIR;
        }
        return Geste.RIEN;
    }

    private static void put(Player player, Container table, BlockPos pos, int cell) {
        ItemStack held = player.getMainHandItem();
        ItemStack there = table.getItem(cell);
        if (there.isEmpty()) {
            table.setItem(cell, held.copyWithCount(1));
        } else {
            there.grow(1);
        }
        held.consume(1, player);
        changed(player.level(), table, pos);
        player.level().playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.6F, 1.1F + player.getRandom().nextFloat() * 0.2F);
    }

    private static void take(Player player, Container table, BlockPos pos, int cell) {
        ItemStack stack = table.removeItemNoUpdate(cell);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        changed(player.level(), table, pos);
        player.level().playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.3F, 1.4F);
    }

    private static void fill(Player player, Container table, BlockPos pos, int cell) {
        Ingredient[] ghosts = Pages.ghosts(player, pos);
        if (ghosts == null || ghosts[cell].isEmpty() || !table.getItem(cell).isEmpty()) {
            return;
        }
        int slot = find(player, ghosts[cell]);
        if (slot < 0) {
            return;
        }
        table.setItem(cell, player.getInventory().removeItem(slot, 1));
        changed(player.level(), table, pos);
        player.level().playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.5F, 1.2F);
    }

    /** L'emplacement de l'inventaire qui a cet ingrédient, ou -1. */
    static int find(Player player, Ingredient ingredient) {
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            ItemStack stack = inventory.items.get(slot);
            if (!stack.isEmpty() && ingredient.test(stack)) {
                return slot;
            }
        }
        return -1;
    }

    private static void craft(Player player, Container table, BlockPos pos) {
        Level level = player.level();
        NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
        for (int i = 0; i < 9; i++) {
            items.set(i, table.getItem(i));
        }
        CraftingInput input = CraftingInput.of(3, 3, items);
        Optional<RecipeHolder<CraftingRecipe>> found = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
        if (found.isEmpty()) {
            changed(level, table, pos);
            return;
        }
        CraftingRecipe recipe = found.get().value();
        ItemStack result = recipe.assemble(input, level.registryAccess());
        if (result.isEmpty()) {
            return;
        }
        int[] origin = origin(items, input);
        NonNullList<ItemStack> remainders = recipe.getRemainingItems(input);
        result.onCraftedBy(level, player, result.getCount());
        EventHooks.firePlayerCraftingEvent(player, result.copy(), table);
        for (int i = 0; i < 9; i++) {
            if (!table.getItem(i).isEmpty()) {
                table.getItem(i).shrink(1);
                if (table.getItem(i).isEmpty()) {
                    table.setItem(i, ItemStack.EMPTY);
                }
            }
        }
        for (int k = 0; k < remainders.size(); k++) {
            ItemStack remainder = remainders.get(k);
            if (remainder.isEmpty()) {
                continue;
            }
            int cell = (origin[1] + k / input.width()) * 3 + origin[0] + k % input.width();
            if (cell < 9 && table.getItem(cell).isEmpty()) {
                table.setItem(cell, remainder);
            } else if (!player.getInventory().add(remainder)) {
                player.drop(remainder, false);
            }
        }
        ItemStack shown = result.copy();
        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }
        changed(level, table, pos);
        level.playSound(null, pos, SoundEvents.WOOD_HIT, SoundSource.BLOCKS, 0.7F, 1.3F);
        level.playSound(null, pos, SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundSource.BLOCKS, 0.5F, 1.2F);
        if (level instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, shown), pos.getX() + 0.5, pos.getY() + 1.1,
                    pos.getZ() + 0.5, 6, 0.15, 0.05, 0.15, 0.05);
        }
    }

    /** Le coin haut gauche de la recette dans la grille : {colonne, rangée}. */
    private static int[] origin(List<ItemStack> items, CraftingInput input) {
        if (input.width() == 3 && input.height() == 3) {
            return new int[] {0, 0};
        }
        int left = 3;
        int top = 3;
        for (int i = 0; i < 9; i++) {
            if (!items.get(i).isEmpty()) {
                left = Math.min(left, i % 3);
                top = Math.min(top, i / 3);
            }
        }
        return new int[] {left == 3 ? 0 : left, top == 3 ? 0 : top};
    }

    /** Recalcule ce que la table montre et l'envoie aux clients. */
    static void changed(Level level, Container table, BlockPos pos) {
        List<ItemStack> items = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            items.add(table.getItem(i));
        }
        CraftingInput input = CraftingInput.of(3, 3, items);
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(holder -> holder.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        Tables.setResult(table, result);
        table.setChanged();
    }
}
