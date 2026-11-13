package io.github.salvagedisc.tilth;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;

/**
 * Les modules de Tilth. Un module ne démarre que si tous les mods qu'il relie sont présents ;
 * sa classe n'est jamais chargée sinon, et ses mixins sont écartés par {@link io.github.salvagedisc.tilth.mixin.TilthMixinPlugin}.
 * L'interrupteur de chaque module est un booléen de {@link TilthConfig}, lu à chaque usage.
 */
public enum TilthModule {
    /** La lune d'Enhanced Celestials et la brume sous ATMOSPHERICS (client). La lune demande aussi Enhanced Celestials. */
    CIEL("ciel", Dist.CLIENT, bus -> io.github.salvagedisc.tilth.ciel.CielModule.start(bus), ModIds.ATMOSPHERICS),
    /** Plus d'expérience : l'enclume sans niveaux, la barre cachée (client et serveur). Aucun mod requis. */
    XP("xp", null, bus -> io.github.salvagedisc.tilth.xp.XpModule.start(bus)),
    /** Le paysage lointain de Distant Horizons suit la brume : caché dans la brume, visible là où elle se lève (client et serveur). */
    HORIZON("horizon", null, bus -> io.github.salvagedisc.tilth.horizon.HorizonModule.start(bus), ModIds.DISTANT_HORIZONS),
    /** La brume se lève autour du feu de veille et de la lanterne de clairière, sans changer le biome (client et serveur). Aucun mod requis. */
    CLAIRIERE("clairiere", null, bus -> io.github.salvagedisc.tilth.clairiere.ClairiereModule.start(bus)),
    /** Le dissipateur, troisième palier des clairières : un bloc cinétique de Create (client et serveur). */
    DISSIPATEUR("dissipateur", null, bus -> io.github.salvagedisc.tilth.dissipateur.DissipateurModule.start(bus), ModIds.CREATE),
    /** À mains nues, on ramasse mais on ne coupe pas : le bois et la roche demandent le bon outil (client et serveur). Aucun mod requis. */
    OUTILS("outils", null, bus -> io.github.salvagedisc.tilth.outils.OutilsModule.start(bus)),
    /** Rideaux de pluie, éclair, feuilles et brins d'herbe dans le vent (client). Aucun mod requis ;
     *  les feuilles demandent Particular. */
    AVERSE("averse", Dist.CLIENT, bus -> io.github.salvagedisc.tilth.averse.AverseModule.start(bus)),
    /** Éclats du soleil sur l'eau (client). Aucun mod requis. */
    RAYONS("rayons", Dist.CLIENT, bus -> io.github.salvagedisc.tilth.rayons.RayonsModule.start(bus)),
    /** Les monuments d'Ancient Remnants posés seulement sur un terrain qui s'y prête : type de structure
     *  {@code tilth:terrain_checked} (client et serveur). Aucun mod requis. */
    MONUMENTS("monuments", null, bus -> io.github.salvagedisc.tilth.monuments.MonumentsModule.start(bus)),
    /** Le bord du monde : un cercle que la brume ne laisse pas franchir, donné par {@code data/tilth/monde/bord.json}
     *  (client et serveur ; tout se passe sur le serveur). Aucun mod requis. */
    BORD("bord", null, bus -> io.github.salvagedisc.tilth.bord.BordModule.start(bus));

    private final String id;
    private final Dist side;
    private final List<String> requiredMods;
    private final Consumer<IEventBus> starter;

    TilthModule(String id, Dist side, Consumer<IEventBus> starter, String... requiredMods) {
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
