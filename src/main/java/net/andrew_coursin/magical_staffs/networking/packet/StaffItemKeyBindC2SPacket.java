package net.andrew_coursin.magical_staffs.networking.packet;

import net.andrew_coursin.magical_staffs.item.custom.StaffItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class StaffItemKeyBindC2SPacket {
    public enum KEY_BINDS {
        CYCLE_FORWARD,
        CYCLE_BACKWARD,
        CYCLE_INCREASE,
        CYCLE_DECREASE,
    }

    private final KEY_BINDS keyBind;

    public StaffItemKeyBindC2SPacket(KEY_BINDS keyBind) {
        this.keyBind = keyBind;
    }

    public StaffItemKeyBindC2SPacket(FriendlyByteBuf buffer) {
        this.keyBind = buffer.readEnum(KEY_BINDS.class);
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (player.getMainHandItem().getItem() instanceof StaffItem staffItem) staffItem.useKeyBind(player.getOffhandItem(), player.getMainHandItem(), player, this.keyBind);
            else if (player.getOffhandItem().getItem() instanceof StaffItem staffItem) staffItem.useKeyBind(player.getMainHandItem(), player.getOffhandItem(), player, this.keyBind);

        });
        context.setPacketHandled(true);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeEnum(this.keyBind);
    }
}
