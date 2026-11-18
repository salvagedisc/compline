package io.github.salvagedisc.compline.clairiere;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

/**
 * Ce qui tient la brume à distance dans un monde : chaque feu de veille, lanterne, dissipateur chargé ou
 * clairière de commande, avec son rayon et l'heure où il a été posé. Le monde lui-même ne change pas :
 * une clairière n'existe que par cette liste.
 */
final class ClearingSources extends SavedData {
    private static final String NAME = "compline_clairieres";

    private final List<Clearing> clearings = new ArrayList<>();
    private ClearingIndex index = ClearingIndex.EMPTY;

    static ClearingSources get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ClearingSources::new, ClearingSources::load, null), NAME);
    }

    ClearingIndex index() {
        return index;
    }

    private void changed() {
        index = new ClearingIndex(clearings);
        setDirty();
    }

    /** Vrai si une clairière est posée en {@code center}. */
    boolean has(BlockPos center) {
        for (Clearing clearing : clearings) {
            if (clearing.center().equals(center)) {
                return true;
            }
        }
        return false;
    }

    Clearing add(BlockPos center, int radius, long since) {
        clearings.removeIf(clearing -> clearing.center().equals(center));
        Clearing clearing = new Clearing(center.immutable(), radius, since, Clearing.OPEN);
        clearings.add(clearing);
        changed();
        return clearing;
    }

    /** Retire la clairière posée en {@code center} et la rend ; {@code null} s'il n'y en avait pas. */
    @Nullable
    Clearing remove(BlockPos center) {
        for (Clearing clearing : clearings) {
            if (clearing.center().equals(center)) {
                clearings.remove(clearing);
                changed();
                return clearing;
            }
        }
        return null;
    }

    /** Retire les clairières dont le centre est à {@code radius} blocs de {@code center} au plus, et les rend. */
    List<Clearing> removeWithin(BlockPos center, int radius) {
        List<Clearing> removed = new ArrayList<>();
        for (Clearing clearing : clearings) {
            if (clearing.covers(center.getX(), center.getZ(), radius)) {
                removed.add(clearing);
            }
        }
        if (clearings.removeAll(removed)) {
            changed();
        }
        return removed;
    }

    private static ClearingSources load(CompoundTag tag, HolderLookup.Provider registries) {
        ClearingSources data = new ClearingSources();
        for (Tag entry : tag.getList("Sources", Tag.TAG_COMPOUND)) {
            CompoundTag source = (CompoundTag) entry;
            // « Since » manque dans les mondes d'avant 0.8.0 : 0, la clairière est ouverte depuis toujours.
            data.clearings.add(new Clearing(BlockPos.of(source.getLong("Pos")), source.getInt("Radius"),
                    source.getLong("Since"), Clearing.OPEN));
        }
        data.index = new ClearingIndex(data.clearings);
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Clearing clearing : clearings) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", clearing.center().asLong());
            entry.putInt("Radius", clearing.radius());
            entry.putLong("Since", clearing.since());
            list.add(entry);
        }
        tag.put("Sources", list);
        return tag;
    }
}
