package io.github.salvagedisc.fallow.etabli.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.etabli.EtabliPage;
import io.github.salvagedisc.fallow.etabli.Grille;
import io.github.salvagedisc.fallow.etabli.Pages;
import io.github.salvagedisc.fallow.etabli.Patron;
import io.github.salvagedisc.fallow.etabli.Tables;
import java.lang.reflect.Field;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Les fantômes du carnet : les ingrédients de la page ouverte, à moitié transparents, dans les cases encore vides de la
 * table, là et comme Visual Workbench dessine les vrais (même hauteur, même balancement, même quart de tour).
 */
public final class EtabliClient {
    private static final float ALPHA = 0.6F;
    private static final double FAR = 12.0;
    private static Field currentAngle;
    private static Field nextAngle;
    private static Field ticks;

    private EtabliClient() {
    }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, EtabliClient::render);
        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> Pages.setClient(null));
    }

    private static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || !FallowConfig.etabliEnabled()) {
            return;
        }
        EtabliPage page = Pages.client();
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (page == null || page.recipe().isEmpty() || level == null || minecraft.player == null) {
            return;
        }
        BlockPos pos = page.table();
        if (minecraft.player.position().distanceTo(pos.getCenter()) > FAR) {
            Pages.setClient(null);
            return;
        }
        Container table = Tables.at(level, pos);
        Optional<CraftingRecipe> recipe = Pages.recipe(level, page.recipe().get());
        if (table == null || recipe.isEmpty()) {
            return;
        }
        Ingredient[] ghosts = Patron.of(recipe.get());
        float partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        Object animation = Tables.animation(table);
        float angle = Mth.lerp(partial, read(animation, "currentAngle"), read(animation, "nextAngle"));
        float tick = read(animation, "ticks") + partial;

        PoseStack pose = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        MultiBufferSource faded = type -> new Fondu(buffers.getBuffer(Sheets.translucentItemSheet()), ALPHA);
        ItemRenderer items = minecraft.getItemRenderer();
        int light = LevelRenderer.getLightColor(level, pos.above());
        long now = Util.getMillis();
        boolean flat = Tables.flat();
        for (int i = 0; i < 9; i++) {
            if (ghosts[i].isEmpty() || !table.getItem(i).isEmpty()) {
                continue;
            }
            ItemStack[] options = ghosts[i].getItems();
            if (options.length == 0) {
                continue;
            }
            ItemStack stack = options[(int) ((now / 1000L) % options.length)];
            double[] centre = Grille.centre(i);
            BakedModel model = items.getModel(stack, level, null, 0);
            boolean gui3d = model.isGui3d();
            pose.pushPose();
            pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);
            if (flat) {
                // Comme Visual Workbench à plat : couché sur le plateau, un peu plus haut pour un bloc.
                pose.translate(0.5, 0.0, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(angle));
                pose.translate(centre[0], gui3d ? 1.0625 : 1.005, centre[1]);
                pose.mulPose(Axis.XP.rotationDegrees(90.0F));
                pose.mulPose(Axis.ZP.rotationDegrees(180.0F));
                float scale = gui3d ? 0.25F : 0.175F;
                pose.scale(scale, scale, scale);
            } else {
                float bob = (float) (Math.abs((tick * 50.0 + i * 1000L) % 5000.0 - 2500.0) / 200000.0);
                pose.translate(0.5, bob, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(angle));
                pose.translate(centre[0], 1.09375, centre[1]);
                float scale = gui3d ? 0.24F : 0.18F;
                pose.scale(scale, scale, scale);
            }
            items.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, pose, faded, level, 0);
            pose.popPose();
        }
        buffers.endBatch(Sheets.translucentItemSheet());
    }

    private static float read(Object animation, String name) {
        if (animation == null) {
            return 0.0F;
        }
        try {
            Field field = switch (name) {
                case "currentAngle" -> currentAngle != null ? currentAngle : (currentAngle = animation.getClass().getField(name));
                case "nextAngle" -> nextAngle != null ? nextAngle : (nextAngle = animation.getClass().getField(name));
                default -> ticks != null ? ticks : (ticks = animation.getClass().getField(name));
            };
            return ((Number) field.get(animation)).floatValue();
        } catch (ReflectiveOperationException exception) {
            return 0.0F;
        }
    }
}
