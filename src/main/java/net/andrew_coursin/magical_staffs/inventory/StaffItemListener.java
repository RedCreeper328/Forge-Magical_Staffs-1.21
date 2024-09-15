package net.andrew_coursin.magical_staffs.inventory;

import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.staff_modes.StaffModes;
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
        if (pContainerToSend instanceof InventoryMenu inventoryMenu) {
            inventoryMenu.getItems().forEach(itemStack -> {
                StaffModes staffModes = itemStack.get(ModComponents.STAFF_MODES.get());
                if (itemStack.getItem() instanceof StaffItem && staffModes != null) {
                    staffModes.reset(true);
                }
            });
        }
    }
}
