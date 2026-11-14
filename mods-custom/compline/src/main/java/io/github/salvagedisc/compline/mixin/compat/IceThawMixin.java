package io.github.salvagedisc.compline.mixin.compat;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.salvagedisc.compline.Compline;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Streams Reflowing fait fondre la glace d'un ruisseau en eau de ruisseau (son {@code IceMeltMixin}
 * remplace l'eau que {@code melt} pose). Spelunkery réécrit {@code melt} en entier, à la même priorité :
 * Mixin refuse alors l'injection de Streams Reflowing, et un ruisseau gelé par l'hiver dégèle en eau
 * immobile. Ce mixin, de priorité plus haute, refait le remplacement dans la méthode de Spelunkery
 * avec la règle de Streams Reflowing elle-même ({@code StreamIceMelt.meltsInto}).
 * Appliqué seulement avec Streams Reflowing et Spelunkery.
 */
@Mixin(value = IceBlock.class, priority = 1100)
abstract class IceThawMixin {
    @Unique
    private static MethodHandle compline$meltsInto;
    @Unique
    private static boolean compline$looked;

    @WrapOperation(method = "melt", require = 0, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean compline$thawIntoStream(Level level, BlockPos pos, BlockState state, Operation<Boolean> original) {
        BlockState stream = compline$streamAt(level, pos);
        return original.call(level, pos, stream != null ? stream : state);
    }

    @Unique
    private static BlockState compline$streamAt(Level level, BlockPos pos) {
        if (!compline$looked) {
            compline$looked = true;
            try {
                Class<?> rule = Class.forName("dev.streamsreflowing.flow.StreamIceMelt");
                compline$meltsInto = MethodHandles.publicLookup().findStatic(rule, "meltsInto",
                        MethodType.methodType(BlockState.class, Level.class, BlockPos.class));
            } catch (ReflectiveOperationException e) {
                Compline.LOGGER.warn("Dégel des ruisseaux : règle de Streams Reflowing introuvable, la glace fond en eau", e);
            }
        }
        if (compline$meltsInto == null) {
            return null;
        }
        try {
            return (BlockState) compline$meltsInto.invokeExact(level, pos);
        } catch (Throwable e) {
            return null;
        }
    }
}
