package io.github.salvagedisc.fallow.xp;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * Module xp : Fallow n'a plus d'expérience. Les orbes et les gains sont coupés par
 * {@code kubejs/server_scripts/sans_xp.js} ; ce module fait ce que KubeJS ne sait pas faire.
 *
 * <ul>
 *   <li>L'enclume ne demande plus de niveaux (mixin {@code xp.AnvilMenuMixin}). Son coût devient
 *       une usure : au-delà de 39, l'objet ne passe plus (« Trop usé ! »).</li>
 *   <li>Facultatif ({@code xp.blockEnchantMerge}) : l'enclume refuse un second objet qui porte des
 *       enchantements (livre enchanté ou objet enchanté), pour que l'enchantement reste à la table.</li>
 *   <li>Chez le client, la barre d'expérience et le chiffre du niveau sont cachés ({@link XpClient}).</li>
 * </ul>
 */
public final class XpModule {
    private XpModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(AnvilUpdateEvent.class, XpModule::onAnvilUpdate);
        if (FMLEnvironment.dist.isClient()) {
            XpClient.register();
        }
        // La config n'est pas encore chargée ici : ses valeurs sont lues à chaque usage.
        Fallow.LOGGER.info("xp : enclume sans niveaux, barre d'expérience cachée (selon config/fallow-common.toml)");
    }

    /** Tourne chez le client et sur le serveur, à chaque changement d'un emplacement de l'enclume. */
    private static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!FallowConfig.xpBlockEnchantMerge()) {
            return;
        }
        ItemStack right = event.getRight();
        if (!right.isEmpty() && EnchantmentHelper.hasAnyEnchantments(right)) {
            // Annulé : pas de résultat, coût 0 (CommonHooks.onAnvilChange).
            event.setCanceled(true);
        }
    }
}
