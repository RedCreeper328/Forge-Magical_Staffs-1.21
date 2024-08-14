//package net.andrew_coursin.magical_staffs.networking;
//
//import net.andrew_coursin.magical_staffs.MagicalStaffs;
//import net.andrew_coursin.magical_staffs.networking.packet.*;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraftforge.network.NetworkDirection;
//import net.minecraftforge.network.NetworkRegistry;
//import net.minecraftforge.network.PacketDistributor;
//import net.minecraftforge.network.simple.SimpleChannel;
//
//public class ModPacketHandler {
//    private static int PACKET_ID = 0;
//    private static SimpleChannel INSTANCE;
//    private static final String PROTOCOL_VERSION = "1.0";
//
//    private static int id() {
//        return PACKET_ID++;
//    }
//
//    public static void register() {
//        INSTANCE = NetworkRegistry.newSimpleChannel(
//                new ResourceLocation(MagicalStaffs.MOD_ID, "messages"),
//                () -> PROTOCOL_VERSION,
//                pString -> true,
//                pString -> true);
//
//        INSTANCE.messageBuilder(AddTimedEnchantmentsTooltipsC2SPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
//                .decoder(AddTimedEnchantmentsTooltipsC2SPacket::new)
//                .encoder(AddTimedEnchantmentsTooltipsC2SPacket::toBytes)
//                .consumerMainThread(AddTimedEnchantmentsTooltipsC2SPacket::handle)
//                .add();
//
//        INSTANCE.messageBuilder(AddTimedEnchantmentsTooltipsS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
//                .decoder(AddTimedEnchantmentsTooltipsS2CPacket::new)
//                .encoder(AddTimedEnchantmentsTooltipsS2CPacket::toBytes)
//                .consumerMainThread(AddTimedEnchantmentsTooltipsS2CPacket::handle)
//                .add();
//
//        INSTANCE.messageBuilder(SetTimerS2CPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
//                .decoder(SetTimerS2CPacket::new)
//                .encoder(SetTimerS2CPacket::toBytes)
//                .consumerMainThread(SetTimerS2CPacket::handle)
//                .add();
//    }
//
//    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
//        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
//    }
//
//    public static <MSG> void sendToServer(MSG message) {
//        INSTANCE.sendToServer(message);
//    }
//}
