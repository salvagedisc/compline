package io.github.salvagedisc.fallow.postes.client;

import java.util.Set;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.ClientChatReceivedEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

import com.pockyl.diegesis.api.client.DiegesisClient;

import io.github.salvagedisc.fallow.ModIds;
import io.github.salvagedisc.fallow.postes.PostesTannerie;

/** L'allure des postes sur le client : une conduite par poste, le livre de la tannerie ouvert à la page du bloc, la forge, le foyer. */
public final class PostesClient {
    private static final Set<ResourceLocation> TANNERIE = Set.of(PostesTannerie.CHEVALET, PostesTannerie.CUVE, PostesTannerie.SECHOIR);

    /** Le dernier clic droit sur un bloc, et quand : un poste ouvert juste après l'a été par ce clic. */
    private static BlockPos dernierUsage;
    private static long dernierUsageA;

    private PostesClient() {
    }

    public static void init() {
        DiegesisClient.registerBehavior(PostesTannerie.CHEVALET, view -> new TannerieBehavior(TannerieBook.RACLAGE));
        DiegesisClient.registerBehavior(PostesTannerie.CUVE, view -> new TannerieBehavior(TannerieBook.TANNAGE));
        DiegesisClient.registerBehavior(PostesTannerie.SECHOIR, view -> new TannerieBehavior(TannerieBook.SECHAGE));
        NeoForge.EVENT_BUS.addListener(ClientChatReceivedEvent.System.class, PostesClient::auPoste);
        NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, PostesClient::usage);
        if (ModIds.isLoaded(ModIds.OVERGEARED)) {
            PostesForge.register();
        }
        if (ModIds.isLoaded(ModIds.COLD_SWEAT)) {
            PostesFoyer.register();
        }
    }

    private static void usage(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isUseItem() && ModIds.isLoaded(ModIds.COLD_SWEAT)) {
            PostesFoyer.viserLeBas(Minecraft.getInstance());
        }
        if (event.isUseItem() && Minecraft.getInstance().hitResult instanceof BlockHitResult hit) {
            dernierUsage = hit.getBlockPos();
            dernierUsageA = Util.getMillis();
        }
    }

    /**
     * Vrai si l'on vient d'ouvrir ce poste d'un clic droit sur son bloc (et non en passant d'un poste voisin). Diegesis a son
     * {@code openedByUse}, mais il n'est pas encore vrai quand le poste s'ouvre.
     */
    static boolean venuParUsage(BlockPos pos) {
        return pos.equals(dernierUsage) && Util.getMillis() - dernierUsageA < 1500L;
    }

    /**
     * Au poste la barre d'action est cachée : ce que le bloc dit (où en est le tannage, le séchage, la place qui manque) passe en
     * légende de la scène.
     */
    private static void auPoste(ClientChatReceivedEvent.System event) {
        if (event.isOverlay()) {
            DiegesisClient.focusedStation().filter(view -> TANNERIE.contains(view.type().id()) || ForgeIds.estForge(view.type().id())
                    || PostesFoyer.FOYER.equals(view.type().id())).ifPresent(view -> {
                view.showCaption(event.getMessage(), 2.5F);
                event.setCanceled(true);
            });
        }
    }
}
