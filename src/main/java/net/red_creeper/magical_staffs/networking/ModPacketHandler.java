package net.red_creeper.magical_staffs.networking;

import net.red_creeper.magical_staffs.MagicalStaffs;
import net.red_creeper.magical_staffs.networking.packet.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;

public class ModPacketHandler {
    private static int PACKET_ID = 0;
    private static SimpleChannel INSTANCE;

    private static int id() {
        return PACKET_ID++;
    }

    public static void register() {
        INSTANCE = ChannelBuilder.named(MagicalStaffs.MOD_ID).simpleChannel();

        INSTANCE.messageBuilder(AddTimedEnchantmentsTooltipsC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AddTimedEnchantmentsTooltipsC2SPacket::new)
                .encoder(AddTimedEnchantmentsTooltipsC2SPacket::toBytes)
                .consumerMainThread(AddTimedEnchantmentsTooltipsC2SPacket::handle)
                .add();

        INSTANCE.messageBuilder(AddTimedEnchantmentsTooltipsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AddTimedEnchantmentsTooltipsS2CPacket::new)
                .encoder(AddTimedEnchantmentsTooltipsS2CPacket::toBytes)
                .consumerMainThread(AddTimedEnchantmentsTooltipsS2CPacket::handle)
                .add();

        INSTANCE.messageBuilder(StaffItemKeyBindC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(StaffItemKeyBindC2SPacket::new)
                .encoder(StaffItemKeyBindC2SPacket::toBytes)
                .consumerMainThread(StaffItemKeyBindC2SPacket::handle)
                .add();
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(message, PacketDistributor.PLAYER.with(player));
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.send(message, PacketDistributor.SERVER.noArg());
    }
}
