package io.github.salvagedisc.fallow.postes.client;

import java.util.IdentityHashMap;
import java.util.Map;

import org.joml.Vector3f;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;

import com.pockyl.diegesis.api.client.DiegesisParticles;
import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.event.SceneEvent;
import com.pockyl.diegesis.api.scene.Visual;
import com.pockyl.diegesis.api.state.SlotRef;

import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Le poste d'une enclume de forge, sur le modèle de la table de Hands-On Vanilla : les pièces se posent dans la grille sur
 * l'enclume, le marteau et le plan à leur place, le livre de forge à côté. Une pièce posée tinte sur l'enclume (et grésille si
 * elle sort du feu), une pièce reprise glisse. Les pièces chaudes fument dans leur case. Quand la grille fait une recette, ce
 * qu'elle fera tourne, pâle, là où l'ouvrage arrivera, et la légende dit de frapper : accroupi, le marteau en main, comme
 * Overgeared le veut. L'ouvrage forgé arrive dans une gerbe d'étincelles. L'inventaire grise ce qui ne se forge pas ici.
 */
final class ForgeBehavior implements StationBehavior {
    private final RecipeBook book;
    private final ForgeBook source;
    private final int[] grille = new int[9];
    private boolean connu;
    /** La recette que fait la grille, la dernière fois : la légende ne revient qu'à une recette nouvelle. */
    private ItemStack annonce = ItemStack.EMPTY;
    /** Ce qui sert ici, objet par objet, lu une fois : le livre de cette enclume, le marteau, le plan. */
    private final Map<Item, Boolean> utiles = new IdentityHashMap<>();
    private final RandomSource random = RandomSource.create();
    private float horloge;
    private float fumee;
    /** L'objet que montre le fantôme de l'ouvrage, pour ne changer son dessin qu'à une recette nouvelle. */
    private Item fantome;
    private int ouvrage;
    /** Vrai quand l'ouvrage attend dans sa case : le fantôme de ce que fera la grille se tait. */
    private boolean fini;
    /** Le temps depuis l'arrivée de l'ouvrage : il grossit puis se pose. */
    private float arrive = 9.0F;

    ForgeBehavior(AnvilTier tier, int debutGrille) {
        this.source = new ForgeBook(tier, debutGrille);
        this.book = new RecipeBook(source);
    }

    @Override
    public void onSceneReady(StationView view) {
        view.node("book_open").ifPresent(node -> node.setPainter(book));
    }

    @Override
    public void onOpen(StationView view) {
        utiles.clear();
        view.setInventoryFilter(stack -> utile(view, stack));
        book.opened(view);
        annonce = ItemStack.EMPTY;
        fantome = null;
        ouvrage = -1;
    }

