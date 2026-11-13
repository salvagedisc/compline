package io.github.salvagedisc.tilth.clairiere;

import io.github.salvagedisc.tilth.Tilth;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Ce que le serveur dit aux clients des clairières : la liste entière à la connexion ({@link #ALL}), puis
 * chaque clairière qui s'ouvre ({@link #OPENED}) ou se referme ({@link #CLOSED}, avec son heure de fermeture).
 * {@code ringTicks} est le rythme de la vague, réglé sur le serveur.
 */
public record ClearingSync(int kind, List<Clearing> clearings, int ringTicks) implements CustomPacketPayload {
    public static final int ALL = 0;
    public static final int OPENED = 1;
    public static final int CLOSED = 2;

    public static final Type<ClearingSync> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Tilth.MOD_ID, "clairieres"));
    public static final StreamCodec<ByteBuf, ClearingSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ClearingSync::kind,
            Clearing.STREAM_CODEC.apply(ByteBufCodecs.list()), ClearingSync::clearings,
            ByteBufCodecs.VAR_INT, ClearingSync::ringTicks,
            ClearingSync::new);

    @Override
    public Type<ClearingSync> type() {
        return TYPE;
    }
}
