//package net.andrew_coursin.magical_staffs.networking.packet;
//
//import net.andrew_coursin.magical_staffs.networking.ModClientPacketHandler;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.fml.DistExecutor;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class SetTimerS2CPacket {
//    private final int slot;
//    private final int time;
//
//    public SetTimerS2CPacket(FriendlyByteBuf pBuffer) {
//        this.slot = pBuffer.readByte();
//        this.time = pBuffer.readVarInt();
//    }
//
//    public SetTimerS2CPacket(int slot, int time) {
//        this.slot = slot;
//        this.time = time;
//    }
//
//    public int getSlot() {
//        return this.slot;
//    }
//
//    public int getTime() {
//        return this.time;
//    }
//
//    public void handle(Supplier<NetworkEvent.Context> supplier) {
//        NetworkEvent.Context context = supplier.get();
//        context.enqueueWork(() -> {
//            // Make sure it's only executed on the physical client
//            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
//                    ModClientPacketHandler.handleSetTimer(this));
//        });
//        context.setPacketHandled(true);
//    }
//
//    public void toBytes(FriendlyByteBuf pBuffer) {
//        pBuffer.writeByte(this.slot);
//        pBuffer.writeVarInt(this.time);
//    }
//}
