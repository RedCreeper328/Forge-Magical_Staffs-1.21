package net.andrew_coursin.magical_staffs.components;

import net.andrew_coursin.magical_staffs.item.forge_material.ForgeMaterial;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.UnaryOperator;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> MOD_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), MOD_ID);

    public static final RegistryObject<DataComponentType<ForgeMaterial>> FORGE_MATERIAL = MOD_COMPONENTS.register("forge_material", () -> new DataComponentType.Builder<ForgeMaterial>().persistent(ForgeMaterial.CODEC).build());

    public static final RegistryObject<DataComponentType<StoredStaffEffects>> STORED_STAFF_EFFECTS = MOD_COMPONENTS.register("stored_staff_effects", () -> new DataComponentType.Builder<StoredStaffEffects>().persistent(StoredStaffEffects.CODEC).build());

    public static final RegistryObject<DataComponentType<TimedEnchantments>> TIMED_ENCHANTMENTS = MOD_COMPONENTS.register("timed_enchantments", () -> new DataComponentType.Builder<TimedEnchantments>().persistent(TimedEnchantments.CODEC).build());

    public static void register(IEventBus eventBus) {
        MOD_COMPONENTS.register(eventBus);
    }
}
