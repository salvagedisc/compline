package io.github.salvagedisc.fallow.mixin.horizon;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.seibel.distanthorizons.common.render.openGl.glObject.GLState;
import com.seibel.distanthorizons.lwjgl.ILWJGLService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Distant Horizons 3.3.3 lit {@code GL_POLYGON_MODE} avec {@code glGetInteger}, qui réserve un seul entier.
 * Le pilote OpenGL des Mac Apple Silicon en écrit deux (face avant, face arrière) : le second déborde sur la
 * mémoire native et fait planter la JVM au hasard (ticket Distant Horizons #1346, corrigé après la 3.3.3).
 * On lit cette valeur dans un tableau de deux entiers et on garde le premier. Les autres lectures sont
 * inchangées. {@code require = 0} : si une version future de DH ne fait plus cet appel, le mixin ne fait rien.
 */
@Mixin(value = GLState.class, remap = false)
abstract class GLStateMixin {
    private static final int GL_POLYGON_MODE = 0x0B40;

    @WrapOperation(method = "saveState", require = 0,
            at = @At(value = "INVOKE", target = "Lcom/seibel/distanthorizons/lwjgl/ILWJGLService;glGetInteger(I)I"))
    private int fallow$safePolygonMode(ILWJGLService gl, int pname, Operation<Integer> original) {
        if (pname != GL_POLYGON_MODE) {
            return original.call(gl, pname);
        }
        int[] values = new int[2];
        gl.glGetIntegerv(pname, values);
        return values[0];
    }
}
