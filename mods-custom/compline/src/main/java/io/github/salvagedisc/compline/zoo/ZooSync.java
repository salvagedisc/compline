package io.github.salvagedisc.compline.zoo;

import io.github.salvagedisc.compline.Compline;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Ce que le serveur dit au client du zoo. {@code full} : la liste entière des espèces et tout le carnet du joueur
 * (connexion, commande), avec le temps d'observation que demande Maîtriser ; sinon des ajouts (une espèce nouvellement aperçue, un tampon, du temps d'observation).
 * Les valeurs du carnet sont celles de {@link ZooRecord#packed} : le masque des tampons, puis les secondes
 * d'observation au-dessus du huitième bit.
 */
public record ZooSync(boolean full, int observeSeconds, List<SpeciesInfo> species, Map<ResourceLocation, Integer> stamps) implements CustomPacketPayload {
    public static final Type<ZooSync> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Compline.MOD_ID, "zoo"));
    public static final StreamCodec<ByteBuf, ZooSync> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ZooSync::full,
            ByteBufCodecs.VAR_INT, ZooSync::observeSeconds,
            SpeciesInfo.STREAM_CODEC.apply(ByteBufCodecs.list()), ZooSync::species,
            ByteBufCodecs.<ByteBuf, ResourceLocation, Integer, Map<ResourceLocation, Integer>>map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), ZooSync::stamps,
            ZooSync::new);

    @Override
    public Type<ZooSync> type() {
        return TYPE;
    }
}
