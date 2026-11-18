package io.github.salvagedisc.compline.zoo.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.zoo.SpeciesInfo;
import io.github.salvagedisc.compline.zoo.Stamp;
import io.github.salvagedisc.compline.zoo.ZooSync;
import io.github.salvagedisc.compline.zoo.client.guide.ZooGuide;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

/**
 * Le zoo côté client : la liste des espèces et le carnet du joueur, tels que le serveur les envoie, et la touche
 * qui ouvre le guide d'Archie sans avoir à le tenir en main.
 */
public final class ZooClient {
    private static final Map<ResourceLocation, SpeciesInfo> species = new LinkedHashMap<>();
    /** Change à chaque modification de la liste des espèces : le guide garde ses listes par milieu jusque-là. */
    private static int speciesVersion;
    private static final Map<ResourceLocation, Integer> stamps = new HashMap<>();
    private static int observeSeconds = 60;
    private static final KeyMapping OPEN_GUIDE = new KeyMapping("key.compline.guide", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_J, "key.categories.compline");

    private ZooClient() {
    }

    public static void start(IEventBus modBus) {
        if (ModIds.isLoaded(ModIds.ARCHIES_DINOSAURS)) {
            modBus.addListener(RegisterKeyMappingsEvent.class, event -> event.register(OPEN_GUIDE));
            modBus.addListener(RegisterClientReloadListenersEvent.class, event -> event.registerReloadListener(
                    (ResourceManagerReloadListener) manager -> ZooGuide.resourcesReloaded()));
            NeoForge.EVENT_BUS.addListener(ScreenEvent.Render.Pre.class, event -> ArchieGuide.beforeRender(event.getScreen()));
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
                Minecraft minecraft = Minecraft.getInstance();
                while (OPEN_GUIDE.consumeClick()) {
                    if (minecraft.screen == null && minecraft.player != null) {
                        ArchieGuide.open();
                    }
                }
            });
        }
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            species.clear();
            speciesVersion++;
            stamps.clear();
            if (ModIds.isLoaded(ModIds.ARCHIES_DINOSAURS)) {
                // Les portraits tiennent des bêtes du monde quitté : on les lâche avec lui.
                io.github.salvagedisc.compline.zoo.client.guide.ZooGuide.forgetWorld();
            }
        });
    }

    public static void receive(ZooSync sync) {
        if (sync.full()) {
            species.clear();
            stamps.clear();
            observeSeconds = sync.observeSeconds();
        }
        for (SpeciesInfo info : sync.species()) {
            species.put(info.id(), info);
        }
        speciesVersion++;
        stamps.putAll(sync.stamps());
    }

    public static int speciesVersion() {
        return speciesVersion;
    }

    public static Collection<SpeciesInfo> species() {
        return Collections.unmodifiableCollection(species.values());
    }

    /** Le masque des tampons du joueur pour cette espèce. */
    public static int stamps(ResourceLocation id) {
        return stamps.getOrDefault(id, 0) & 0xFF;
    }

    public static boolean has(ResourceLocation id, Stamp stamp) {
        return stamp.in(stamps(id));
    }

    /** Secondes d'observation que demande Maîtriser, réglées sur le serveur. */
    public static int observeSecondsNeeded() {
        return observeSeconds;
    }

    /** La touche qui ouvre le guide. */
    public static KeyMapping guideKey() {
        return OPEN_GUIDE;
    }

    /** Secondes passées à observer l'espèce, telles que le serveur les a comptées (par paliers de cinq). */
    public static int observedSeconds(ResourceLocation id) {
        return stamps.getOrDefault(id, 0) >>> 8;
    }
}
