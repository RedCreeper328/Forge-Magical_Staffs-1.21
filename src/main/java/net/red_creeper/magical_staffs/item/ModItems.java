package net.red_creeper.magical_staffs.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Rarity;
import net.red_creeper.magical_staffs.item.custom.StaffItem;
import net.red_creeper.magical_staffs.components.forge_material.ForgeMaterials;
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

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ModItems {
    // Create a Deferred Register to hold Items which will all be registered under the "magical_staffs" namespace
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<SmithingTemplateItem> FORGE_UPGRADE_SMITHING_TEMPLATE = ITEMS.register("forge_upgrade_smithing_template", ModItems::createForgeUpgradeTemplate);

    public static final RegistryObject<StaffItem> WOODEN_STAFF = ITEMS.register("wooden_staff", () -> new StaffItem(ForgeMaterials.WOOD.activeDuration(), ForgeMaterials.WOOD.cooldownFactor(), ForgeMaterials.WOOD.enchantmentSlots(), ForgeMaterials.WOOD.potionSlots(), staffItemProperties("wooden_staff")));

    public static final RegistryObject<StaffItem> GOLDEN_STAFF = ITEMS.register("golden_staff", () -> new StaffItem(ForgeMaterials.GOLD.activeDuration(), ForgeMaterials.GOLD.cooldownFactor(), ForgeMaterials.GOLD.enchantmentSlots(), ForgeMaterials.GOLD.potionSlots(), staffItemProperties("golden_staff")));

    public static final RegistryObject<StaffItem> AMETHYST_STAFF = ITEMS.register("amethyst_staff", () -> new StaffItem(ForgeMaterials.AMETHYST.activeDuration(), ForgeMaterials.AMETHYST.cooldownFactor(), ForgeMaterials.AMETHYST.enchantmentSlots(), ForgeMaterials.AMETHYST.potionSlots(), staffItemProperties("amethyst_staff")));

    public static final RegistryObject<StaffItem> IRON_STAFF = ITEMS.register("iron_staff", () -> new StaffItem(ForgeMaterials.IRON.activeDuration(), ForgeMaterials.IRON.cooldownFactor(), ForgeMaterials.IRON.enchantmentSlots(), ForgeMaterials.IRON.potionSlots(), staffItemProperties("iron_staff")));

    public static final RegistryObject<StaffItem> DIAMOND_STAFF = ITEMS.register("diamond_staff", () -> new StaffItem(ForgeMaterials.DIAMOND.activeDuration(), ForgeMaterials.DIAMOND.cooldownFactor(), ForgeMaterials.DIAMOND.enchantmentSlots(), ForgeMaterials.DIAMOND.potionSlots(), staffItemProperties("diamond_staff")));

    public static final RegistryObject<StaffItem> BLAZE_STAFF = ITEMS.register("blaze_staff", () -> new StaffItem(ForgeMaterials.BLAZE.activeDuration(), ForgeMaterials.BLAZE.cooldownFactor(), ForgeMaterials.BLAZE.enchantmentSlots(), ForgeMaterials.BLAZE.potionSlots(), staffItemProperties("blaze_staff")));

    public static final RegistryObject<StaffItem> NETHERITE_STAFF = ITEMS.register("netherite_staff", () -> new StaffItem(ForgeMaterials.NETHERITE.activeDuration(), ForgeMaterials.NETHERITE.cooldownFactor(), ForgeMaterials.NETHERITE.enchantmentSlots(), ForgeMaterials.NETHERITE.potionSlots(), staffItemProperties("netherite_staff")));

    private static Item.Properties staffItemProperties(String name) {
        return new Item.Properties().rarity(Rarity.UNCOMMON).stacksTo(1).useItemDescriptionPrefix().setId(ITEMS.key(name));
    }

    public static SmithingTemplateItem createForgeUpgradeTemplate() {
        ChatFormatting DESCRIPTION_FORMAT = ChatFormatting.BLUE;
        Component appliesTo = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.applies_to"))).withStyle(DESCRIPTION_FORMAT);
        Component ingredients = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.ingredients"))).withStyle(DESCRIPTION_FORMAT);
        Component baseSlotDescription = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.base_slot_description")));
        Component additionsSlotDescription = Component.translatable(Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(MOD_ID, "smithing_template.forge_upgrade.additions_slot_description")));
        List<ResourceLocation> baseSlotEmptyIcons = List.of(ResourceLocation.withDefaultNamespace("container/slot/staff"));
        List<ResourceLocation> additionalSlotEmptyIcons = List.of(ResourceLocation.withDefaultNamespace("container/slot/block"), ResourceLocation.withDefaultNamespace("container/slot/ingot"), ResourceLocation.withDefaultNamespace("container/slot/amethyst_shard"), ResourceLocation.withDefaultNamespace("container/slot/diamond"), ResourceLocation.withDefaultNamespace("container/slot/blaze_rod"), ResourceLocation.withDefaultNamespace("container/slot/nether_star"), ResourceLocation.withDefaultNamespace("container/slot/dragon_breath"));
        return new SmithingTemplateItem(appliesTo, ingredients, baseSlotDescription, additionsSlotDescription, baseSlotEmptyIcons, additionalSlotEmptyIcons, new Item.Properties().rarity(Rarity.RARE).useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(MOD_ID, "forge_upgrade_smithing_template"))));
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