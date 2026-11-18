package io.github.salvagedisc.fallow.climat;

import com.teamtea.eclipticseasons.api.util.EclipticUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Le temps qu'il fait en un point, vu par le corps : neige, tempête de neige ou tempête de sable. Le même calcul sert
 * au serveur (la température, la soif) et au client (la poussière, la neige soufflée, la brume) : il ne lit que ce
 * que les deux côtés connaissent, la météo du monde et la précipitation qu'Ecliptic Seasons donne au biome.
 *
 * <ul>
 *   <li><b>Neige</b> : il neige là où l'on est (Ecliptic décide pluie ou neige selon le biome et la saison).</li>
 *   <li><b>Tempête de neige</b> : il neige et l'orage gronde.</li>
 *   <li><b>Tempête de sable</b> : le monde est sous la pluie, mais rien ne tombe dans un biome chaud et sec par nature
 *       (désert, badlands, savane hors de sa saison des pluies) : le front de pluie y devient un vent de sable, plus
 *       fort sous l'orage.</li>
 * </ul>
 *
 * @param kind ce qui tombe ou souffle
 * @param strength de 0 à 1 : 0,4 pour une simple neige, 0,6 pour un vent de sable, 1 sous l'orage
 */
public record Storm(Kind kind, float strength) {
    public static final Storm CALM = new Storm(Kind.CALM, 0.0F);

    /** Température de biome au-dessus de laquelle un biome sans précipitation est un pays de sable. */
    private static final float HOT = 1.0F;

    public enum Kind {
        CALM,
        SNOW,
        SAND
    }

    public boolean isSnow() {
        return kind == Kind.SNOW;
    }

    public boolean isSand() {
        return kind == Kind.SAND;
    }

    /** Une vraie tempête : neige sous l'orage, ou vent de sable. */
    public boolean isStorm() {
        return kind == Kind.SAND || (kind == Kind.SNOW && strength >= 1.0F);
    }

    /** Ce qui tombe ou souffle en {@code pos}, à l'abri ou non (voir {@link #exposed}). */
    public static Storm at(Level level, BlockPos pos) {
        if (!level.isRaining()) {
            return CALM;
        }
        boolean thunder = level.isThundering();
        Biome biome = level.getBiome(pos).value();
        Biome.Precipitation precipitation = EclipticUtil.getRainOrSnow(level, biome, pos);
        if (precipitation == Biome.Precipitation.SNOW) {
            return new Storm(Kind.SNOW, thunder ? 1.0F : 0.4F);
        }
        // Ecliptic fait pleuvoir certains biomes secs à la saison des pluies (savane) : ce qu'il dit l'emporte, et le
        // biome n'est un pays de sable que là où il ne tombe rien et où ses données d'origine ne prévoient aucune pluie.
        if (precipitation == Biome.Precipitation.NONE && !biome.getModifiedClimateSettings().hasPrecipitation()
                && biome.getBaseTemperature() >= HOT) {
            return new Storm(Kind.SAND, thunder ? 1.0F : 0.6F);
        }
        return CALM;
    }

    /** À découvert : rien au-dessus de la tête jusqu'au ciel. */
    public static boolean exposed(Level level, BlockPos head) {
        return level.canSeeSky(head);
    }
}
