package net.andrew_coursin.magical_staffs.capability.attack_effects;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class AttackEffectsCapabilityProvider implements ICapabilitySerializable<ListTag> {
    private AttackEffectsCapabilityInterface attackMobEffectInstances = null;
    private final LazyOptional<AttackEffectsCapabilityInterface> optional = LazyOptional.of(this::createAttackMobEffectInstances);
    public static Capability<AttackEffectsCapabilityInterface> ATTACK_EFFECTS = CapabilityManager.get(new CapabilityToken<>() {});
    public static ResourceLocation KEY = ResourceLocation.fromNamespaceAndPath(MOD_ID, "attack_mob_effect_instances");

    private AttackEffectsCapabilityInterface createAttackMobEffectInstances() {
        if (this.attackMobEffectInstances == null) {
            this.attackMobEffectInstances = new AttackEffectsCapabilityImplementation();
        }

        return this.attackMobEffectInstances;
    }

    public void invalidateCaps() {
        optional.invalidate();
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        return createAttackMobEffectInstances().serializeNBT(provider);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
        createAttackMobEffectInstances().deserializeNBT(provider, nbt);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ATTACK_EFFECTS) {
            return optional.cast();
        }

        return LazyOptional.empty();
    }
}
