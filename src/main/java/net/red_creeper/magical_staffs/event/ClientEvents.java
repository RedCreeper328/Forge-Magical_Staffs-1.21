package net.red_creeper.magical_staffs.event;

import net.red_creeper.magical_staffs.MagicalStaffs;
import net.red_creeper.magical_staffs.util.ModKeyBindings;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = MagicalStaffs.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            MagicalStaffs.LOGGER.info("HELLO FROM CLIENT SETUP");
            MagicalStaffs.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }

        @SubscribeEvent
        public static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(ModKeyBindings.CYCLE_EFFECTS_FORWARD);
            event.register(ModKeyBindings.CYCLE_EFFECTS_BACKWARD);
            event.register(ModKeyBindings.CYCLE_EFFECTS_INCREASE);
            event.register(ModKeyBindings.CYCLE_EFFECTS_DECREASE);
        }
    }
}
