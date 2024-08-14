//package net.andrew_coursin.magical_staffs.capability.timed_enchantment;
//
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.common.capabilities.*;
//import net.minecraftforge.common.util.LazyOptional;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.ArrayList;
//
//import static net.andrew_coursin.magical_staffs.MagicalStaffs.MOD_ID;
//
//public class TimedEnchantmentsCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
//    private final LazyOptional<TimedEnchantmentsCapabilityImplementation> optional = LazyOptional.of(this::createTimedEnchantments);
//    private final TimedEnchantmentsCapabilityImplementation timedEnchantments;
//    public static Capability<TimedEnchantmentsCapabilityImplementation> TIMED_ENCHANTMENTS = CapabilityManager.get(new CapabilityToken<>() {});
//    public static ResourceLocation KEY = new ResourceLocation(MOD_ID, "timed_enchantments");
//
//    public TimedEnchantmentsCapabilityProvider(ItemStack pItemStack) {
//        this.timedEnchantments = new TimedEnchantmentsCapabilityImplementation(new ArrayList<>(), pItemStack);
//    }
//
//    private TimedEnchantmentsCapabilityImplementation createTimedEnchantments() {
//        return this.timedEnchantments;
//    }
//
//    public void invalidateCaps() {
//        optional.invalidate();
//    }
//
//    @Override
//    public CompoundTag serializeNBT() {
//        return createTimedEnchantments().serializeNBT();
//    }
//
//    @Override
//    public void deserializeNBT(CompoundTag nbt) {
//        createTimedEnchantments().deserializeNBT(nbt);
//    }
//
//    @Override
//    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//        if (cap == TIMED_ENCHANTMENTS) {
//            return optional.cast();
//        }
//
//        return LazyOptional.empty();
//    }
//}