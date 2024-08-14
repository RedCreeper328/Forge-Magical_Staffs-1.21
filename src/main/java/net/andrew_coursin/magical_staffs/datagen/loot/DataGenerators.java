package net.andrew_coursin.magical_staffs.datagen.loot;

import net.andrew_coursin.magical_staffs.datagen.ModGlobalLootModifiersProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.concurrent.CompletableFuture;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ParametersAreNonnullByDefault
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput, provider));
    }
}
