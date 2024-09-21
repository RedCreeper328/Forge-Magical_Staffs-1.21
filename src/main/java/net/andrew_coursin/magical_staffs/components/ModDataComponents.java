package net.andrew_coursin.magical_staffs.components;

import com.mojang.serialization.Codec;
import net.andrew_coursin.magical_staffs.components.forge_material.ForgeMaterial;
import net.andrew_coursin.magical_staffs.components.staff_modes.StaffModes;
import net.andrew_coursin.magical_staffs.components.stored_staff_effects.StoredStaffEffects;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantments;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.UnaryOperator;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModDataComponents {
    private static final DeferredRegister<DataComponentType<?>> MOD_DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final RegistryObject<DataComponentType<ForgeMaterial>> FORGE_MATERIAL = register("forge_material", builder -> builder.persistent(ForgeMaterial.CODEC));

    public static final RegistryObject<DataComponentType<StaffModes>> STAFF_MODES = register("staff_modes", builder -> builder.persistent(StaffModes.CODEC));

    public static final RegistryObject<DataComponentType<StoredStaffEffects>> STORED_STAFF_EFFECTS = register("stored_staff_effects", builder -> builder.persistent(StoredStaffEffects.CODEC).networkSynchronized(StoredStaffEffects.STREAM_CODEC));

    public static final RegistryObject<DataComponentType<Integer>> STAFF_TIMER = register("staff_timer", builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final RegistryObject<DataComponentType<TimedEnchantments>> TIMED_ENCHANTMENTS = register("timed_enchantments", builder -> builder.persistent(TimedEnchantments.CODEC).networkSynchronized(TimedEnchantments.STREAM_CODEC));

    private static <T> RegistryObject<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return MOD_DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    public static void register(IEventBus eventBus) {
        MOD_DATA_COMPONENTS.register(eventBus);
    }
}
