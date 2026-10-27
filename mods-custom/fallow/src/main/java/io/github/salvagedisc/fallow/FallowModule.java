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
    /** Boire au tuyau de Traveler's Backpack désaltère dans Thirst Was Reclaimed (client et serveur). */
    SAC("sac", null, bus -> io.github.salvagedisc.fallow.sac.SacModule.start(bus), ModIds.TRAVELERS_BACKPACK, ModIds.THIRST),
    /** Plus d'expérience : l'enclume sans niveaux, la barre cachée (client et serveur). Aucun mod requis. */
    XP("xp", null, bus -> io.github.salvagedisc.fallow.xp.XpModule.start(bus)),
    /** Le paysage lointain de Distant Horizons suit la brume : caché dans la brume, visible là où elle se lève (client et serveur). */
    HORIZON("horizon", null, bus -> io.github.salvagedisc.fallow.horizon.HorizonModule.start(bus), ModIds.DISTANT_HORIZONS),
    /** La brume se lève : feu de veille et lanterne de clairière changent les biomes autour d'eux (client et serveur). Aucun mod requis. */
    CLAIRIERE("clairiere", null, bus -> io.github.salvagedisc.fallow.clairiere.ClairiereModule.start(bus)),
    /** Le dissipateur, troisième palier des clairières : un bloc cinétique de Create (client et serveur). */
    DISSIPATEUR("dissipateur", null, bus -> io.github.salvagedisc.fallow.dissipateur.DissipateurModule.start(bus), ModIds.CREATE),
    /** Le nom de chaque écran sur une plaque au-dessus du panneau, dessinée par le pack d'interface (client). Aucun mod requis. */
    TITRES("titres", Dist.CLIENT, bus -> io.github.salvagedisc.fallow.titres.TitresModule.start(bus));

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
