package net.red_creeper.magical_staffs.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.red_creeper.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.red_creeper.magical_staffs.event.ModEvents;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimerSavedData extends SavedData {
    private int STAFF_TIMERS_MAX_ID = 0;
    private int TIMED_ENCHANTMENTS_MAX_ID = 0;
    private Map<Integer, Integer> staffTimers = new HashMap<>();
    private Map<Integer, TimedEnchantment> timedEnchantments = new HashMap<>();

    private static ServerLevel SERVER_LEVEL;

    private static final Codec<Map<Integer, Integer>> STAFF_TIMERS_MAP_CODEC = Codec.pair(Codec.INT.fieldOf("id").codec(), Codec.INT.fieldOf("time").codec()).listOf().xmap(
            // Function to go from List<Pair<Integer, StaffTimer>> to Map<Integer, StaffTimer>
            (list) -> {
                Map<Integer, Integer> map = new HashMap<>();
                list.forEach(pair -> map.put(pair.getFirst(), pair.getSecond()));
                return map;
            },
            // Function to go from Map<Integer, StaffTimer> to List<Pair<Integer, StaffTimer>>
            (map) -> {
                List<Pair<Integer, Integer>> list = new ArrayList<>();
                map.forEach((id, staffTimer) -> list.add(new Pair<>(id, staffTimer)));
                return list;
            }
    );

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
            }
    );

    private static final Codec<TimerSavedData> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    STAFF_TIMERS_MAP_CODEC.fieldOf("StaffTimers").forGetter(timerSavedData -> timerSavedData.staffTimers),
                    TIMED_ENCHANTMENTS_MAP_CODEC.fieldOf("TimedEnchantments").forGetter(timerSavedData -> timerSavedData.timedEnchantments)
                    )
                    .apply(instance, TimerSavedData::new)
    );

    public static final String FILE_NAME = "magical_staffs_timers";
    public static final SavedDataType<TimerSavedData> TYPE = new SavedDataType<>(FILE_NAME, TimerSavedData::new, CODEC, DataFixTypes.LEVEL);

    public TimerSavedData() {
        this.setDirty();
    }

    private TimerSavedData(Map<Integer, Integer> staffTimers, Map<Integer, TimedEnchantment> timedEnchantments) {
        this.staffTimers = staffTimers;
        this.timedEnchantments = timedEnchantments;
    }

    private static TimerSavedData getInstance() {
        return SERVER_LEVEL.getDataStorage().computeIfAbsent(TYPE);
    }

    public void setServerLevel(ServerLevel serverLevel) {
        SERVER_LEVEL = serverLevel;
    }

    public static boolean hasStaffTimerId(int id) {
        return getInstance().staffTimers.containsKey(id);
    }

    public static boolean hasTimedEnchantmentId(int id) {
        return getInstance().timedEnchantments.containsKey(id);
    }

    public static int addStaffTimer(int time) {
        int id = getInstance().STAFF_TIMERS_MAX_ID++;
        getInstance().staffTimers.put(id, time);
        getInstance().setDirty();
        return id;
    }

    public static int addTimedEnchantment(TimedEnchantment timedEnchantment) {
        int id = getInstance().TIMED_ENCHANTMENTS_MAX_ID++;
        getInstance().timedEnchantments.put(id, timedEnchantment);
        getInstance().setDirty();
        return id;
    }

    public static int getStaffTimer(int id) {
        return getInstance().staffTimers.getOrDefault(id, 0);
    }

    public static TimedEnchantment getTimedEnchantment(int id) {
        return getInstance().timedEnchantments.get(id);
    }

    public static void tick() {
        getInstance().setDirty();

        // Creates empty maps for removed values to be added to
        Map<Integer, Integer> removedStaffTimers = new HashMap<>();
        Map<Integer, TimedEnchantment> removedTimedEnchantments = new HashMap<>();

        // Ticks every value in the two maps
        getInstance().staffTimers.forEach((id, time) -> { if (time-- <= 0) removedStaffTimers.put(id, time); getInstance().staffTimers.put(id, time); } );
        getInstance().timedEnchantments.forEach((id, timedEnchantment) -> { if (timedEnchantment.tick()) removedTimedEnchantments.put(id, timedEnchantment); });

        // Removes any staff timers that have ended
        removedStaffTimers.forEach((id, time) -> {
            getInstance().staffTimers.remove(id, time);
            ModEvents.TIMED_STAFFS.forEach((containerMenu, indices) -> indices.removeIf(index -> ModEvents.removeStaffTimer(id, containerMenu.getItems().get(index))));
        });

        // Removes any timed enchantments that have ended
        removedTimedEnchantments.forEach((id, timedEnchantment) -> {
            getInstance().timedEnchantments.remove(id, timedEnchantment);
            ModEvents.TIMED_ITEM_STACKS.forEach((containerMenu, indices) -> indices.removeIf(index -> ModEvents.removeTimedEnchantment(id, containerMenu.getItems().get(index), timedEnchantment)));
        });
    }
}
