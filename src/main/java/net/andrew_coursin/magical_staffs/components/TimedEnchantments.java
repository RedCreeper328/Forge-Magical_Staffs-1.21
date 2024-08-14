package net.andrew_coursin.magical_staffs.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.TimedEnchantment;

import java.util.ArrayList;
import java.util.List;

public class TimedEnchantments {
    public static final TimedEnchantments EMPTY = new TimedEnchantments(new ArrayList<>());
    public static final Codec<TimedEnchantments> CODEC;
    private static final Codec<List<TimedEnchantment>> LIST_CODEC;
    private final List<TimedEnchantment> timedEnchantments;

    TimedEnchantments(List<TimedEnchantment> timedEnchantments) {
        this.timedEnchantments = timedEnchantments;
    }

    public void add(TimedEnchantment timedEnchantment) {
        this.timedEnchantments.add(timedEnchantment);
    }

    public void remove(TimedEnchantment timedEnchantment) {
        this.timedEnchantments.remove(timedEnchantment);
    }

    static {
        LIST_CODEC = TimedEnchantment.CODEC.listOf();

        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                LIST_CODEC.fieldOf("timed_enchantments").forGetter(timedEnchantments -> timedEnchantments.timedEnchantments)
            ).apply(instance, TimedEnchantments::new)
        );
    }
}
