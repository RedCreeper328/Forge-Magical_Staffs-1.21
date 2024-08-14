package net.andrew_coursin.magical_staffs.capability.attack_effects;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

public class AttackEffectsCapabilityImplementation implements AttackEffectsCapabilityInterface {
    private final List<MobEffectInstance> effects = new ArrayList<>();

    @Override
    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    @Override
    public ListTag serializeNBT(HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();
        effects.forEach(effect -> listTag.add(effect.save()));
        return listTag;
    }

    @Override
    public void addEffect(MobEffectInstance mobEffectInstance) {
        this.effects.add(mobEffectInstance);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, ListTag nbt) {
        nbt.forEach(tag -> effects.add(MobEffectInstance.load((CompoundTag) tag)));
    }

    @Override
    public void removeEffect(MobEffect mobEffect) {
        this.effects.removeIf(mobEffectInstance -> mobEffectInstance.getEffect() == mobEffect);
    }
}
