//package net.andrew_coursin.magical_staffs.capability.timer;
//
//import net.andrew_coursin.magical_staffs.MagicalStaffs;
//import net.minecraft.core.Direction;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraftforge.common.capabilities.*;
//import net.minecraftforge.common.util.LazyOptional;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class TimerCapabilityProvider implements ICapabilitySerializable<CompoundTag> {
//    private TimerCapabilityInterface timer = null;
//    private final LazyOptional<TimerCapabilityInterface> optional = LazyOptional.of(this::createTimer);
//    public static Capability<TimerCapabilityInterface> TIMER = CapabilityManager.get(new CapabilityToken<>() {});
//    public static ResourceLocation KEY = new ResourceLocation(MagicalStaffs.MOD_ID, "timer");
//
//    private TimerCapabilityInterface createTimer() {
//        if (this.timer == null) {
//            this.timer = new TimerCapabilityImplementation();
//        }
//
//        return this.timer;
//    }
//
//    public void invalidateCaps() {
//        optional.invalidate();
//    }
//
//    @Override
//    public CompoundTag serializeNBT() {
//        return createTimer().serializeNBT();
//    }
//
//    @Override
//    public void deserializeNBT(CompoundTag nbt) {
//        createTimer().deserializeNBT(nbt);
//    }
//
//    @Override
//    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
//        if (cap == TIMER) {
//            return optional.cast();
//        }
//
//        return LazyOptional.empty();
//    }
//}
