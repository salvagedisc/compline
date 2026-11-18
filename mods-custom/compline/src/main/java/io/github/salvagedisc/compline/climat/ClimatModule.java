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
import io.github.salvagedisc.compline.ModIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module climat : le climat pèse sur le corps (docs/plan/climat.md). Cold Sweat ne connaît que Serene Seasons ; ce
 * module lui donne la saison d'Ecliptic Seasons, par un modificateur de température {@code compline:saison} placé
 * juste après celui du biome, comme Cold Sweat place le sien pour Serene Seasons. La neige refroidit et se pose
 * sur les vêtements ({@code compline:tempete}), le vent de sable assoiffe (avec Thirst Was Reclaimed), et le client
 * voit les tempêtes ({@link io.github.salvagedisc.compline.climat.client.StormClient}).
 *
 * <p>La température se calcule sur le serveur : Cold Sweat l'envoie au client.
 */
public final class ClimatModule {
    public static final ResourceLocation SAISON = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "saison");
    public static final ResourceLocation TEMPETE = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "tempete");

    private ClimatModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(ClimatModule::registerModifiers);
        NeoForge.EVENT_BUS.addListener(ClimatModule::addDefaultModifiers);
        if (ModIds.isLoaded(ModIds.THIRST)) {
            NeoForge.EVENT_BUS.addListener(SoifDeSable::tick);
        }
        if (FMLEnvironment.dist.isClient()) {
            io.github.salvagedisc.compline.climat.client.StormClient.start();
        }
    }

    private static void registerModifiers(TempModifierRegisterEvent event) {
        event.register(SAISON, SaisonTempModifier::new);
        event.register(TEMPETE, TempeteTempModifier::new);
    }

    /**
     * Chaque entité qui a une température (le joueur, et les bêtes que Cold Sweat suit) reçoit la saison. Le rythme
     * suit celui que Cold Sweat donne à Serene Seasons : toutes les 3 secondes pour un joueur, plus lentement pour une bête.
     * La neige suit, chaque seconde pour un joueur : on la sent dès qu'on sort.
     */
    private static void addDefaultModifiers(DefaultTempModifiersEvent event) {
        boolean player = event.getEntity() instanceof Player;
        event.addModifierById(Temperature.Trait.WORLD, SAISON, mod -> mod.tickRate(player ? 60 : 400),
                Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof BiomeTempModifier).noDuplicates(Matcher.SAME_CLASS));
        event.addModifierById(Temperature.Trait.WORLD, TEMPETE, mod -> mod.tickRate(player ? 20 : 200),
                Placement.of(Mode.ADD_AFTER, Order.FIRST, mod -> mod instanceof SaisonTempModifier).noDuplicates(Matcher.SAME_CLASS));
    }
}
