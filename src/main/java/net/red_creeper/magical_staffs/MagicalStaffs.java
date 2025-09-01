package net.red_creeper.magical_staffs;

import com.mojang.logging.LogUtils;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.crafting.ModRecipeSerializers;
import net.red_creeper.magical_staffs.effect.ModEffects;
import net.red_creeper.magical_staffs.item.ModItems;
import net.red_creeper.magical_staffs.loot.ModLootModifiers;
import net.red_creeper.magical_staffs.networking.ModPacketHandler;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(MagicalStaffs.MOD_ID)
public class MagicalStaffs
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "magical_staffs";

    public MagicalStaffs(FMLJavaModLoadingContext context)
    {
        BusGroup modBusGroup = context.getModBusGroup();

        // Register mod registries
        ModDataComponents.register(modBusGroup);
        ModEffects.register(modBusGroup);
        ModItems.register(modBusGroup);
        ModLootModifiers.register(modBusGroup);
        ModRecipeSerializers.register(modBusGroup);

        // Register the commonSetup method for mod loading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(MagicalStaffs::commonSetup);

        // Register ourselves for server and other game events we are interested in
        BusGroup.DEFAULT.register(MethodHandles.lookup(), MagicalStaffs.class);

        // Register the mod items to a creative tab
        BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(ModItems::addCreative);

        ServerStartingEvent.BUS.addListener(MagicalStaffs::onServerStarting);
    }

    @SubscribeEvent
    private static void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        event.enqueueWork(ModPacketHandler::register);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}