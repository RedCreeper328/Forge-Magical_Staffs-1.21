package net.andrew_coursin.magical_staffs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.level.TimedEnchantmentSavedData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(modid = MagicalStaffs.MOD_ID)
public class TimedEnchantment {
    private final Holder<Enchantment> enchantment;
    private final int duration;
    private final int id;
    private final int level;
    private static int MAX_ID = 0;
    public static final TimedEnchantmentSavedData TIMED_ENCHANTMENT_SAVED_DATA = new TimedEnchantmentSavedData();
    public static final Codec<TimedEnchantment> CODEC;

    public TimedEnchantment(Holder<Enchantment> pEnchantment, int pDuration, int pLevel) {
        this.duration = pDuration;
        this.enchantment = pEnchantment;
        this.level = pLevel;
        this.id = MAX_ID++;
        TIMED_ENCHANTMENT_SAVED_DATA.add(this);
    }

    private TimedEnchantment(Holder<Enchantment> pEnchantment, int pDuration, int pLevel, int pId) {
        this.duration = pDuration;
        this.enchantment = pEnchantment;
        this.level = pLevel;
        this.id = pId;
        MAX_ID = Math.max(MAX_ID, pId);
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putInt("duration", this.duration);
        compoundTag.putString("enchantment", this.enchantment.getRegisteredName());
        compoundTag.putInt("lvl", this.level);
        compoundTag.putInt("id", this.id);
        return compoundTag;
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getId() {
        return this.id;
    }

    public int getLevel() {
        return this.level;
    }

    static {
        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Enchantment.CODEC.fieldOf("enchantment").forGetter(timedEnchantment -> timedEnchantment.enchantment),
                Codec.INT.fieldOf("duration").forGetter(timedEnchantment -> timedEnchantment.duration),
                Codec.intRange(0, 255).fieldOf("level").forGetter(timedEnchantment -> timedEnchantment.level),
                Codec.INT.fieldOf("id").forGetter(timedEnchantment -> timedEnchantment.id)
            ).apply(instance, TimedEnchantment::new)
        );
    }
}
