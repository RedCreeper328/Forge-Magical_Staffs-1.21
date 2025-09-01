package net.red_creeper.magical_staffs.crafting;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

public class ModRecipeSerializers {
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MOD_ID);

    public static final RegistryObject<RecipeSerializer<SmithingForgeRecipe>> SMITHING_FORGE = RECIPE_SERIALIZERS.register("smithing_forge", SmithingForgeRecipe.Serializer::new);

    public static void register(BusGroup busGroup) {
        RECIPE_SERIALIZERS.register(busGroup);
    }
}
