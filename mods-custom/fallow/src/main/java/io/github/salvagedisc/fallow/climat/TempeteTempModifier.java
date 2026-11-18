package io.github.salvagedisc.fallow.climat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import io.github.salvagedisc.fallow.FallowConfig;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

/**
 * La neige dans la température de Cold Sweat : froid quand elle tombe sur soi, et neige qui reste sur les épaules.
 *
 * <p>À découvert sous la neige, le froid s'ajoute tout de suite ({@link FallowConfig#climatSnowCold()}, deux fois
 * et demie plus fort sous une tempête). La neige se pose aussi peu à peu sur les vêtements : en une minute et demie
 * de neige simple, en une demi-minute de tempête, on en est couvert, et ce « mouillé » refroidit encore
 * ({@link FallowConfig#climatSnowWet()}). À l'abri, elle fond en une minute, en vingt secondes près d'un feu ou dans
 * un lieu tiède. Cold Sweat ne mouille le corps que sous la pluie : la neige en était exclue.
 *
 * <p>La neige posée est gardée dans les données du modificateur, que Cold Sweat enregistre avec le joueur.
 */
public final class TempeteTempModifier extends TempModifier {
    private static final String COVER = "neige";

    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        if (!FallowConfig.climatEnabled()) {
            return temp -> temp;
        }
        BlockPos head = BlockPos.containing(entity.getEyePosition());
        Storm storm = Storm.at(entity.level(), head);
        boolean exposed = Storm.exposed(entity.level(), head);
        double seconds = Math.max(1, getTickRate()) / 20.0;

        float cover = getNBT().getFloat(COVER);
        float before = cover;
        if (storm.isSnow() && exposed && !entity.isInWater()) {
            cover += (float) (seconds * storm.strength() / 36.0);
        } else {
            // Fond plus vite là où il fait doux : la température du lieu, biome et feux compris, sans ce modificateur.
            boolean mild = getLastInput(trait) > 0.4 || entity.isInWater();
            cover -= (float) (seconds / (mild ? 20.0 : 60.0));
        }
        cover = Mth.clamp(cover, 0.0F, 1.0F);
        if (cover != before) {
            getNBT().putFloat(COVER, cover);
            markDirty();
        }

        double fall = storm.isSnow() && exposed ? (storm.isStorm() ? 2.5 : 1.0) : 0.0;
        double offset = -(fall * FallowConfig.climatSnowCold() + cover * FallowConfig.climatSnowWet());
        return temp -> temp + offset;
    }

    /** La part de neige posée sur les vêtements, de 0 à 1. */
    public float cover() {
        return getNBT().getFloat(COVER);
    }
}
