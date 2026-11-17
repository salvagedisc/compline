package io.github.salvagedisc.compline.ecran;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import io.github.salvagedisc.compline.Compline;
import io.github.salvagedisc.compline.ComplineConfig;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

/**
 * L'interface qui s'efface. Chaque tick, on regarde si le joueur a fait quelque chose qui demande l'interface ; après
 * {@code ecran.idleSeconds} sans rien de tel, les couches de {@code ecran.layers} passent en fondu à zéro, puis ne sont
 * plus dessinées du tout. Elles reviennent en un quart de seconde.
 *
 * <p>Pendant le fondu, chaque couche est dessinée dans une image à part, vidée en transparent, puis posée sur l'écran
 * avec l'opacité du moment. Baisser seulement la couleur des sprites ne suffit pas : les objets de la barre d'action
 * sont dessinés opaques et resteraient pleins au milieu d'une barre à moitié effacée. Hors fondu, la couche est
 * dessinée normalement (opacité 1) ou pas du tout (opacité 0) : rien ne coûte au repos.
 */
final class EcranClient {
    /** Fondu de sortie, puis de retour, en millisecondes. */
    private static final float FADE_OUT_MS = 900.0F;
    private static final float FADE_IN_MS = 220.0F;

    private static float alpha = 1.0F;
    private static long lastActive = Util.getMillis();
    private static long lastFrame = Util.getMillis();

    private static int lastSlot = -1;
    private static ItemStack lastHeld = ItemStack.EMPTY;
    private static ItemStack lastOffhand = ItemStack.EMPTY;
    private static float lastHealth = -1.0F;
    private static float lastAbsorption = -1.0F;
    private static int lastFood = -1;
    private static int lastArmor = -1;
    private static int lastThirst = EcranGauges.NONE;
    private static Entity lastVehicle;

    private static Set<ResourceLocation> faded = Set.of();
    private static String fadedSource;

    private static TextureTarget target;
    /** Une couche est en train d'être dessinée dans {@link #target}. */
    private static boolean drawing;

