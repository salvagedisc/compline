package io.github.salvagedisc.fallow.zoo.client.guide;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.salvagedisc.fallow.Fallow;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Corrige au chargement quelques textes du guide d'Archie's Dinosaurs : ceux qui envoient au Nether ou dans l'End,
 * d'où le pack a sorti l'Obsidiceratops et le Chorusoatl. Les remplacements sont dans
 * {@code assets/fallow/archie/guide/<langue>.json} ; le fichier du mod n'est ni copié ni remplacé.
 */
public final class GuidePatches {
    private GuidePatches() {
    }

    public static Document apply(Document document) {
        try {
            String language = Minecraft.getInstance().getLanguageManager().getSelected();
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(
                    ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "archie/guide/" + language + ".json"));
            if (resource.isEmpty()) {
                return document;
            }
            JsonObject patches;
            try (Reader reader = resource.get().openAsReader()) {
                patches = JsonParser.parseReader(reader).getAsJsonObject();
            }
            int applied = 0;
            NodeList categories = document.getDocumentElement().getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++) {
                Element category = (Element) categories.item(i);
                JsonElement list = patches.get(category.getAttribute("id"));
                if (list != null && list.isJsonArray()) {
                    applied += apply(category, list.getAsJsonArray());
                }
            }
            Fallow.LOGGER.debug("zoo : {} texte(s) du guide d'Archie corrigé(s)", applied);
        } catch (Exception error) {
            Fallow.LOGGER.warn("zoo : corrections du guide d'Archie illisibles", error);
        }
        return document;
    }

    private static int apply(Element category, JsonArray patches) {
        List<Element> pages = children(category, "page");
        int applied = 0;
        for (JsonElement entry : patches) {
            JsonObject patch = entry.getAsJsonObject();
            int page = patch.get("page").getAsInt();
            int label = patch.get("label").getAsInt();
            if (page < 1 || page > pages.size()) {
                continue;
            }
            List<Element> labels = children(pages.get(page - 1), "label");
            if (label < 1 || label > labels.size()) {
                continue;
            }
            Element target = labels.get(label - 1);
            String expected = patch.has("si") ? patch.get("si").getAsString() : "";
            if (!target.getTextContent().contains(expected)) {
                Fallow.LOGGER.warn("zoo : texte {} p.{} l.{} du guide d'Archie inattendu, laissé tel quel",
                        category.getAttribute("id"), page, label);
                continue;
            }
            target.setTextContent(patch.get("texte").getAsString());
            applied++;
        }
        return applied;
    }

    private static List<Element> children(Element parent, String tag) {
        List<Element> out = new ArrayList<>();
        NodeList nodes = parent.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element element && tag.equals(element.getTagName())) {
                out.add(element);
            }
        }
        return out;
    }
}
