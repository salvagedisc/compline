package io.github.salvagedisc.compline.mixin.ciel;

import io.github.salvagedisc.compline.ComplineConfig;
import io.github.salvagedisc.compline.ciel.FogCache;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vide le cache de brume d'ATMOSPHERICS et ralentit son fondu quand le serveur envoie de nouveaux biomes
 * ({@code /fillbiome} et les clairières renvoient ce paquet aux joueurs). {@code TAIL} n'est atteint que sur le fil du client, une fois les
 * biomes remplacés : {@code ensureRunningOnSameThread} interrompt d'abord l'appel venu du réseau.
 * Appliqué seulement avec ATMOSPHERICS.
 */
@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
    @Inject(method = "handleChunksBiomes", at = @At("TAIL"))
    private void compline$clearFogCache(ClientboundChunksBiomesPacket packet, CallbackInfo ci) {
        if (ComplineConfig.cielClearFogOnBiomeUpdate()) {
            FogCache.clear();
        }
        FogCache.slowFade();
    }
}
