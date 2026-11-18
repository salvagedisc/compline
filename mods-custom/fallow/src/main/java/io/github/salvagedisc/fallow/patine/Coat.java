package io.github.salvagedisc.fallow.patine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

/**
 * Ce que l'armure a pris du monde en plus de la terre, de 0 à {@link Patine#MAX} chacun : le sable, qui tombe de
 * lui-même en marchant ailleurs ; la neige, qui fond hors du froid ; l'eau, qui sèche à l'air.
 */
public record Coat(int sand, int snow, int wet) {
    public static final Coat NONE = new Coat(0, 0, 0);
    private static final Codec<Integer> AMOUNT = ExtraCodecs.intRange(0, Patine.MAX);
    public static final Codec<Coat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AMOUNT.optionalFieldOf("sand", 0).forGetter(Coat::sand),
            AMOUNT.optionalFieldOf("snow", 0).forGetter(Coat::snow),
            AMOUNT.optionalFieldOf("wet", 0).forGetter(Coat::wet)).apply(instance, Coat::new));
    public static final StreamCodec<ByteBuf, Coat> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Coat::sand, ByteBufCodecs.VAR_INT, Coat::snow, ByteBufCodecs.VAR_INT, Coat::wet, Coat::new);

    public Coat {
        sand = clamp(sand);
        snow = clamp(snow);
        wet = clamp(wet);
    }

    public boolean isEmpty() {
        return sand == 0 && snow == 0 && wet == 0;
    }

    /** Le palier visible du sable (0 à 3). */
    public int sandLevel() {
        return sand < 10 ? 0 : sand < 35 ? 1 : sand < 65 ? 2 : 3;
    }

    /** Le palier visible de la neige (0 à 3). */
    public int snowLevel() {
        return snow < 10 ? 0 : snow < 35 ? 1 : snow < 65 ? 2 : 3;
    }

    /** Le palier visible de l'eau (0 sec, 1 humide, 2 trempée). */
    public int wetLevel() {
        return wet < 8 ? 0 : wet < 55 ? 1 : 2;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(Patine.MAX, value));
    }
}
