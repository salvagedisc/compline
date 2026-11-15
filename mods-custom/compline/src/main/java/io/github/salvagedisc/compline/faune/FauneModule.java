package io.github.salvagedisc.compline.faune;

import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.ModIds;
import io.github.salvagedisc.compline.faune.warden.WardenRenderer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Module faune (client) : des créatures d'autres mods ou du jeu, redessinées et réanimées par Compline avec GeckoLib.
 * La créature garde son entité (IA, sons, objets, boîte de collision) ; seul son rendu change. On ne remplace pas le
 * rendu enregistré par le mod d'origine : on l'intercepte à chaque image ({@link RenderLivingEvent.Pre}), ce qui
 * ne dépend pas de l'ordre de chargement des mods et permet de revenir au modèle d'origine en jeu, depuis la config.
 */
public final class FauneModule {
    private record Replacement(BooleanSupplier enabled, EntityRenderer<Entity> renderer) {
    }

    public interface Factory {
        EntityRenderer<?> create(EntityRendererProvider.Context context);
    }

    private record Entry(ResourceLocation type, BooleanSupplier enabled, Factory factory) {
    }

    private static final Map<EntityType<?>, Replacement> REPLACEMENTS = new IdentityHashMap<>();
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private FauneModule() {
    }

    public static void start(IEventBus modBus) {
        add(ResourceLocation.withDefaultNamespace("warden"), ComplineConfig::fauneWarden, WardenRenderer::new);
        if (ModIds.isLoaded(ModIds.ALEXS_MOBS)) {
            io.github.salvagedisc.compline.faune.moose.MooseReplacement.register();
        }
        modBus.addListener(EntityRenderersEvent.AddLayers.class, FauneModule::createRenderers);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, false, RenderLivingEvent.Pre.class, FauneModule::onRenderLiving);
        Compline.LOGGER.info("faune : {} créature(s) redessinée(s)", ENTRIES.size());
    }

    /**
     * Déclare une créature redessinée, par l'identifiant de son type d'entité (les registres ne sont pas encore
     * remplis quand le module démarre). Le rendu est créé à chaque rechargement des ressources.
     */
    public static void add(ResourceLocation type, BooleanSupplier enabled, Factory factory) {
        ENTRIES.add(new Entry(type, enabled, factory));
    }

    @SuppressWarnings("unchecked")
    private static void createRenderers(EntityRenderersEvent.AddLayers event) {
        REPLACEMENTS.clear();
        for (Entry entry : ENTRIES) {
            Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(entry.type());
            if (type.isEmpty()) {
                Compline.LOGGER.warn("faune : type d'entité {} introuvable", entry.type());
                continue;
            }
            try {
                EntityRenderer<Entity> renderer = (EntityRenderer<Entity>) entry.factory().create(event.getContext());
                REPLACEMENTS.put(type.get(), new Replacement(entry.enabled(), renderer));
            } catch (RuntimeException e) {
                Compline.LOGGER.error("faune : rendu de {} impossible, on garde celui d'origine", entry.type(), e);
            }
        }
    }

    private static void onRenderLiving(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        Replacement replacement = REPLACEMENTS.get(entity.getType());
        if (replacement == null || !replacement.enabled().getAsBoolean()) {
            return;
        }
        event.setCanceled(true);
        float partialTick = event.getPartialTick();
        float yaw = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
        replacement.renderer().render(entity, yaw, partialTick, event.getPoseStack(), event.getMultiBufferSource(),
                event.getPackedLight());
    }
}
