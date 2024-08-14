//package net.andrew_coursin.magical_staffs.networking.packet;
//
//import net.andrew_coursin.magical_staffs.networking.ModClientPacketHandler;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.fml.DistExecutor;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class AddTimedEnchantmentsTooltipsS2CPacket {
//    private final CompoundTag compoundTag;
//    private final int slot;
//
//    public AddTimedEnchantmentsTooltipsS2CPacket(CompoundTag pCompoundTag, int pSlot) {
//        this.compoundTag = pCompoundTag;
//        this.slot = pSlot;
//    }
//
//    public AddTimedEnchantmentsTooltipsS2CPacket(FriendlyByteBuf pBuffer) {
//        this.compoundTag = pBuffer.readNbt();
//        this.slot = pBuffer.readByte();
//    }
//
//    public CompoundTag getCompoundTag() {
//        return this.compoundTag;
//    }
//
//    public int getSlot() {
//        return this.slot;
//    }
//
//    public void handle(Supplier<NetworkEvent.Context> supplier) {
//        NetworkEvent.Context context = supplier.get();
//        context.enqueueWork(() -> {
//            // Make sure it's only executed on the physical client
//            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
//                () -> () -> ModClientPacketHandler.handleAddTimedEnchantmentTooltips(this)
//            );
//        });
//        context.setPacketHandled(true);
//    }
//
//    public void toBytes(FriendlyByteBuf pBuffer) {
//        pBuffer.writeNbt(this.compoundTag);
//        pBuffer.writeByte(this.slot);
//    }
//}
