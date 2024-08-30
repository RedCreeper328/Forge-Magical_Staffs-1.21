package net.andrew_coursin.magical_staffs.inventory;

import net.andrew_coursin.magical_staffs.components.ModComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.event.ModEvents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TimedEnchantmentsListener implements ContainerListener {
    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
        ItemStack itemStack = pContainerToSend.getItems().get(pDataSlotIndex);
        if (!itemStack.getOrDefault(ModComponents.TIMED_ENCHANTMENTS.get(), TimedEnchantments.EMPTY).isEmpty() && !ModEvents.TIMED_ITEM_STACKS.contains(itemStack)) {
            ModEvents.TIMED_ITEM_STACKS.add(itemStack);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

    }
}
