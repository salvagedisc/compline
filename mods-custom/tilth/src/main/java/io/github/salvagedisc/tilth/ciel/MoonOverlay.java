package io.github.salvagedisc.tilth.ciel;

import com.beash.atmospherics.Atmospherics;
import com.beash.atmospherics.config.FogConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import dev.corgitaco.enhancedcelestials2core.EnhancedCelestials;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.LunarEvent;
import dev.corgitaco.enhancedcelestials2core.api.lunarevent.LunarForecast;
import dev.corgitaco.enhancedcelestials2core.client.ECWorldRenderer;
import io.github.salvagedisc.tilth.Tilth;
import io.github.salvagedisc.tilth.TilthConfig;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Redessine la lune d'Enhanced Celestials sous ATMOSPHERICS.
 *
 * <p>Déroulé d'une image :
 * <ol>
 *   <li>{@link #beginSky()} juste avant l'appel à {@code renderSky} : on s'arme si le module est
 *       actif et si la dimension a des données lunaires Enhanced Celestials ;</li>
 *   <li>si le corps vanilla de {@code renderSky} s'exécute (ATMOSPHERICS n'a pas pris la main :
 *       sous l'eau, dans une autre dimension…), {@link #markVanillaSky()} désarme tout : Enhanced Celestials
 *       dessine alors lui-même sa lune ;</li>
 *   <li>sinon ATMOSPHERICS lie la texture vanilla {@code moon_phases.png} pour sa lune :
 *       {@link #swapMoonTexture} la remplace par une texture transparente (son quad ne dessine rien) ;</li>
 *   <li>{@link #endSky} après {@code renderSky} : si la lune d'ATMOSPHERICS a bien été cachée, on dessine
 *       celle d'Enhanced Celestials au même endroit, avec les mêmes matrices.</li>
 * </ol>
 * Si une étape manque (ATMOSPHERICS ne dessine pas de lune, ciel caché dans le vide…), rien n'est caché
 * et rien n'est dessiné.
 *
 * <p>Ce qu'on reprend d'Enhanced Celestials, par son API : la texture de l'évènement en cours, la taille
 * fondue entre l'évènement précédent et l'actuel, et la couleur par {@link ECWorldRenderer#changeMoonColor}
 * (la même fonction qu'il appelle dans le ciel vanilla). D'ATMOSPHERICS on garde l'échelle et la rotation
 * de lune réglées dans sa config. Ne charger cette classe que si les deux mods sont présents.
 */
public final class MoonOverlay {
    /** Texture de lune vanilla, que lie ATMOSPHERICS. */
    private static final ResourceLocation VANILLA_MOON = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    /** Texture entièrement transparente, mise à la place de la précédente pendant le ciel d'ATMOSPHERICS. */
    private static final ResourceLocation HIDDEN_MOON = ResourceLocation.fromNamespaceAndPath(Tilth.MOD_ID, "textures/environment/hidden_moon.png");

    private static boolean armed;
    private static boolean vanillaSky;
    private static boolean hidden;
    private static boolean errorLogged;

    private MoonOverlay() {
    }

    public static void beginSky() {
        armed = false;
        vanillaSky = false;
        hidden = false;
        if (!TilthConfig.cielRedrawMoon()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        armed = level != null && EnhancedCelestials.lunarForecastWorldData(level).isPresent();
    }

    public static void markVanillaSky() {
        vanillaSky = true;
    }

    public static ResourceLocation swapMoonTexture(ResourceLocation texture) {
        if (armed && !vanillaSky && VANILLA_MOON.equals(texture)) {
            hidden = true;
            return HIDDEN_MOON;
        }
        return texture;
    }

    public static void endSky(Matrix4f frustumMatrix, float partialTick) {
        boolean draw = hidden;
        armed = false;
        vanillaSky = false;
        hidden = false;
        if (!draw) {
            return;
        }
        try {
            drawMoon(frustumMatrix, partialTick);
        } catch (RuntimeException e) {
            // Une erreur ici ne doit pas faire planter le rendu à chaque image : on la note une fois.
            if (!errorLogged) {
                errorLogged = true;
                Tilth.LOGGER.error("ciel : impossible de dessiner la lune d'Enhanced Celestials", e);
            }
        } finally {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }
    }

    private static void drawMoon(Matrix4f frustumMatrix, float partialTick) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Optional<LunarForecast> data = EnhancedCelestials.lunarForecastWorldData(level);
        if (data.isEmpty()) {
            return;
        }
        float timeOfDay = level.getTimeOfDay(partialTick);
        // La lune est au zénith à 0,5 et sous l'horizon quand le cosinus est positif ; marge pour une grosse lune.
        if (Mth.cos(timeOfDay * Mth.TWO_PI) > 0.45F) {
            return;
        }

        LunarForecast forecast = data.get();
        LunarEvent current = forecast.currentLunarEvent();
        LunarEvent previous = forecast.lastLunarEvent();
        ResourceLocation texture = current.getMoonTextureLocation() != null ? current.getMoonTextureLocation() : VANILLA_MOON;
        // La taille est le demi-côté du quad à 100 blocs (20 pour la lune vanilla), fondue entre deux évènements.
        float size = Mth.clampedLerp(previous.getMoonSize(), current.getMoonSize(), forecast.getBlend()) * atmosphericsMoonScale();
        if (size <= 0.0F) {
            return;
        }

        PoseStack poseStack = new PoseStack();
        poseStack.mulPose(frustumMatrix);
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(timeOfDay * 360.0F));
        float rotation = atmosphericsMoonRotation();
        if (rotation != 0.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        }
        Matrix4f pose = poseStack.last().pose();

        int phase = level.getMoonPhase();
        int column = phase % 4;
        int row = phase / 4 % 2;
        float u0 = column / 4.0F;
        float v0 = row / 2.0F;
        float u1 = (column + 1) / 4.0F;
        float v1 = (row + 1) / 2.0F;

        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        ECWorldRenderer.changeMoonColor(partialTick);

        BufferBuilder builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        builder.addVertex(pose, -size, -100.0F, size).setUv(u1, v1);
        builder.addVertex(pose, size, -100.0F, size).setUv(u0, v1);
        builder.addVertex(pose, size, -100.0F, -size).setUv(u0, v0);
        builder.addVertex(pose, -size, -100.0F, -size).setUv(u1, v0);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    /** Échelle de lune d'ATMOSPHERICS (même lecture que lui : 1 si absente ou nulle, bornée à [0,25 ; 3]). */
    private static float atmosphericsMoonScale() {
        FogConfig config = Atmospherics.getConfig();
        if (config == null || !(config.moonTextureScale > 0.0F)) {
            return 1.0F;
        }
        return Mth.clamp(config.moonTextureScale, 0.25F, 3.0F);
    }

    /** Rotation de la texture de lune réglée dans ATMOSPHERICS, en degrés. */
    private static float atmosphericsMoonRotation() {
        FogConfig config = Atmospherics.getConfig();
        if (config == null || !Float.isFinite(config.moonTextureRotation)) {
            return 0.0F;
        }
        return config.moonTextureRotation % 360.0F;
    }
}
