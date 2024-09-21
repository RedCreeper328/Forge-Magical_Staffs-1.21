package net.red_creeper.magical_staffs.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AttackMobEffect extends MobEffect {
    private final Holder<MobEffect> appliedEffect;

    protected AttackMobEffect(Holder<MobEffect> pAppliedEffect, int pColor) {
        super(MobEffectCategory.BENEFICIAL, pColor);
        this.appliedEffect = pAppliedEffect;
    }

    public Holder<MobEffect> getAppliedEffect() {
        return appliedEffect;
    }
}
