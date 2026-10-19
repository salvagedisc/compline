package io.github.salvagedisc.fallow;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration de Fallow : un booléen par module, plus quelques réglages.
 * {@code config/fallow-client.toml} pour les modules client, {@code config/fallow-common.toml} pour les autres.
 */
public final class FallowConfig {
    public static final ModConfigSpec CLIENT_SPEC;
    public static final ModConfigSpec COMMON_SPEC;

    static {
        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        CLIENT_SPEC = client.build();

        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        COMMON_SPEC = common.build();
    }

    private FallowConfig() {
    }

    /** Lit une valeur sans planter si la config n'est pas encore chargée. */
    static boolean read(ModConfigSpec spec, ModConfigSpec.BooleanValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }

    static int read(ModConfigSpec spec, ModConfigSpec.IntValue value) {
        return spec.isLoaded() ? value.get() : value.getDefault();
    }
}
