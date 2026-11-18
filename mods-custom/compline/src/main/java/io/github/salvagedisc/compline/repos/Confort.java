package io.github.salvagedisc.compline.repos;

import io.github.salvagedisc.compline.Compline;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Le confort d'un lieu : le nombre de familles d'objets différentes autour d'un point (un lit, une chaise, une
 * table, un tapis, un coffre, des livres, une bougie, un pot de fleurs, un coin cuisine). Compter des familles et
 * non des blocs récompense une maison variée, pas un entrepôt de chaises. Chaque famille est un tag de blocs
 * {@code compline:confort/<famille>}, que le pack peut compléter. Prévu pour servir aussi au module vie.
 */
public final class Confort {
    public static final List<TagKey<Block>> FAMILIES = List.of("couchage", "siege", "table", "tapis", "rangement",
            "livres", "lumiere", "decoration", "cuisine").stream()
            .map(name -> TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "confort/" + name)))
            .toList();

    private Confort() {
    }

    /** Nombre de familles présentes dans un pavé de rayon {@code radius} (hauteur : {@code height} de part et d'autre). */
    public static int count(Level level, BlockPos center, int radius, int height) {
        int found = 0;
        int all = (1 << FAMILIES.size()) - 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -height; y <= height && found != all; y++) {
            for (int x = -radius; x <= radius && found != all; x++) {
                for (int z = -radius; z <= radius; z++) {
                    pos.setWithOffset(center, x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) {
                        continue;
                    }
                    for (int i = 0; i < FAMILIES.size(); i++) {
                        if ((found & (1 << i)) == 0 && state.is(FAMILIES.get(i))) {
                            found |= 1 << i;
                        }
                    }
                }
            }
        }
        return Integer.bitCount(found);
    }
}
