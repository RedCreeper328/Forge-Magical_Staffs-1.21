package net.andrew_coursin.magical_staffs.components.timed_enchantments;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.andrew_coursin.magical_staffs.level.TimedEnchantmentSavedData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantments {
    public static final TimedEnchantments EMPTY = new TimedEnchantments(new HashMap<>());
    public static final Codec<TimedEnchantments> CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, TimedEnchantments> STREAM_CODEC;
    private static final Codec<Map<Integer, TimedEnchantment>> MAP_CODEC;
    private final Map<Integer, TimedEnchantment> timedEnchantments;

    private TimedEnchantments(Map<Integer, TimedEnchantment> timedEnchantments) {
        this.timedEnchantments = timedEnchantments;
    }

    public TimedEnchantments add(int id, TimedEnchantment timedEnchantment) {
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new HashMap<>(this.timedEnchantments));
        newTimedEnchantments.timedEnchantments.put(id, timedEnchantment);
        return newTimedEnchantments;
    }

    public boolean isEmpty() {
        return this.timedEnchantments.isEmpty();
    }

    public TimedEnchantments remove(int id) {
        TimedEnchantments newTimedEnchantments = new TimedEnchantments(new HashMap<>(this.timedEnchantments));
        newTimedEnchantments.timedEnchantments.remove(id);
        if (newTimedEnchantments.isEmpty()) return EMPTY;
        return newTimedEnchantments;
    }

    public void deserializeDurations(List<Integer> durations) {
        if (this.timedEnchantments.size() != durations.size()) return;

        for (int i = 0; i < durations.size(); i++) {
            this.timedEnchantments.values().stream().toList().get(i).setDuration(durations.get(i));
        }
    }

    public List<Integer> serializeDurations() {
        List<Integer> list = new ArrayList<>();
        this.timedEnchantments.forEach((id, timedEnchantment) -> list.add(TimedEnchantmentSavedData.get(id).getDuration()));
        return list;
    }

    static {
        MAP_CODEC = Codec.pair(Codec.INT.fieldOf("id").codec(), TimedEnchantment.CODEC.fieldOf("timed_enchantment").codec()).listOf().xmap(
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

        CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                MAP_CODEC.fieldOf("timed_enchantments").forGetter(timedEnchantments -> timedEnchantments.timedEnchantments)
            ).apply(instance, TimedEnchantments::new)
        );

        STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.VAR_INT, TimedEnchantment.STREAM_CODEC),
            timedEnchantments -> timedEnchantments.timedEnchantments,
            TimedEnchantments::new
        );
    }

    public void forEach(BiConsumer<Integer, TimedEnchantment> action) {
        this.timedEnchantments.forEach(action);
    }

    public boolean has(int id) {
        return this.timedEnchantments.containsKey(id);
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
