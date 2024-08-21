//package net.andrew_coursin.magical_staffs.capability.timed_enchantment;
//
//import net.andrew_coursin.magical_staffs.components.timed_enchantments.TimedEnchantment;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraftforge.common.util.INBTSerializable;
//
//import java.util.ArrayList;
//
//public interface TimedEnchantmentsCapabilityInterface extends INBTSerializable<CompoundTag> {
//    ArrayList<TimedEnchantment> getTimedEnchantments();
//    CompoundTag serializeDurations();
//    int getDisplayDuration(int id);
//    void addTimedEnchantment(TimedEnchantment timedEnchantment);
//    void deserializeDurations(CompoundTag compoundTag);
//    void removeTimedEnchantment(int id);
//    void timedEnchantmentEnd(TimedEnchantment timedEnchantment);
//}
