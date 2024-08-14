//package net.andrew_coursin.magical_staffs.capability.timer;
//
//import net.minecraft.nbt.CompoundTag;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.event.TickEvent;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//
//@ParametersAreNonnullByDefault
//public class TimerCapabilityImplementation implements TimerCapabilityInterface {
//    private int time = 0;
//
//    public TimerCapabilityImplementation() {
//        MinecraftForge.EVENT_BUS.addListener(this::tickTime);
//    }
//
//    @Override
//    public CompoundTag serializeNBT() {
//        CompoundTag compoundTag = new CompoundTag();
//        compoundTag.putInt("time", this.time);
//        return compoundTag;
//    }
//
//    @Override
//    public int getTime() {
//        return this.time;
//    }
//
//    @Override
//    public void deserializeNBT(CompoundTag nbt) {
//        this.time = nbt.getInt("time");
//    }
//
//    @Override
//    public void setTime(int time) {
//        this.time = time;
//    }
//
//    @Override
//    public void tickTime(TickEvent.ServerTickEvent event) {
//        if (event.phase == TickEvent.Phase.START) {
//            if (this.time > 0) {
//                --this.time;
//            } else {
//                this.time = 0;
//            }
//        }
//    }
//}
