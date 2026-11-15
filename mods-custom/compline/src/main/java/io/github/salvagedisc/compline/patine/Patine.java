package io.github.salvagedisc.compline.patine;

import io.github.salvagedisc.compline.ComplineConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * La saleté des armures, côté serveur. Chaque seconde, chaque pièce portée a une chance de prendre un point (sur
 * {@link #MAX}) : plus en courant, sur la terre et la boue, au combat ; les bottes plus vite que le casque. Dans l'eau,
 * elle se lave en quelques secondes (le casque seulement tête sous l'eau) ; la pluie la rince lentement. Le tirage au
 * sort évite de garder un compteur par joueur, et les pièces se salissent chacune à son rythme.
 */
public final class Patine {
    public static final int MAX = 100;
    /** Les pièces qui voient le sol se salissent plus vite. */
    private static final float[] SLOT_WEIGHT = {1.5F, 1.2F, 1.0F, 0.7F};
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
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

    static void tick(Player player) {
        if (!ComplineConfig.patineEnabled() || player.isCreative() || player.isSpectator()) {
            return;
        }
        RandomSource random = player.getRandom();
        float rate = MAX / (60.0F * ComplineConfig.patineMinutesToFull()) * activity(player);
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
        }
    }

    /**
     * Combien le joueur se salit en ce moment : 0 immobile, 1 en marchant (ou porté par une monture qui avance),
     * davantage dans la boue ou au combat. Le serveur ne connaît pas la vitesse d'un joueur qui marche : on compare sa
     * position à celle de la seconde d'avant.
     */
    private static float activity(Player player) {
        Vec3 here = player.position();
        Vec3 before = LAST_POSITION.put(player.getUUID(), here);
        boolean moving = before != null && before.distanceToSqr(here) > 1.0 && before.distanceToSqr(here) < 2500.0;
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
        if (ground.is(BlockTags.DIRT) || ground.is(BlockTags.SAND) || ground.is(BlockTags.SOUL_SPEED_BLOCKS)) {
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

    /** Les monstres qui naissent armés portent une armure déjà sale. */
    static void dirtyMob(Mob mob) {
        if (!ComplineConfig.patineEnabled()) {
            return;
        }
        RandomSource random = mob.getRandom();
        for (EquipmentSlot slot : SLOTS) {
            ItemStack stack = mob.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem && of(stack) == 0) {
                set(stack, 25 + random.nextInt(66));
            }
        }
    }

    /** Le palier visible (0 propre à 3 crottée). */
    public static int level(ItemStack stack) {
        int value = of(stack);
        return value < 12 ? 0 : value < 35 ? 1 : value < 65 ? 2 : 3;
    }
}
