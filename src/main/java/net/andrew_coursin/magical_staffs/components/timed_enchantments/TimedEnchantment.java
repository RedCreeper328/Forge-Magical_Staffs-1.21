package net.andrew_coursin.magical_staffs.components.timed_enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.event.TimedEnchantmentEndEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantment {
    private final Holder<Enchantment> enchantment;
    private int duration;
    private final int id;
    private final int level;
    private static int MAX_ID = 0;
    public static final Codec<TimedEnchantment> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantment> STREAM_CODEC;

    public TimedEnchantment(Holder<Enchantment> pEnchantment, int pDuration, int pLevel) {
        this(pEnchantment, pDuration, pLevel, ++MAX_ID);
    }

    private TimedEnchantment(Holder<Enchantment> pEnchantment, int pDuration, int pLevel, int pId) {
        this.duration = pDuration;
        this.enchantment = pEnchantment;
        this.level = pLevel;
        this.id = pId;
        MAX_ID = Math.max(MAX_ID, pId);
        MinecraftForge.EVENT_BUS.register(this);
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

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof TimedEnchantment timedEnchantment && this.id == timedEnchantment.id;
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        this.duration--;

        if (this.duration > 0) return;

        MinecraftForge.EVENT_BUS.post(new TimedEnchantmentEndEvent(this.id));
        MinecraftForge.EVENT_BUS.unregister(this);
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

        STREAM_CODEC = StreamCodec.composite(
                Enchantment.STREAM_CODEC,
                timedEnchantment -> timedEnchantment.enchantment,
                ByteBufCodecs.VAR_INT,
                timedEnchantment -> timedEnchantment.duration,
                ByteBufCodecs.VAR_INT,
                timedEnchantment -> timedEnchantment.level,
                ByteBufCodecs.VAR_INT,
                timedEnchantment -> timedEnchantment.id,
                TimedEnchantment::new
        );
    }
}
