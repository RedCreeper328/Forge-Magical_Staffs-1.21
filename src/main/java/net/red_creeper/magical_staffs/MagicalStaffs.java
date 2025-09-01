package net.red_creeper.magical_staffs;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.red_creeper.magical_staffs.components.ModDataComponents;
import net.red_creeper.magical_staffs.crafting.ModRecipeSerializers;
import net.red_creeper.magical_staffs.effect.ModEffects;
import net.red_creeper.magical_staffs.item.ModItems;
import net.red_creeper.magical_staffs.loot.ModLootModifiers;
import net.red_creeper.magical_staffs.networking.ModPacketHandler;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.red_creeper.magical_staffs.util.ModKeyBindings;
import org.slf4j.Logger;

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

        // Register the mod items to a creative tab
        BuildCreativeModeTabContentsEvent.getBus(modBusGroup).addListener(ModItems::addCreative);

        // Register the commonSetup method for mod loading
        FMLCommonSetupEvent.getBus(modBusGroup).addListener(MagicalStaffs::commonSetup);

        // Register the server and client setup methods
        ServerStartingEvent.BUS.addListener(MagicalStaffs::onServerStarting);
        FMLClientSetupEvent.getBus(modBusGroup).addListener(MagicalStaffs::onClientSetup);

        // Register the key binds on the client
        RegisterKeyMappingsEvent.getBus(modBusGroup).addListener(MagicalStaffs::onKeyRegister);
    }

    private static void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        // Register the ModPacketHandler on both the server and client
        event.enqueueWork(ModPacketHandler::register);
    }

    public static void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    public static void onClientSetup(FMLClientSetupEvent event)
    {
        // Some client setup code
        MagicalStaffs.LOGGER.info("HELLO FROM CLIENT SETUP");
        MagicalStaffs.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    public static void onKeyRegister(RegisterKeyMappingsEvent event) {
        event.register(ModKeyBindings.CYCLE_EFFECTS_FORWARD);
        event.register(ModKeyBindings.CYCLE_EFFECTS_BACKWARD);
        event.register(ModKeyBindings.CYCLE_EFFECTS_INCREASE);
        event.register(ModKeyBindings.CYCLE_EFFECTS_DECREASE);
    }
}