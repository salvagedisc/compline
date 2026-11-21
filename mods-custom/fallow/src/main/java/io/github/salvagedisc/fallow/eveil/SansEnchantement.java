package io.github.salvagedisc.fallow.eveil;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.salvagedisc.fallow.FallowConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Rien ne sort enchanté d'un butin : coffres, urnes, primes, pêche, archéologie, bêtes. Un objet perd ses
 * enchantements, un livre enchanté redevient un livre. Seul l'éveil enchante (docs/plan/eveil.md).
 */
final class SansEnchantement extends LootModifier {
    static final MapCodec<SansEnchantement> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .apply(instance, SansEnchantement::new));

    SansEnchantement(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!FallowConfig.eveilEnabled()) {
            return loot;
        }
        for (int i = 0; i < loot.size(); i++) {
            loot.set(i, nu(loot.get(i)));
        }
        return loot;
    }

    /** L'objet sans enchantement (un livre enchanté devient un livre). */
    static ItemStack nu(ItemStack stack) {
        if (stack.is(Items.ENCHANTED_BOOK)) {
            return new ItemStack(Items.BOOK, stack.getCount());
        }
        if (stack.isEnchanted()) {
            stack.set(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
        if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            stack.remove(DataComponents.STORED_ENCHANTMENTS);
        }
        return stack;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
