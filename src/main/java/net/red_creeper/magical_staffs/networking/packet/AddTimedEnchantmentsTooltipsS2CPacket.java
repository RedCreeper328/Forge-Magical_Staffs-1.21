package net.red_creeper.magical_staffs.networking.packet;

import net.red_creeper.magical_staffs.networking.ModClientPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;
import java.util.List;

public class AddTimedEnchantmentsTooltipsS2CPacket {
    private final List<Integer> list;
    private final int slot;

    public AddTimedEnchantmentsTooltipsS2CPacket(int slot, List<Integer> list) {
        this.list = list;
        this.slot = slot;
    }

    public AddTimedEnchantmentsTooltipsS2CPacket(FriendlyByteBuf buffer) {
        this.list = buffer.readCollection(ArrayList::new, FriendlyByteBuf::readInt);
        this.slot = buffer.readByte();
    }

    public List<Integer> getList() {
        return this.list;
    }

    public int getSlot() {
        return this.slot;
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            // Make sure it's only executed on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ModClientPacketHandler.handleAddTimedEnchantmentTooltips(this)
            );
        });
        context.setPacketHandled(true);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeCollection(this.list, FriendlyByteBuf::writeInt);
        buffer.writeByte(this.slot);
    }
}
