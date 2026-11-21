package io.github.salvagedisc.fallow.postes.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.client.DiegesisParticles;
import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;

import com.momosoftworks.coldsweat.common.block.HearthBottomBlock;
import com.momosoftworks.coldsweat.common.blockentity.HearthBlockEntity;

/**
 * Le poste du foyer de Cold Sweat : la case du combustible devant la grille, et sur la façade, au-dessus, les deux jauges de la
 * fenêtre de Cold Sweat, le chaud à gauche, le froid à droite, avec leur part en infobulle ; elles montent et descendent
 * doucement. Le feu vit derrière la grille : des flammes et des crépitements à la mesure du combustible chaud, un peu de fumée,
 * et un souffle de givre qui tombe de la grille quand le foyer refroidit. Un combustible jeté au feu fait jaillir des
 * étincelles, un froid une poudre de neige. L'inventaire grise ce qui ne brûle ni ne refroidit ici (les potions passent : le
 * foyer en répand l'effet).
 */
final class FoyerBehavior implements StationBehavior {
    private static final ResourceLocation CHAUD = cs("textures/gui/sprites/hearth/fuel_gauge_hot.png");
    private static final ResourceLocation CHAUD_VIDE = cs("textures/gui/sprites/hearth/fuel_gauge_hot_empty.png");
    private static final ResourceLocation FROID = cs("textures/gui/sprites/hearth/fuel_gauge_cold.png");
    private static final ResourceLocation FROID_VIDE = cs("textures/gui/sprites/hearth/fuel_gauge_cold_empty.png");
    /** Le côté d'une jauge sur la façade, en pixels du bloc. */
    private static final float COTE = 3.5F;

    /** La part de chaque combustible, de 0 à 1, lue à chaque tick ; les jauges et leurs infobulles la montrent. */
    private float chaud;
    private float froid;
    private int chaudLu = -1;
    private int froidLu = -1;
    /** Le combustible chaud et froid du foyer au tick d'avant. */
    private int chaudBrut = -1;
    private int froidBrut = -1;
    /** Les parts que montrent les jauges, qui rejoignent doucement les vraies. */
    private float montreChaud;
    private float montreFroid;
    private float flammes;
    private float fumee;
    private float givre;
    private final RandomSource random = RandomSource.create();

    private static ResourceLocation cs(String path) {
        return ResourceLocation.fromNamespaceAndPath("cold_sweat", path);
    }

    /** Vrai si cet objet, au foyer de {@code pos}, est un combustible (chaud ou froid) : un clic le jette au foyer. */
    static boolean combustible(Level level, BlockPos pos, ItemStack stack) {
        return !stack.isEmpty() && level.getBlockEntity(pos) instanceof HearthBlockEntity hearth && hearth.getItemFuel(stack) != 0;
    }

    /** Le blanc du jeu, teint par sommet : la lueur des fentes. */
    private static final ResourceLocation BLANC = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    /** Les fentes de la grille, en x (de gauche à droite vu de derrière), entre y 4 et 7.8. */
    private static final float[] FENTES = {4.0F, 6.0F, 8.0F, 10.0F};

    @Override
    public void onSceneReady(StationView view) {
        view.node("braises").ifPresent(node -> node.setPainter((pose, buffers, light) -> braises(pose, buffers)));
        view.node("jauge_chaud").ifPresent(node -> node.setPainter((pose, buffers, light) -> jauge(pose, buffers, CHAUD_VIDE, CHAUD, montreChaud)));
        view.node("jauge_froid").ifPresent(node -> node.setPainter((pose, buffers, light) -> jauge(pose, buffers, FROID_VIDE, FROID, montreFroid)));
    }

    @Override
    public void onOpen(StationView view) {
        view.setInventoryFilter(stack -> combustible(view.level(), view.pos(), stack) || stack.is(Items.MILK_BUCKET)
                || stack.has(DataComponents.POTION_CONTENTS));
        chaudLu = -1;
        froidLu = -1;
        chaudBrut = -1;
        froidBrut = -1;
        posee = -1;
        lire(view);
        montreChaud = chaud;
        montreFroid = froid;
    }

