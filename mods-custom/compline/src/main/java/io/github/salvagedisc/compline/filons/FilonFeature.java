package io.github.salvagedisc.compline.filons;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/**
 * Un filon : un boyau de minerai qui serpente sous le sol, en partant d'une profondeur donnée sous la surface,
 * puis quelques échantillons posés sur le sol juste au-dessus. Le filon reste à 12 blocs au plus de son départ
 * à l'horizontale, pour ne jamais écrire hors des chunks que la génération autorise.
 */
public class FilonFeature extends Feature<FilonConfig> {
    /** Écart horizontal maximal au départ (le départ est dans le chunk, l'écriture va jusqu'à 16 blocs autour). */
    private static final int BORNE = 12;
    private static final TagKey<Block> GRES_ROUGE = BlockTags.create(ResourceLocation.parse("c:sandstone/red_blocks"));
    private static final TagKey<Block> GRES = BlockTags.create(ResourceLocation.parse("c:sandstone/blocks"));

    public FilonFeature(Codec<FilonConfig> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<FilonConfig> ctx) {
        FilonConfig cfg = ctx.config();
        WorldGenLevel level = ctx.level();
        RandomSource random = ctx.random();
        BlockPos origin = ctx.origin();

        int sol = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, origin.getX(), origin.getZ());
        int y0 = Math.min(sol - 8, Math.max(cfg.yMin(), sol - cfg.profondeur().sample(random)));
        if (y0 <= level.getMinBuildHeight() + 2) {
            return false;
        }

        double x = origin.getX() + 0.5, y = y0 + 0.5, z = origin.getZ() + 0.5;
        float lacet = random.nextFloat() * Mth.TWO_PI;
        float tangage = (random.nextFloat() - 0.5f) * 0.4f;
        int pas = cfg.longueur().sample(random);
        int poses = 0;
        BlockPos.MutableBlockPos cur = new BlockPos.MutableBlockPos();
        for (int i = 0; i < pas; i++) {
            float r = cfg.rayon().sample(random);
            poses += boule(level, random, cfg, x, y, z, r, cur);
            x += Mth.cos(lacet) * Mth.cos(tangage);
            z += Mth.sin(lacet) * Mth.cos(tangage);
            y += Mth.sin(tangage);
            lacet += (random.nextFloat() - 0.5f) * 0.6f;
            tangage = Mth.clamp(tangage + (random.nextFloat() - 0.5f) * 0.2f, -0.45f, 0.45f);
            if (Math.abs(x - origin.getX()) > BORNE || Math.abs(z - origin.getZ()) > BORNE) {
                lacet += Mth.PI;
                x = Mth.clamp(x, origin.getX() - BORNE, origin.getX() + BORNE);
                z = Mth.clamp(z, origin.getZ() - BORNE, origin.getZ() + BORNE);
            }
            if (y < cfg.yMin() || y > sol - 6) {
                tangage = -tangage;
                y = Mth.clamp(y, cfg.yMin(), sol - 6);
            }
        }
        if (poses == 0) {
            return false;
        }
        cfg.echantillon().ifPresent(e -> affleurer(level, random, cfg, origin, e));
        return true;
    }

    private static int boule(WorldGenLevel level, RandomSource random, FilonConfig cfg, double cx, double cy, double cz,
            float r, BlockPos.MutableBlockPos cur) {
        int poses = 0;
        int rr = Mth.ceil(r);
        for (int dx = -rr; dx <= rr; dx++) {
            for (int dy = -rr; dy <= rr; dy++) {
                for (int dz = -rr; dz <= rr; dz++) {
                    double d2 = dx * dx + dy * dy + dz * dz;
                    if (d2 > r * r) {
                        continue;
                    }
                    // Plus dense au cœur, plus clairsemé sur les bords.
                    double chance = cfg.densite() * (1 - 0.6 * Math.sqrt(d2) / r);
                    if (random.nextDouble() >= chance) {
                        continue;
                    }
                    cur.set(Mth.floor(cx) + dx, Mth.floor(cy) + dy, Mth.floor(cz) + dz);
                    if (!level.ensureCanWrite(cur)) {
                        continue;
                    }
                    BlockState roche = level.getBlockState(cur);
                    for (OreConfiguration.TargetBlockState cible : cfg.cibles()) {
                        if (cible.target.test(roche, random)) {
                            BlockState pose = cfg.blocBrut().isPresent() && random.nextFloat() < cfg.chanceBrut()
                                    ? cfg.blocBrut().get() : cible.state;
                            level.setBlock(cur, pose, 2);
                            poses++;
                            break;
                        }
                    }
                }
            }
        }
        return poses;
    }

    /** Pose les échantillons sur le sol au-dessus du filon, à l'air libre ou sous l'eau. */
    private static void affleurer(WorldGenLevel level, RandomSource random, FilonConfig cfg, BlockPos origin, BlockState echantillon) {
        int voulus = cfg.affleurements().sample(random);
        int e = cfg.etendue();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int essai = 0, poses = 0; essai < voulus * 4 && poses < voulus; essai++) {
            int x = origin.getX() + random.nextInt(2 * e + 1) - e;
            int z = origin.getZ() + random.nextInt(2 * e + 1) - e;
            pos.set(x, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z), z);
            if (!level.ensureCanWrite(pos)) {
                continue;
            }
            BlockState ici = level.getBlockState(pos);
            boolean eau = ici.getFluidState().is(FluidTags.WATER) && ici.getFluidState().isSource();
            if (!ici.isAir() && !(eau && ici.is(Blocks.WATER))) {
                continue;
            }
            BlockPos dessous = pos.below();
            BlockState sol = level.getBlockState(dessous);
            if (!sol.isFaceSturdy(level, dessous, Direction.UP) || sol.is(BlockTags.LEAVES)) {
                continue;
            }
            BlockState pose = avec(echantillon, "flavour", saveur(sol));
            if (pose.hasProperty(BlockStateProperties.WATERLOGGED)) {
                pose = pose.setValue(BlockStateProperties.WATERLOGGED, eau);
            }
            level.setBlock(pos, pose, 2);
            poses++;
        }
    }

    /** La roche de l'échantillon suit le sol : grès rouge, grès ou pierre. */
    private static String saveur(BlockState sol) {
        if (sol.is(Blocks.RED_SAND) || sol.is(GRES_ROUGE)) {
            return "red_sandy";
        }
        if (sol.is(BlockTags.SAND) || sol.is(GRES)) {
            return "sandy";
        }
        return "stony";
    }

    private static BlockState avec(BlockState state, String nom, String valeur) {
        Property<?> p = state.getBlock().getStateDefinition().getProperty(nom);
        return p == null ? state : avec(state, p, valeur);
    }

    private static <T extends Comparable<T>> BlockState avec(BlockState state, Property<T> p, String valeur) {
        Optional<T> v = p.getValue(valeur);
        return v.isPresent() ? state.setValue(p, v.get()) : state;
    }
}
