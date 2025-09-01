package net.red_creeper.magical_staffs.networking;

import io.netty.util.AttributeKey;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.simple.handler.SimpleHandlerProtocol;
import net.minecraftforge.network.simple.handler.SimplePacket;
import net.red_creeper.magical_staffs.networking.packet.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

public class ModPacketHandler {
    private static SimpleChannel INSTANCE;

    public static void register() {
        INSTANCE = ChannelBuilder.named(MOD_ID).simpleChannel();

        AttributeKey<CustomPayloadEvent.Context> attributeKey = AttributeKey.newInstance(MOD_ID);

        SimpleHandlerProtocol<RegistryFriendlyByteBuf, SimplePacket<CustomPayloadEvent.Context>> simpleHandlerProtocol = INSTANCE.protocol(attributeKey , NetworkProtocol.PLAY);
        simpleHandlerProtocol.flow(PacketFlow.SERVERBOUND).addMain(AddTimedEnchantmentsTooltipsC2SPacket.class, AddTimedEnchantmentsTooltipsC2SPacket.STREAM_CODEC);
        simpleHandlerProtocol.flow(PacketFlow.CLIENTBOUND).addMain(AddTimedEnchantmentsTooltipsS2CPacket.class, AddTimedEnchantmentsTooltipsS2CPacket.STREAM_CODEC);
        simpleHandlerProtocol.flow(PacketFlow.SERVERBOUND).addMain(StaffItemKeyBindC2SPacket.class, StaffItemKeyBindC2SPacket.STREAM_CODEC);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }
}
