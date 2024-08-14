//package net.andrew_coursin.magical_staffs.networking;
//
//import net.andrew_coursin.magical_staffs.networking.packet.SetTimerS2CPacket;
//import net.andrew_coursin.magical_staffs.networking.packet.AddTimedEnchantmentsTooltipsS2CPacket;
//import net.andrew_coursin.magical_staffs.capability.timed_enchantment.TimedEnchantmentsCapabilityProvider;
//import net.andrew_coursin.magical_staffs.capability.timer.TimerCapabilityProvider;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
//import net.minecraft.client.player.LocalPlayer;
//import net.minecraft.world.inventory.AbstractContainerMenu;
//
//public class ModClientPacketHandler {
//    public static void handleAddTimedEnchantmentTooltips(AddTimedEnchantmentsTooltipsS2CPacket packet) {
//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player == null) return;
//        AbstractContainerMenu abstractContainerMenu = player.containerMenu instanceof CreativeModeInventoryScreen.ItemPickerMenu ? player.inventoryMenu : player.containerMenu;
//        abstractContainerMenu.getItems().get(packet.getSlot()).getCapability(TimedEnchantmentsCapabilityProvider.TIMED_ENCHANTMENTS).ifPresent(
//            timedEnchantments -> timedEnchantments.deserializeDurations(packet.getCompoundTag())
//        );
//    }
//
//    public static void handleSetTimer(SetTimerS2CPacket packet) {
//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player == null) return;
//        player.getInventory().getItem(packet.getSlot()).getCapability(TimerCapabilityProvider.TIMER).ifPresent(
//            timer -> timer.setTime(packet.getTime())
//        );
//    }
//}
