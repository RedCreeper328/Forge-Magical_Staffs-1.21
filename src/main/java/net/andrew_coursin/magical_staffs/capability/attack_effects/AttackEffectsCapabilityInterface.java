package net.andrew_coursin.magical_staffs.capability.attack_effects;

import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.List;

@AutoRegisterCapability
public interface AttackEffectsCapabilityInterface extends INBTSerializable<ListTag> {
    List<MobEffectInstance> getEffects();
    void addEffect(MobEffectInstance mobEffectInstance);
    void removeEffect(MobEffect mobEffect);
}
