package net.andrew_coursin.magical_staffs.inventory;

import net.andrew_coursin.magical_staffs.event.ModEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TimerListener implements ContainerListener {
    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
        ModEvents.addIfTimedItemStack(pContainerToSend, pDataSlotIndex);
        ModEvents.addIfTimedStaff(pContainerToSend, pDataSlotIndex);
    }

    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

    }
}
