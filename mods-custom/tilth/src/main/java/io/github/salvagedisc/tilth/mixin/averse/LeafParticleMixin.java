package io.github.salvagedisc.tilth.mixin.averse;

import io.github.salvagedisc.tilth.TilthConfig;
import io.github.salvagedisc.tilth.averse.Wind;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TextureSheetParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Les feuilles de Particular dans le vent. Dans l'air, leur vitesse horizontale rejoint peu à peu
 * celle du vent (Where Winds Blow s'il est là) : par temps calme elles tombent presque droit, sous
 * l'orage elles filent de biais. Posées, elles restent au sol {@code averse.leafRestSeconds} avant
 * de s'effacer, au lieu de cinq secondes. Appliqué seulement avec Particular.
 */
@Pseudo
@Mixin(targets = "com.leclowndu93150.particular.particles.leaves.LeafParticle", remap = false)
abstract class LeafParticleMixin extends TextureSheetParticle {
    @Shadow
    protected boolean expiring;

    @Unique
    private boolean tilth$rested;

    protected LeafParticleMixin(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z);
    }

    @Inject(method = "tick", at = @At("TAIL"), require = 0)
    private void tilth$wind(CallbackInfo ci) {
        if (!TilthConfig.averseWindblownLeaves()) {
            return;
        }
        if (this.expiring) {
            if (!this.tilth$rested) {
                this.tilth$rested = true;
                // le fondu de Particular commence 100 ticks avant la fin : on le repousse
                this.lifetime = this.age + 100 + TilthConfig.averseLeafRestTicks();
            }
            return;
        }
        if (this.age <= 4) {
            return;
        }
        float[] wind = Wind.here(this.level);
        float speed = Math.min(wind[2], 3.0F) * 0.045F;
        // une feuille ne suit pas le vent d'un coup : elle s'y laisse prendre
        this.xd += (wind[0] * speed - this.xd) * 0.08;
        this.zd += (wind[1] * speed - this.zd) * 0.08;
        if (wind[2] > 1.5F && this.random.nextFloat() < 0.03F) {
            this.yd += 0.015 + this.random.nextFloat() * 0.02; // une rafale la relève un instant
        }
    }
}
