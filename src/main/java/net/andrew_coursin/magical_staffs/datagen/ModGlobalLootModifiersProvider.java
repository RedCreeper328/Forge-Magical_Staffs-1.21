package net.andrew_coursin.magical_staffs.datagen;

import net.andrew_coursin.magical_staffs.item.ModItems;
import net.andrew_coursin.magical_staffs.loot.AddItemModifier;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModGlobalLootModifiersProvider extends GlobalLootModifierProvider {
    public ModGlobalLootModifiersProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, MOD_ID, registries);
    }

    @Override
    protected void start(HolderLookup.@NotNull Provider provider) {
        add("forge_upgrade_smithing_template_from_end_city_treasure", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(ResourceLocation.parse("chests/end_city_treasure")).build(),
                LootItemRandomChanceCondition.randomChance(1.0F / 15.0F).build()
            }, ModItems.FORGE_UPGRADE_SMITHING_TEMPLATE.get())
        );
    }
}
