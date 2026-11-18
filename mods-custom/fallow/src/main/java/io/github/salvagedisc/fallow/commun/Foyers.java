package io.github.salvagedisc.fallow.commun;

import io.github.salvagedisc.fallow.Fallow;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Les feux du tag {@code fallow:foyers} (feux de camp, fourneaux, poêles…), communs au repos au foyer et au
 * séchoir de la tannerie : un foyer compte s'il est allumé, ou s'il n'a pas d'état allumé.
 */
public final class Foyers {
    public static final TagKey<Block> TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "foyers"));

    private Foyers() {
    }

    public static boolean lit(BlockState state) {
        return state.is(TAG) && (!state.hasProperty(BlockStateProperties.LIT) || state.getValue(BlockStateProperties.LIT));
    }

    /** Vrai si un foyer allumé est à {@code radius} blocs (à plat) et {@code height} blocs (en hauteur) de {@code center}. */
    public static boolean near(Level level, BlockPos center, int radius, int height) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -height; y <= height; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (lit(level.getBlockState(pos.setWithOffset(center, x, y, z)))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
