package io.github.salvagedisc.fallow.postes.client;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.client.DiegesisParticles;
import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.scene.Visual;

import io.github.salvagedisc.fallow.FallowConfig;
import io.github.salvagedisc.fallow.tannerie.ChevaletBlock;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/**
 * Le chevalet au poste, sur le modèle de la planche à découper de Hands-On Delight : le couteau qu'on tient n'est plus au
 * curseur, il est posé sur la peau, à l'endroit visé ; un clic donne un coup qui glisse vers soi, soulève des copeaux et fait
 * trembler la peau. La peau brute pâlit coup après coup jusqu'à la peau raclée. Au-dessus flotte, pâle, ce qu'il faut poser
 * ensuite : une peau brute, puis un couteau.
 */
final class ChevaletAllure implements Allure {
    /** Durée d'un coup : la lame part vers soi, puis revient. */
    private static final float COUP = 0.32F;
    private static final int CHAIR = 0xFFC07A62;

    private final RandomSource random = RandomSource.create();
    private float horloge;
    /** Temps depuis le dernier coup ; au-delà de {@link #COUP} le couteau suit le curseur. */
    private float coup = 9.0F;
    private float vise = 8.0F;
    private Item tenu;
    private Item fantome;
    private boolean peau;
    private int raclage;
    private final Envol envol = new Envol();
    private final Arrivee.Volee volee = new Arrivee.Volee();
    /** Faux jusqu'au premier tick du poste : une peau déjà tendue n'arrive pas. */
    private boolean connu;

    @Override
    public void onOpen(StationView view) {
        tenu = null;
        fantome = null;
        connu = false;
    }

    private static boolean couteau(ItemStack stack) {
        return stack.is(TannerieModule.COUTEAUX);
    }

    @Override
    public void frame(StationView view, float dt) {
        BlockState state = view.level().getBlockState(view.pos());
        if (!(state.getBlock() instanceof ChevaletBlock)) {
            return;
        }
        horloge += dt;
        coup += dt;
        envol.frame(view, dt);
        volee.frame(view, dt);
        boolean shown = view.isBlockHidden();
        boolean hide = state.getValue(ChevaletBlock.PEAU);
        float done = Math.min(1.0F, state.getValue(ChevaletBlock.RACLAGE) / (float) Math.max(1, Math.min(15, FallowConfig.tannerieStrokes())));
        view.node("chevalet").ifPresent(node -> node.setVisible(shown));
        view.node("peau").ifPresent(node -> node.setVisible(shown && hide));
        view.node("raclee").ifPresent(node -> {
            node.setVisible(shown && hide && done > 0.0F);
            node.setTint((int) (done * 0.85F * 255.0F) << 24 | 0xFFFFFF);
        });

        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack carried = player == null ? ItemStack.EMPTY : player.inventoryMenu.getCarried();
        boolean knife = shown && view.isFocused() && hide && couteau(carried);
        view.setCarriedHidden(knife);
        view.node("couteau").ifPresent(node -> {
            node.setVisible(knife);
            if (!knife) {
                return;
            }
            if (carried.getItem() != tenu) {
                tenu = carried.getItem();
                node.setVisual(Visual.ofItem(tenu));
            }
            if (coup >= COUP) {
                view.cursorOn(13.5F).ifPresent(point -> vise = Mth.clamp(point.z(), 3.0F, 13.0F));
            }
            // Un coup : la lame appuie et file de 4 pixels vers soi, puis revient se poser.
            float t = Math.min(1.0F, coup / COUP);
            float stroke = t < 0.45F ? -4.0F * Mth.sin(t / 0.45F * Mth.HALF_PI) : -4.0F * (1.0F - (t - 0.45F) / 0.55F);
            float press = t < 0.45F ? -0.5F * Mth.sin(t / 0.45F * Mth.PI) : 0.0F;
            float rest = 0.15F * Mth.sin(horloge * 2.0F);
            node.moveTo(0.0F, press + rest, vise - 8.0F + stroke, 0.0F);
            node.rotateTo(t < 0.45F ? 12.0F * Mth.sin(t / 0.45F * Mth.PI) : 0.0F, 0.0F, 0.0F, 0.0F);
        });

        // La poutre vide montre, pâle et battant doucement, la peau à y tendre ; la peau posée attend le couteau.
        boolean inUse = shown && view.isInUse();
        boolean wantsHide = inUse && !hide && !carried.is(TannerieModule.PEAU_BRUTE.get());
        view.node("fantome").ifPresent(node -> {
            node.setVisible(wantsHide);
            if (wantsHide) {
                node.setTint((int) (110.0F + 45.0F * Mth.sin(horloge * 2.6F)) << 24 | 0xFFFFFF);
            }
        });
        Item wanted = inUse && hide && !couteau(carried) ? couteauAttendu() : null;
        view.node("fantome_couteau").ifPresent(node -> {
            node.setVisible(wanted != null);
            if (wanted != fantome) {
                fantome = wanted;
                if (wanted != null) {
                    node.setVisual(Visual.ofItem(wanted).withTint(0x70FFFFFF).asTranslucent());
                }
            }
            if (wanted != null) {
                node.moveTo(0.0F, 0.5F * Mth.sin(horloge * 2.2F), 0.0F, 0.0F);
            }
        });
    }

