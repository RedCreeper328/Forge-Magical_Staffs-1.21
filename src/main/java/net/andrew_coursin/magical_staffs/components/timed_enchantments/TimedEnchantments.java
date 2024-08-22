package net.andrew_coursin.magical_staffs.components.timed_enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.event.TimedEnchantmentEndEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
public class TimedEnchantments {
    public static final TimedEnchantments EMPTY = new TimedEnchantments(new ArrayList<>());
    public static final Codec<TimedEnchantments> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantments> STREAM_CODEC;
    private static final Codec<List<TimedEnchantment>> LIST_CODEC;
    private final List<TimedEnchantment> timedEnchantments;

    TimedEnchantments(List<TimedEnchantment> timedEnchantments) {
        this.timedEnchantments = timedEnchantments;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void add(TimedEnchantment timedEnchantment) {
        this.timedEnchantments.add(timedEnchantment);
    }

    @SubscribeEvent
    public void remove(TimedEnchantmentEndEvent event) {
        this.timedEnchantments.removeIf(timedEnchantment -> timedEnchantment.getId() == event.getId());
    }

    static {
        LIST_CODEC = TimedEnchantment.CODEC.listOf().xmap(ArrayList::new, Function.identity());

        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                LIST_CODEC.fieldOf("timed_enchantments").forGetter(timedEnchantments -> timedEnchantments.timedEnchantments)
            ).apply(instance, TimedEnchantments::new)
        );

        STREAM_CODEC = StreamCodec.composite(
            TimedEnchantment.STREAM_CODEC.apply(ByteBufCodecs.list()),
            timedEnchantments -> timedEnchantments.timedEnchantments,
            TimedEnchantments::new
        );
    }

    public void forEach(Consumer<TimedEnchantment> action) {
        this.timedEnchantments.forEach(action);
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof TimedEnchantments otherTimedEnchantments && this.timedEnchantments.equals(otherTimedEnchantments.timedEnchantments);
        }
    }

    @Override
    public int hashCode() {
        return this.timedEnchantments.hashCode();
    }
}
