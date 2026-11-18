package io.github.salvagedisc.fallow.zoo;

import io.github.salvagedisc.fallow.Fallow;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * La liste des espèces du zoo, tenue par le serveur et envoyée aux clients.
 *
 * <p>Au démarrage du serveur, une espèce est inscrite si elle apparaît naturellement dans un biome de l'Overworld
 * (modificateurs de biomes des mods compris) ou si le tag {@code fallow:zoo/inclus} la nomme ; le tag
 * {@code fallow:zoo/absents} retire celles qui n'apparaissent pas (refusées par In Control, par exemple) et
 * {@code fallow:zoo/exclus} celles qui n'y entrent jamais (les dinosaures, qui ont leur propre page de tampons).
 * Une bête aperçue qui n'était pas inscrite (née d'une structure, d'un œuf, d'un village) l'est à ce moment-là, et
 * le monde s'en souvient ({@link Discovered}).
 */
public final class ZooSpecies {
    public static final TagKey<EntityType<?>> INCLUS = tag("inclus");
    public static final TagKey<EntityType<?>> ABSENTS = tag("absents");
    public static final TagKey<EntityType<?>> EXCLUS = tag("exclus");

    private static final Map<ResourceLocation, SpeciesInfo> species = new LinkedHashMap<>();
    private static final Set<ResourceLocation> rejected = new java.util.HashSet<>();

    private ZooSpecies() {
    }

    private static TagKey<EntityType<?>> tag(String path) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "zoo/" + path));
    }

    public static Collection<SpeciesInfo> all() {
        return Collections.unmodifiableCollection(species.values());
    }

    public static SpeciesInfo get(ResourceLocation id) {
        return species.get(id);
    }

    /** Recalcule la liste (démarrage du serveur, rechargement des données). */
    static void rebuild(MinecraftServer server) {
        species.clear();
        rejected.clear();
        ServerLevel overworld = server.overworld();
        Set<EntityType<?>> spawning = new java.util.HashSet<>();
        for (Holder<Biome> biome : overworld.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()) {
            MobSpawnSettings settings = biome.value().getMobSettings();
            for (MobCategory category : MobCategory.values()) {
                settings.getMobs(category).unwrap().forEach(data -> spawning.add(data.type));
            }
        }
        Set<ResourceLocation> discovered = Discovered.get(server).ids;
        Set<ResourceLocation> ids = new TreeSet<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            boolean listed = (spawning.contains(type) && !type.is(ABSENTS)) || type.is(INCLUS) || discovered.contains(id);
            if (listed && !type.is(EXCLUS)) {
                ids.add(id);
            }
        }
        int skipped = 0;
        for (ResourceLocation id : ids) {
            SpeciesInfo info = classify(overworld, BuiltInRegistries.ENTITY_TYPE.get(id));
            if (info != null) {
                species.put(id, info);
            } else {
                skipped++;
            }
        }
        Fallow.LOGGER.info("zoo : {} espèces ({} types écartés : pas des bêtes)", species.size(), skipped);
    }

    /**
     * Inscrit l'espèce d'une bête aperçue si elle ne l'est pas encore ; renvoie sa fiche, ou {@code null} si ce
     * type n'entre pas au zoo.
     */
    static SpeciesInfo admit(Entity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        SpeciesInfo known = species.get(id);
        if (known != null || rejected.contains(id)) {
            return known;
        }
        if (!(entity instanceof Mob) || type.is(EXCLUS)) {
            rejected.add(id);
            return null;
        }
        SpeciesInfo info = describe(id, type, entity);
        species.put(id, info);
        if (entity.level() instanceof ServerLevel level) {
            Discovered discovered = Discovered.get(level.getServer());
            discovered.ids.add(id);
            discovered.setDirty();
            Zoo.broadcastSpecies(level.getServer(), info);
        }
        Fallow.LOGGER.info("zoo : nouvelle espèce aperçue, {}", id);
        return info;
    }

    /** Fiche d'un type, d'après une bête créée pour l'occasion (jamais ajoutée au monde) ; {@code null} si ce n'en est pas une. */
    private static SpeciesInfo classify(ServerLevel level, EntityType<?> type) {
        Entity sample;
        try {
            sample = type.create(level);
        } catch (Throwable error) {
            Fallow.LOGGER.debug("zoo : {} impossible à créer pour le classer", type, error);
            return null;
        }
        if (!(sample instanceof Mob)) {
            return null;
        }
        return describe(BuiltInRegistries.ENTITY_TYPE.getKey(type), type, sample);
    }

    private static SpeciesInfo describe(ResourceLocation id, EntityType<?> type, Entity sample) {
        boolean housable = sample instanceof OwnableEntity || sample instanceof Animal || sample instanceof Bucketable;
        return new SpeciesInfo(id, tabOf(type, sample), housable);
    }

    private static ZooTab tabOf(EntityType<?> type, Entity sample) {
        for (ZooTab tab : ZooTab.values()) {
            if (type.is(tab.tag())) {
                return tab;
            }
        }
        if (sample instanceof Enemy) {
            return ZooTab.CREATURES;
        }
        MobCategory category = type.getCategory();
        // Beaucoup de bêtes marines de mods ne sont ni des WaterAnimal ni rangées dans une catégorie d'eau : elles
        // naissent dans l'eau, ou ne savent se déplacer qu'en nageant.
        if (sample instanceof WaterAnimal || category == MobCategory.WATER_CREATURE || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.UNDERGROUND_WATER_CREATURE || category == MobCategory.AXOLOTLS
                || SpawnPlacements.getPlacementType(type) == SpawnPlacementTypes.IN_WATER
                || (sample instanceof Mob mob && mob.getNavigation() instanceof WaterBoundPathNavigation)) {
            return ZooTab.EAUX;
        }
        if (sample instanceof FlyingAnimal || sample instanceof AmbientCreature) {
            return ZooTab.CIEL;
        }
        return ZooTab.TERRE;
    }

    /** Les espèces vues hors de la liste de départ, gardées avec le monde. */
    static final class Discovered extends SavedData {
        private static final String NAME = "fallow_zoo";
        final Set<ResourceLocation> ids = new TreeSet<>();

        static Discovered get(MinecraftServer server) {
            return server.overworld().getDataStorage().computeIfAbsent(
                    new SavedData.Factory<>(Discovered::new, Discovered::load, null), NAME);
        }

        private static Discovered load(CompoundTag tag, HolderLookup.Provider registries) {
            Discovered data = new Discovered();
            ListTag list = tag.getList("especes", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
                if (id != null) {
                    data.ids.add(id);
                }
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            ids.forEach(id -> list.add(StringTag.valueOf(id.toString())));
            tag.put("especes", list);
            return tag;
        }
    }
}