    @Override
    public void tick(StationView view) {
        lire(view);
        // Le combustible posé par Maj + clic vole de la barre à sa place devant la grille ; lâché du curseur, il vient de la main.
        net.minecraft.client.player.LocalPlayer joueur = net.minecraft.client.Minecraft.getInstance().player;
        int tenu = joueur == null ? 0 : joueur.containerMenu.getCarried().getCount();
        ItemStack pose = view.menu() == null ? ItemStack.EMPTY : view.state().getItem(com.pockyl.diegesis.api.state.SlotRef.of("combustible", 0));
        if (posee >= 0 && pose.getCount() > posee && tenu >= porte && view.level().getGameTime() - jeteA >= 10L) {
            Arrivee.caseDepuisLaBarre(view, "combustible", pose.getItem(), 0);
        }
        posee = view.menu() == null ? -1 : pose.getCount();
        porte = tenu;
        BlockState state = view.level().getBlockState(view.pos());
        boolean lit = state.hasProperty(HearthBottomBlock.LIT) && state.getValue(HearthBottomBlock.LIT);
        if (view.isFocused() && chaud > 0.0F && random.nextFloat() < (lit ? 0.03F + 0.1F * chaud : 0.01F + 0.02F * chaud)) {
            Son.joue(view, SoundEvents.CAMPFIRE_CRACKLE, "combustible", 0.25F + 0.3F * chaud, 0.9F + random.nextFloat() * 0.2F);
        }
    }

    /** Ce que tenaient la case du combustible et le curseur au tick d'avant. */
    private int posee = -1;
    private int porte;

    @Override
    public boolean onInventoryShiftClick(StationView view, int slot, ItemStack stack) {
        Arrivee.depuis(view, slot);
        // Le foyer brûle aussitôt ce qu'il reçoit : la case ne le montre jamais. Le combustible vole donc de la barre jusqu'à
        // la grille, et le feu ne répond qu'à son arrivée.
        if (combustible(view.level(), view.pos(), stack)) {
            volee.arrive(view, stack.getItem(), GRILLE);
            jeteA = view.level().getGameTime();
        }
        return false;
    }

    /** Le point de la grille où arrive ce qu'on jette au foyer. */
    private static final Vector3f GRILLE = new Vector3f(8.0F, 4.0F, -2.0F);
    private final Arrivee.Volee volee = new Arrivee.Volee();
    /** Le temps du jeu au dernier combustible envoyé par Maj + clic. */
    private long jeteA = -1000L;
    /** Ce que le feu doit encore montrer quand le combustible en vol arrive. */
    private boolean chaudEnAttente;
    private boolean froidEnAttente;

    /** La lueur des fentes : orange du feu (vive s'il brûle, sourde s'il couve), bleue du froid quand il domine. */
    private float lueur;
    private int teinte = 0xFF8A30;
    private float vacille;

