package io.github.salvagedisc.fallow.tannerie;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Le travail d'un bloc de la tannerie, le même au clic dans le monde et au poste (module postes) : là, l'objet vient du curseur
 * ou d'une case de l'inventaire au lieu de la main, et ce qui sort va droit dans l'inventaire.
 */
public interface Ouvrage {
    /**
     * Ce que {@code stack} fait sur le bloc. {@code rendre} reçoit l'objet qui le remplace (le seau vide après l'eau),
     * {@code produit} ce qui sort du bloc (la peau raclée, le gras).
     */
    ItemInteractionResult travailler(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            Consumer<ItemStack> rendre, Consumer<ItemStack> produit);

    /** La main vide : reprendre ce qui est fini, ou dire où en est le travail. */
    InteractionResult mainVide(BlockState state, Level level, BlockPos pos, Player player);
}
