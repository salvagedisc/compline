package io.github.salvagedisc.fallow.hotes.client;

import io.github.salvagedisc.fallow.hotes.Peuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/**
 * Rendu d'un masque porté : un seul modèle par peuple, cuit à la première image puis réutilisé (pas un modèle neuf par
 * image comme l'armure d'Archie). Sa forme vient du fichier de forme du peuple ; la texture, du matériau du masque.
 */
public final class MasqueClient implements IClientItemExtensions {
    private final Peuple peuple;
    private MasqueModel modele;

    public MasqueClient(Peuple peuple) {
        this.peuple = peuple;
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        if (modele == null) {
            modele = new MasqueModel(Minecraft.getInstance().getEntityModels().bakeLayer(MasqueModel.couche(peuple)));
        }
        @SuppressWarnings("unchecked")
        HumanoidModel<LivingEntity> source = (HumanoidModel<LivingEntity>) original;
        source.copyPropertiesTo(modele);
        modele.setAllVisible(false);
        modele.head.visible = slot == EquipmentSlot.HEAD;
        return modele;
    }
}