    private EcranClient() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> tick());
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Pre.class, event -> beginFrame());
        NeoForge.EVENT_BUS.addListener(RenderGuiEvent.Post.class, event -> finishLayer(event.getGuiGraphics()));
        // En dernier : une couche qu'un autre mod (ou le module xp) annule n'est pas touchée.
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiLayerEvent.Pre.class, EcranClient::onLayerPre);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, RenderGuiLayerEvent.Post.class, EcranClient::onLayerPost);
    }

    /** Ce que le joueur a fait depuis le tick précédent, et ce qui doit garder l'interface affichée. */
    private static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || !ComplineConfig.ecranEnabled()) {
            lastActive = Util.getMillis();
            lastSlot = -1;
            return;
        }
        Options options = minecraft.options;
        boolean active = minecraft.screen != null
                || options.keyAttack.isDown() || options.keyUse.isDown() || options.keyPickItem.isDown()
                || options.keyDrop.isDown() || options.keySwapOffhand.isDown()
                || player.hurtTime > 0
                || player.isUnderWater() || player.getAirSupply() < player.getMaxAirSupply()
                || player.getHealth() <= player.getMaxHealth() * 0.3F
                || (!player.getAbilities().invulnerable && player.getFoodData().getFoodLevel() <= 6)
                || (ComplineConfig.ecranShowWhileMoving() && (player.input.forwardImpulse != 0.0F
                        || player.input.leftImpulse != 0.0F || player.input.jumping));

        boolean survival = !player.getAbilities().invulnerable;
        int thirst = EcranGauges.thirst(player);
        int temperature = EcranGauges.bodyTemperature();
        // Soif basse, corps qui approche du seuil des dégâts de Cold Sweat (±100) : l'interface reste.
        active |= survival && ((thirst != EcranGauges.NONE && thirst <= 6)
                || (temperature != EcranGauges.NONE && Math.abs(temperature) >= 50));

        int slot = player.getInventory().selected;
        ItemStack held = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (slot != lastSlot || changed(held, lastHeld) || changed(offhand, lastOffhand)
                || player.getHealth() != lastHealth || player.getAbsorptionAmount() != lastAbsorption
                || player.getFoodData().getFoodLevel() != lastFood || player.getArmorValue() != lastArmor
                || thirst != lastThirst || player.getVehicle() != lastVehicle) {
            active |= lastSlot != -1;
            lastSlot = slot;
            lastHeld = held.copy();
            lastOffhand = offhand.copy();
            lastHealth = player.getHealth();
            lastAbsorption = player.getAbsorptionAmount();
            lastFood = player.getFoodData().getFoodLevel();
            lastArmor = player.getArmorValue();
            lastThirst = thirst;
            lastVehicle = player.getVehicle();
        }
        if (active) {
            lastActive = Util.getMillis();
        }
    }

    /** Un autre objet, ou le même en un autre nombre ; l'usure d'un outil qui travaille ne compte pas. */
    private static boolean changed(ItemStack now, ItemStack before) {
        return !ItemStack.isSameItem(now, before) || now.getCount() != before.getCount();
    }

    /** Une fois par image, avant les couches : l'opacité suit le temps écoulé depuis le dernier geste. */
    private static void beginFrame() {
        long now = Util.getMillis();
        float dt = Math.min(now - lastFrame, 1000L);
        lastFrame = now;
        if (!ComplineConfig.ecranEnabled()) {
            alpha = 1.0F;
            return;
        }
        boolean shown = now - lastActive < ComplineConfig.ecranIdleSeconds() * 1000L;
        alpha = shown ? Math.min(1.0F, alpha + dt / FADE_IN_MS) : Math.max(0.0F, alpha - dt / FADE_OUT_MS);
        refreshLayers();
    }

    private static void refreshLayers() {
        String source = String.join(",", ComplineConfig.ecranLayers());
        if (source.equals(fadedSource)) {
            return;
        }
        Set<ResourceLocation> set = new HashSet<>();
        for (String id : ComplineConfig.ecranLayers()) {
            ResourceLocation name = ResourceLocation.tryParse(id);
            if (name != null) {
                set.add(name);
            } else {
                Compline.LOGGER.warn("ecran : couche d'interface mal écrite, ignorée : {}", id);
            }
        }
        faded = Set.copyOf(set);
        fadedSource = source;
    }

    private static void onLayerPre(RenderGuiLayerEvent.Pre event) {
        GuiGraphics graphics = event.getGuiGraphics();
        // Une couche précédente n'a pas reçu son Post (annulée après nous) : on la pose maintenant.
        finishLayer(graphics);
        if (event.isCanceled() || alpha >= 1.0F || !faded.contains(event.getName())) {
            return;
        }
        if (alpha <= 0.0F) {
            event.setCanceled(true);
            return;
        }
        graphics.flush();
        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (target == null) {
            target = new TextureTarget(main.width, main.height, true, Minecraft.ON_OSX);
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        } else if (target.width != main.width || target.height != main.height) {
            target.resize(main.width, main.height, Minecraft.ON_OSX);
        }
        target.clear(Minecraft.ON_OSX);
        target.bindWrite(true);
        drawing = true;
    }

    private static void onLayerPost(RenderGuiLayerEvent.Post event) {
        finishLayer(event.getGuiGraphics());
    }

    /** Pose sur l'écran la couche dessinée à part, avec l'opacité du moment. */
    private static void finishLayer(GuiGraphics graphics) {
        if (!drawing) {
            return;
        }
        drawing = false;
        graphics.flush();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        // L'image à part est vidée en transparent : ses couleurs y sont déjà multipliées par leur opacité.
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, target.getColorTextureId());
        RenderSystem.setShaderColor(alpha, alpha, alpha, alpha);
        Matrix4f pose = graphics.pose().last().pose();
        float w = graphics.guiWidth();
        float h = graphics.guiHeight();
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(pose, 0.0F, h, 0.0F).setUv(0.0F, 0.0F);
        buffer.addVertex(pose, w, h, 0.0F).setUv(1.0F, 0.0F);
        buffer.addVertex(pose, w, 0.0F, 0.0F).setUv(1.0F, 1.0F);
        buffer.addVertex(pose, 0.0F, 0.0F, 0.0F).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
}
