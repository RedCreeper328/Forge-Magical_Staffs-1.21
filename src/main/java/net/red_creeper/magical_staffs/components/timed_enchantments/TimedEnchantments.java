package net.red_creeper.magical_staffs.components.timed_enchantments;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.red_creeper.magical_staffs.level.TimerSavedData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantments {
    private final Map<Integer, TimedEnchantment> timedEnchantments;
    private final Map<Holder<Enchantment>, Integer> overflowEnchantments;
    private static final Codec<Map<Integer, TimedEnchantment>> TIMED_ENCHANTMENTS_MAP_CODEC = Codec.pair(Codec.INT.fieldOf("id").codec(), TimedEnchantment.CODEC.fieldOf("timed_enchantment").codec()).listOf().xmap(
    // Function to go from List<Pair<Integer, TimedEnchantment>> to Map<Integer, TimedEnchantment>
    (list) -> {
        Map<Integer, TimedEnchantment> map = new HashMap<>();
        list.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));
        return map;
    },
    // Function to go from Map<Integer, TimedEnchantment> to List<Pair<Integer, TimedEnchantment>>
    (map) -> {
        List<Pair<Integer, TimedEnchantment>> list = new ArrayList<>();
        map.forEach((id, timedEnchantment) -> list.add(new Pair<>(id, timedEnchantment)));
        return list;
    });

    private static final Codec<Map<Holder<Enchantment>, Integer>> OVERFLOW_ENCHANTMENTS_MAP_CODEC = Codec.pair(Enchantment.CODEC.fieldOf("enchantment").codec(), Codec.INT.fieldOf("overflow").codec()).listOf().xmap(
            // Function to go from List<Pair<Holder<Enchantment>, Integer>> to Map<Holder<Enchantment>, Integer>
            (list) -> {
                Map<Holder<Enchantment>, Integer> map = new HashMap<>();
                list.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));
                return map;
            },
            // Function to go from Map<Holder<Enchantment>, Integer> to List<Pair<Holder<Enchantment>, Integer>>
            (map) -> {
                List<Pair<Holder<Enchantment>, Integer>> list = new ArrayList<>();
                map.forEach((enchantment, overflow) -> list.add(new Pair<>(enchantment, overflow)));
                return list;
            });


    public static final Codec<TimedEnchantments> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TIMED_ENCHANTMENTS_MAP_CODEC.fieldOf("timed_enchantments").forGetter(timedEnchantments -> timedEnchantments.timedEnchantments),
            OVERFLOW_ENCHANTMENTS_MAP_CODEC.fieldOf("overflow_enchantments").forGetter(timedEnchantments -> timedEnchantments.overflowEnchantments)
    ).apply(instance, TimedEnchantments::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantments> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, TimedEnchantment.STREAM_CODEC),
            timedEnchantments -> timedEnchantments.timedEnchantments,
            ByteBufCodecs.map(HashMap::new, Enchantment.STREAM_CODEC, ByteBufCodecs.VAR_INT),
            timedEnchantments -> timedEnchantments.overflowEnchantments,
            TimedEnchantments::new
    );

    public static final TimedEnchantments EMPTY = new TimedEnchantments(new HashMap<>(), new HashMap<>());

    private TimedEnchantments(Map<Integer, TimedEnchantment> timedEnchantments, Map<Holder<Enchantment>, Integer> overflowEnchantments) {
        this.timedEnchantments = timedEnchantments;
        this.overflowEnchantments = overflowEnchantments;
    }

    public boolean has(int id) {
        return this.timedEnchantments.containsKey(id);
    }

    public boolean isEmpty() {
        return this.timedEnchantments.isEmpty();
    }

    public int getOverflow(Holder<Enchantment> enchantment) {
        return this.overflowEnchantments.getOrDefault(enchantment, 0);
    }

    public List<Integer> serializeDurations() {
        List<Integer> list = new ArrayList<>();
        this.timedEnchantments.forEach((id, timedEnchantment) -> list.add(TimerSavedData.getTimedEnchantment(id).getDuration()));
        return list;
    }

    public TimedEnchantments add(int overflow, int id, TimedEnchantment timedEnchantment) {
        // New HashMap instances are created so that the changing the newTimedEnchantments doesn't change the empty timedEnchantments
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new HashMap<>(this.timedEnchantments), new HashMap<>(this.overflowEnchantments));
        newTimedEnchantments.timedEnchantments.put(id, timedEnchantment);
        newTimedEnchantments.overflowEnchantments.put(timedEnchantment.getEnchantment(), getOverflow(timedEnchantment.getEnchantment()) + overflow);
        return newTimedEnchantments;
    }

    public TimedEnchantments remove(int id) {
        // New HashMap instances are created so that the changing the newTimedEnchantments doesn't change the empty timedEnchantments
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new HashMap<>(this.timedEnchantments), new HashMap<>(this.overflowEnchantments));
        TimedEnchantment timedEnchantment = newTimedEnchantments.timedEnchantments.remove(id);

        // Overflow cannot be below zero
        int overflow = Math.max(getOverflow(timedEnchantment.getEnchantment()) - timedEnchantment.getLevel(), 0);
        newTimedEnchantments.overflowEnchantments.put(timedEnchantment.getEnchantment(), overflow);
        if (newTimedEnchantments.isEmpty()) return EMPTY;
        return newTimedEnchantments;
    }

    public void deserializeDurations(List<Integer> durations) {
        if (this.timedEnchantments.size() != durations.size()) return;

        for (int i = 0; i < durations.size(); i++) {
            this.timedEnchantments.values().stream().toList().get(i).setDuration(durations.get(i));
        }
    }

    public void forEach(BiConsumer<Integer, TimedEnchantment> action) {
        this.timedEnchantments.forEach(action);
    }

    @Override
    public boolean equals(Object pOther) {
        if (this == pOther) {
            return true;
        } else {
            return pOther instanceof TimedEnchantments otherTimedEnchantments
                    && this.timedEnchantments.equals(otherTimedEnchantments.timedEnchantments);
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(timedEnchantments);
    }
}
