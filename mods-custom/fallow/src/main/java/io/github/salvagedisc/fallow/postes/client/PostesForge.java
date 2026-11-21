package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.Diegesis;
import com.pockyl.diegesis.api.client.DiegesisClient;
import com.pockyl.diegesis.api.state.StationSchema;
import com.pockyl.diegesis.api.station.MenuBinding;
import com.pockyl.diegesis.api.station.StationType;

import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.block.entity.AbstractSmithingAnvilBlockEntity;
import net.stirdrem.overgeared.util.ModTags;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;

/**
 * Les enclumes de forge d'Overgeared en postes, comme les blocs vanilla de Hands-On Vanilla : le poste tient la fenêtre
 * d'Overgeared (son menu, côté serveur rien ne change) et en montre les cases sur l'enclume. La grille de 3 x 3 est sur la
 * table de l'enclume, le marteau à droite, le plan (enclumes de métal) à gauche, l'ouvrage fini au-dessus de la bigorne. Les
 * frappes restent celles d'Overgeared : accroupi, le marteau en main, hors du poste. Enregistrés sur le client seul.
 */
final class PostesForge {
    static final ResourceLocation ENCLUME_DE_PIERRE = ForgeIds.ENCLUME_DE_PIERRE;
    static final ResourceLocation ENCLUME = ForgeIds.ENCLUME;

    static final String MARTEAU = "marteau";
    static final String PLAN = "plan";
    static final String GRILLE = "grille";
    static final String OUVRAGE = "ouvrage";

    private PostesForge() {
    }

    private static ResourceLocation og(String path) {
        return ResourceLocation.fromNamespaceAndPath("overgeared", path);
    }

    static void register() {
        // Tout par nom : ni les blocs ni les menus d'Overgeared ne sont encore enregistrés quand le poste l'est. Diegesis veut
        // pourtant un type de menu en main : celui d'un coffre d'une rangée, qu'une enclume n'ouvre jamais.
        Diegesis.station(ENCLUME_DE_PIERRE)
                .block(() -> BuiltInRegistries.BLOCK.get(og("stone_anvil")))
                .orientation(StationType.Orientation.PLAYER)
                .reach(4.5)
                .schema(StationSchema.builder().slots(MARTEAU, 1).slots(GRILLE, 9).output(OUVRAGE).build())
                .menu(MenuBinding.of(MenuType.GENERIC_9x1).alsoAccepts(og("stone_smithing_anvil_menu")).slots(MARTEAU, 0, 1).slots(GRILLE, 1, 9)
                        .output(OUVRAGE, 10).build())
                .openWhen(PostesForge::ouvre)
                .register();
        Diegesis.station(ENCLUME)
                .block(() -> BuiltInRegistries.BLOCK.get(og("smithing_anvil")))
                .block(() -> BuiltInRegistries.BLOCK.get(og("tier_a_smithing_anvil")))
                .block(() -> BuiltInRegistries.BLOCK.get(og("tier_b_smithing_anvil")))
                .orientation(StationType.Orientation.PLAYER)
                .reach(4.5)
                .schema(StationSchema.builder().slots(MARTEAU, 1).slots(PLAN, 1).slots(GRILLE, 9).output(OUVRAGE).build())
                // Le menu d'Overgeared : le marteau, le plan, les neuf cases, l'ouvrage.
                .menu(MenuBinding.of(MenuType.GENERIC_9x1).alsoAccepts(og("smithing_anvil_menu"), og("tier_a_smithing_anvil_menu"),
                        og("tier_b_smithing_anvil_menu")).slots(MARTEAU, 0, 1).slots(PLAN, 1, 1).slots(GRILLE, 2, 9)
                        .output(OUVRAGE, 11).build())
                .openWhen(PostesForge::ouvre)
                .register();
        DiegesisClient.registerBehavior(ENCLUME_DE_PIERRE, view -> new ForgeBehavior(AnvilTier.STONE, 1));
        DiegesisClient.registerBehavior(ENCLUME, view -> new ForgeBehavior(tier(view.level().getBlockState(view.pos())), 2));
    }

    /** L'enclume de l'acier, de rang A ou B : son rang dit quelles recettes s'y forgent. */
    private static AnvilTier tier(BlockState state) {
        ResourceLocation block = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return switch (block.getPath()) {
            case "tier_a_smithing_anvil" -> AnvilTier.ABOVE_A;
            case "tier_b_smithing_anvil" -> AnvilTier.ABOVE_B;
            default -> AnvilTier.IRON;
        };
    }

    /**
     * Accroupi, l'enclume garde ses gestes d'Overgeared (lancer la forge) ; le marteau en main aussi tant que des pièces attendent
     * sur l'enclume (les frapper). Sinon le poste s'ouvre, si la config le permet.
     */
    private static boolean ouvre(Player player, BlockPos pos, BlockState state) {
        if (!FallowConfig.postesForge() || player.isSecondaryUseActive()) {
            return false;
        }
        return !player.getMainHandItem().is(ModTags.Items.SMITHING_HAMMERS)
                || !(player.level().getBlockEntity(pos) instanceof AbstractSmithingAnvilBlockEntity anvil) || grilleVide(anvil);
    }

    private static boolean grilleVide(AbstractSmithingAnvilBlockEntity anvil) {
        for (int cell = 0; cell < 9; cell++) {
            if (!anvil.getItemHandler().getStackInSlot(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
