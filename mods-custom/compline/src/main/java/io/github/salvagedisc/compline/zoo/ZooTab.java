package io.github.salvagedisc.compline.zoo;

import io.github.salvagedisc.compline.Compline;
import java.util.Locale;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

/**
 * Les milieux du zoo, un onglet chacun dans le guide. Une espèce va dans le milieu que donne sa nature
 * ({@link ZooSpecies#classify}), sauf si un tag {@code compline:zoo/<milieu>} la place ailleurs.
 */
public enum ZooTab {
    TERRE(0x0D820F),
    EAUX(0x0C939B),
    CIEL(0x3F8FD8),
    CREATURES(0x9717B3);

    private final int color;
    private final TagKey<EntityType<?>> tag;

    ZooTab(int color) {
        this.color = color;
        this.tag = TagKey.create(Registries.ENTITY_TYPE,
                ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "zoo/" + name().toLowerCase(Locale.ROOT)));
    }

    /** Couleur du milieu, celle de ses rubans dans le guide (infobulles des tampons obtenus). */
    public int color() {
        return color;
    }

    public TagKey<EntityType<?>> tag() {
        return tag;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Clé du nom du milieu ({@code zoo.compline.milieu.terre}). */
    public String nameKey() {
        return "zoo.compline.milieu." + id();
    }

    public static ZooTab byIndex(int index) {
        ZooTab[] tabs = values();
        return index >= 0 && index < tabs.length ? tabs[index] : TERRE;
    }
}
