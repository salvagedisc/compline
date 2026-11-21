package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.sounds.SoundEvent;

import com.pockyl.diegesis.api.client.StationView;

import io.github.salvagedisc.fallow.FallowConfig;

/**
 * Les bruits des postes passent tous par ici, comme chez Hands-On Vanilla ({@code StationHands.play}, MIT) : un seul réglage
 * ({@code postes.soundVolume}) les baisse ensemble, et un volume nul ne joue rien.
 */
final class Son {
    private Son() {
    }

    static void joue(StationView view, SoundEvent sound, String node, float volume, float pitch) {
        float scaled = volume * FallowConfig.postesSoundVolume();
        if (scaled > 0.0F) {
            view.playSound(sound, node, scaled, pitch);
        }
    }
}
