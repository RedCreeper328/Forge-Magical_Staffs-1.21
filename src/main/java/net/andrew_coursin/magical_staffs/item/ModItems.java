package net.andrew_coursin.magical_staffs.item;

import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.stored_staff_effects.StoredStaffEffects;
import net.andrew_coursin.magical_staffs.item.custom.StaffItem;
import net.andrew_coursin.magical_staffs.components.forge_material.ForgeMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModItems {
    // Create a Deferred Register to hold Items which will all be registered under the "magical_staffs" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<SmithingTemplateItem> FORGE_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("forge_upgrade_smithing_template", ModItems::createForgeUpgradeTemplate);

    public static final RegistryObject<StaffItem> WOODEN_STAFF = ITEMS.register("wooden_staff", () -> new StaffItem(ForgeMaterials.WOOD.activeDuration(), ForgeMaterials.WOOD.cooldownFactor(), ForgeMaterials.WOOD.enchantmentSlots(), ForgeMaterials.WOOD.potionSlots(), new Item.Properties().stacksTo(1).component(ModComponents.STORED_STAFF_EFFECTS.get(), StoredStaffEffects.EMPTY)));

    public static final RegistryObject<StaffItem> GOLDEN_STAFF = ITEMS.register("golden_staff", () -> new StaffItem(ForgeMaterials.GOLD.activeDuration(), ForgeMaterials.GOLD.cooldownFactor(), ForgeMaterials.GOLD.enchantmentSlots(), ForgeMaterials.GOLD.potionSlots(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<StaffItem> AMETHYST_STAFF = ITEMS.register("amethyst_staff", () -> new StaffItem(ForgeMaterials.AMETHYST.activeDuration(), ForgeMaterials.AMETHYST.cooldownFactor(), ForgeMaterials.AMETHYST.enchantmentSlots(), ForgeMaterials.AMETHYST.potionSlots(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<StaffItem> IRON_STAFF = ITEMS.register("iron_staff", () -> new StaffItem(ForgeMaterials.IRON.activeDuration(), ForgeMaterials.IRON.cooldownFactor(), ForgeMaterials.IRON.enchantmentSlots(), ForgeMaterials.IRON.potionSlots(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<StaffItem> DIAMOND_STAFF = ITEMS.register("diamond_staff", () -> new StaffItem(ForgeMaterials.DIAMOND.activeDuration(), ForgeMaterials.DIAMOND.cooldownFactor(), ForgeMaterials.DIAMOND.enchantmentSlots(), ForgeMaterials.DIAMOND.potionSlots(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<StaffItem> BLAZE_STAFF = ITEMS.register("blaze_staff", () -> new StaffItem(ForgeMaterials.BLAZE.activeDuration(), ForgeMaterials.BLAZE.cooldownFactor(), ForgeMaterials.BLAZE.enchantmentSlots(), ForgeMaterials.BLAZE.potionSlots(), new Item.Properties().stacksTo(1)));

    public static final RegistryObject<StaffItem> NETHERITE_STAFF = ITEMS.register("netherite_staff", () -> new StaffItem(ForgeMaterials.NETHERITE.activeDuration(), ForgeMaterials.NETHERITE.cooldownFactor(), ForgeMaterials.NETHERITE.enchantmentSlots(), ForgeMaterials.NETHERITE.potionSlots(), new Item.Properties().stacksTo(1)));

    public static SmithingTemplateItem createForgeUpgradeTemplate() {
        ChatFormatting TITLE_FORMAT = ChatFormatting.GRAY;
        ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
        Component appliesTo = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);
        Component ingredients = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);
        Component upgradeDescription = Component.translatable(Util.makeDescriptionId("upgrade", ResourceLocation.fromNamespaceAndPath(MOD_ID, "forge_upgrade"))).withStyle(TITLE_FORMAT);
        Component baseSlotDescription = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.base_slot_description")));
        Component additionsSlotDescription = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.additions_slot_description")));
        List<ResourceLocation> baseSlotEmptyIcons = List.of(ResourceLocation.withDefaultNamespace("item/empty_slot_staff"));
        List<ResourceLocation> additionalSlotEmptyIcons = List.of(ResourceLocation.withDefaultNamespace("block/empty_slot_block"), ResourceLocation.withDefaultNamespace("item/empty_slot_ingot"), ResourceLocation.withDefaultNamespace("item/empty_slot_amethyst_shard"), ResourceLocation.withDefaultNamespace("item/empty_slot_diamond"), ResourceLocation.withDefaultNamespace("item/empty_slot_blaze_rod"), ResourceLocation.withDefaultNamespace("item/empty_slot_nether_star"), ResourceLocation.withDefaultNamespace("item/empty_slot_dragon_breath"));
        return new SmithingTemplateItem(appliesTo, ingredients, upgradeDescription, baseSlotDescription, additionsSlotDescription, baseSlotEmptyIcons, additionalSlotEmptyIcons);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(WOODEN_STAFF);
            event.accept(GOLDEN_STAFF);
            event.accept(AMETHYST_STAFF);
            event.accept(IRON_STAFF);
            event.accept(DIAMOND_STAFF);
            event.accept(BLAZE_STAFF);
            event.accept(NETHERITE_STAFF);
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(FORGE_UPGRADE_SMITHING_TEMPLATE);
        }
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}