    /** Le premier couteau à écharner connu, montré pâle quand la peau l'attend. */
    private static Item couteauAttendu() {
        return BuiltInRegistries.ITEM.getTag(TannerieModule.COUTEAUX).flatMap(tag -> tag.stream().findFirst())
                .map(holder -> holder.value()).orElse(null);
    }

    @Override
    public void geste(StationView view, ItemStack stack) {
        BlockState state = view.level().getBlockState(view.pos());
        if (!(state.getBlock() instanceof ChevaletBlock) || !state.getValue(ChevaletBlock.PEAU) || !couteau(stack) || coup < COUP * 0.6F) {
            return;
        }
        coup = 0.0F;
        // Les copeaux partent de la lame, vers soi et un peu en l'air ; la peau tremble sous le coup.
        view.spawnParticles(DiegesisParticles.DUST, new Vector3f(8.0F, 13.7F, vise - 1.0F), new Vector3f(0.0F, 6.0F, -10.0F), 1.5F, 12, CHAIR);
        view.spawnParticles(DiegesisParticles.DUST, new Vector3f(8.0F, 13.7F, vise - 2.5F), new Vector3f(0.0F, 3.0F, -6.0F), 1.0F, 6, 0xFFE8D6C0);
        view.node("peau").ifPresent(node -> node.shake(0.25F, 0.15F));
        view.node("raclee").ifPresent(node -> node.shake(0.25F, 0.15F));
        view.playSound(SoundEvents.SHEEP_SHEAR, "couteau", 0.18F, 1.5F + random.nextFloat() * 0.3F);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND, false);
        }
    }

    @Override
    public void tick(StationView view) {
        BlockState state = view.level().getBlockState(view.pos());
        if (!(state.getBlock() instanceof ChevaletBlock)) {
            return;
        }
        boolean hide = state.getValue(ChevaletBlock.PEAU);
        if (peau && !hide && view.isBlockHidden()) {
            // La peau est finie (ou reprise) : elle quitte le chevalet dans un éclat et vole jusqu'à la barre.
            view.spawnParticles(DiegesisParticles.GLINT, new Vector3f(8.0F, 13.8F, 8.0F), new Vector3f(0.0F, 4.0F, 0.0F), 4.0F, 8, 0xFFFFF0D0);
            envol.part(view, raclage > 0 ? TannerieModule.PEAU_RACLEE.get() : TannerieModule.PEAU_BRUTE.get(), new Vector3f(8.0F, 14.0F, 8.0F));
        }
        if (connu && !peau && hide && view.isBlockHidden()) {
            volee.arrive(view, TannerieModule.PEAU_BRUTE.get(), new Vector3f(8.0F, 14.0F, 8.0F));
        }
        connu = true;
        peau = hide;
        raclage = hide ? state.getValue(ChevaletBlock.RACLAGE) : 0;
    }
}
