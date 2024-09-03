package net.andrew_coursin.magical_staffs.components.timed_enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantment {
    private final Holder<Enchantment> enchantment;
    private int duration;
    private final int level;
    public static final Codec<TimedEnchantment> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantment> STREAM_CODEC;

    public TimedEnchantment(Holder<Enchantment> pEnchantment, int pDuration, int pLevel) {
        this.duration = pDuration;
        this.enchantment = pEnchantment;
        this.level = pLevel;

        if (this.level < 0 || this.level > 255) {
            throw new IllegalArgumentException("Timed enchantment has invalid level " + this.level);
        }
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public int getDuration() {
        return this.duration;
    }

    public int getLevel() {
        return this.level;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof TimedEnchantment timedEnchantment && this.enchantment == timedEnchantment.enchantment && this.duration == timedEnchantment.duration && this.level == timedEnchantment.level;
        }
    }

    @Override
    public int hashCode() {
        int i = this.enchantment.hashCode();
        i = 31 * i + this.duration;
        i = 31 * i + this.level;
        return i;
    }

    public boolean tick() {
        this.duration--;
        return duration <= 0;
    }

    static {
        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                Enchantment.CODEC.fieldOf("enchantment").forGetter(timedEnchantment -> timedEnchantment.enchantment),
                Codec.INT.fieldOf("duration").forGetter(timedEnchantment -> timedEnchantment.duration),
                Codec.intRange(0, 255).fieldOf("level").forGetter(timedEnchantment -> timedEnchantment.level)
            ).apply(instance, TimedEnchantment::new)
        );

        STREAM_CODEC = StreamCodec.composite(
                Enchantment.STREAM_CODEC,
                timedEnchantment -> timedEnchantment.enchantment,
                ByteBufCodecs.VAR_INT,
                timedEnchantment -> timedEnchantment.duration,
                ByteBufCodecs.VAR_INT,
                timedEnchantment -> timedEnchantment.level,
                TimedEnchantment::new
        );
    }
}
