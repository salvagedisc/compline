package io.github.salvagedisc.fallow.postes.client;

import java.util.List;

import org.joml.Vector3f;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.client.DiegesisParticles;
import com.pockyl.diegesis.api.client.Floater;
import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.scene.Visual;

import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.tannerie.CuveBlock;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/**
 * La cuve à tan au poste, sur le modèle de la marmite de Hands-On Delight : une vraie eau qui ondule, qui prend la couleur du
 * tan à mesure qu'on y met de l'écorce et que les peaux trempent, l'écorce qui flotte, les peaux sous la surface qui fonçent
 * jusqu'au cuir, des bulles tant qu'elles trempent. Chaque chose qui arrive plonge et ride l'eau ; le cuir prêt brille et
 * sonne. Au-dessus flotte, pâle, ce qu'il faut y mettre ensuite.
 */
final class CuveAllure implements Allure {
    /** Le liquide de la cuve : sa couleur est donnée à chaque image. */
    static final ResourceLocation LIQUIDE = ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "eau_de_cuve");

    private static final int TAN = 0x6B3A12;
    private static final int CUIR = 0xFF9A7452;
    private static final int ECORCE = 0xFF6B4A2E;
    private static final float ARRIVEE = 0.45F;

    private final RandomSource random = RandomSource.create();
    private int teinte = -1;
    private float horloge;
    private float bulles;
    private int eau = -1;
    private int tanin = -1;
    private int peaux = -1;
    private int tannage = -1;
    private int flottants = -1;
    private boolean pret;
    /** Depuis quand chaque peau est arrivée, pour la voir plonger. */
    private final float[] arrivees = { 9.0F, 9.0F, 9.0F, 9.0F };
    private final boolean[] cuir = new boolean[4];
    private Item fantome;
    private final Envol envol = new Envol();
    /** Les notes du cuir prêt, jouées l'une après l'autre. */
    private int carillon = -1;

    @Override
    public void onOpen(StationView view) {
        fantome = null;
    }

    @Override
    public void frame(StationView view, float dt) {
        envol.frame(view, dt);
        BlockState state = view.level().getBlockState(view.pos());
        if (!(state.getBlock() instanceof CuveBlock)) {
            return;
        }
        horloge += dt;
        boolean shown = view.isBlockHidden();
        boolean water = state.getValue(CuveBlock.EAU);
        int bark = state.getValue(CuveBlock.TANIN);
        int hides = state.getValue(CuveBlock.PEAUX);
        int step = state.getValue(CuveBlock.TANNAGE);
        boolean ready = hides > 0 && step >= CuveBlock.ETAPES;
        float soak = hides > 0 ? step / (float) CuveBlock.ETAPES : 0.0F;
        view.node("cuve").ifPresent(node -> node.setVisible(shown));

        view.node("eau").ifPresent(node -> {
            node.setVisible(shown && water);
            node.showLiquid(LIQUIDE, water ? 1.0F : 0.0F);
            int clear = BiomeColors.getAverageWaterColor(view.level(), view.pos()) & 0xFFFFFF;
            float brown = Math.min(1.0F, bark / 2.0F) * 0.8F + soak * 0.15F;
            int alpha = (int) Mth.lerp(brown, 0xA0, 0xF0);
            int target = alpha << 24 | melange(clear, TAN, brown);
            teinte = teinte == -1 ? target : approche(teinte, target, Math.min(1.0F, dt * 1.5F));
            node.setLiquidColor(teinte);
            node.setAgitation(hides > 0 && !ready ? 0.035F : 0.01F);
            node.setMottle(brown * 0.7F);
            int floating = water ? Math.min(8, Math.max(0, bark - hides)) : 0;
            if (floating != flottants) {
                flottants = floating;
                node.setFloating(List.of(new Floater(Floater.SHRED, ECORCE, 2.0F), new Floater(Floater.LEAF, 0xFF7E5B36, 1.6F)), floating);
            }
        });
        if (shown && water && hides > 0 && !ready) {
            bulles += dt * 0.9F;
            if (bulles >= 1.0F) {
                view.spawnInLiquid(DiegesisParticles.BUBBLE, "eau", (int) bulles, 0x90E8D8C0, new Vector3f(0.0F, 5.0F, 0.0F));
                bulles -= (int) bulles;
            }
        }

        for (int i = 0; i < 4; i++) {
            final int index = i;
            arrivees[i] += dt;
            boolean here = shown && i < hides;
            view.node("peau_" + i).ifPresent(node -> {
                node.setVisible(here);
                if (!here) {
                    return;
                }
                if (cuir[index] != ready) {
                    cuir[index] = ready;
                    node.setVisual(Visual.ofModel(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID,
                            ready ? "diegesis/cuir_mouille" : "diegesis/peau_raclee")));
                }
                node.setTint(ready ? 0xFFFFFFFF : 0xFF000000 | melange(0xFFFFFF, CUIR & 0xFFFFFF, soak));
                // Arrivée : elle tombe de haut et s'enfonce ; ensuite elle bouge à peine avec l'eau.
                float t = Math.min(1.0F, arrivees[index] / ARRIVEE);
                float fall = (1.0F - t) * (1.0F - t) * 7.0F;
                float bob = 0.22F * Mth.sin(horloge * 1.3F + index * 1.7F);
                node.moveTo(0.0F, fall + bob, 0.0F, 0.0F);
            });
        }

        Item wanted = shown && view.isInUse() ? attendu(water, bark, hides, ready) : null;
        view.node("fantome").ifPresent(node -> {
            node.setVisible(wanted != null);
            if (wanted != fantome) {
                fantome = wanted;
                if (wanted != null) {
                    node.setVisual(Visual.ofItem(wanted).withTint(0x90FFFFFF).asTranslucent());
                }
            }
            if (wanted != null) {
                node.moveTo(0.0F, 0.5F * Mth.sin(horloge * 2.2F), 0.0F, 0.0F);
                node.rotateTo(0.0F, 12.0F * Mth.sin(horloge * 1.3F), 0.0F, 0.0F);
            }
        });
    }

    /** Ce qui manque à la cuve, dans l'ordre où on l'y met ; rien quand elle est pleine ou que le cuir attend. */
    private static Item attendu(boolean water, int bark, int hides, boolean ready) {
        if (ready || hides >= 4) {
            return null;
        }
        if (!water) {
            return Items.WATER_BUCKET;
        }
        if (bark <= hides) {
            return BuiltInRegistries.ITEM.getTag(TannerieModule.TANINS).flatMap(tag -> tag.stream().findFirst())
                    .map(holder -> holder.value()).orElse(null);
        }
        return TannerieModule.PEAU_RACLEE.get();
    }

    @Override
    public void tick(StationView view) {
        BlockState state = view.level().getBlockState(view.pos());
        if (!(state.getBlock() instanceof CuveBlock)) {
            return;
        }
        int water = state.getValue(CuveBlock.EAU) ? 1 : 0;
        int bark = state.getValue(CuveBlock.TANIN);
        int hides = state.getValue(CuveBlock.PEAUX);
        int step = state.getValue(CuveBlock.TANNAGE);
        boolean ready = hides > 0 && step >= CuveBlock.ETAPES;
        boolean known = eau >= 0;
        if (known && view.isBlockHidden()) {
            if (water > eau) {
                eclabousse(view, 8.0F, 8.0F, 1.2F, 10);
            }
            if (bark > tanin) {
                eclabousse(view, 5.0F + random.nextFloat() * 6.0F, 5.0F + random.nextFloat() * 6.0F, 0.6F, 4);
            }
            if (hides > peaux) {
                for (int i = Math.max(0, peaux); i < hides; i++) {
                    arrivees[i] = 0.0F;
                }
                eclabousse(view, 8.0F, 8.0F, 0.9F, 8);
            }
            if (hides < peaux) {
                eclabousse(view, 8.0F, 8.0F, 0.7F, 6);
                // Le cuir sort de la cuve et vole jusqu'à la barre (la peau reprise avant d'être prête aussi).
                envol.part(view, pret ? TannerieModule.CUIR_MOUILLE.get() : TannerieModule.PEAU_RACLEE.get(), new Vector3f(8.0F, 13.0F, 8.0F));
            }
            if (ready && !pret) {
                carillon = 0;
                view.spawnInLiquid(DiegesisParticles.GLINT, "eau", 10, 0xFFFFE2A0, new Vector3f(0.0F, 4.0F, 0.0F));
            }
        }
        if (carillon >= 0) {
            // Trois notes qui montent, une tous les trois ticks, comme les étoiles d'un plat de Hands-On Delight.
            if (carillon % 3 == 0) {
                float[] pitches = { 1.0F, 1.26F, 1.5F };
                view.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), "eau", 0.35F, pitches[carillon / 3]);
            }
            carillon = carillon >= 6 ? -1 : carillon + 1;
        }
        eau = water;
        tanin = bark;
        peaux = hides;
        tannage = step;
        pret = ready;
    }

    private void eclabousse(StationView view, float x, float z, float force, int gouttes) {
        view.node("eau").ifPresent(node -> node.ripple(x, z, force));
        view.spawnParticles(DiegesisParticles.SPLASH, new Vector3f(x, 13.2F, z), new Vector3f(0.0F, 10.0F, 0.0F), 2.0F, gouttes,
                0xC0C8B89A);
    }

    private static int melange(int a, int b, float t) {
        int r = (int) Mth.lerp(t, a >> 16 & 0xFF, b >> 16 & 0xFF);
        int g = (int) Mth.lerp(t, a >> 8 & 0xFF, b >> 8 & 0xFF);
        int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
        return r << 16 | g << 8 | bl;
    }

    private static int approche(int from, int to, float t) {
        int alpha = (int) Mth.lerp(t, from >>> 24, to >>> 24);
        return alpha << 24 | melange(from & 0xFFFFFF, to & 0xFFFFFF, t);
    }
}
