package net.red_creeper.magical_staffs.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.network.simple.handler.SimplePacket;
import net.red_creeper.magical_staffs.item.custom.StaffItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record StaffItemKeyBindC2SPacket(KEY_BINDS keyBind) implements SimplePacket<CustomPayloadEvent.Context> {
    public enum KEY_BINDS {
        CYCLE_FORWARD,
        CYCLE_BACKWARD,
        CYCLE_INCREASE,
        CYCLE_DECREASE,
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, StaffItemKeyBindC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT.map(ordinal -> KEY_BINDS.values()[ordinal], Enum::ordinal),
            StaffItemKeyBindC2SPacket::keyBind,
            StaffItemKeyBindC2SPacket::new
    );

    @Override
    public boolean handle(CustomPayloadEvent.Context handler, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return true;
        if (player.getMainHandItem().getItem() instanceof StaffItem staffItem) {
            ItemStack staffItemStack = player.getMainHandItem();
            staffItem.useKeyBind(player.getOffhandItem(), staffItemStack, player, this.keyBind);
        } else if (player.getOffhandItem().getItem() instanceof StaffItem staffItem) {
            ItemStack staffItemStack = player.getOffhandItem();
            staffItem.useKeyBind(player.getMainHandItem(), staffItemStack, player, this.keyBind);
        }
        return true;
    }
}
