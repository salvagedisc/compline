package io.github.salvagedisc.fallow.ciel;

import com.farcr.nomansland.client.renderer.FriendMoonRenderer;
import com.farcr.nomansland.client.renderer.dreams.ClientDreamRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import io.github.salvagedisc.fallow.Fallow;
import io.github.salvagedisc.fallow.FallowConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.joml.Matrix4f;

/**
 * Rend la Lune Amie de No Man's Land visible sous ATMOSPHERICS.
 *
 * <p>Le cercle vicieux : NML dessine la Lune Amie et son ombre dans le corps vanilla de
 * {@code renderSky}, et c'est là seulement que l'opacité de la Lune Amie monte. ATMOSPHERICS annule ce
 * corps, sauf quand la Lune Amie est déjà visible (opacité &gt; 0). Elle ne pouvait donc jamais
 * apparaître au Point de rencontre, et l'ombre qui montre le chemin ne se dessinait jamais.
 *
 * <p>La réponse : quand ATMOSPHERICS a dessiné le ciel à sa façon, on fait nous-mêmes, juste après,
 * l'appel que NML fait dans le ciel vanilla. Dès que la Lune Amie se lève, ATMOSPHERICS rend la main
 * au ciel vanilla et NML continue seul. L'ombre, visible loin du Point, reste dessinée ici, sur le
 * ciel d'ATMOSPHERICS. Pendant un rêve, ATMOSPHERICS cède le ciel au rêve ({@link #dreamRenders()}).
 *
 * <p>Repérer le ciel d'ATMOSPHERICS : il lance la mise en place de la brume du ciel ({@code skyFogSetup})
 * après avoir annulé le corps vanilla. Si ce {@code Runnable} tourne sans que le corps vanilla ait
 * démarré, c'est ATMOSPHERICS qui a dessiné. Ne charger cette classe que si les deux mods sont présents.
 */
public final class FriendMoonRelay {
    private static boolean fogSetupRan;
    private static boolean vanillaSky;
    private static boolean errorLogged;

    private FriendMoonRelay() {
    }

    /** Juste avant {@code renderSky} : rend la mise en place de la brume, entourée pour savoir si elle tourne. */
    public static Runnable beginSky(Runnable skyFogSetup) {
        fogSetupRan = false;
        vanillaSky = false;
        if (!FallowConfig.cielFriendMoon()) {
            return skyFogSetup;
        }
        return () -> {
            fogSetupRan = true;
            skyFogSetup.run();
        };
    }

    public static void markVanillaSky() {
        vanillaSky = true;
    }

    /** Juste après {@code renderSky} : si ATMOSPHERICS a dessiné le ciel, la Lune Amie et son ombre passent par ici. */
    public static void endSky(Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTick) {
        boolean draw = fogSetupRan && !vanillaSky;
        fogSetupRan = false;
        vanillaSky = false;
        if (!draw) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.player == null || level.effects().skyType() != DimensionSpecialEffects.SkyType.NORMAL) {
            return;
        }
        Tesselator tesselator = Tesselator.getInstance();
        try {
            // État du ciel vanilla au moment où NML dessine (après la lune) : pas d'écriture de profondeur, couleur blanche.
            RenderSystem.depthMask(false);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            FriendMoonRenderer renderer = FriendMoonRenderer.getInstance();
            renderer.renderFriendMoon(frustumMatrix, projectionMatrix, tesselator, new PoseStack(), partialTick);
            renderer.renderFriendShadow(frustumMatrix, projectionMatrix, tesselator, new PoseStack(), partialTick);
        } catch (RuntimeException e) {
            // Une erreur ici ne doit pas faire planter le rendu à chaque image : on la note une fois.
            if (!errorLogged) {
                errorLogged = true;
                Fallow.LOGGER.error("ciel : impossible de dessiner la Lune Amie sous ATMOSPHERICS", e);
            }
        } finally {
            // Ce que NML fait à la fin du ciel vanilla : le masque (stencil) qui cache les étoiles derrière la lune est levé.
            FriendMoonRenderer.renderFinalize(frustumMatrix, projectionMatrix, tesselator, partialTick);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
        }
    }

    /** Vrai pendant le rêve de la Lune Amie : NML dessine alors son propre ciel. */
    public static boolean dreamRenders() {
        return FallowConfig.cielFriendMoon() && ClientDreamRenderer.getInstance().dreamShouldRender();
    }
}
