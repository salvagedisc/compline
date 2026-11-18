package io.github.salvagedisc.compline.bord;

import com.google.gson.JsonObject;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ComplineConfig;
import java.io.Reader;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Module bord : le monde est un disque, et la brume en garde le bord. Le fichier de données
 * {@code data/compline/monde/bord.json} (centre et rayon, écrit par {@code scripts/terrain/disc.py} avec la forme
 * du continent) donne un cercle dans l'Overworld. Un joueur qui le dépasse est ramené deux blocs en deçà, avec sa
 * monture ou son bateau, et un message le lui dit ; à l'approche, un autre message le prévient.
 *
 * <p>Le bord est rond, ce que la bordure du jeu ne sait pas faire, et il ne se voit pas : en mer, la brume cache
 * déjà le large. Sans le fichier, le module ne fait rien. Les spectateurs passent. Un joueur à bord d'un navire de
 * Sable (dirigeable) passe aussi, prévenu par un message : le ramener sans son navire le lui ferait perdre
 * (voir {@link Ships}).
 */
public final class BordModule {
    private static final ResourceLocation FILE = ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "monde/bord.json");
    /** Largeur de la bande, en blocs, où le joueur est prévenu avant le bord. */
    private static final double WARNING = 64.0;
    /** Le joueur est ramené à cette distance en deçà du bord. */
    private static final double STEP_BACK = 2.0;
    /** Un rappel toutes les cinq secondes dans la bande d'approche. */
    private static final int WARNING_INTERVAL = 100;

    private static volatile Edge edge;

    /** Le bord du monde : un cercle dans l'Overworld. */
    public record Edge(double centerX, double centerZ, double radius) {
        /** Le point où ramener ce qui est sorti, ou {@code null} si la position est dans le cercle. */
        public Vec3 inside(double x, double y, double z) {
            double dx = x - centerX;
            double dz = z - centerZ;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance <= radius) {
                return null;
            }
            double scale = Math.max(0.0, radius - STEP_BACK) / distance;
            return new Vec3(centerX + dx * scale, y, centerZ + dz * scale);
        }

        boolean isNear(double x, double z) {
            double dx = x - centerX;
            double dz = z - centerZ;
            double near = radius - WARNING;
            return near <= 0.0 || dx * dx + dz * dz >= near * near;
        }
    }

    private BordModule() {
    }

    public static void start(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class,
                event -> event.addListener((ResourceManagerReloadListener) BordModule::load));
        NeoForge.EVENT_BUS.addListener(PlayerTickEvent.Post.class, BordModule::onPlayerTick);
        Compline.LOGGER.info("bord : le bord du monde suit data/compline/monde/bord.json");
    }

    /** Le bord en vigueur, ou {@code null} s'il n'y en a pas. */
    public static Edge edge() {
        return edge;
    }

    private static void load(ResourceManager manager) {
        Optional<Resource> resource = manager.getResource(FILE);
        if (resource.isEmpty()) {
            edge = null;
            Compline.LOGGER.info("bord : pas de fichier {}, le monde n'a pas de bord", FILE);
            return;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject json = GsonHelper.parse(reader);
            Edge loaded = new Edge(GsonHelper.getAsDouble(json, "center_x", 0.0), GsonHelper.getAsDouble(json, "center_z", 0.0),
                    GsonHelper.getAsDouble(json, "radius"));
            if (loaded.radius() <= 0.0) {
                throw new IllegalArgumentException("radius doit être positif");
            }
            edge = loaded;
            Compline.LOGGER.info("bord : cercle de rayon {} autour de {}, {}", loaded.radius(), loaded.centerX(), loaded.centerZ());
        } catch (Exception e) {
            edge = null;
            Compline.LOGGER.error("bord : {} illisible, le monde n'a pas de bord", FILE, e);
        }
    }

    private static void onPlayerTick(PlayerTickEvent.Post event) {
        Edge current = edge;
        if (current == null || !(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()
                || player.level().dimension() != Level.OVERWORLD) {
            return;
        }
        // Les parcelles de Sable sont loin hors du disque : un joueur bien à l'intérieur est à terre, et la
        // question à Sable n'est posée que près du bord ou à bord d'un navire.
        if (!ComplineConfig.bordEnabled() || !current.isNear(player.getX(), player.getZ())) {
            return;
        }
        Vec3 position = Ships.worldPosition(player);
        if (!current.isNear(position.x, position.z)) {
            return;
        }
        Vec3 back = current.inside(position.x, position.y, position.z);
        if (back == null) {
            if (player.tickCount % WARNING_INTERVAL == 0) {
                player.displayClientMessage(Component.translatable("message.compline.bord.proche"), true);
            }
            return;
        }
        if (Ships.isAboard(player)) {
            // Ramener le joueur sans son navire le lui ferait perdre, et ce module ne sait pas encore arrêter un navire.
            if (player.tickCount % WARNING_INTERVAL == 0) {
                player.displayClientMessage(Component.translatable("message.compline.bord.navire"), true);
            }
            return;
        }
        Entity root = player.getRootVehicle();
        if (root == player) {
            player.connection.teleport(back.x, back.y, back.z, player.getYRot(), player.getXRot());
        } else {
            // Un bateau ou une monture est dirigé par le client : il faut lui dire que le véhicule a reculé et s'est arrêté.
            Vec3 rootBack = current.inside(root.getX(), root.getY(), root.getZ());
            if (rootBack == null) {
                rootBack = new Vec3(back.x, root.getY(), back.z);
            }
            root.moveTo(rootBack.x, rootBack.y, rootBack.z, root.getYRot(), root.getXRot());
            root.setDeltaMovement(Vec3.ZERO);
            for (Entity rider : root.getIndirectPassengers()) {
                if (rider instanceof ServerPlayer riding) {
                    riding.connection.send(new ClientboundMoveVehiclePacket(root));
                    riding.connection.send(new ClientboundSetEntityMotionPacket(root));
                }
            }
        }
        player.displayClientMessage(Component.translatable("message.compline.bord.limite"), true);
    }
}
