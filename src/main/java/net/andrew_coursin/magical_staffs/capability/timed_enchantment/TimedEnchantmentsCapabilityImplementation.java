//package net.andrew_coursin.magical_staffs.capability.timed_enchantment;
//
//import net.andrew_coursin.magical_staffs.TimedEnchantment;
//import net.andrew_coursin.magical_staffs.event.TimedEnchantmentEndEvent;
//import net.minecraft.MethodsReturnNonnullByDefault;
//import net.minecraft.core.HolderLookup;
//import net.minecraft.nbt.*;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.enchantment.Enchantment;
//import net.minecraft.world.item.enchantment.EnchantmentHelper;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//import java.util.*;
//
//@MethodsReturnNonnullByDefault
//@ParametersAreNonnullByDefault
//public class TimedEnchantmentsCapabilityImplementation implements TimedEnchantmentsCapabilityInterface {
//    private final ArrayList<TimedEnchantment> timedEnchantments;
//    private final ItemStack itemStack;
//    private final Map<Integer, Integer> displayDurations = new HashMap<>();
//    private static final String KEY = "TimedEnchantments";
//
//    public TimedEnchantmentsCapabilityImplementation(ArrayList<TimedEnchantment> pTimedEnchantments, ItemStack pItemStack) {
//        this.itemStack = pItemStack;
//        this.timedEnchantments = pTimedEnchantments;
//    }
//
//    @Override
//    public ArrayList<TimedEnchantment> getTimedEnchantments() {
//        return this.timedEnchantments;
//    }
//
//    @Override
//    public CompoundTag serializeDurations() {
//        CompoundTag compoundTag = new CompoundTag();
//        this.timedEnchantments.forEach(
//            timedEnchantment -> compoundTag.putInt(String.valueOf(timedEnchantment.getId()), timedEnchantment.getDuration())
//        );
//        return compoundTag;
//    }
//
//    @Override
//    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
//        CompoundTag compoundTag = new CompoundTag();
//        ListTag listTag = new ListTag();
//
//        for (TimedEnchantment timedEnchantment : this.timedEnchantments) {
//            listTag.add(timedEnchantment.save());
//        }
//
//        compoundTag.put(KEY, listTag);
//        return compoundTag;
//    }
//
//    @Override
//    public int getDisplayDuration(int id) {
//        return this.displayDurations.getOrDefault(id, 0);
//    }
//
//    @Override
//    public void addTimedEnchantment(TimedEnchantment timedEnchantment) {
//        this.timedEnchantments.add(timedEnchantment);
//        MinecraftForge.EVENT_BUS.register(this);
//    }
//
//    @Override
//    public void deserializeDurations(CompoundTag compoundTag) {
//        this.displayDurations.clear();
//        compoundTag.getAllKeys().forEach(
//            key -> this.displayDurations.put(Integer.valueOf(key), compoundTag.getInt(key))
//        );
//    }
//
//    @Override
//    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag pNbt) {
//        this.timedEnchantments.clear();
//        for (Tag tag : pNbt.getList(KEY, Tag.TAG_COMPOUND)) {
//            CompoundTag compoundTag = (CompoundTag) tag;
//            TimedEnchantment timedEnchantment = TimedEnchantment.TIMED_ENCHANTMENT_SAVED_DATA.get(compoundTag.getInt("id"));
//            if (timedEnchantment == null) timedEnchantmentEnd(new TimedEnchantment(false, compoundTag));
//            else addTimedEnchantment(timedEnchantment);
//        }
//    }
//
//    @Override
//    public void removeTimedEnchantment(int id) {
//        for (TimedEnchantment timedEnchantment : this.timedEnchantments) {
//            if (timedEnchantment.getId() == id) {
//                this.timedEnchantments.remove(timedEnchantment);
//                timedEnchantmentEnd(timedEnchantment);
//                break;
//            }
//        }
//    }
//
//    @Override
//    public void timedEnchantmentEnd(TimedEnchantment timedEnchantment) {
//        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(this.itemStack);
//        int newLevel = EnchantmentHelper.getItemEnchantmentLevel(timedEnchantment.getEnchantment(), this.itemStack) - timedEnchantment.getLevel();
//        if (newLevel <= 0) enchantments.remove(timedEnchantment.getEnchantment());
//        else enchantments.put(timedEnchantment.getEnchantment(), newLevel);
//        EnchantmentHelper.setEnchantments(enchantments, this.itemStack);
//        if (this.timedEnchantments.isEmpty()) MinecraftForge.EVENT_BUS.unregister(this);
//    }
//
//    @SubscribeEvent
//    public void timedEnchantmentEnd(TimedEnchantmentEndEvent event) {
//        removeTimedEnchantment(event.getId());
//    }
//}