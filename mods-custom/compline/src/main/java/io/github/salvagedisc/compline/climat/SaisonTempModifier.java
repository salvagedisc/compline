package io.github.salvagedisc.compline.climat;

import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.teamtea.eclipticseasons.api.constant.climate.BiomeClimateSettings;
import com.teamtea.eclipticseasons.api.constant.solar.SolarTerm;
import com.teamtea.eclipticseasons.common.core.SolarHolders;
import com.teamtea.eclipticseasons.common.core.biome.BiomeClimateManager;
import com.teamtea.eclipticseasons.common.core.solar.SolarDataManager;
import io.github.salvagedisc.compline.ComplineConfig;
import java.util.function.Function;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * La saison d'Ecliptic Seasons dans la température de Cold Sweat.
 *
 * <p>Ecliptic découpe l'année en 24 termes solaires et donne à chaque biome un écart de température par terme (celui
 * qui fait geler l'eau et tomber la neige, et qui règle ses cultures). On reprend cet écart au lieu d'inventer une
 * courbe : un biome qu'Ecliptic garde doux l'hiver (une jungle, un désert configurés à part) le reste aussi pour le
 * corps. L'écart glisse d'un terme au suivant au fil des jours, pour que le froid vienne peu à peu et non d'un coup.
 *
 * <p>L'écart est en unités de biome ; Cold Sweat compte ses températures dans la même unité (1 = 25 °C), multipliée
 * ici par la force de la saison ({@link ComplineConfig#climatSeasonStrength()}).
 */
public final class SaisonTempModifier extends TempModifier {
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        double offset = offset(entity.level(), entity.level().getBiome(entity.blockPosition()).value());
        return temp -> temp + offset;
    }

    /** L'écart de saison du biome, déjà multiplié par la force réglée ; 0 hors des dimensions à saisons. */
    static double offset(Level level, Biome biome) {
        double strength = ComplineConfig.climatSeasonStrength();
        if (strength <= 0.0) {
            return 0.0;
        }
        SolarDataManager solar = SolarHolders.getSaveData(level);
        if (solar == null || !solar.isValidDimension()) {
            return 0.0;
        }
        SolarTerm term = solar.getSolarTerm();
        if (term == SolarTerm.NONE) {
            return 0.0;
        }
        BiomeClimateSettings climate = BiomeClimateManager.getBiomeClimateSettings(biome, !level.isClientSide());
        int lasting = Math.max(1, solar.getSolarTermLastingDays());
        // Au milieu du premier jour d'un terme on est presque à son écart ; au dernier jour, on glisse vers le suivant.
        float progress = (solar.getSolarTermDaysInPeriod() + 0.5F) / lasting;
        float change = Mth.lerp(progress, climate.getTemperatureChange(term), climate.getTemperatureChange(solar.getNextSolarTerm()));
        return change * strength;
    }
}
