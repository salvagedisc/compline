package io.github.salvagedisc.fallow.postes;

import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.station.StationContext;
import com.pockyl.diegesis.api.station.StationLogic;

import io.github.salvagedisc.fallow.tannerie.ChevaletBlock;
import io.github.salvagedisc.fallow.tannerie.CuveBlock;
import io.github.salvagedisc.fallow.tannerie.Ouvrage;
import io.github.salvagedisc.fallow.tannerie.SechoirBlock;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/** Le travail d'un poste de la tannerie, sur le serveur : les règles sont celles du bloc, l'objet vient du curseur ou de l'inventaire. */
final class OuvrageLogic implements StationLogic {
    OuvrageLogic(StationContext station) {
    }

    @Override
    public void onCustomAction(StationContext station, Player player, String action, CompoundTag data) {
        Level level = station.level();
        BlockPos pos = station.pos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof Ouvrage ouvrage)) {
            return;
        }
        switch (action) {
            case PostesTannerie.TRAVAILLER -> travailler(ouvrage, level, pos, player,
                    data.contains(PostesTannerie.DEPUIS) ? data.getInt(PostesTannerie.DEPUIS) : -1);
            case PostesTannerie.MAIN_VIDE -> ouvrage.mainVide(state, level, pos, player);
            case PostesTannerie.GARNIR -> garnir(ouvrage, level, pos, player, data.getBoolean(PostesTannerie.TOUT));
            default -> {
            }
        }
    }

    /** L'objet du curseur ({@code depuis} = -1) ou d'une case de l'inventaire travaille ; ce qui le remplace y retourne. */
    private static void travailler(Ouvrage ouvrage, Level level, BlockPos pos, Player player, int depuis) {
        if (depuis >= Inventory.INVENTORY_SIZE) {
            return;
        }
        ItemStack stack = depuis >= 0 ? player.getInventory().getItem(depuis) : player.inventoryMenu.getCarried();
        if (stack.isEmpty()) {
            return;
        }
        ouvrage.travailler(stack, level.getBlockState(pos), level, pos, player, rendu -> rendre(player, depuis, rendu),
                sorti -> donner(player, sorti));
        if (depuis < 0 && player.inventoryMenu.getCarried() == stack && stack.isEmpty()) {
            player.inventoryMenu.setCarried(ItemStack.EMPTY);
        }
    }

    /**
     * La page choisie au livre : le bloc reçoit de l'inventaire ce qu'il lui faut. La cuve prend l'eau s'il en manque, puis une
     * écorce et une peau raclée (avec {@code tout} : autant de paires que la cuve en tient) ; le chevalet une peau brute ; le
     * séchoir un cuir mouillé.
     */
    private static void garnir(Ouvrage ouvrage, Level level, BlockPos pos, Player player, boolean tout) {
        if (ouvrage instanceof ChevaletBlock) {
            utiliser(ouvrage, level, pos, player, stack -> stack.is(TannerieModule.PEAU_BRUTE.get()));
        } else if (ouvrage instanceof SechoirBlock) {
            utiliser(ouvrage, level, pos, player, stack -> stack.is(TannerieModule.CUIR_MOUILLE.get()));
        } else if (ouvrage instanceof CuveBlock) {
            if (!level.getBlockState(pos).getValue(CuveBlock.EAU)) {
                utiliser(ouvrage, level, pos, player, stack -> stack.is(Items.WATER_BUCKET));
            }
            for (int fois = 0; fois < (tout ? 4 : 1); fois++) {
                int peaux = level.getBlockState(pos).getValue(CuveBlock.PEAUX);
                if (level.getBlockState(pos).getValue(CuveBlock.TANIN) <= peaux) {
                    utiliser(ouvrage, level, pos, player, stack -> stack.is(TannerieModule.TANINS));
                }
                if (!utiliser(ouvrage, level, pos, player, stack -> stack.is(TannerieModule.PEAU_RACLEE.get()))
                        || level.getBlockState(pos).getValue(CuveBlock.PEAUX) == peaux) {
                    break;
                }
            }
        }
    }

    /** La première case de l'inventaire qui convient travaille sur le bloc ; faux s'il n'y en a pas. */
    private static boolean utiliser(Ouvrage ouvrage, Level level, BlockPos pos, Player player, Predicate<ItemStack> convient) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++) {
            if (convient.test(inventory.getItem(slot))) {
                travailler(ouvrage, level, pos, player, slot);
                return true;
            }
        }
        return false;
    }

    private static void rendre(Player player, int depuis, ItemStack rendu) {
        if (depuis >= 0) {
            player.getInventory().setItem(depuis, rendu);
        } else {
            player.inventoryMenu.setCarried(rendu);
        }
    }

    private static void donner(Player player, ItemStack sorti) {
        Consumer<ItemStack> poser = stack -> {
            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        };
        poser.accept(sorti);
    }
}
