package io.github.salvagedisc.fallow.etabli;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import io.github.salvagedisc.fallow.FallowConfig;

/**
 * Le carnet de l'établi : un petit livre ouvert qu'on pose à côté d'une table de craft. Main vide, on tourne la page
 * (accroupi, on revient en arrière) : chaque page est une recette qu'on peut faire avec ce qu'on porte et ce qui est
 * déjà sur la table, et la table montre ses ingrédients en fantôme. Pas de fenêtre : les recettes restent dans EMI.
 */
public class CarnetBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<CarnetBlock> CODEC = simpleCodec(CarnetBlock::new);
    private static final VoxelShape SHAPE_NS = Block.box(3.0, 0.0, 4.0, 13.0, 2.0, 12.0);
    private static final VoxelShape SHAPE_EW = Block.box(4.0, 0.0, 3.0, 12.0, 2.0, 13.0);
    /** Jusqu'où le carnet cherche sa table. */
    static final int REACH = 2;

    public CarnetBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NS : SHAPE_EW;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!FallowConfig.etabliEnabled()) {
            return InteractionResult.PASS;
        }
        if (player instanceof ServerPlayer server) {
            turn(server, pos, player.isSecondaryUseActive());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    static void turn(ServerPlayer player, BlockPos carnet, boolean back) {
        Level level = player.level();
        BlockPos tablePos = nearestTable(level, carnet);
        if (tablePos == null) {
            player.displayClientMessage(Component.translatable("etabli.fallow.carnet.sans_table"), true);
            return;
        }
        Container table = Tables.at(level, tablePos);
        List<ItemStack> onTable = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            onTable.add(table.getItem(i));
        }
        List<ItemStack> available = new ArrayList<>(player.getInventory().items);
        available.addAll(onTable);
        List<RecipeHolder<CraftingRecipe>> pages = new ArrayList<>();
        for (RecipeHolder<CraftingRecipe> holder : level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING)) {
            CraftingRecipe recipe = holder.value();
            if (recipe.isSpecial() || recipe.getResultItem(level.registryAccess()).isEmpty() || Patron.needed(recipe).isEmpty()) {
                continue;
            }
            if (Patron.uses(recipe, onTable) && Patron.canMake(recipe, available)) {
                pages.add(holder);
            }
        }
        level.playSound(null, carnet, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.9F + player.getRandom().nextFloat() * 0.2F);
        if (pages.isEmpty()) {
            send(player, new EtabliPage(tablePos, Optional.empty()));
            player.displayClientMessage(Component.translatable("etabli.fallow.carnet.rien"), true);
            return;
        }
        pages.sort(Comparator.comparing((RecipeHolder<CraftingRecipe> holder) ->
                holder.value().getResultItem(level.registryAccess()).getHoverName().getString()).thenComparing(holder -> holder.id().toString()));
        EtabliPage current = Pages.get(player);
        int index = -1;
        if (current != null && current.table().equals(tablePos) && current.recipe().isPresent()) {
            for (int i = 0; i < pages.size(); i++) {
                if (pages.get(i).id().equals(current.recipe().get())) {
                    index = i;
                    break;
                }
            }
        }
        if (index < 0) {
            index = back ? pages.size() - 1 : 0;
        } else {
            index = Math.floorMod(index + (back ? -1 : 1), pages.size());
        }
        RecipeHolder<CraftingRecipe> page = pages.get(index);
        send(player, new EtabliPage(tablePos, Optional.of(page.id())));
        ItemStack result = page.value().getResultItem(level.registryAccess());
        Component name = result.getCount() > 1
                ? Component.translatable("etabli.fallow.carnet.nombre", result.getCount(), result.getHoverName())
                : result.getHoverName();
        player.displayClientMessage(Component.translatable("etabli.fallow.carnet.page", name, index + 1, pages.size()), true);
    }

    private static void send(ServerPlayer player, EtabliPage page) {
        Pages.set(player, page);
        PacketDistributor.sendToPlayer(player, page);
    }

    /** La table de Visual Workbench la plus proche du carnet, à deux blocs au plus. */
    static BlockPos nearestTable(Level level, BlockPos carnet) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(carnet.offset(-REACH, -1, -REACH), carnet.offset(REACH, 1, REACH))) {
            if (Tables.at(level, pos) != null) {
                double distance = pos.distSqr(carnet);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = pos.immutable();
                }
            }
        }
        return best;
    }
}
