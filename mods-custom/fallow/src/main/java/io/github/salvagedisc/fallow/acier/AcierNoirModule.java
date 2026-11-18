package io.github.salvagedisc.fallow.acier;

import io.github.salvagedisc.fallow.Fallow;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Module acier_noir : le palier entre l'acier d'Overgeared et le diamant (docs/plan/progression.md).
 * Acier, magnétite de Spelunkery et charbon de bois au four à alliage, chauffé, martelé, refroidi ;
 * les plaques, les têtes et les armures se forgent sur l'enclume de palier A d'Overgeared (l'enclume de maître),
 * posée sur un bloc d'acier noir. Seule une pioche d'acier noir (ou mieux) casse le diamant.
 *
 * <p>Ce module ne fait qu'enregistrer les objets, le matériau d'armure et le palier d'outil : les recettes
 * (types d'Overgeared), les tags et la chaleur sont des données du pack ({@code pack/kubejs/data/}). Les objets
 * sont toujours enregistrés, le serveur et les clients doivent avoir les mêmes registres.
 */
public final class AcierNoirModule {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Fallow.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Fallow.MOD_ID);
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Fallow.MOD_ID);

    /** Ce qu'un outil d'acier noir ne casse pas : ce qui demande le diamant, sauf l'obsidienne (voir le tag). */
    public static final TagKey<Block> INCORRECT_FOR_BLACK_STEEL =
            BlockTags.create(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "incorrect_for_black_steel_tool"));

    /** Valeurs de la décision : entre l'acier d'Overgeared (500, 7, 3) et le diamant (1561, 8, 3). */
    public static final int DURABILITE_OUTILS = 900;
    public static final int MULTIPLICATEUR_ARMURE = 30;

    public static final DeferredItem<Item> ACIER_NOIR_BRUT = ITEMS.registerSimpleItem("acier_noir_brut");
    public static final DeferredItem<Item> ACIER_NOIR_BRUT_CHAUFFE = ITEMS.registerSimpleItem("acier_noir_brut_chauffe");
    public static final DeferredItem<Item> LINGOT_CHAUFFE = ITEMS.registerSimpleItem("lingot_d_acier_noir_chauffe");
    public static final DeferredItem<Item> LINGOT = ITEMS.registerSimpleItem("lingot_d_acier_noir");
    public static final DeferredItem<Item> PLAQUE = ITEMS.registerSimpleItem("plaque_d_acier_noir");

    public static final DeferredBlock<Block> BLOC = BLOCKS.register("bloc_d_acier_noir",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK)
                    .strength(6.0F, 7.0F).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredItem<BlockItem> BLOC_ITEM = ITEMS.registerSimpleBlockItem(BLOC);

    public static final DeferredItem<Item> TETE_DE_PIOCHE = ITEMS.registerSimpleItem("tete_de_pioche_en_acier_noir");
    public static final DeferredItem<Item> TETE_DE_HACHE = ITEMS.registerSimpleItem("tete_de_hache_en_acier_noir");
    public static final DeferredItem<Item> TETE_DE_PELLE = ITEMS.registerSimpleItem("tete_de_pelle_en_acier_noir");
    public static final DeferredItem<Item> TETE_DE_HOUE = ITEMS.registerSimpleItem("tete_de_houe_en_acier_noir");
    public static final DeferredItem<Item> LAME_D_EPEE = ITEMS.registerSimpleItem("lame_d_epee_en_acier_noir");

    public static final Tier PALIER = new SimpleTier(INCORRECT_FOR_BLACK_STEEL, DURABILITE_OUTILS, 7.5F, 3.0F, 12,
            () -> Ingredient.of(LINGOT.get()));

    public static final DeferredItem<PickaxeItem> PIOCHE = ITEMS.register("pioche_en_acier_noir",
            () -> new PickaxeItem(PALIER, new Item.Properties().attributes(PickaxeItem.createAttributes(PALIER, 1.0F, -2.8F))));
    public static final DeferredItem<AxeItem> HACHE = ITEMS.register("hache_en_acier_noir",
            () -> new AxeItem(PALIER, new Item.Properties().attributes(AxeItem.createAttributes(PALIER, 5.0F, -3.0F))));
    public static final DeferredItem<ShovelItem> PELLE = ITEMS.register("pelle_en_acier_noir",
            () -> new ShovelItem(PALIER, new Item.Properties().attributes(ShovelItem.createAttributes(PALIER, 1.5F, -3.0F))));
    public static final DeferredItem<HoeItem> HOUE = ITEMS.register("houe_en_acier_noir",
            () -> new HoeItem(PALIER, new Item.Properties().attributes(HoeItem.createAttributes(PALIER, -3.0F, 0.0F))));
    public static final DeferredItem<SwordItem> EPEE = ITEMS.register("epee_en_acier_noir",
            () -> new SwordItem(PALIER, new Item.Properties().attributes(SwordItem.createAttributes(PALIER, 3, -2.4F))));

    /** Armure 19 (3/7/6/3), robustesse 1,5 par pièce, entre l'acier (17, 1) et le diamant (20, 2). */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> MATERIAU = ARMOR_MATERIALS.register("acier_noir",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                        map.put(ArmorItem.Type.BOOTS, 3);
                        map.put(ArmorItem.Type.LEGGINGS, 6);
                        map.put(ArmorItem.Type.CHESTPLATE, 7);
                        map.put(ArmorItem.Type.HELMET, 3);
                        map.put(ArmorItem.Type.BODY, 7);
                    }),
                    12, SoundEvents.ARMOR_EQUIP_IRON, () -> Ingredient.of(LINGOT.get()),
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(Fallow.MOD_ID, "acier_noir"))),
                    1.5F, 0.0F));

    public static final DeferredItem<ArmorItem> CASQUE = armure("casque_en_acier_noir", ArmorItem.Type.HELMET);
    public static final DeferredItem<ArmorItem> PLASTRON = armure("plastron_en_acier_noir", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<ArmorItem> JAMBIERES = armure("jambieres_en_acier_noir", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<ArmorItem> BOTTES = armure("bottes_en_acier_noir", ArmorItem.Type.BOOTS);

    private AcierNoirModule() {
    }

    private static DeferredItem<ArmorItem> armure(String id, ArmorItem.Type type) {
        return ITEMS.register(id, () -> new ArmorItem(MATERIAU, type,
                new Item.Properties().durability(type.getDurability(MULTIPLICATEUR_ARMURE))));
    }

    public static void start(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        ARMOR_MATERIALS.register(modBus);
        modBus.addListener(BuildCreativeModeTabContentsEvent.class, AcierNoirModule::onCreativeTab);
    }

    private static void onCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ACIER_NOIR_BRUT);
            event.accept(LINGOT);
            event.accept(PLAQUE);
            event.accept(TETE_DE_PIOCHE);
            event.accept(TETE_DE_HACHE);
            event.accept(TETE_DE_PELLE);
            event.accept(TETE_DE_HOUE);
            event.accept(LAME_D_EPEE);
        } else if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(BLOC_ITEM);
        } else if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(PIOCHE);
            event.accept(HACHE);
            event.accept(PELLE);
            event.accept(HOUE);
        } else if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(EPEE);
            event.accept(HACHE);
            event.accept(CASQUE);
            event.accept(PLASTRON);
            event.accept(JAMBIERES);
            event.accept(BOTTES);
        }
    }
}
