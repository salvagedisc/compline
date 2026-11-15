package io.github.salvagedisc.compline.faune;

import software.bernie.geckolib.animation.AnimationProcessor;
import software.bernie.geckolib.cache.object.GeoBone;

/**
 * Petites retouches d'os après les images clés. Les os sont partagés par toutes les créatures d'un même modèle, et
 * GeckoLib ne réécrit à chaque image que ce que les animations touchent : un ajout sur un os qu'aucune animation
 * n'a réécrit s'ajouterait à celui de l'image précédente, ou d'une autre créature. D'où deux règles :
 * {@link #rotate} seulement sur un os que toutes les animations en boucle font tourner (il est réécrit à chaque
 * image), et {@link #setScale} en valeur absolue.
 */
public final class Bones {
    private Bones() {
    }

    /** Ajoute une rotation, en radians, à l'os s'il existe. L'os doit être tourné par toutes les animations en boucle. */
    public static void rotate(AnimationProcessor<?> processor, String name, float x, float y, float z) {
        GeoBone bone = processor.getBone(name);
        if (bone != null) {
            bone.updateRotation(bone.getRotX() + x, bone.getRotY() + y, bone.getRotZ() + z);
        }
    }

    /** Fixe l'échelle de l'os s'il existe, en valeur absolue (pour un os dont aucune animation ne touche l'échelle). */
    public static void setScale(AnimationProcessor<?> processor, String name, float scale) {
        GeoBone bone = processor.getBone(name);
        if (bone != null) {
            bone.updateScale(scale, scale, scale);
        }
    }

    /** Cache ou montre l'os et tout ce qui en dépend. */
    public static void hide(AnimationProcessor<?> processor, String name, boolean hidden) {
        GeoBone bone = processor.getBone(name);
        if (bone != null) {
            bone.setHidden(hidden);
        }
    }
}
