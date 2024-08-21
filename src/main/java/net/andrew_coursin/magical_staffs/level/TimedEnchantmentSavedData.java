package net.andrew_coursin.magical_staffs.level;

import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantmentSavedData extends SavedData {
    private final Map<Integer, Integer> idsToDurations = new HashMap<>();
    public static final String ID = "timed_enchantment";

    public static SavedData.Factory<TimedEnchantmentSavedData> factory() {
        return new SavedData.Factory<>(TimedEnchantmentSavedData::new, TimedEnchantmentSavedData::load, DataFixTypes.SAVED_DATA_MAP_INDEX);
    }

    public TimedEnchantmentSavedData() {}

    public static TimedEnchantmentSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        TimedEnchantmentSavedData timedEnchantmentSavedData = new TimedEnchantmentSavedData();

        ListTag listTag = compoundTag.getList(ID, Tag.TAG_COMPOUND);
        listTag.forEach(tag -> timedEnchantmentSavedData.idsToDurations.put(((CompoundTag) tag).getInt("id"), ((CompoundTag) tag).getInt("duration")));

        return timedEnchantmentSavedData;
    }

    public void add(TimedEnchantment timedEnchantment) {
        this.idsToDurations.put(timedEnchantment.getId(), timedEnchantment.getDuration());
        this.setDirty();
    }

    public List<Integer> updateDurations() {
        List<Integer> removedIds = new ArrayList<>();
        this.idsToDurations.forEach((id, duration) -> {
            if (duration - 1 > 0) {
                this.idsToDurations.put(id, duration - 1);
            } else {
                removedIds.add(id);
            }
        });
        removedIds.forEach(this.idsToDurations::remove);
        this.setDirty();
        return removedIds;
    }

    @Override
    public CompoundTag save(CompoundTag pCompoundTag, HolderLookup.Provider provider) {
        ListTag listTag = new ListTag();

        this.idsToDurations.forEach((id, duration) -> {
            pCompoundTag.putInt("id", id);
            pCompoundTag.putInt("duration", duration);
        });

        pCompoundTag.put(ID, listTag);
        return pCompoundTag;
    }
}
