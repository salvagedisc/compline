package io.github.salvagedisc.compline.filons;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/**
 * Réglages d'un filon.
 *
 * @param cibles       roche remplacée et minerai posé à sa place (comme un filon de minerai du jeu)
 * @param blocBrut     bloc de minerai brut semé dans le filon, s'il y en a un
 * @param chanceBrut   part des blocs du filon qui deviennent du minerai brut
 * @param longueur     nombre de pas du filon
 * @param rayon        rayon du filon à chaque pas
 * @param densite      part de la roche du filon qui devient minerai, au cœur
 * @param profondeur   profondeur du filon sous le sol, à son départ
 * @param yMin         hauteur la plus basse du départ
 * @param echantillon  échantillon posé en surface au-dessus du filon (Surface Samples), s'il y en a un
 * @param affleurements nombre d'échantillons en surface
 * @param etendue      écart horizontal maximal d'un échantillon autour du départ du filon
 */
public record FilonConfig(List<OreConfiguration.TargetBlockState> cibles, Optional<BlockState> blocBrut, float chanceBrut,
        IntProvider longueur, FloatProvider rayon, float densite, IntProvider profondeur, int yMin,
        Optional<BlockState> echantillon, IntProvider affleurements, int etendue) implements FeatureConfiguration {

    public static final Codec<FilonConfig> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.list(OreConfiguration.TargetBlockState.CODEC).fieldOf("cibles").forGetter(FilonConfig::cibles),
            BlockState.CODEC.optionalFieldOf("bloc_brut").forGetter(FilonConfig::blocBrut),
            Codec.floatRange(0, 1).optionalFieldOf("chance_brut", 0f).forGetter(FilonConfig::chanceBrut),
            IntProvider.codec(1, 64).fieldOf("longueur").forGetter(FilonConfig::longueur),
            FloatProvider.codec(0.5f, 3f).fieldOf("rayon").forGetter(FilonConfig::rayon),
            Codec.floatRange(0, 1).fieldOf("densite").forGetter(FilonConfig::densite),
            IntProvider.codec(6, 128).fieldOf("profondeur").forGetter(FilonConfig::profondeur),
            Codec.INT.optionalFieldOf("y_min", -48).forGetter(FilonConfig::yMin),
            BlockState.CODEC.optionalFieldOf("echantillon").forGetter(FilonConfig::echantillon),
            IntProvider.codec(0, 16).optionalFieldOf("affleurements", net.minecraft.util.valueproviders.ConstantInt.of(0))
                    .forGetter(FilonConfig::affleurements),
            Codec.intRange(0, 12).optionalFieldOf("etendue", 8).forGetter(FilonConfig::etendue)
    ).apply(i, FilonConfig::new));
}
