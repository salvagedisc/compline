package io.github.salvagedisc.fallow;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/**
 * Contrôle de démarrage de la CI ({@code scripts/boot/server.mjs}) : lancé avec
 * {@code -Dfallow.bootcheck=<fichier>}, le serveur écrit la liste de ses registres (objets, blocs,
 * entités, fluides, effets, recettes) dans ce fichier dès qu'il est prêt, puis s'arrête. Sans la
 * propriété, rien n'est fait.
 */
final class BootCheck {
    private static final String PROPERTY = "fallow.bootcheck";

    private BootCheck() {
    }

    static void register() {
        String target = System.getProperty(PROPERTY);
        if (target == null || target.isBlank()) {
            return;
        }
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent.class, event -> {
            MinecraftServer server = event.getServer();
            JsonObject dump = new JsonObject();
            JsonArray mods = new JsonArray();
            ModList.get().getMods().forEach(mod -> mods.add(mod.getModId()));
            dump.add("mods", mods);
            dump.add("item", ids(BuiltInRegistries.ITEM));
            dump.add("block", ids(BuiltInRegistries.BLOCK));
            dump.add("entity_type", ids(BuiltInRegistries.ENTITY_TYPE));
            dump.add("fluid", ids(BuiltInRegistries.FLUID));
            dump.add("mob_effect", ids(BuiltInRegistries.MOB_EFFECT));
            dump.add("recipe_type", ids(BuiltInRegistries.RECIPE_TYPE));
            JsonArray recipes = new JsonArray();
            server.getRecipeManager().getRecipeIds().forEach(id -> recipes.add(id.toString()));
            dump.add("recipe", recipes);
            try {
                Files.writeString(Path.of(target), new GsonBuilder().setPrettyPrinting().create().toJson(dump),
                        StandardCharsets.UTF_8);
                Fallow.LOGGER.info("Contrôle de démarrage : registres écrits dans {}", target);
            } catch (IOException e) {
                Fallow.LOGGER.error("Contrôle de démarrage : impossible d'écrire {}", target, e);
            }
            server.halt(false);
        });
    }

    private static JsonArray ids(Registry<?> registry) {
        JsonArray array = new JsonArray();
        registry.keySet().stream().map(Object::toString).sorted().forEach(array::add);
        return array;
    }
}
