package net.andrew_coursin.magical_staffs.networking;

import net.andrew_coursin.magical_staffs.components.ModDataComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.networking.packet.AddTimedEnchantmentsTooltipsS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class ModClientPacketHandler {
    public static void handleAddTimedEnchantmentTooltips(AddTimedEnchantmentsTooltipsS2CPacket packet) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        AbstractContainerMenu abstractContainerMenu = player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu ? player.inventoryMenu : player.containerMenu;
        TimedEnchantments timedEnchantments = abstractContainerMenu.getItems().get(packet.getSlot()).get(ModDataComponents.TIMED_ENCHANTMENTS.get());
        if (timedEnchantments == null) return;
        timedEnchantments.deserializeDurations(packet.getList());
    }
}
