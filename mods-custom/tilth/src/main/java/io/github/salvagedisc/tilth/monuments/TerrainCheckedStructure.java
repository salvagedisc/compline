package io.github.salvagedisc.tilth.monuments;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.salvagedisc.tilth.Tilth;
import java.util.Optional;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

/**
 * Une structure posée seulement si le terrain s'y prête. La structure enveloppée ({@code structure}) est
 * assemblée comme d'habitude, puis le terrain est lu sous son emprise (agrandie de {@code margin} blocs),
 * tous les {@code step} blocs, avant que le moindre bloc ne soit généré :
 * <ul>
 *   <li>{@code max_height_difference} : écart maximal entre le point le plus haut et le plus bas du sol ;</li>
 *   <li>{@code max_water_share} : part maximale des points couverts d'eau (0 = aucun) ;</li>
 *   <li>{@code min_clearance} : si présent, le sol doit rester au moins à cette distance sous le bas de la
 *       structure (un monolithe qui flotte ne doit pas entrer dans une colline) ;</li>
 *   <li>{@code top_margin} : le haut de la structure reste à cette distance sous le plafond du monde.</li>
 * </ul>
 * Les biomes, l'étape et l'adaptation du terrain sont ceux de cette structure ; ceux de la structure
 * enveloppée sont aussi vérifiés, à son propre point de départ.
 */
public final class TerrainCheckedStructure extends Structure {
    public static final MapCodec<TerrainCheckedStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Structure.DIRECT_CODEC.fieldOf("structure").forGetter(s -> s.inner),
            Check.CODEC.fieldOf("check").forGetter(s -> s.check)
    ).apply(instance, TerrainCheckedStructure::new));

    private final Structure inner;
    private final Check check;

    public TerrainCheckedStructure(StructureSettings settings, Structure inner, Check check) {
        super(settings);
        this.inner = inner;
        this.check = check;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        Optional<GenerationStub> found = inner.findValidGenerationPoint(context);
        if (found.isEmpty()) {
            return found;
        }
        GenerationStub stub = found.get();
        StructurePiecesBuilder builder = stub.getPiecesBuilder();
        if (builder.isEmpty()) {
            return Optional.empty();
        }
        String refusal = check.refusal(builder.getBoundingBox(), context.chunkGenerator(), context.heightAccessor(),
                context.randomState());
        if (refusal != null) {
            Tilth.LOGGER.debug("monuments : {} refusé en {} ({})", inner.type(), stub.position(), refusal);
            return Optional.empty();
        }
        return Optional.of(new GenerationStub(stub.position(), Either.right(builder)));
    }

    @Override
    public StructureType<?> type() {
        return MonumentsModule.TERRAIN_CHECKED.get();
    }

    /** Les règles de terrain d'une structure. */
    public record Check(int margin, int step, int maxHeightDifference, float maxWaterShare,
                        Optional<Integer> minClearance, int topMargin) {
        public static final Codec<Check> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 64).optionalFieldOf("margin", 4).forGetter(Check::margin),
                Codec.intRange(2, 32).optionalFieldOf("step", 8).forGetter(Check::step),
                Codec.intRange(0, 384).optionalFieldOf("max_height_difference", 16).forGetter(Check::maxHeightDifference),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("max_water_share", 0.0F).forGetter(Check::maxWaterShare),
                Codec.intRange(0, 256).optionalFieldOf("min_clearance").forGetter(Check::minClearance),
                Codec.intRange(0, 128).optionalFieldOf("top_margin", 4).forGetter(Check::topMargin)
        ).apply(instance, Check::new));

        /** {@code null} si le terrain convient, sinon la raison du refus (pour le journal). */
        String refusal(BoundingBox box, ChunkGenerator generator, LevelHeightAccessor level, RandomState random) {
            if (box.maxY() > level.getMaxBuildHeight() - 1 - topMargin) {
                return "trop haut : " + box.maxY();
            }
            int low = Integer.MAX_VALUE;
            int high = Integer.MIN_VALUE;
            int samples = 0;
            int wet = 0;
            int x0 = box.minX() - margin;
            int x1 = box.maxX() + margin;
            int z0 = box.minZ() - margin;
            int z1 = box.maxZ() + margin;
            for (int x = x0; x <= x1 + step - 1; x += step) {
                for (int z = z0; z <= z1 + step - 1; z += step) {
                    int sx = Math.min(x, x1);
                    int sz = Math.min(z, z1);
                    int surface = generator.getBaseHeight(sx, sz, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                    int floor = generator.getBaseHeight(sx, sz, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                    samples++;
                    if (floor < surface) {
                        wet++;
                    }
                    low = Math.min(low, floor);
                    high = Math.max(high, floor);
                }
            }
            if (high - low > maxHeightDifference) {
                return "pente : " + (high - low);
            }
            if (wet > maxWaterShare * samples) {
                return "eau : " + wet + "/" + samples;
            }
            if (minClearance.isPresent() && high > box.minY() - minClearance.get()) {
                return "sol trop près : " + high + " sous " + box.minY();
            }
            return null;
        }
    }
}