    private boolean utile(StationView view, ItemStack stack) {
        if (stack.is(ModTags.Items.SMITHING_HAMMERS) || stack.getItem() instanceof SmithingTemplateItem
                || net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().equals("blueprint")) {
            return true;
        }
        return utiles.computeIfAbsent(stack.getItem(), item -> {
            ItemStack one = new ItemStack(item);
            for (RecipeBook.Page page : source.pages(view)) {
                for (Ingredient ingredient : page.ingredients()) {
                    if (!ingredient.isEmpty() && ingredient.test(one)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    @Override
    public void onClose(StationView view) {
        book.shut(view);
        connu = false;
    }

    @Override
    public void frame(StationView view, float deltaSeconds) {
        book.frame(view, deltaSeconds);
        horloge += deltaSeconds;
        arrive += deltaSeconds;
        Item fera = view.isFocused() && !fini && !annonce.isEmpty() ? annonce.getItem() : null;
        view.node("fera").ifPresent(node -> {
            node.setVisible(fera != null);
            if (fera != fantome && fera != null) {
                node.setVisual(Visual.ofItem(fera).withTint(0x80FFFFFF).asTranslucent());
            }
            if (fera != null) {
                node.moveTo(0.0F, 0.35F * Mth.sin(horloge * 2.2F), 0.0F, 0.0F);
                node.rotateTo(-60.0F, horloge * 40.0F % 360.0F, 0.0F, 0.0F);
            }
        });
        fantome = fera;
        // L'ouvrage forgé arrive gros, puis se pose à sa taille.
        if (arrive < 0.5F) {
            float t = arrive / 0.5F;
            view.node(PostesForge.OUVRAGE).ifPresent(node -> node.scaleTo(1.0F + 0.35F * (1.0F - t) * (1.0F - t), 0.0F));
        }
        // Les pièces chaudes fument dans leur case.
        if (!view.isFocused()) {
            return;
        }
        int chaudes = 0;
        for (int cell = 0; cell < grille.length; cell++) {
            if (chaude(view.state().getItem(SlotRef.of(PostesForge.GRILLE, cell)))) {
                chaudes++;
            }
        }
        fumee += deltaSeconds * Math.min(3, chaudes) * 1.5F;
        while (fumee >= 1.0F && chaudes > 0) {
            fumee -= 1.0F;
            int pick = random.nextInt(chaudes);
            for (int cell = 0; cell < grille.length; cell++) {
                if (chaude(view.state().getItem(SlotRef.of(PostesForge.GRILLE, cell))) && pick-- == 0) {
                    view.spawnParticles(DiegesisParticles.SMOKE, "grille_" + cell, 1, 0x50D8D0C8);
                }
            }
        }
    }

    private static boolean chaude(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(ModTags.Items.HOT_ITEMS) || stack.is(ModTags.Items.HEATED_METALS));
    }

    @Override
    public void tick(StationView view) {
        if (!view.isFocused() || view.menu() == null) {
            return;
        }
        int pose = 0;
        int pris = 0;
        boolean grille_chaude = false;
        for (int cell = 0; cell < grille.length; cell++) {
            ItemStack stack = view.state().getItem(SlotRef.of(PostesForge.GRILLE, cell));
            int now = stack.getCount();
            if (connu && now > grille[cell]) {
                pose++;
                grille_chaude |= chaude(stack);
            } else if (connu && now < grille[cell]) {
                pris++;
            }
            grille[cell] = now;
        }
        connu = true;
        // Comme l'enclume de Hands-On Vanilla : un tintement clair quand une pièce se pose, un glissement quand elle part.
        if (pose > 0) {
            play(view, SoundEvents.ANVIL_PLACE, 0.08F, 1.7F + random.nextFloat() * 0.2F);
            if (grille_chaude) {
                play(view, SoundEvents.FIRE_EXTINGUISH, 0.12F, 1.6F + random.nextFloat() * 0.2F);
            }
        } else if (pris > 0) {
            play(view, SoundEvents.ITEM_FRAME_REMOVE_ITEM, 0.3F, 1.0F + random.nextFloat() * 0.1F);
        }
        AbstractSmithingAnvilMenu enclume = view.menu().handle() instanceof AbstractSmithingAnvilMenu menu ? menu : null;
        int forge = enclume == null ? 0 : enclume.getResultSlot().getItem().getCount();
        fini = forge > 0;
        if (ouvrage >= 0 && forge > ouvrage) {
            // L'ouvrage forgé arrive : une gerbe d'étincelles, et il grossit avant de se poser.
            arrive = 0.0F;
            view.spawnParticles(DiegesisParticles.SPARK, new Vector3f(-2.0F, 17.5F, 11.5F), new Vector3f(0.0F, 14.0F, 0.0F), 6.0F, 14, 0xFFFFC870);
            view.spawnParticles(DiegesisParticles.GLINT, new Vector3f(-2.0F, 18.0F, 11.5F), new Vector3f(0.0F, 2.0F, 0.0F), 3.0F, 6, 0xFFFFF0D0);
            view.playSound(SoundEvents.ANVIL_PLACE, PostesForge.OUVRAGE, 0.12F, 1.4F + random.nextFloat() * 0.2F);
        } else if (ouvrage > 0 && forge < ouvrage) {
            view.playSound(SoundEvents.ITEM_PICKUP, PostesForge.OUVRAGE, 0.3F, 1.2F + random.nextFloat() * 0.2F);
        }
        ouvrage = forge;
        ItemStack fera = enclume != null ? enclume.getGhostResult() : ItemStack.EMPTY;
        if (!fera.isEmpty() && !ItemStack.isSameItem(fera, annonce)) {
            view.showCaption(Component.translatable("postes.fallow.forge.pret", fera.getHoverName()), 3.0F);
            view.spawnParticles(DiegesisParticles.GLINT, new Vector3f(-2.0F, 18.5F, 11.5F), new Vector3f(0.0F, 1.5F, 0.0F), 2.5F, 5, 0xFFFFF0D0);
            view.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, "fera", 0.4F, 1.4F + random.nextFloat() * 0.2F);
        }
        annonce = fera.copy();
    }

    private static void play(StationView view, SoundEvent sound, float volume, float pitch) {
        view.playSound(sound, view.node("grille").isPresent() ? "grille" : null, volume, pitch);
    }

    @Override
    public void onEvent(StationView view, SceneEvent event) {
        if (event instanceof SceneEvent.Click click && "book_open".equals(click.node())) {
            book.click(view, click.button());
        }
    }

    @Override
    public boolean onItemClick(StationView view, String node, ItemStack stack, int button, boolean shift) {
        if ("book_open".equals(node)) {
            book.click(view, button);
            return true;
        }
        return false;
    }

    @Override
    public boolean onScroll(StationView view, String node, double amount) {
        return "book_open".equals(node) && book.scroll(amount);
    }

    @Override
    public boolean onKey(StationView view, int keyCode, int scanCode, int modifiers) {
        return book.key(view, keyCode, modifiers);
    }

    @Override
    public boolean onChar(StationView view, char character, int modifiers) {
        return book.typed(view, character);
    }
}
