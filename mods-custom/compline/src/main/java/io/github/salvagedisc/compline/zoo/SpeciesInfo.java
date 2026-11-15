package io.github.salvagedisc.compline.zoo;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/** Une espèce du zoo : son type d'entité, son milieu et si l'on peut la garder (troisième tampon). */
public record SpeciesInfo(ResourceLocation id, ZooTab tab, boolean housable) {
    public static final StreamCodec<ByteBuf, SpeciesInfo> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SpeciesInfo::id,
            ByteBufCodecs.VAR_INT.map(ZooTab::byIndex, ZooTab::ordinal), SpeciesInfo::tab,
            ByteBufCodecs.BOOL, SpeciesInfo::housable,
            SpeciesInfo::new);

    /** Les tampons que demande Maîtriser, en plus de l'observation. */
    public int masterRequirement() {
        int mask = Stamp.APERCEVOIR.bit() | Stamp.PHOTOGRAPHIER.bit();
        return housable ? mask | Stamp.HEBERGER.bit() : mask;
    }
}
