package net.andrew_coursin.magical_staffs.inventory;

import net.andrew_coursin.magical_staffs.item.custom.StaffItem;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StaffItemListener implements ContainerListener {
    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

    }

    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
        if (pContainerToSend instanceof InventoryMenu inventoryMenu && pDataSlotIndex == InventoryMenu.SHIELD_SLOT) {
            inventoryMenu.getItems().forEach(itemStack -> {
                if (itemStack.getItem() instanceof StaffItem staffItem) {
                    staffItem.reset(true);
                }
            });
        }
    }
}
