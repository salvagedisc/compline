package io.github.salvagedisc.compline.clairiere;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.world.level.ChunkPos;

/**
 * Les clairières d'un monde, rangées par carré de 64 blocs pour répondre vite à « ce point est-il dégagé ? ».
 * Un index ne change jamais : on en construit un autre à chaque changement, ce qui permet de le lire depuis
 * les fils qui dessinent les tronçons.
 */
public final class ClearingIndex {
    public static final ClearingIndex EMPTY = new ClearingIndex(List.of());
    private static final int SHIFT = 6;
    private static final Clearing[] NONE = new Clearing[0];

    private final List<Clearing> all;
    private final Long2ObjectMap<Clearing[]> squares = new Long2ObjectOpenHashMap<>();

    public ClearingIndex(Collection<Clearing> clearings) {
        this.all = List.copyOf(clearings);
        Long2ObjectMap<List<Clearing>> lists = new Long2ObjectOpenHashMap<>();
        for (Clearing clearing : all) {
            int minX = (clearing.center().getX() - clearing.radius()) >> SHIFT;
            int maxX = (clearing.center().getX() + clearing.radius()) >> SHIFT;
            int minZ = (clearing.center().getZ() - clearing.radius()) >> SHIFT;
            int maxZ = (clearing.center().getZ() + clearing.radius()) >> SHIFT;
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    lists.computeIfAbsent(ChunkPos.asLong(x, z), key -> new ArrayList<>()).add(clearing);
                }
            }
        }
        lists.forEach((key, list) -> squares.put(key.longValue(), list.toArray(NONE)));
    }

    public List<Clearing> all() {
        return all;
    }

    public boolean isEmpty() {
        return all.isEmpty();
    }

    private Clearing[] near(double x, double z) {
        return squares.getOrDefault(ChunkPos.asLong((int) Math.floor(x) >> SHIFT, (int) Math.floor(z) >> SHIFT), NONE);
    }

    /** Vrai si une clairière, à son plein rayon, couvre {@code (x, z)} ; {@code except} n'est pas comptée. */
    public boolean covers(double x, double z, Clearing except) {
        for (Clearing clearing : near(x, z)) {
            if (clearing != except && clearing.covers(x, z, clearing.radius())) {
                return true;
            }
        }
        return false;
    }

    /** Vrai si la vague d'une clairière a déjà dégagé {@code (x, z)} à l'heure {@code time}. */
    public boolean coversAt(double x, double z, long time, int ringTicks) {
        for (Clearing clearing : near(x, z)) {
            if (clearing.covers(x, z, clearing.radiusAt(time, ringTicks))) {
                return true;
            }
        }
        return false;
    }
}
