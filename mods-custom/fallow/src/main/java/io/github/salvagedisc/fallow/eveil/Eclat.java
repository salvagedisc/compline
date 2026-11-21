package io.github.salvagedisc.fallow.eveil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Ce que porte un éclat de grande bête : l'enchantement, son niveau, et la bête (clé de traduction de la créature). */
public record Eclat(ResourceLocation enchantement, int niveau, String bete) {
    public static final Codec<Eclat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("enchantement").forGetter(Eclat::enchantement),
            Codec.intRange(1, 255).fieldOf("niveau").forGetter(Eclat::niveau),
            Codec.STRING.fieldOf("bete").forGetter(Eclat::bete)
    ).apply(instance, Eclat::new));

    public static final StreamCodec<ByteBuf, Eclat> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, Eclat::enchantement,
            ByteBufCodecs.VAR_INT, Eclat::niveau,
            ByteBufCodecs.STRING_UTF8, Eclat::bete,
            Eclat::new);
}
