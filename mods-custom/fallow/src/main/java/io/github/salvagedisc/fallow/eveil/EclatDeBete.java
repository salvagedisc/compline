package io.github.salvagedisc.fallow.eveil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.salvagedisc.fallow.FallowConfig;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Une grande bête vaincue laisse son éclat ({@code eclat} : l'enchantement, le niveau, la bête), avec la chance
 * {@code chance}. La bête est choisie par les conditions du fichier ({@code neoforge:loot_table_id}).
 */
final class EclatDeBete extends LootModifier {
    static final MapCodec<EclatDeBete> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .and(Eclat.CODEC.fieldOf("eclat").forGetter(m -> m.eclat))
            .and(Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(m -> m.chance))
            .apply(instance, EclatDeBete::new));

    private final Eclat eclat;
    private final float chance;

    EclatDeBete(LootItemCondition[] conditions, Eclat eclat, float chance) {
        super(conditions);
        this.eclat = eclat;
        this.chance = chance;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (FallowConfig.eveilEnabled() && context.getRandom().nextFloat() < chance) {
            ItemStack stack = new ItemStack(EveilModule.ECLAT_DE_BETE.get());
            stack.set(EveilModule.ECLAT.get(), eclat);
            loot.add(stack);
        }
        return loot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
