package net.andrew_coursin.magical_staffs.components;

import net.andrew_coursin.magical_staffs.components.forge_material.ForgeMaterial;
import net.andrew_coursin.magical_staffs.components.stored_staff_effects.StoredStaffEffects;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.andrew_coursin.magical_staffs.components.timer.Timer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.UnaryOperator;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModComponents {
    public static final DeferredRegister<DataComponentType<?>> MOD_COMPONENTS = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE.key(), MOD_ID);

    public static final RegistryObject<DataComponentType<ForgeMaterial>> FORGE_MATERIAL = register("forge_material", builder -> builder.persistent(ForgeMaterial.CODEC));

    public static final RegistryObject<DataComponentType<StoredStaffEffects>> STORED_STAFF_EFFECTS = register("stored_staff_effects", builder -> builder.persistent(StoredStaffEffects.CODEC).networkSynchronized(StoredStaffEffects.STREAM_CODEC));

    public static final RegistryObject<DataComponentType<TimedEnchantments>> TIMED_ENCHANTMENTS = register("timed_enchantments", builder -> builder.persistent(TimedEnchantments.CODEC).networkSynchronized(TimedEnchantments.STREAM_CODEC));

    public static final RegistryObject<DataComponentType<Timer>> TIMER = register("timer", builder -> builder.persistent(Timer.CODEC).networkSynchronized(Timer.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        MOD_COMPONENTS.register(eventBus);
    }

    public static <T> RegistryObject<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return MOD_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }
}
