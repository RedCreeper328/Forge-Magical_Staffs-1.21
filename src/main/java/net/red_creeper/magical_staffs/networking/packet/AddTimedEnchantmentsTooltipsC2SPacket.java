package net.red_creeper.magical_staffs.networking.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.network.simple.handler.SimplePacket;
import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.red_creeper.magical_staffs.networking.ModPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;

public record AddTimedEnchantmentsTooltipsC2SPacket(int slot) implements SimplePacket<CustomPayloadEvent.Context> {
    public static final StreamCodec<RegistryFriendlyByteBuf, AddTimedEnchantmentsTooltipsC2SPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            AddTimedEnchantmentsTooltipsC2SPacket::slot,
            AddTimedEnchantmentsTooltipsC2SPacket::new
    );

    @Override
    public boolean handle(CustomPayloadEvent.Context handler, CustomPayloadEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player == null) return true;
        TimedEnchantments timedEnchantments = player.containerMenu.getItems().get(this.slot).get(ModDataComponents.TIMED_ENCHANTMENTS.get());
        if (timedEnchantments == null) return true;
        ModPacketHandler.sendToPlayer(new AddTimedEnchantmentsTooltipsS2CPacket(this.slot, timedEnchantments.serializeDurations()), player);
        return true;
    }
}
