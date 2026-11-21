// Adapted from Hands-On Vanilla by Pocky (MIT License, https://github.com/Pocky-l/handson-vanilla, branch 1.21.1,
// version 1.2.2). The licence travels with the mod: META-INF/LICENSE_handson_vanilla.
package io.github.salvagedisc.fallow.postes.client;

import io.github.salvagedisc.fallow.Fallow;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Which tab of a station's recipe book the player last chose, per station type, kept on the client between games: the board opens on
 * its dishes for a player who only wants those.
 */
final class BookTabs {
    private static final String FILE = Fallow.MOD_ID + "-livres.properties";
    private static Properties chosen;

    private BookTabs() {
    }

    /** The tab chosen for {@code book}, or null for all of it. */
    static synchronized String get(String book) {
        return values().getProperty(book);
    }

    static synchronized void put(String book, String tab) {
        Properties values = values();
        if (tab == null) {
            values.remove(book);
        } else {
            values.setProperty(book, tab);
        }
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                values.store(writer, "The tab each station's recipe book opens on; written by the game.");
            }
        } catch (IOException exception) {
            Fallow.LOGGER.warn("Could not write {}", path, exception);
        }
    }

    private static Properties values() {
        if (chosen == null) {
            chosen = new Properties();
            Path path = path();
            if (Files.exists(path)) {
                try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    chosen.load(reader);
                } catch (IOException | IllegalArgumentException exception) {
                    Fallow.LOGGER.warn("Could not read {}", path, exception);
                }
            }
        }
        return chosen;
    }

    private static Path path() {
        return net.minecraft.client.Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve(FILE);
    }
}
