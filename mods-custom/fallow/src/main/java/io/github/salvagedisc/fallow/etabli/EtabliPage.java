package io.github.salvagedisc.fallow.etabli;

import io.github.salvagedisc.fallow.Fallow;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** La page du carnet ouverte pour une table : la recette dont le client dessine les ingrédients en fantôme. */
public record EtabliPage(BlockPos table, Optional<ResourceLocation> recipe) implements CustomPacketPayload {
    public static final Type<EtabliPage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "etabli_page"));
    public static final StreamCodec<ByteBuf, EtabliPage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, EtabliPage::table,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), EtabliPage::recipe,
            EtabliPage::new);

    @Override
    public Type<EtabliPage> type() {
        return TYPE;
    }
}
