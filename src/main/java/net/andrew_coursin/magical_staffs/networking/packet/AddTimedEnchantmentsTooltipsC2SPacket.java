package net.andrew_coursin.magical_staffs.networking.packet;

import net.andrew_coursin.magical_staffs.components.ModDataComponents;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.networking.ModPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class AddTimedEnchantmentsTooltipsC2SPacket {
    private final int slot;

    public AddTimedEnchantmentsTooltipsC2SPacket(FriendlyByteBuf buffer) {
        this.slot = buffer.readByte();
    }

    public AddTimedEnchantmentsTooltipsC2SPacket(int slot) {
        this.slot = slot;
    }

    public void handle(CustomPayloadEvent.Context context) {
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            TimedEnchantments timedEnchantments = player.containerMenu.getItems().get(this.slot).get(ModDataComponents.TIMED_ENCHANTMENTS.get());
            if (timedEnchantments == null) return;
            ModPacketHandler.sendToPlayer(new AddTimedEnchantmentsTooltipsS2CPacket(this.slot, timedEnchantments.serializeDurations()), player);
        });
        context.setPacketHandled(true);
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeByte(this.slot);
    }
}
