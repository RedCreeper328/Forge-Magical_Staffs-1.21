//package net.andrew_coursin.magical_staffs.networking.packet;
//
//import net.andrew_coursin.magical_staffs.capability.timed_enchantment.TimedEnchantmentsCapabilityProvider;
//import net.andrew_coursin.magical_staffs.networking.ModPacketHandler;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class AddTimedEnchantmentsTooltipsC2SPacket {
//    private final int slot;
//
//    public AddTimedEnchantmentsTooltipsC2SPacket(FriendlyByteBuf pBuffer) {
//        this.slot = pBuffer.readByte();
//    }
//
//    public AddTimedEnchantmentsTooltipsC2SPacket(int pSlot) {
//        this.slot = pSlot;
//    }
//
//    public void handle(Supplier<NetworkEvent.Context> supplier) {
//        NetworkEvent.Context context = supplier.get();
//        context.enqueueWork(() -> {
//            ServerPlayer player = context.getSender();
//            if (player == null) return;
//            player.containerMenu.getItems().get(this.slot).getCapability(TimedEnchantmentsCapabilityProvider.TIMED_ENCHANTMENTS).ifPresent(
//                timedEnchantments -> ModPacketHandler.sendToPlayer(new AddTimedEnchantmentsTooltipsS2CPacket(timedEnchantments.serializeDurations(), this.slot), player)
//            );
//        });
//        context.setPacketHandled(true);
//    }
//
//    public void toBytes(FriendlyByteBuf pBuffer) {
//        pBuffer.writeByte(this.slot);
//    }
//}
