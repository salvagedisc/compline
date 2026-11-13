package io.github.salvagedisc.tilth.dissipateur;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Le dissipateur : un carter de laiton traversé par un arbre, sur l'axe choisi à la pose. */
public class DissipateurBlock extends RotatedPillarKineticBlock implements IBE<DissipateurBlockEntity> {
    public DissipateurBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }

    @Override
    public SpeedLevel getMinimumRequiredSpeedLevel() {
        return SpeedLevel.MEDIUM;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tilth.dissipateur.usage", DissipateurBlockEntity.RADIUS)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public Class<DissipateurBlockEntity> getBlockEntityClass() {
        return DissipateurBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DissipateurBlockEntity> getBlockEntityType() {
        return DissipateurModule.DISSIPATEUR_BE.get();
    }
}
