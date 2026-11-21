package io.github.salvagedisc.fallow.postes.client;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;

import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;
import com.pockyl.diegesis.api.event.SceneEvent;
import com.pockyl.diegesis.api.state.SlotRef;

import net.stirdrem.overgeared.AnvilTier;
import net.stirdrem.overgeared.screen.AbstractSmithingAnvilMenu;
import net.stirdrem.overgeared.util.ModTags;

/**
 * Le poste d'une enclume de forge, sur le modèle de la table de Hands-On Vanilla : les pièces se posent dans la grille sur
 * l'enclume, le marteau et le plan à leur place, le livre de forge à côté. Un petit bruit de métal quand une pièce arrive ou
 * part. Quand la grille fait une recette, la légende dit de frapper : accroupi, le marteau en main, comme Overgeared le veut.
 * L'inventaire grise ce qui ne se forge pas ici.
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
    }

    @Override
    public void tick(StationView view) {
        if (!view.isFocused() || view.menu() == null) {
            return;
        }
        int pose = 0;
        int pris = 0;
        for (int cell = 0; cell < grille.length; cell++) {
            int now = view.state().getItem(SlotRef.of(PostesForge.GRILLE, cell)).getCount();
            if (connu && now > grille[cell]) {
                pose++;
            } else if (connu && now < grille[cell]) {
                pris++;
            }
            grille[cell] = now;
        }
        connu = true;
        if (pose > 0) {
            play(view, SoundEvents.CHAIN_PLACE, 0.5F, 1.3F);
        } else if (pris > 0) {
            play(view, SoundEvents.CHAIN_BREAK, 0.4F, 1.4F);
        }
        ItemStack fera = view.menu().handle() instanceof AbstractSmithingAnvilMenu menu ? menu.getGhostResult() : ItemStack.EMPTY;
        if (!fera.isEmpty() && !ItemStack.isSameItem(fera, annonce)) {
            view.showCaption(Component.translatable("postes.fallow.forge.pret", fera.getHoverName()), 3.0F);
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
