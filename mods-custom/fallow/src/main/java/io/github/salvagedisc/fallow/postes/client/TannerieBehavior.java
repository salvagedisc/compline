package io.github.salvagedisc.fallow.postes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

import com.pockyl.diegesis.api.event.SceneEvent;
import com.pockyl.diegesis.api.client.DiegesisClient;
import com.pockyl.diegesis.api.client.StationBehavior;
import com.pockyl.diegesis.api.client.StationView;

import io.github.salvagedisc.fallow.postes.PostesTannerie;
import io.github.salvagedisc.fallow.tannerie.ChevaletBlock;
import io.github.salvagedisc.fallow.tannerie.CuveBlock;
import io.github.salvagedisc.fallow.tannerie.SechoirBlock;
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
    /** Les nœuds qui tremblent quand le bloc refuse : sa copie et ce qu'il porte. */
    private static final String[] TREMBLENT = { "cuve", "chevalet", "cadre", "eau", "peau", "raclee", "cuir", "sec", "peau_0", "peau_1",
            "peau_2", "peau_3" };

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

    /** Le premier objet connu d'une étiquette (le fantôme à montrer, l'écorce du livre), ou null si elle est vide. */
    static net.minecraft.world.item.Item premier(net.minecraft.tags.TagKey<net.minecraft.world.item.Item> tag) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(tag).flatMap(set -> set.stream().findFirst())
                .map(holder -> holder.value()).orElse(null);
    }

    /** Vrai si le bloc du poste, tel qu'il est, prend {@code stack} : les mêmes règles que le bloc sur le serveur. */
    static boolean accepte(BlockState state, ItemStack stack) {
        if (state.getBlock() instanceof CuveBlock) {
            return CuveBlock.accepte(state, stack);
        }
        if (state.getBlock() instanceof ChevaletBlock) {
            return ChevaletBlock.accepte(state, stack);
        }
        return state.getBlock() instanceof SechoirBlock && SechoirBlock.accepte(state, stack);
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
        // Ce qu'on tient et que le bloc prendrait : le bloc s'entoure, comme la marmite de Hands-On Delight sous un bol.
        LocalPlayer player = Minecraft.getInstance().player;
        ItemStack tenu = player == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
        boolean offre = view.isFocused() && view.isBlockHidden() && !tenu.isEmpty() && accepte(view.level().getBlockState(view.pos()), tenu);
        for (String bloc : new String[] { "cuve", "chevalet", "cadre" }) {
            view.node(bloc).ifPresent(node -> node.setOutlined(offre));
        }
    }

    @Override
    public void tick(StationView view) {
        allure.tick(view);
    }

    /** Le temps du jeu au dernier refus montré d'avance : le serveur qui le confirme ne le rejoue pas. */
    private long refusA = -1000L;

    /**
     * Le serveur a travaillé un objet sur le bloc. Ceux qui regardent voient le geste ou le refus ; celui qui l'a fait l'a vu
     * d'avance, sauf si le serveur a refusé ce que les règles du bloc laissaient passer.
     */
    @Override
    public void onEffect(StationView view, net.minecraft.resources.ResourceLocation effect, String node, CompoundTag data) {
        if (!PostesTannerie.GESTE.equals(effect)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        boolean moi = player != null && data.hasUUID(PostesTannerie.ACTEUR) && player.getUUID().equals(data.getUUID(PostesTannerie.ACTEUR));
        boolean pris = data.getBoolean(PostesTannerie.PRIS);
        if (moi) {
            if (!pris && view.level().getGameTime() - refusA > 20L) {
                refus(view);
            }
            return;
        }
        if (!pris) {
            refus(view);
            return;
        }
        net.minecraft.resources.ResourceLocation id = net.minecraft.resources.ResourceLocation.tryParse(data.getString(PostesTannerie.OBJET));
        if (id != null) {
            net.minecraft.core.registries.BuiltInRegistries.ITEM.getOptional(id).ifPresent(item -> allure.geste(view, new ItemStack(item)));
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
        // Le refus se sait tout de suite, par les règles du bloc : il tremble sans attendre le serveur, qui dit pourquoi.
        if (accepte(view.level().getBlockState(view.pos()), stack)) {
            allure.geste(view, stack);
        } else {
            refus(view);
            refusA = view.level().getGameTime();
        }
        Arrivee.parLeCurseur(view);
        view.sendAction(PostesTannerie.TRAVAILLER, new CompoundTag());
        return true;
    }

    /** Le bloc n'a pas pris l'objet : il tremble, avec un bruit de bois mat (le serveur dit pourquoi dans la légende). */
    private static void refus(StationView view) {
        for (String bloc : TREMBLENT) {
            view.node(bloc).ifPresent(node -> node.shake(0.35F, 0.2F));
        }
        Son.joue(view, net.minecraft.sounds.SoundEvents.WOOD_HIT, null, 0.5F, 0.7F);
    }

    @Override
    public boolean onInventoryShiftClick(StationView view, int slot, ItemStack stack) {
        if (!utile(stack)) {
            return false;
        }
        if (!accepte(view.level().getBlockState(view.pos()), stack)) {
            refus(view);
            refusA = view.level().getGameTime();
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
