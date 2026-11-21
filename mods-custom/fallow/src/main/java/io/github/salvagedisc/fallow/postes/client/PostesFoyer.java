package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import com.pockyl.diegesis.api.Diegesis;
import com.pockyl.diegesis.api.client.DiegesisClient;
import com.pockyl.diegesis.api.state.StationSchema;
import com.pockyl.diegesis.api.station.MenuBinding;
import com.pockyl.diegesis.api.station.StationType;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;

/**
 * Le foyer de Cold Sweat en poste, comme les fourneaux de Hands-On Vanilla : le poste tient la fenêtre de Cold Sweat (son menu,
 * côté serveur rien ne change) et en montre la case devant la grille du foyer ; les deux jauges de combustible, le chaud et le
 * froid, sont sur la façade. Un combustible en main se jette toujours au foyer d'un clic, sans poste. Enregistré sur le client
 * seul ; cette classe ne touche pas aux classes de Cold Sweat, que {@link FoyerBehavior} est seul à lire.
 */
final class PostesFoyer {
    static final ResourceLocation FOYER = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "foyer");
    /** Le menu du foyer de Cold Sweat. */
    static final ResourceLocation MENU = ResourceLocation.fromNamespaceAndPath("cold_sweat", "hearth");

    static final String COMBUSTIBLE = "combustible";

    private static final ResourceLocation BAS = ResourceLocation.fromNamespaceAndPath("cold_sweat", "hearth_bottom");
    private static final ResourceLocation HAUT = ResourceLocation.fromNamespaceAndPath("cold_sweat", "hearth_top");
    private static final ResourceLocation CHEMINEE = ResourceLocation.fromNamespaceAndPath("cold_sweat", "smokestack");

    private PostesFoyer() {
    }

    static void register() {
        // Par nom, comme les enclumes : Diegesis veut un type de menu en main, celui d'un coffre d'une rangée, qu'un foyer
        // n'ouvre jamais ; le vrai est accepté en plus.
        Diegesis.station(FOYER)
                .block(() -> BuiltInRegistries.BLOCK.get(BAS))
                .orientation(StationType.Orientation.BLOCK)
                .reach(4.5)
                .schema(StationSchema.builder().slots(COMBUSTIBLE, 1).build())
                .menu(MenuBinding.of(MenuType.GENERIC_9x1).alsoAccepts(MENU).slots(COMBUSTIBLE, 0, 1).build())
                .openWhen(PostesFoyer::ouvre)
                .register();
        DiegesisClient.registerBehavior(FOYER, view -> new FoyerBehavior());
    }

    /**
     * Le haut du foyer (sa cheminée) ouvre la même fenêtre que le bas : un clic droit dessus vise le bas, pour que le poste s'ouvre
     * aussi. Appelé quand la touche d'usage part, avant que Minecraft lise ce que vise le joueur. Une cheminée en main garde son
     * geste (en poser une de plus).
     */
    static void viserLeBas(Minecraft minecraft) {
        if (!(minecraft.hitResult instanceof BlockHitResult hit) || minecraft.level == null || minecraft.player == null
                || !est(minecraft.level.getBlockState(hit.getBlockPos()).getBlock(), HAUT)
                || BuiltInRegistries.ITEM.getKey(minecraft.player.getMainHandItem().getItem()).equals(CHEMINEE)) {
            return;
        }
        BlockPos bas = hit.getBlockPos().below();
        BlockState state = minecraft.level.getBlockState(bas);
        if (!est(state.getBlock(), BAS) || !ouvre(minecraft.player, bas, state)) {
            return;
        }
        Direction face = state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING) : hit.getDirection();
        Vec3 devant = Vec3.atCenterOf(bas).add(face.getStepX() * 0.5, 0.0, face.getStepZ() * 0.5);
        minecraft.hitResult = new BlockHitResult(devant, face, bas, false);
    }

    private static boolean est(Block block, ResourceLocation id) {
        return BuiltInRegistries.BLOCK.getKey(block).equals(id);
    }

    /** Accroupi, ou un combustible en main (il se jette au foyer), le foyer garde ses gestes de Cold Sweat. */
    private static boolean ouvre(Player player, BlockPos pos, BlockState state) {
        return FallowConfig.postesFoyer() && !player.isSecondaryUseActive()
                && !FoyerBehavior.combustible(player.level(), pos, player.getMainHandItem());
    }
}
