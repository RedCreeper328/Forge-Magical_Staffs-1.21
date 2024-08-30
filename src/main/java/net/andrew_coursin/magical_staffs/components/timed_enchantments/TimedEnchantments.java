package net.andrew_coursin.magical_staffs.components.timed_enchantments;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantments {
    public static final TimedEnchantments EMPTY = new TimedEnchantments(new ArrayList<>());
    public static final Codec<TimedEnchantments> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantments> STREAM_CODEC;
    private static final Codec<List<TimedEnchantment>> LIST_CODEC;
    private final List<TimedEnchantment> timedEnchantments = new ArrayList<>();

    private TimedEnchantments(List<TimedEnchantment> timedEnchantments) {
        for (TimedEnchantment timedEnchantment : timedEnchantments) {
            if (timedEnchantment.getDuration() > 0) {
                this.timedEnchantments.add(timedEnchantment);
            }
        }
    }

    public TimedEnchantments add(TimedEnchantment timedEnchantment) {
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new ArrayList<>(this.timedEnchantments));
        newTimedEnchantments.timedEnchantments.add(timedEnchantment);
        return newTimedEnchantments;
    }

    public boolean isEmpty() {
        return this.timedEnchantments.isEmpty();
    }

    public TimedEnchantments remove() {
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new ArrayList<>(this.timedEnchantments));
        newTimedEnchantments.timedEnchantments.removeIf(timedEnchantment -> timedEnchantment.getDuration() <= 0);
        if (newTimedEnchantments.isEmpty()) return EMPTY;
        return newTimedEnchantments;
    }

    public void deserializeDurations(List<Integer> durations) {
        if (this.timedEnchantments.size() != durations.size()) return;

        for (int i = 0; i < durations.size(); i++) {
            this.timedEnchantments.get(i).setDuration(durations.get(i));
        }
    }

    public List<Integer> serializeDurations() {
        List<Integer> list = new ArrayList<>();
        this.timedEnchantments.forEach(timedEnchantment -> list.add(timedEnchantment.getDuration()));
        return list;
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
