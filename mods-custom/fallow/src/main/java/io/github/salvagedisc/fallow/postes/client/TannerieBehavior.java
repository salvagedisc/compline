package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import com.pockyl.diegesis.api.event.SceneEvent;
import com.pockyl.diegesis.api.client.DiegesisClient;
import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;

import io.github.salvagedisc.fallow.postes.PostesTannerie;
import io.github.salvagedisc.fallow.tannerie.TannerieModule;

/**
 * Un poste de la tannerie sur le client, sur le modèle de la planche de Hands-On Delight : ce qu'on lâche sur le bloc depuis le
 * curseur y travaille (la peau se tend, le couteau racle, l'eau et l'écorce vont dans la cuve), un clic à main vide reprend ce
 * qui est fini ou dit où en est le travail, Maj + clic dans l'inventaire y envoie la pile. Venu au bloc avec un objet en main,
 * le premier geste est fait comme au clic dans le monde. Le livre de la tannerie est à gauche.
 */
final class TannerieBehavior implements StationBehavior {
    private final RecipeBook book;
    private final Allure allure;
    /** La case de la main quand on est venu au bloc avec un objet ; le premier geste part quand le poste est ouvert côté serveur. */
    private int enMain = -1;
    /** Le bloc et le curseur au moment d'un objet lâché sur le bloc : si rien n'a bougé un peu plus tard, le bloc l'a refusé. */
    private net.minecraft.world.level.block.state.BlockState avantGeste;
    private ItemStack tenuAvant = ItemStack.EMPTY;
    private long gesteA = Long.MIN_VALUE;

    TannerieBehavior(String page, Allure allure) {
        this.book = new RecipeBook(new TannerieBook(page));
        this.allure = allure;
    }

    /** Ce qui sert à la tannerie : le reste de l'inventaire est grisé. */
    static boolean utile(ItemStack stack) {
        return stack.is(TannerieModule.PEAU_BRUTE.get()) || stack.is(TannerieModule.PEAU_RACLEE.get())
                || stack.is(TannerieModule.CUIR_MOUILLE.get()) || stack.is(TannerieModule.COUTEAUX) || stack.is(TannerieModule.TANINS)
                || stack.is(Items.WATER_BUCKET) || stack.is(Items.BUCKET);
    }

    @Override
    public void onSceneReady(StationView view) {
        view.node("book_open").ifPresent(node -> node.setPainter(book));
        allure.onSceneReady(view);
    }

    @Override
    public void onOpen(StationView view) {
        book.opened(view);
        allure.onOpen(view);
        view.setInventoryFilter(TannerieBehavior::utile);
        LocalPlayer player = Minecraft.getInstance().player;
        enMain = player != null && PostesClient.venuParUsage(view.pos()) && utile(player.getMainHandItem()) && player.inventoryMenu.getCarried().isEmpty()
                ? player.getInventory().selected
                : -1;
    }

    @Override
    public void onClose(StationView view) {
        enMain = -1;
        book.shut(view);
    }

    @Override
    public void frame(StationView view, float deltaSeconds) {
        // Comme le four de Hands-On Vanilla, qui attend son menu : envoyé à l'ouverture, le geste arriverait au serveur avant le
        // poste et se perdrait. Il part quand la caméra est arrivée.
        if (enMain >= 0 && view.isFocused() && DiegesisClient.arrival() >= 1.0F) {
            CompoundTag how = new CompoundTag();
            how.putInt(PostesTannerie.DEPUIS, enMain);
            Arrivee.parLeCurseur(view);
            view.sendAction(PostesTannerie.TRAVAILLER, how);
            enMain = -1;
        }
        book.frame(view, deltaSeconds);
        allure.frame(view, deltaSeconds);
    }

    @Override
    public void tick(StationView view) {
        allure.tick(view);
        if (avantGeste != null && view.level().getGameTime() - gesteA >= 6L) {
            LocalPlayer player = Minecraft.getInstance().player;
            ItemStack tenu = player == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
            if (view.level().getBlockState(view.pos()) == avantGeste && ItemStack.matches(tenu, tenuAvant)) {
                refus(view);
            }
            avantGeste = null;
        }
    }

    @Override
    public void onEvent(StationView view, SceneEvent event) {
        if (!(event instanceof SceneEvent.Click click)) {
            return;
        }
        if ("book_open".equals(click.node())) {
            book.click(view, click.button());
        } else if ("surface".equals(click.node()) || "pret".equals(click.node())) {
            view.sendAction(PostesTannerie.MAIN_VIDE, new CompoundTag());
        }
    }

    @Override
    public boolean onItemClick(StationView view, String node, ItemStack stack, int button, boolean shift) {
        if ("book_open".equals(node)) {
            book.click(view, button);
            return true;
        }
        allure.geste(view, stack);
        Arrivee.parLeCurseur(view);
        // Le couteau peut racler à vide entre deux coups : seul un objet posé est guetté.
        if (!stack.is(TannerieModule.COUTEAUX)) {
            avantGeste = view.level().getBlockState(view.pos());
            tenuAvant = stack.copy();
            gesteA = view.level().getGameTime();
        }
        view.sendAction(PostesTannerie.TRAVAILLER, new CompoundTag());
        return true;
    }

    /** Le bloc n'a pas pris l'objet : il tremble, avec un bruit de bois mat (le serveur dit pourquoi dans la légende). */
    private static void refus(StationView view) {
        for (String bloc : new String[] { "cuve", "chevalet", "cadre" }) {
            view.node(bloc).ifPresent(node -> node.shake(0.35F, 0.2F));
        }
        view.playSound(net.minecraft.sounds.SoundEvents.WOOD_HIT, null, 0.5F, 0.7F);
    }

    @Override
    public boolean onInventoryShiftClick(StationView view, int slot, ItemStack stack) {
        if (!utile(stack)) {
            return false;
        }
        CompoundTag how = new CompoundTag();
        how.putInt(PostesTannerie.DEPUIS, slot);
        Arrivee.depuis(view, slot);
        view.sendAction(PostesTannerie.TRAVAILLER, how);
        return true;
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
