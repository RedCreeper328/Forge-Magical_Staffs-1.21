package net.red_creeper.magical_staffs.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import static net.red_creeper.magical_staffs.MagicalStaffs.MOD_ID;

public class ModEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
    private static final RegistryObject<MobEffect> ATTACK_BLINDNESS = MOB_EFFECTS.register("attack_blindness", () -> new AttackMobEffect(MobEffects.BLINDNESS, MobEffects.BLINDNESS.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_DARKNESS = MOB_EFFECTS.register("attack_darkness", () -> new AttackMobEffect(MobEffects.DARKNESS, MobEffects.DARKNESS.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_GLOWING = MOB_EFFECTS.register("attack_glowing", () -> new AttackMobEffect(MobEffects.GLOWING, MobEffects.GLOWING.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_HUNGER = MOB_EFFECTS.register("attack_hunger", () -> new AttackMobEffect(MobEffects.HUNGER, MobEffects.HUNGER.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_INFESTED = MOB_EFFECTS.register("attack_infested", () -> new AttackMobEffect(MobEffects.INFESTED, MobEffects.INFESTED.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_INSTANT_DAMAGE = MOB_EFFECTS.register("attack_instant_damage", () -> new AttackMobEffect(MobEffects.INSTANT_DAMAGE, MobEffects.INSTANT_DAMAGE.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_LEVITATION = MOB_EFFECTS.register("attack_levitation", () -> new AttackMobEffect(MobEffects.LEVITATION, MobEffects.LEVITATION.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_MINING_FATIGUE = MOB_EFFECTS.register("attack_mining_fatigue", () -> new AttackMobEffect(MobEffects.MINING_FATIGUE, MobEffects.MINING_FATIGUE.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_NAUSEA = MOB_EFFECTS.register("attack_nausea", () -> new AttackMobEffect(MobEffects.NAUSEA, MobEffects.NAUSEA.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_OOZING = MOB_EFFECTS.register("attack_oozing", () -> new AttackMobEffect(MobEffects.OOZING, MobEffects.OOZING.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_POISON = MOB_EFFECTS.register("attack_poison", () -> new AttackMobEffect(MobEffects.POISON, MobEffects.POISON.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_SLOWNESS = MOB_EFFECTS.register("attack_slowness", () -> new AttackMobEffect(MobEffects.SLOWNESS, MobEffects.SLOWNESS.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_UNLUCK = MOB_EFFECTS.register("attack_unluck", () -> new AttackMobEffect(MobEffects.UNLUCK, MobEffects.UNLUCK.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_WEAKNESS = MOB_EFFECTS.register("attack_weakness", () -> new AttackMobEffect(MobEffects.WEAKNESS, MobEffects.WEAKNESS.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_WEAVING = MOB_EFFECTS.register("attack_weaving", () -> new AttackMobEffect(MobEffects.WEAVING, MobEffects.WEAVING.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_WIND_CHARGED = MOB_EFFECTS.register("attack_wind_charged", () -> new AttackMobEffect(MobEffects.WIND_CHARGED, MobEffects.WIND_CHARGED.get().getColor()));
    private static final RegistryObject<MobEffect> ATTACK_WITHER = MOB_EFFECTS.register("attack_wither", () -> new AttackMobEffect(MobEffects.WITHER, MobEffects.WITHER.get().getColor()));

    public static @Nullable Holder<MobEffect> getAttackEffect(Holder<MobEffect> mobEffect) {
        for (RegistryObject<MobEffect> mobEffectRegistryObject : MOB_EFFECTS.getEntries()) {
            if (((AttackMobEffect) mobEffectRegistryObject.get()).getAppliedEffect() == mobEffect) {
                return mobEffectRegistryObject.getHolder().orElse(null);
            }
        }

        return null;
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
