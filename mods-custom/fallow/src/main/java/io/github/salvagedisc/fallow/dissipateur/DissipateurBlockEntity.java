package io.github.salvagedisc.fallow.dissipateur;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.clairiere.Clearings;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Le dissipateur se charge tant qu'il tourne assez vite sans surcharge ; chargé, il convertit une fois
 * le disque autour de lui. Cassé, il rend la brume à ce disque ; reposé, il se recharge et convertit là où il est.
 */
public class DissipateurBlockEntity extends KineticBlockEntity {
    public static final int RADIUS = 64;
    /** Dix secondes de rotation continue. */
    static final int CHARGE_TICKS = 200;

    private int charge;
    private boolean done;

    public DissipateurBlockEntity(BlockPos pos, BlockState state) {
        super(DissipateurModule.DISSIPATEUR_BE.get(), pos, state);
    }

    @Override
    public void tick() {
        super.tick();
        if (level == null) {
            return;
        }
        boolean running = getSpeed() != 0 && isSpeedRequirementFulfilled() && !isOverStressed();
        if (level.isClientSide) {
            if (running && level.random.nextInt(done ? 6 : 2) == 0) {
                spawnMist();
            }
            return;
        }
        if (done || !running || !FallowConfig.clairiereEnabled()) {
            if (!running && charge > 0 && !done) {
                charge = 0;
                setChanged();
            }
            return;
        }
        if (++charge % 20 == 0) {
            sendData(); // la jauge des lunettes d'ingénieur suit la charge
        }
        if (charge >= CHARGE_TICKS) {
            done = true;
            setChanged();
            Clearings.open((ServerLevel) level, worldPosition, RADIUS, null);
            sendData();
        }
    }

    /** Appelé par Create quand le bloc est retiré du monde (pas au déchargement du tronçon). */
    @Override
    public void destroy() {
        super.destroy();
        if (done && level instanceof ServerLevel server && FallowConfig.clairiereEnabled()) {
            Player nearest = level.getNearestPlayer(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5, 8.0, false);
            Clearings.close(server, worldPosition, nearest instanceof ServerPlayer player ? player : null);
        }
    }

    private void spawnMist() {
        double x = worldPosition.getX() + 0.5, y = worldPosition.getY() + 1.05, z = worldPosition.getZ() + 0.5;
        level.addParticle(ParticleTypes.CLOUD, x + (level.random.nextDouble() - 0.5) * 0.6, y,
                z + (level.random.nextDouble() - 0.5) * 0.6, 0.0, 0.08 + level.random.nextDouble() * 0.05, 0.0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        tooltip.add(Component.literal("    ").append(Component.translatable(done
                ? "tooltip.fallow.dissipateur.fait"
                : "tooltip.fallow.dissipateur.charge", charge * 100 / CHARGE_TICKS)).withStyle(ChatFormatting.GRAY));
        return true;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putInt("FallowCharge", charge);
        tag.putBoolean("FallowDone", done);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        charge = tag.getInt("FallowCharge");
        done = tag.getBoolean("FallowDone");
    }
}
