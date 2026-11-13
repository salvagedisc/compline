package io.github.salvagedisc.tilth.bord;

import io.github.salvagedisc.tilth.Tilth;
import io.github.salvagedisc.tilth.ModIds;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Les navires de Sable (dirigeables de Create Aeronautics) vus depuis le bord du monde. Sable range les blocs
 * d'un navire dans une grille de parcelles très loin du monde, et un joueur assis à bord a ses coordonnées
 * là-bas : sans cette classe, le bord le croirait sorti du disque. Elle demande à Sable où le joueur se trouve
 * vraiment, et s'il est à bord d'un navire.
 *
 * <p>Sable n'est pas une dépendance : ses deux méthodes sont cherchées par réflexion, une fois. Sans Sable, ou
 * si une version future les renomme, la position du joueur est prise telle quelle.
 */
final class Ships {
    private static final Object COMPANION;
    private static final MethodHandle PROJECT;
    private static final MethodHandle SHIP_OF;

    static {
        Object companion = null;
        MethodHandle project = null;
        MethodHandle shipOf = null;
        if (ModIds.isLoaded(ModIds.SABLE)) {
            try {
                Class<?> type = Class.forName("dev.ryanhcode.sable.companion.SableCompanion");
                Class<?> ship = Class.forName("dev.ryanhcode.sable.companion.SubLevelAccess");
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                companion = type.getField("INSTANCE").get(null);
                project = lookup.findVirtual(type, "projectOutOfSubLevel", MethodType.methodType(Vec3.class, Level.class, Vec3.class));
                shipOf = lookup.findVirtual(type, "getTrackingOrVehicleSubLevel", MethodType.methodType(ship, Entity.class));
                Tilth.LOGGER.info("bord : Sable présent, la position des joueurs à bord d'un navire est demandée à Sable");
            } catch (ReflectiveOperationException | RuntimeException e) {
                companion = null;
                Tilth.LOGGER.warn("bord : interface de Sable introuvable, un joueur assis à bord d'un navire peut être pris pour sorti du monde", e);
            }
        }
        COMPANION = companion;
        PROJECT = project;
        SHIP_OF = shipOf;
    }

    private Ships() {
    }

    /** La position de l'entité dans le monde, même si elle est rangée dans la parcelle d'un navire. */
    static Vec3 worldPosition(Entity entity) {
        Vec3 position = entity.position();
        if (COMPANION == null) {
            return position;
        }
        try {
            Vec3 projected = (Vec3) PROJECT.invoke(COMPANION, entity.level(), position);
            return projected != null ? projected : position;
        } catch (Throwable e) {
            return position;
        }
    }

    /** Vrai si l'entité se tient ou est assise sur un navire. */
    static boolean isAboard(Entity entity) {
        if (COMPANION == null) {
            return false;
        }
        try {
            return SHIP_OF.invoke(COMPANION, entity) != null;
        } catch (Throwable e) {
            return false;
        }
    }
}
