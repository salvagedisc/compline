package io.github.salvagedisc.fallow.eveil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.salvagedisc.fallow.Fallow;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Les hauts faits : quels actes comptent pour quels objets, et vers quels enchantements ils penchent. Lus dans
 * {@code data/<espace>/eveil/*.json}, un fichier par famille d'objets :
 *
 * <pre>{@code
 * {
 *   "objets": "#minecraft:pickaxes",
 *   "actes": [
 *     {"acte": "casser", "cible": "#c:ores", "points": 3, "enchantements": {"minecraft:fortune": 3, "minecraft:efficiency": 1}}
 *   ]
 * }
 * }</pre>
 *
 * {@code objets} et {@code cible} prennent un identifiant ou un tag ({@code #}). Les actes : {@code casser} (cible : bloc),
 * {@code vaincre} (cible : créature ; un point par 20 points de vie de la bête, plafonné à 10), {@code encaisser}
 * (cible : type de dégâts ; un point par point de dégât encaissé, sur chaque pièce d'armure portée), {@code parcourir}
 * (cible : {@code marche} ou {@code nage} ; un point par bloc), {@code sous_l_eau} (un point par seconde),
 * {@code bloquer} (un point par point de dégât arrêté au bouclier), {@code pecher} (un point par prise).
 * Sans cible, l'acte compte toujours. Une entrée garde sa place dans le fichier : c'est sa clé dans {@link Actes}.
 */
public final class HautsFaits extends SimpleJsonResourceReloadListener {
    public enum Type {
        CASSER, VAINCRE, ENCAISSER, PARCOURIR, SOUS_L_EAU, BLOQUER, PECHER;

        static Type of(String name) {
            return valueOf(name.toUpperCase(Locale.ROOT));
        }
    }

    /** Une entrée : son acte, sa cible (identifiant ou tag, vide pour toutes), ses points par unité et ses penchants. */
    public record Entree(String cle, Type type, String cible, double points, Map<ResourceLocation, Double> enchantements) {
        boolean bloc(BlockState state) {
            if (cible.isEmpty()) {
                return true;
            }
            if (cible.startsWith("#")) {
                return state.is(TagKey.create(Registries.BLOCK, ResourceLocation.parse(cible.substring(1))));
            }
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(cible));
            return state.is(block);
        }

        boolean creature(EntityType<?> type) {
            if (cible.isEmpty()) {
                return true;
            }
            if (cible.startsWith("#")) {
                return type.is(TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.parse(cible.substring(1))));
            }
            return BuiltInRegistries.ENTITY_TYPE.getKey(type).equals(ResourceLocation.parse(cible));
        }

        boolean degats(DamageSource source) {
            if (cible.isEmpty()) {
                return true;
            }
            if (cible.startsWith("#")) {
                return source.is(TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(cible.substring(1))));
            }
            return source.typeHolder().is(ResourceLocation.parse(cible));
        }

        boolean mode(String value) {
            return cible.isEmpty() || cible.equals(value);
        }
    }

    record Famille(String objets, List<Entree> entrees) {
        boolean pour(ItemStack stack) {
            if (objets.startsWith("#")) {
                return stack.is(TagKey.create(Registries.ITEM, ResourceLocation.parse(objets.substring(1))));
            }
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(objets));
            return stack.is(item);
        }
    }

    private static final Gson GSON = new Gson();
    private static volatile List<Famille> familles = List.of();

    HautsFaits() {
        super(GSON, "eveil");
    }

    /** Toutes les entrées qui valent pour cet objet. */
    static List<Entree> pour(ItemStack stack) {
        if (stack.isEmpty()) {
            return List.of();
        }
        List<Entree> out = new ArrayList<>();
        for (Famille famille : familles) {
            if (famille.pour(stack)) {
                out.addAll(famille.entrees());
            }
        }
        return out;
    }

    static int nombre() {
        return familles.size();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
        List<Famille> loaded = new ArrayList<>();
        files.forEach((id, json) -> {
            try {
                JsonObject root = GsonHelper.convertToJsonObject(json, "eveil");
                String objets = GsonHelper.getAsString(root, "objets");
                List<Entree> entrees = new ArrayList<>();
                int index = 0;
                for (JsonElement element : GsonHelper.getAsJsonArray(root, "actes")) {
                    JsonObject acte = GsonHelper.convertToJsonObject(element, "acte");
                    Map<ResourceLocation, Double> penchants = new LinkedHashMap<>();
                    GsonHelper.getAsJsonObject(acte, "enchantements").entrySet()
                            .forEach(e -> penchants.put(ResourceLocation.parse(e.getKey()), e.getValue().getAsDouble()));
                    entrees.add(new Entree(id + "#" + index++, Type.of(GsonHelper.getAsString(acte, "acte")),
                            GsonHelper.getAsString(acte, "cible", ""), GsonHelper.getAsDouble(acte, "points", 1.0),
                            Map.copyOf(penchants)));
                }
                loaded.add(new Famille(objets, List.copyOf(entrees)));
            } catch (RuntimeException e) {
                Fallow.LOGGER.error("eveil : fichier {} illisible", id, e);
            }
        });
        familles = List.copyOf(loaded);
        Fallow.LOGGER.info("eveil : {} familles d'objets", loaded.size());
    }
}
