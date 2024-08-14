package net.andrew_coursin.magical_staffs.util;

import net.andrew_coursin.magical_staffs.item.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModItemProperties {
    private static void makeStaff(Item item) {
        ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(MOD_ID, "casting"), (itemStack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1.0F : 0.0F);
    }

    public static void addCustomItemProperties() {
        makeStaff(ModItems.WOODEN_STAFF.get());
        makeStaff(ModItems.GOLDEN_STAFF.get());
        makeStaff(ModItems.AMETHYST_STAFF.get());
        makeStaff(ModItems.IRON_STAFF.get());
        makeStaff(ModItems.DIAMOND_STAFF.get());
        makeStaff(ModItems.BLAZE_STAFF.get());
        makeStaff(ModItems.NETHERITE_STAFF.get());
    }
}
