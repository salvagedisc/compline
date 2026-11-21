package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.resources.ResourceLocation;

import io.github.salvagedisc.fallow.Fallow;

/** Les noms des postes de forge, à part de {@link PostesForge} : on les lit sans charger les classes d'Overgeared. */
final class ForgeIds {
    static final ResourceLocation ENCLUME_DE_PIERRE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "enclume_de_pierre");
    static final ResourceLocation ENCLUME = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "enclume");

    private ForgeIds() {
    }

    static boolean estForge(ResourceLocation station) {
        return ENCLUME.equals(station) || ENCLUME_DE_PIERRE.equals(station);
    }
}
