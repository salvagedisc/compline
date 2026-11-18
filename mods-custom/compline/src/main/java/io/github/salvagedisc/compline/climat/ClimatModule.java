package io.github.salvagedisc.compline.climat;

import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.BiomeTempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Mode;
import com.momosoftworks.coldsweat.api.util.placement.Order;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import io.github.salvagedisc.compline.Compline;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module climat : le climat pèse sur le corps (docs/plan/climat.md). Cold Sweat ne connaît que Serene Seasons ; ce
 * module lui donne la saison d'Ecliptic Seasons, par un modificateur de température {@code compline:saison} placé
 * juste après celui du biome, comme Cold Sweat place le sien pour Serene Seasons.
 *
 * <p>Tout se passe sur le serveur : Cold Sweat calcule la température là et l'envoie au client.
 */
public final class ClimatModule {
    public static final ResourceLocation SAISON = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "saison");

    private ClimatModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClimatModule::registerModifiers);
        NeoForge.EVENT_BUS.addListener(ClimatModule::addDefaultModifiers);
    }

    private static void registerModifiers(TempModifierRegisterEvent event) {
        event.register(SAISON, SaisonTempModifier::new);
    }

    /**
     * Chaque entité qui a une température (le joueur, et les bêtes que Cold Sweat suit) reçoit la saison. Le rythme
     * suit celui que Cold Sweat donne à Serene Seasons : toutes les 3 secondes pour un joueur, plus lentement pour une bête.
     */
    private static void addDefaultModifiers(DefaultTempModifiersEvent event) {
        int tickRate = event.getEntity() instanceof Player ? 60 : 400;
        event.addModifierById(Temperature.Trait.WORLD, SAISON, mod -> mod.tickRate(tickRate),
                Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));
    }
}
