package net.red_creeper.magical_staffs.networking.packet;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.simple.handler.SimplePacket;
import net.red_creeper.magical_staffs.networking.ModClientPacketHandler;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.util.List;

public record AddTimedEnchantmentsTooltipsS2CPacket(int slot, List<Integer> list) implements SimplePacket<CustomPayloadEvent.Context> {
    public static final StreamCodec<RegistryFriendlyByteBuf, AddTimedEnchantmentsTooltipsS2CPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            packet -> packet.slot,
            ByteBufCodecs.fromCodec(Codec.INT.listOf()),
            packet -> packet.list,
            AddTimedEnchantmentsTooltipsS2CPacket::new
    );

    @Override
    public boolean handle(CustomPayloadEvent.Context handler, CustomPayloadEvent.Context context) {
        if (FMLEnvironment.dist.isClient()) {
            ModClientPacketHandler.handleAddTimedEnchantmentTooltips(this);
        }
        return true;
    }
}
