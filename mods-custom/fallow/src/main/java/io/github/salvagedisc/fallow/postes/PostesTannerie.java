package io.github.salvagedisc.fallow.postes;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.Diegesis;
import com.pockyl.diegesis.api.state.StationSchema;
import com.pockyl.diegesis.api.state.TankSpec;
import com.pockyl.diegesis.api.station.StationType;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/**
 * Les trois blocs de la tannerie en postes : la caméra vient au bloc, on y pose ce qu'on tient au curseur, le couteau racle sur
 * le chevalet, et le livre de la tannerie est à côté. Le travail reste celui des blocs ({@link io.github.salvagedisc.fallow.tannerie.Ouvrage}) :
 * le poste ne fait que choisir l'objet (curseur, case de l'inventaire) et envoyer ce qui sort dans l'inventaire.
 */
public final class PostesTannerie {
    public static final ResourceLocation CHEVALET = id("chevalet");
    public static final ResourceLocation CUVE = id("cuve_a_tan");
    public static final ResourceLocation SECHOIR = id("sechoir");

    /** {@code travailler} : l'objet du curseur, ou de la case {@link #DEPUIS} de l'inventaire, travaille sur le bloc. */
    public static final String TRAVAILLER = "travailler";
    /** {@code main_vide} : reprendre ce qui est fini, ou lire où en est le travail. */
    public static final String MAIN_VIDE = "main_vide";
    /**
     * {@code garnir} : la page choisie dans le livre ({@link #PAGE}) garnit le bloc depuis l'inventaire : une fois, ou avec
     * {@link #TOUT} tant qu'il y a de la place et de quoi.
     */
    public static final String GARNIR = "garnir";
    public static final String DEPUIS = "depuis";
    public static final String PAGE = "page";
    public static final String TOUT = "tout";
    /** Le réservoir de la cuve, que la scène montre plein d'eau quand il y en a. */
    public static final String EAU = "eau";

    private PostesTannerie() {
    }

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, path);
    }

    static void register() {
        Diegesis.register(station(CHEVALET).block(TannerieModule.CHEVALET).hidesBlock().build());
        // La cuve est redessinée par la scène (son eau, ses peaux) : elle a un réservoir pour que l'eau y ondule.
        Diegesis.register(station(CUVE).block(TannerieModule.CUVE_A_TAN).hidesBlock()
                .schema(StationSchema.builder().tank(EAU, TankSpec.of(100)).build()).build());
        Diegesis.register(station(SECHOIR).block(TannerieModule.SECHOIR).hidesBlock().build());
    }

    private static StationType.Builder station(ResourceLocation id) {
        return Diegesis.station(id)
                .schema(StationSchema.builder().build())
                .logic(OuvrageLogic::new)
                .orientation(StationType.Orientation.AUTO)
                .openWhen(PostesTannerie::ouvre);
    }

    /** Accroupi, le bloc garde son usage d'avant ; le poste coupé (config) aussi. */
    static boolean ouvre(Player player, BlockPos pos, BlockState state) {
        return !player.isSecondaryUseActive() && FallowConfig.tannerieEnabled() && FallowConfig.postesTannerie();
    }
}
