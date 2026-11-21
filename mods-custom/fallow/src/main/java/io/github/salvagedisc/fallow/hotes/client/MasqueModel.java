package io.github.salvagedisc.fallow.hotes.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.hotes.Peuple;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Le masque d'un peuple porté sur la tête, à la place d'un casque : une plaque sculptée tenue par un lien, dans les
 * matières et les couleurs de l'armure de sa bête (Archie's Dinosaurs), un texel par unité comme cette armure. La
 * capuche sombre de l'Hôte reste visible autour ; les autres parties du corps sont vides.
 *
 * <p>La forme de chaque masque est décrite une seule fois, dans {@code assets/fallow/hotes/masques/<peuple>.json} : une
 * liste de pièces (coin de texture, boîte, gonflement, miroir, pivot, rotation en radians). Les modèles d'objet et
 * l'icône d'inventaire sont tirés du même fichier, si bien que les trois ne peuvent pas diverger.
 */
public class MasqueModel extends HumanoidModel<LivingEntity> {
    private static final Map<Peuple, ModelLayerLocation> COUCHES = new EnumMap<>(Peuple.class);

    static {
        for (Peuple peuple : Peuple.values()) {
            COUCHES.put(peuple, new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "masque_" + peuple.id()), "main"));
        }
    }

    public MasqueModel(ModelPart root) {
        super(root);
    }

    public static ModelLayerLocation couche(Peuple peuple) {
        return COUCHES.get(peuple);
    }

    public static LayerDefinition definition(Peuple peuple) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition tete = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));
        for (JsonElement element : lire("masques", peuple).getAsJsonArray("pieces")) {
            ajouter(tete, element.getAsJsonObject());
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    /** Ajoute une pièce décrite en JSON sous {@code parent} ; commun au masque et à la parure. */
    static void ajouter(PartDefinition parent, JsonObject piece) {
        float[] uv = nombres(piece.getAsJsonArray("uv"));
        float[] b = nombres(piece.getAsJsonArray("boite"));
        float gonfle = piece.has("gonfle") ? piece.get("gonfle").getAsFloat() : 0.0F;
        CubeListBuilder cube = CubeListBuilder.create().texOffs((int) uv[0], (int) uv[1]);
        if (piece.has("miroir") && piece.get("miroir").getAsBoolean()) {
            cube = cube.mirror();
        }
        cube.addBox(b[0], b[1], b[2], b[3], b[4], b[5], new CubeDeformation(gonfle));
        float[] p = piece.has("pivot") ? nombres(piece.getAsJsonArray("pivot")) : new float[3];
        float[] r = piece.has("rotation") ? nombres(piece.getAsJsonArray("rotation")) : new float[3];
        parent.addOrReplaceChild(piece.get("nom").getAsString(), cube, PartPose.offsetAndRotation(p[0], p[1], p[2], r[0], r[1], r[2]));
    }

    static JsonObject lire(String dossier, Peuple peuple) {
        String chemin = "/assets/" + Fallow.MOD_ID + "/hotes/" + dossier + "/" + peuple.id() + ".json";
        try (InputStream in = MasqueModel.class.getResourceAsStream(chemin)) {
            if (in == null) {
                throw new IllegalStateException("Forme introuvable : " + chemin);
            }
            try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Forme illisible : " + chemin, e);
        }
    }

    private static float[] nombres(JsonArray tableau) {
        float[] out = new float[tableau.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = tableau.get(i).getAsFloat();
        }
        return out;
    }
}
