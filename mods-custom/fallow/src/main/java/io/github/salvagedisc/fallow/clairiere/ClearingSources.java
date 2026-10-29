package io.github.salvagedisc.fallow.clairiere;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Ce qui tient la brume à distance dans un monde : chaque feu de veille, lanterne, dissipateur chargé ou
 * clairière de commande, avec son rayon. Quand l'un disparaît, la brume ne revient que là où aucun autre
 * ne la retient encore.
 */
final class ClearingSources extends SavedData {
    private static final String NAME = "fallow_clairieres";

    record Source(BlockPos center, int radius) {
        boolean covers(long x, long z) {
            long dx = x - center.getX(), dz = z - center.getZ();
            return dx * dx + dz * dz <= (long) radius * radius;
        }
    }

    private final List<Source> sources = new ArrayList<>();

    static ClearingSources get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ClearingSources::new, ClearingSources::load, null), NAME);
    }

    void add(BlockPos center, int radius) {
        sources.removeIf(source -> source.center.equals(center));
        sources.add(new Source(center.immutable(), radius));
        setDirty();
    }

    /** Retire la source posée en {@code center} ; sans source enregistrée (monde d'avant 0.4.12), ne fait rien. */
    void remove(BlockPos center) {
        if (sources.removeIf(source -> source.center.equals(center))) {
            setDirty();
        }
    }

    /** Retire les sources dont le centre est dans le disque, et les rend pour que leur brume revienne aussi. */
    List<Source> removeWithin(BlockPos center, int radius) {
        List<Source> removed = new ArrayList<>();
        Source area = new Source(center, radius);
        sources.removeIf(source -> {
            boolean inside = area.covers(source.center.getX(), source.center.getZ());
            if (inside) {
                removed.add(source);
            }
            return inside;
        });
        if (!removed.isEmpty()) {
            setDirty();
        }
        return removed;
    }

    boolean covers(long x, long z) {
        for (Source source : sources) {
            if (source.covers(x, z)) {
                return true;
            }
        }
        return false;
    }

    private static ClearingSources load(CompoundTag tag, HolderLookup.Provider registries) {
        ClearingSources data = new ClearingSources();
        for (Tag entry : tag.getList("Sources", Tag.TAG_COMPOUND)) {
            CompoundTag source = (CompoundTag) entry;
            data.sources.add(new Source(BlockPos.of(source.getLong("Pos")), source.getInt("Radius")));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Source source : sources) {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Pos", source.center.asLong());
            entry.putInt("Radius", source.radius);
            list.add(entry);
        }
        tag.put("Sources", list);
        return tag;
    }
}
