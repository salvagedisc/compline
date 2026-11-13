package io.github.salvagedisc.fallow;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;

/**
 * Les modules de Fallow. Un module ne démarre que si tous les mods qu'il relie sont présents ;
 * sa classe n'est jamais chargée sinon, et ses mixins sont écartés par {@link io.github.salvagedisc.fallow.mixin.FallowMixinPlugin}.
 * L'interrupteur de chaque module est un booléen de {@link FallowConfig}, lu à chaque usage.
 */
public enum FallowModule {
    /** La lune d'Enhanced Celestials et la brume sous ATMOSPHERICS (client). La lune demande aussi Enhanced Celestials. */
    CIEL("ciel", Dist.CLIENT, bus -> io.github.salvagedisc.fallow.ciel.CielModule.start(bus), ModIds.ATMOSPHERICS),
    /** Plus d'expérience : l'enclume sans niveaux, la barre cachée (client et serveur). Aucun mod requis. */
    XP("xp", null, bus -> io.github.salvagedisc.fallow.xp.XpModule.start(bus)),
    /** Le paysage lointain de Distant Horizons suit la brume : caché dans la brume, visible là où elle se lève (client et serveur). */
    HORIZON("horizon", null, bus -> io.github.salvagedisc.fallow.horizon.HorizonModule.start(bus), ModIds.DISTANT_HORIZONS),
    /** La brume se lève autour du feu de veille et de la lanterne de clairière, sans changer le biome (client et serveur). Aucun mod requis. */
    CLAIRIERE("clairiere", null, bus -> io.github.salvagedisc.fallow.clairiere.ClairiereModule.start(bus)),
    /** Le dissipateur, troisième palier des clairières : un bloc cinétique de Create (client et serveur). */
    DISSIPATEUR("dissipateur", null, bus -> io.github.salvagedisc.fallow.dissipateur.DissipateurModule.start(bus), ModIds.CREATE),
    /** Le nom de chaque écran sur une plaque au-dessus du panneau, dessinée par le pack d'interface (client). Aucun mod requis. */
    TITRES("titres", Dist.CLIENT, bus -> io.github.salvagedisc.fallow.titres.TitresModule.start(bus)),
    /** À mains nues, on ramasse mais on ne coupe pas : le bois et la roche demandent le bon outil (client et serveur). Aucun mod requis. */
    OUTILS("outils", null, bus -> io.github.salvagedisc.fallow.outils.OutilsModule.start(bus)),
    /** Pluie teintée et à contre-jour, rideaux de pluie, éclair, feuilles et brins d'herbe dans le vent (client). Aucun mod requis ;
     *  la teinte demande Where Winds Blow, les feuilles Particular. */
    AVERSE("averse", Dist.CLIENT, bus -> io.github.salvagedisc.fallow.averse.AverseModule.start(bus)),
    /** Rayons de soleil dans les trouées et éclats du soleil sur l'eau (client). Aucun mod requis. */
    RAYONS("rayons", Dist.CLIENT, bus -> io.github.salvagedisc.fallow.rayons.RayonsModule.start(bus)),
    /** Les monuments d'Ancient Remnants posés seulement sur un terrain qui s'y prête : type de structure
     *  {@code fallow:terrain_checked} (client et serveur). Aucun mod requis. */
    MONUMENTS("monuments", null, bus -> io.github.salvagedisc.fallow.monuments.MonumentsModule.start(bus));

    private final String id;
    private final Dist side;
    private final List<String> requiredMods;
    private final Consumer<IEventBus> starter;

    FallowModule(String id, Dist side, Consumer<IEventBus> starter, String... requiredMods) {
        this.id = id;
        this.side = side;
        this.requiredMods = List.of(requiredMods);
        this.starter = starter;
    }

    public String id() {
        return id;
    }

    public List<String> requiredMods() {
        return requiredMods;
    }

    /** Le module tourne-t-il de ce côté ? ({@code null} : client et serveur.) */
    public boolean runsOn(Dist dist) {
        return side == null || side == dist;
    }

    public boolean isAvailable() {
        return requiredMods.stream().allMatch(ModIds::isLoaded);
    }

    public List<String> missingMods() {
        return requiredMods.stream().filter(m -> !ModIds.isLoaded(m)).toList();
    }

    void start(IEventBus modBus) {
        starter.accept(modBus);
    }

    @Override
    public String toString() {
        return id + Arrays.toString(requiredMods.toArray());
    }
}
