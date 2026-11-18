package io.github.salvagedisc.fallow.patine;

import io.github.salvagedisc.fallow.FallowConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * La saleté des armures, côté serveur. Chaque seconde, chaque pièce portée a une chance de prendre un point (sur
 * {@link #MAX}) : plus en courant, sur la terre et la boue, au combat ; les bottes plus vite que le casque. Dans l'eau,
 * elle se lave en quelques secondes (le casque seulement tête sous l'eau) ; la pluie la rince lentement. Le tirage au
 * sort évite de garder un compteur par joueur, et les pièces se salissent chacune à son rythme.
 *
 * <p>L'armure prend aussi le monde ({@link Coat}) : le sable en marchant dessus, surtout les bottes, et qui retombe en
 * marchant ailleurs ; la neige qui tombe sur le casque et les épaules, ou tassée autour des bottes, et qui fond hors du
 * froid ; l'eau d'une baignade ou de la pluie, qui sèche en moins d'une minute, plus vite au chaud.
 */
public final class Patine {
    public static final int MAX = 100;
    /** Les pièces qui voient le sol se salissent plus vite. */
    private static final float[] SLOT_WEIGHT = {1.5F, 1.2F, 1.0F, 0.7F};
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    /** Sable pris par seconde de marche sur le sable, des bottes au casque. */
    private static final float[] SAND_STEP = {3.0F, 1.8F, 0.8F, 0.4F};
    /** Neige reçue par seconde sous la neige qui tombe : le dessus d'abord. */
    private static final float[] SNOW_FALL = {0.6F, 1.0F, 2.4F, 3.0F};
    /** Neige tassée par seconde de marche dans la neige. */
    private static final float[] SNOW_STEP = {3.0F, 1.0F, 0.0F, 0.0F};
    /** Position de chaque joueur à la seconde d'avant (oubliée à la déconnexion). */
    private static final Map<UUID, Vec3> LAST_POSITION = new HashMap<>();

    private Patine() {
    }

    public static int of(ItemStack stack) {
        Integer value = stack.get(PatineModule.PATINE.get());
        return value == null ? 0 : value;
    }

    static void set(ItemStack stack, int value) {
        value = Math.max(0, Math.min(MAX, value));
        if (value == of(stack)) {
            return;
        }
        if (value == 0) {
            stack.remove(PatineModule.PATINE.get());
        } else {
            stack.set(PatineModule.PATINE.get(), value);
        }
    }

    public static Coat coat(ItemStack stack) {
        Coat coat = stack.get(PatineModule.COAT.get());
        return coat == null ? Coat.NONE : coat;
    }

    static void setCoat(ItemStack stack, Coat coat) {
        if (coat.equals(coat(stack))) {
            return;
        }
        if (coat.isEmpty()) {
            stack.remove(PatineModule.COAT.get());
        } else {
            stack.set(PatineModule.COAT.get(), coat);
        }
    }

    static void tick(Player player) {
        if (!FallowConfig.patineEnabled() || player.isCreative() || player.isSpectator()) {
            return;
        }
        RandomSource random = player.getRandom();
        Vec3 here = player.position();
        Vec3 before = LAST_POSITION.put(player.getUUID(), here);
        boolean moving = before != null && before.distanceToSqr(here) > 1.0 && before.distanceToSqr(here) < 2500.0;
        float rate = MAX / (60.0F * FallowConfig.patineMinutesToFull()) * activity(player, moving);
        Weather weather = Weather.at(player, moving);
        for (int i = 0; i < SLOTS.length; i++) {
            ItemStack stack = player.getItemBySlot(SLOTS[i]);
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) {
                continue;
            }
            boolean soaked = SLOTS[i] == EquipmentSlot.HEAD ? player.isUnderWater() : player.isInWater();
            if (soaked) {
                set(stack, of(stack) - 12);
            } else if (player.isInWaterOrRain()) {
                if (random.nextFloat() < 0.3F) {
                    set(stack, of(stack) - 1);
                }
            } else if (random.nextFloat() < rate * SLOT_WEIGHT[i]) {
                set(stack, of(stack) + 1);
            }
            setCoat(stack, weather.apply(coat(stack), i, soaked, random));
        }
    }

    /** Ce que le monde fait à l'armure pendant une seconde, au même endroit pour toutes les pièces. */
    private record Weather(boolean onSand, boolean onSnow, boolean powder, boolean snowing, boolean raining,
            boolean cold, boolean hot, boolean burning, float stride) {
        static Weather at(Player player, boolean moving) {
            Level level = player.level();
            BlockPos feet = player.blockPosition();
            BlockPos head = BlockPos.containing(player.getEyePosition());
            BlockState ground = player.getBlockStateOn();
            BlockState atFeet = level.getBlockState(feet);
            Biome biome = level.getBiome(feet).value();
            boolean open = level.isRaining() && level.canSeeSky(head);
            boolean snowing = open && biome.getPrecipitationAt(feet) == Biome.Precipitation.SNOW;
            boolean onSnow = atFeet.is(Blocks.SNOW) || ground.is(Blocks.SNOW) || ground.is(Blocks.SNOW_BLOCK)
                    || ground.is(Blocks.POWDER_SNOW);
            return new Weather(ground.is(BlockTags.SAND), onSnow, player.isInPowderSnow || player.wasInPowderSnow,
                    snowing, level.isRainingAt(head), biome.coldEnoughToSnow(feet), biome.getBaseTemperature() > 1.0F,
                    player.isOnFire() || player.isInLava(), moving ? (player.isSprinting() ? 1.5F : 1.0F) : 0.0F);
        }

        Coat apply(Coat coat, int slot, boolean soaked, RandomSource random) {
            float sand = coat.sand();
            float snow = coat.snow();
            float wet = coat.wet();
            if (soaked) {
                sand -= 20;
                snow -= 20;
                wet = MAX;
            } else {
                // Le sable : pris en marchant dessus, il retombe en marchant ailleurs ; la pluie le rince.
                if (onSand) {
                    sand += SAND_STEP[slot] * stride;
                } else {
                    sand -= raining ? 3.0F : 0.4F + 0.6F * stride;
                }
                // La neige : elle tombe sur le dessus, se tasse autour des bottes ; elle fond hors du froid.
                if (powder) {
                    snow += slot == 3 ? 3 : 6;
                } else if (snowing || onSnow) {
                    // Debout dans la neige, celle des bottes ne fond pas.
                    snow += (snowing ? SNOW_FALL[slot] : 0) + (onSnow ? SNOW_STEP[slot] * stride : 0);
                } else {
                    snow -= burning ? 30 : raining ? 3.0F : cold ? 0.3F : hot ? 4.0F : 2.0F;
                }
                // L'eau : la pluie mouille peu à peu, l'air sèche en moins d'une minute.
                if (raining) {
                    wet = Math.max(wet, 30) + 6;
                } else {
                    wet -= burning ? 30 : hot ? 4.0F : cold ? 1.0F : 2.0F;
                }
            }
            return new Coat(round(sand, random), round(snow, random), round(wet, random));
        }

        /** L'arrondi tiré au sort garde les petits pas : 0,4 point par seconde en fait 2 en 5 secondes. */
        private static int round(float value, RandomSource random) {
            int whole = (int) Math.floor(value);
            return whole + (random.nextFloat() < value - whole ? 1 : 0);
        }
    }

    /**
     * Combien le joueur se salit en ce moment : 0 immobile, 1 en marchant (ou porté par une monture qui avance),
     * davantage dans la boue ou au combat. Le serveur ne connaît pas la vitesse d'un joueur qui marche : on compare sa
     * position à celle de la seconde d'avant.
     */
    private static float activity(Player player, boolean moving) {
        boolean fighting = player.getLastHurtByMobTimestamp() > 0 && player.tickCount - player.getLastHurtByMobTimestamp() < 200
                || player.tickCount - player.getLastHurtMobTimestamp() < 200 && player.getLastHurtMob() != null;
        if (!moving && !fighting) {
            return 0.0F;
        }
        float activity = moving ? 1.0F : 0.0F;
        if (player.isSprinting()) {
            activity *= 1.5F;
        }
        BlockState ground = player.getBlockStateOn();
        if (ground.is(BlockTags.DIRT) || ground.is(BlockTags.SOUL_SPEED_BLOCKS)) {
            activity *= 1.6F;
        }
        if (fighting) {
            activity += 2.0F;
        }
        return activity;
    }

    static void forget(Player player) {
        LAST_POSITION.remove(player.getUUID());
    }

    /**
     * Les monstres qui naissent armés portent une armure déjà sale : ensablée au désert et dans les badlands,
     * enneigée là où il neige.
     */
    static void dirtyMob(Mob mob) {
        if (!FallowConfig.patineEnabled()) {
            return;
        }
        RandomSource random = mob.getRandom();
        BlockPos feet = mob.blockPosition();
        boolean sandy = mob.level().getBlockState(feet.below()).is(BlockTags.SAND);
        boolean snowy = mob.level().getBiome(feet).value().coldEnoughToSnow(feet);
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem && of(stack) == 0) {
                set(stack, 25 + random.nextInt(66));
                if (sandy || snowy) {
                    int amount = 30 + random.nextInt(60);
                    setCoat(stack, new Coat(sandy ? amount : 0, snowy ? amount : 0, 0));
                }
            }
        }
    }

    /** Le palier visible (0 propre à 3 crottée). */
    public static int level(ItemStack stack) {
        int value = of(stack);
        return value < 12 ? 0 : value < 35 ? 1 : value < 65 ? 2 : 3;
    }
}
