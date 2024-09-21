package net.andrew_coursin.magical_staffs.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class AttackMobEffectInstance extends MobEffectInstance {
    private MobEffectInstance appliedMobEffectInstance;

    public AttackMobEffectInstance(Holder<MobEffect> mobEffect, int amplifier, int duration) {
        super(mobEffect, duration, amplifier);

        if (mobEffect.get() instanceof AttackMobEffect attackMobEffect) this.appliedMobEffectInstance = new MobEffectInstance(attackMobEffect.getAppliedEffect(), duration / 20, amplifier);
    }

    public MobEffectInstance getAppliedMobEffectInstance() {
        return new MobEffectInstance(this.appliedMobEffectInstance);
    }
}
