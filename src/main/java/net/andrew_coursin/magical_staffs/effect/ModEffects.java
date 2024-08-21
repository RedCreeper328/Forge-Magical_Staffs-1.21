package net.andrew_coursin.magical_staffs.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    public static final RegistryObject<MobEffect> ATTACK_BAD_OMEN = MOB_EFFECTS.register("attack_bad_omen", () -> new AttackMobEffect(MobEffects.BAD_OMEN, MobEffects.BAD_OMEN.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_BLINDNESS = MOB_EFFECTS.register("attack_blindness", () -> new AttackMobEffect(MobEffects.BLINDNESS, MobEffects.BLINDNESS.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_CONFUSION = MOB_EFFECTS.register("attack_nausea", () -> new AttackMobEffect(MobEffects.CONFUSION, MobEffects.CONFUSION.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_DARKNESS = MOB_EFFECTS.register("attack_darkness", () -> new AttackMobEffect(MobEffects.DARKNESS, MobEffects.DARKNESS.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_DIG_SLOWDOWN = MOB_EFFECTS.register("attack_mining_fatigue", () -> new AttackMobEffect(MobEffects.DIG_SLOWDOWN, MobEffects.DIG_SLOWDOWN.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_GLOWING = MOB_EFFECTS.register("attack_glowing", () -> new AttackMobEffect(MobEffects.GLOWING, MobEffects.GLOWING.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_HARM = MOB_EFFECTS.register("attack_instant_damage", () -> new AttackMobEffect(MobEffects.HARM, MobEffects.HARM.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_HUNGER = MOB_EFFECTS.register("attack_hunger", () -> new AttackMobEffect(MobEffects.HUNGER, MobEffects.HUNGER.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_LEVITATION = MOB_EFFECTS.register("attack_levitation", () -> new AttackMobEffect(MobEffects.LEVITATION, MobEffects.LEVITATION.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_MOVEMENT_SLOWDOWN = MOB_EFFECTS.register("attack_slowness", () -> new AttackMobEffect(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.MOVEMENT_SLOWDOWN.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_POISON = MOB_EFFECTS.register("attack_poison", () -> new AttackMobEffect(MobEffects.POISON, MobEffects.POISON.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_UNLUCK = MOB_EFFECTS.register("attack_unluck", () -> new AttackMobEffect(MobEffects.UNLUCK, MobEffects.UNLUCK.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_WEAKNESS = MOB_EFFECTS.register("attack_weakness", () -> new AttackMobEffect(MobEffects.WEAKNESS, MobEffects.WEAKNESS.get().getColor()));
    public static final RegistryObject<MobEffect> ATTACK_WITHER = MOB_EFFECTS.register("attack_wither", () -> new AttackMobEffect(MobEffects.WITHER, MobEffects.WITHER.get().getColor()));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

    public static @Nullable Holder<MobEffect> getAttackEffect(Holder<MobEffect> mobEffect) {
        for (RegistryObject<MobEffect> mobEffectRegistryObject : MOB_EFFECTS.getEntries()) {
            if (((AttackMobEffect) mobEffectRegistryObject.get()).getAppliedEffect() == mobEffect) {
                return mobEffectRegistryObject.getHolder().orElse(null);
            }
        }

        return null;
    }
}
