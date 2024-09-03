package net.andrew_coursin.magical_staffs.level;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
import net.andrew_coursin.magical_staffs.event.TimedEnchantmentEndEvent;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TimedEnchantmentSavedData extends SavedData {
    public static final String FILE_NAME = "timed_enchantments";
    private Map<Integer, TimedEnchantment> timedEnchantments = new HashMap<>();
    private static final Codec<Map<Integer, TimedEnchantment>> MAP_CODEC;
    private static ServerLevel SERVER_LEVEL;
    private int MAX_ID = 0;

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
                }
        );
    }

    public TimedEnchantmentSavedData(ServerLevel serverLevel) {
        SERVER_LEVEL = serverLevel;
    }

    public static SavedData.Factory<TimedEnchantmentSavedData> factory(ServerLevel serverLevel) {
        return new SavedData.Factory<>(() -> new TimedEnchantmentSavedData(serverLevel), (compoundTag, provider) -> load(serverLevel, compoundTag, provider), null);
    }

    public static TimedEnchantmentSavedData load(ServerLevel serverLevel, CompoundTag pCompoundTag, HolderLookup.Provider pRegistries) {
        TimedEnchantmentSavedData timedEnchantmentSavedData = new TimedEnchantmentSavedData(serverLevel);
        timedEnchantmentSavedData.MAX_ID = pCompoundTag.getInt("MaxID");
        RegistryOps<Tag> registryops = pRegistries.createSerializationContext(NbtOps.INSTANCE);
        timedEnchantmentSavedData.timedEnchantments = MAP_CODEC.decode(registryops, pCompoundTag.get("TimedEnchantments")).getOrThrow().getFirst();
        return timedEnchantmentSavedData;
    }

    private static TimedEnchantmentSavedData get() {
        return SERVER_LEVEL.getDataStorage().computeIfAbsent(factory(SERVER_LEVEL), FILE_NAME);
    }

    public static int addTimedEnchantment(TimedEnchantment timedEnchantment) {
        int id = get().MAX_ID++;
        get().timedEnchantments.put(id, timedEnchantment);
        get().setDirty();
        return id;
    }

    public static boolean has(int id) {
        return get().timedEnchantments.containsKey(id);
    }

    public static TimedEnchantment get(int id) {
        return get().timedEnchantments.get(id);
    }

    public static void tick() {
        Map<Integer, TimedEnchantment> copy = new HashMap<>(get().timedEnchantments);
        copy.forEach((id, timedEnchantment) -> {
            if (timedEnchantment.tick()) {
                get().timedEnchantments.remove(id, timedEnchantment);
                MinecraftForge.EVENT_BUS.post(new TimedEnchantmentEndEvent(id, timedEnchantment));
            }
        });
        get().setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        pTag.putInt("MaxID", this.MAX_ID);
        RegistryOps<Tag> registryops = pRegistries.createSerializationContext(NbtOps.INSTANCE);
        pTag.put("TimedEnchantments", MAP_CODEC.encodeStart(registryops, this.timedEnchantments).getOrThrow());
        return pTag;
    }
}
