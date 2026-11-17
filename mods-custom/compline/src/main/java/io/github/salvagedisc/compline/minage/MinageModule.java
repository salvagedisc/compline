package io.github.salvagedisc.compline.minage;

import com.mojang.serialization.MapCodec;
import io.github.salvagedisc.compline.Compline;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Module minage : une pioche bien forgée rend plus de minerai (docs/plan/progression.md). Le module n'enregistre que le
 * type de modificateur de butin ; ses réglages sont dans {@code data/compline/loot_modifiers/rendement_de_la_pioche.json}.
 */
public final class MinageModule {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Compline.MOD_ID);

    static {
        LOOT_MODIFIERS.register("rendement_de_la_pioche", () -> RendementDeLaPioche.CODEC);
    }

    private MinageModule() {
    }

    public static void start(IEventBus modBus) {
        LOOT_MODIFIERS.register(modBus);
    }
}
