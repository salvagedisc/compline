package io.github.salvagedisc.fallow.zoo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * Le carnet d'un joueur, attaché à lui et gardé à sa mort : pour chaque espèce, le masque de ses tampons
 * ({@link Stamp#bit()}) et le temps passé à l'observer, en ticks, jusqu'au seuil du tampon Maîtriser.
 */
public final class ZooRecord {
    public static final Codec<ZooRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("tampons", Map.of()).forGetter(r -> r.stamps),
            Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT).optionalFieldOf("observe", Map.of()).forGetter(r -> r.observed))
            .apply(instance, ZooRecord::new));

    private final Map<ResourceLocation, Integer> stamps;
    private final Map<ResourceLocation, Integer> observed;

    public ZooRecord() {
        this(Map.of(), Map.of());
    }

    private ZooRecord(Map<ResourceLocation, Integer> stamps, Map<ResourceLocation, Integer> observed) {
        this.stamps = new HashMap<>(stamps);
        this.observed = new HashMap<>(observed);
    }

    public int stamps(ResourceLocation species) {
        return stamps.getOrDefault(species, 0);
    }

    public boolean has(ResourceLocation species, Stamp stamp) {
        return stamp.in(stamps(species));
    }

    /** Ajoute un tampon ; vrai s'il est nouveau. */
    boolean add(ResourceLocation species, Stamp stamp) {
        int mask = stamps(species);
        if (stamp.in(mask)) {
            return false;
        }
        stamps.put(species, mask | stamp.bit());
        return true;
    }

    public int observedTicks(ResourceLocation species) {
        return observed.getOrDefault(species, 0);
    }

    void observe(ResourceLocation species, int ticks, int cap) {
        observed.put(species, Math.min(cap, observedTicks(species) + ticks));
    }

    void clear() {
        stamps.clear();
        observed.clear();
    }

    /** Ce que le client reçoit : le masque en bas, les secondes d'observation au-dessus du huitième bit. */
    int packed(ResourceLocation species) {
        return stamps(species) | (observedTicks(species) / 20) << 8;
    }

    Map<ResourceLocation, Integer> packedAll() {
        Map<ResourceLocation, Integer> out = new HashMap<>();
        for (ResourceLocation id : stamps.keySet()) {
            out.put(id, packed(id));
        }
        for (ResourceLocation id : observed.keySet()) {
            out.put(id, packed(id));
        }
        return out;
    }
}
