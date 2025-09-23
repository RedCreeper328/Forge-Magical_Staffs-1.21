package net.red_creeper.magical_staffs.datagen.loot;

import net.red_creeper.magical_staffs.datagen.ModGlobalLootModifiersProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;

import javax.annotation.ParametersAreNonnullByDefault;

import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class DataGenerators {
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> provider = event.getLookupProvider();

        generator.addProvider(event.includeServer(), new ModGlobalLootModifiersProvider(packOutput, provider));
    }
}
