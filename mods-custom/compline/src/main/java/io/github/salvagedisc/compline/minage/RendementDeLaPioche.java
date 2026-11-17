package io.github.salvagedisc.compline.minage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Map;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Un minerai cassé avec un outil forgé d'Overgeared rend parfois une pièce de plus : chaque objet du butin gagne un
 * exemplaire avec la chance donnée pour la qualité de l'outil ({@code chances}, par nom de qualité : poor, well,
 * expert, perfect, master). Rien sur un bloc hors de {@code #c:ores}, ni quand le bloc tombe tel quel (toucher de soie).
 * La qualité est lue par le registre des composants, sans dépendre des classes d'Overgeared.
 */
public final class RendementDeLaPioche extends LootModifier {
    public static final MapCodec<RendementDeLaPioche> CODEC = RecordCodecBuilder.mapCodec(instance -> codecStart(instance)
            .and(Codec.unboundedMap(Codec.STRING, Codec.floatRange(0.0F, 1.0F)).fieldOf("chances").forGetter(m -> m.chances))
            .apply(instance, RendementDeLaPioche::new));

    private static final ResourceLocation QUALITE = ResourceLocation.fromNamespaceAndPath("overgeared", "forging_quality");

    private final Map<String, Float> chances;

    public RendementDeLaPioche(LootItemCondition[] conditions, Map<String, Float> chances) {
        super(conditions);
        this.chances = Map.copyOf(chances);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        if (tool == null || tool.isEmpty() || state == null || !state.is(Tags.Blocks.ORES)) {
            return loot;
        }
        float chance = chances.getOrDefault(qualite(tool), 0.0F);
        if (chance <= 0.0F) {
            return loot;
        }
        for (ItemStack stack : loot) {
            if (stack.is(state.getBlock().asItem())) {
                return loot;
            }
        }
        for (ItemStack stack : loot) {
            if (!stack.isEmpty() && stack.getCount() < stack.getMaxStackSize() && context.getRandom().nextFloat() < chance) {
                stack.grow(1);
            }
        }
        return loot;
    }

    static String qualite(ItemStack tool) {
        DataComponentType<?> type = BuiltInRegistries.DATA_COMPONENT_TYPE.get(QUALITE);
        if (type == null) {
            return "none";
        }
        Object value = tool.get(type);
        return value instanceof StringRepresentable named ? named.getSerializedName() : "none";
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
