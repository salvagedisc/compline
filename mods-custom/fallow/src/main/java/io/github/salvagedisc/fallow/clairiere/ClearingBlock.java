package io.github.salvagedisc.fallow.clairiere;

import io.github.salvagedisc.fallow.FallowConfig;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Feu de veille et lanterne de clairière : posés par un joueur, ils changent les biomes autour d'eux en
 * clairière. La conversion est définitive : casser le bloc ne rend pas la brume.
 */
public class ClearingBlock extends Block {
    private final int radius;
    private final VoxelShape shape;
    private final boolean smokes;

    public ClearingBlock(Properties properties, int radius, VoxelShape shape, boolean smokes) {
        super(properties);
        this.radius = radius;
        this.shape = shape;
        this.smokes = smokes;
    }

    public int radius() {
        return radius;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel server && placer instanceof ServerPlayer player && FallowConfig.clairiereEnabled()) {
            ClearingConverter.convert(server, pos, radius, player);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.fallow.clairiere.rayon", radius).withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!smokes) {
            return;
        }
        double x = pos.getX() + 0.5, y = pos.getY() + 0.45, z = pos.getZ() + 0.5;
        if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SMALL_FLAME, x + (random.nextDouble() - 0.5) * 0.4, y,
                    z + (random.nextDouble() - 0.5) * 0.4, 0.0, 0.01, 0.0);
        }
        if (random.nextInt(4) == 0) {
            level.addAlwaysVisibleParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, true, x + random.nextDouble() / 3.0 - 1.0 / 6.0,
                    y + 0.3, z + random.nextDouble() / 3.0 - 1.0 / 6.0, 0.0, 0.05, 0.0);
        }
    }
}