    private void braises(PoseStack pose, MultiBufferSource buffers) {
        if (lueur <= 0.01F) {
            return;
        }
        // Opaque, comme les jauges : la lueur est une couleur plus ou moins vive, qui part du noir des fentes.
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(BLANC));
        Matrix4f m = pose.last().pose();
        int light = LightTexture.FULL_BRIGHT;
        int r = teinte >> 16 & 0xFF;
        int g = teinte >> 8 & 0xFF;
        int b = teinte & 0xFF;
        for (int i = 0; i < FENTES.length; i++) {
            // Chaque fente vacille un peu à sa façon ; le bas est plus vif que le haut.
            float a = Math.min(1.0F, lueur * (0.85F + 0.15F * net.minecraft.util.Mth.sin(vacille * 7.0F + i * 1.7F)));
            float bas = a;
            float haut = a * 0.25F;
            float x0 = FENTES[i] - 8.0F;
            float x1 = x0 + 1.0F;
            lueurSommet(pose, out, m, x1, 4.0F, r, g, b, bas, light);
            lueurSommet(pose, out, m, x0, 4.0F, r, g, b, bas, light);
            lueurSommet(pose, out, m, x0, 7.8F, r, g, b, haut, light);
            lueurSommet(pose, out, m, x1, 7.8F, r, g, b, haut, light);
        }
    }

    /** Un sommet de la lueur : la teinte assombrie vers le noir des fentes selon {@code force}. */
    private static void lueurSommet(PoseStack pose, VertexConsumer out, Matrix4f m, float x, float y, int r, int g, int b, float force, int light) {
        int noir = 29;
        out.addVertex(m, x, y, 0.0F)
                .setColor(noir + (int) ((r - noir) * force), noir + (int) ((g - noir) * force), noir + (int) ((b - noir) * force), 255)
                .setUv(0.5F, 0.5F).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }

    @Override
    public void frame(StationView view, float deltaSeconds) {
        vacille += deltaSeconds;
        volee.frame(view, deltaSeconds);
        if (!volee.enVol()) {
            if (chaudEnAttente) {
                jaillit(view);
            }
            if (froidEnAttente) {
                poudre(view);
            }
        }
        float pas = Math.min(1.0F, deltaSeconds * 2.5F);
        montreChaud += (chaud - montreChaud) * pas;
        montreFroid += (froid - montreFroid) * pas;
        if (!view.isFocused()) {
            return;
        }
        BlockState state = view.level().getBlockState(view.pos());
        if (!state.hasProperty(HearthBottomBlock.LIT)) {
            return;
        }
        // Les flammes montent dans les fentes de la grille (x 4 à 12), plus hautes et plus nombreuses avec le combustible chaud ;
        // foyer éteint, le combustible chaud couve encore : quelques petites flammes basses.
        boolean lit = state.getValue(HearthBottomBlock.LIT);
        boolean froidDomine = montreFroid > montreChaud;
        float vise = froidDomine ? 0.25F + 0.45F * montreFroid : montreChaud <= 0.0F ? 0.0F : lit ? 0.6F + 0.4F * montreChaud : 0.4F + 0.3F * montreChaud;
        lueur += (vise - lueur) * pas;
        teinte = froidDomine ? 0x8FD0FF : lit ? 0xFF9A30 : 0xD0501C;
        if (chaud > 0.0F) {
            flammes += deltaSeconds * (lit ? 2.0F + 7.0F * chaud : 0.4F + 1.2F * chaud);
            while (flammes >= 1.0F) {
                flammes -= 1.0F;
                float x = FENTES[random.nextInt(FENTES.length)] + 0.2F + random.nextFloat() * 0.6F;
                float monte = lit ? 2.0F + 3.0F * chaud : 1.0F;
                view.spawnParticles(DiegesisParticles.FLAME, new Vector3f(x, 4.0F + random.nextFloat(), -1.1F),
                        new Vector3f(0.0F, monte, -0.2F), 0.3F, 1, lit ? 0xFFFFB050 : 0xC0FF8030);
            }
            fumee += deltaSeconds * (lit ? 0.8F : 0.3F) * chaud;
            while (fumee >= 1.0F) {
                fumee -= 1.0F;
                view.spawnParticles(DiegesisParticles.SMOKE, new Vector3f(6.0F + random.nextFloat() * 4.0F, 8.6F, -1.2F),
                        new Vector3f(0.0F, 2.5F, -0.6F), 0.5F, 1, 0x60404040);
            }
        }
        // Le froid tombe de la grille en un souffle de givre.
        if (froid > 0.0F && (state.getValue(HearthBottomBlock.COOLING) || state.getValue(HearthBottomBlock.FROSTED))) {
            givre += deltaSeconds * (0.6F + 2.5F * froid);
            while (givre >= 1.0F) {
                givre -= 1.0F;
                view.spawnParticles(DiegesisParticles.STEAM, new Vector3f(4.0F + random.nextFloat() * 8.0F, 3.0F + random.nextFloat() * 5.0F, -1.3F),
                        new Vector3f(0.0F, -0.8F, -1.6F), 0.4F, 1, 0x70D8F0FF);
            }
        }
    }

    /** Jeté au feu : des étincelles jaillissent de la grille. */
    private void jaillit(StationView view) {
        chaudEnAttente = false;
        view.spawnParticles(DiegesisParticles.SPARK, new Vector3f(8.0F, 5.0F, -1.3F), new Vector3f(0.0F, 14.0F, -6.0F), 4.0F, 14, 0xFFFFC870);
        Son.joue(view, SoundEvents.FIRECHARGE_USE, "combustible", 0.15F, 1.3F + random.nextFloat() * 0.2F);
        view.node("jauge_chaud").ifPresent(node -> node.shake(0.15F, 0.2F));
    }

    /** Un froid jeté au foyer : une poudre de neige. */
    private void poudre(StationView view) {
        froidEnAttente = false;
        view.spawnParticles(DiegesisParticles.SPLASH, new Vector3f(8.0F, 4.5F, -1.2F), new Vector3f(0.0F, 3.0F, -4.0F), 4.0F, 12, 0xFFE8F4FF);
        Son.joue(view, SoundEvents.POWDER_SNOW_PLACE, "combustible", 0.5F, 1.1F + random.nextFloat() * 0.2F);
        view.node("jauge_froid").ifPresent(node -> node.shake(0.15F, 0.2F));
    }

    private void lire(StationView view) {
        if (!(view.level().getBlockEntity(view.pos()) instanceof HearthBlockEntity hearth) || hearth.getMaxFuel() <= 0) {
            return;
        }
        chaud = Math.min(1.0F, hearth.getHotFuel() / (float) hearth.getMaxFuel());
        froid = Math.min(1.0F, hearth.getColdFuel() / (float) hearth.getMaxFuel());
        int chaudPct = Math.round(chaud * 100.0F);
        int froidPct = Math.round(froid * 100.0F);
        // Comparé au combustible lui-même, pas au pourcentage : un petit bout de bois se voit aussi.
        if (chaudBrut >= 0 && hearth.getHotFuel() > chaudBrut) {
            chaudEnAttente = true;
        }
        if (froidBrut >= 0 && hearth.getColdFuel() > froidBrut) {
            froidEnAttente = true;
        }
        chaudBrut = hearth.getHotFuel();
        froidBrut = hearth.getColdFuel();
        if (chaudPct != chaudLu) {
            chaudLu = chaudPct;
            view.node("jauge_chaud").ifPresent(node -> node.setTooltip(Component.translatable("postes.fallow.foyer.chaud", chaudPct)));
        }
        if (froidPct != froidLu) {
            froidLu = froidPct;
            view.node("jauge_froid").ifPresent(node -> node.setTooltip(Component.translatable("postes.fallow.foyer.froid", froidPct)));
        }
    }

    /**
     * Une jauge à plat contre la façade, tournée vers l'avant (z négatif) : la vide en entier, puis la pleine du bas jusqu'à sa
     * part, comme la fenêtre de Cold Sweat (deux pixels sur quatorze dès qu'il y a du combustible). Elle luit un peu.
     */
    private static void jauge(PoseStack pose, MultiBufferSource buffers, ResourceLocation vide, ResourceLocation pleine, float part) {
        int light = LightTexture.FULL_BRIGHT;
        carre(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(vide)), 1.0F, light, 0.0F);
        if (part > 0.0F) {
            float haut = (2.0F + 12.0F * part) / 14.0F;
            carre(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(pleine)), haut, light, -0.01F);
        }
    }

    /** Le bas de la texture sur {@code haut} (de 0 à 1) de la hauteur, centré sur le nœud. */
    private static void carre(PoseStack pose, VertexConsumer out, float haut, int light, float z) {
        Matrix4f m = pose.last().pose();
        float x0 = -COTE / 2.0F;
        float x1 = COTE / 2.0F;
        float y0 = -COTE / 2.0F;
        float y1 = y0 + COTE * haut;
        float v0 = 1.0F - haut;
        // Vu de devant (z négatif), le côté de x grand est à gauche : u y commence.
        sommet(pose, out, m, x1, y0, z, 0.0F, 1.0F, light);
        sommet(pose, out, m, x0, y0, z, 1.0F, 1.0F, light);
        sommet(pose, out, m, x0, y1, z, 1.0F, v0, light);
        sommet(pose, out, m, x1, y1, z, 0.0F, v0, light);
    }

    private static void sommet(PoseStack pose, VertexConsumer out, Matrix4f m, float x, float y, float z, float u, float v, int light) {
        out.addVertex(m, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light)
                .setNormal(pose.last(), 0.0F, 0.0F, -1.0F);
    }
}
